package com.onanon.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ConversationListActivity extends AppCompatActivity {

    private String currentUserName;
    private String selectedConvoPushId;
    private String currentUserUID;
    private DatabaseReference baseRef;
    private FirebaseListAdapter<Conversation> mListAdapter;
    private PrefManager prefManager;
    private String mUserEmail, mUserProfilePicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        baseRef = FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(toolbar);

        setFloatingActionButton();

        prefManager = new PrefManager(this);
        getUserName();
    }

    private void getUserName(){
        if (!prefManager.isUserNameInSharedPreferencesEmpty()) {
            currentUserName = prefManager.getUserNameFromSharedPreferences();
            setFirebaseListToUserName();
            showUserNameInTextView();
        } else {
            getUserFromFirebase();
        }
    }

    private void getUserFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserUID = user.getUid();
            mUserEmail = user.getEmail();
            getUserNameFromUID();
        } else {
            Toast.makeText(getApplicationContext(),
                    "There has been an error. Please sign in again.", Toast.LENGTH_LONG).show();
            prefManager.setUserNameToPreferences("");
            logOutFromFirebase();
        }
    }

    private void getUserNameFromUID(){
        DatabaseReference uIDRef = baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS).child(currentUserUID);

        uIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (existsUserProfile(snapshot)) {
                    System.out.println(snapshot.getValue());

                    currentUserName = (String) snapshot.getValue();

                    prefManager.setUserNameToPreferences(currentUserName);

                    setFirebaseListToUserName();
                    showUserNameInTextView();
                } else {
                    getUserNameForNewProfile();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void showUserNameInTextView(){
        ((TextView)findViewById(R.id.log_in_indicator)).setText("Logged in as: " + currentUserName);
    }

    private void setFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewConversation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.add_conversation_menu:
                startNewConversation();
                return true;
            case R.id.log_out_menu:
                prefManager.setUserNameToPreferences("");
                logOutFromFirebase();
                return true;
            case R.id.see_intro_slides_menu:
                Intent intent = new Intent(ConversationListActivity.this, IntroSliderActivity.class);
                intent.putExtra(Constants.INITIATING_ACTIVITY_INTENT_KEY, Constants.CONVO_LIST);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFirebaseListToUserName() {
        final ListView listView = (ListView) this.findViewById(R.id.conversation_list);

        listView.setEmptyView(findViewById(android.R.id.empty));

        mListAdapter = new FirebaseListAdapter<Conversation>(this, Conversation.class,
                R.layout.layout_conversation_list_item,
                baseRef.child(Constants.FB_LOCATION_USER_CONVOS).child(currentUserName)) {

            @Override
            protected void populateView(View v, Conversation conversation, int position) {
                setViewsBasedOnConversationStatus(conversation, v);
            }
        };

        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation selectedConvo = mListAdapter.getItem(position);
                if (selectedConvo != null) {
                    selectedConvoPushId = mListAdapter.getRef(position).getKey();
                    determineActivityToStart(selectedConvo);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Conversation selectedConvo = mListAdapter.getItem(position);
                if (selectedConvo != null) {
                        selectedConvoPushId = mListAdapter.getRef(position).getKey();

                        CharSequence photoOptions[] = new CharSequence[]{
                                "Add someone to the conversation.",
                                "Leave this conversation.",
                                getResources().getString(R.string.cancel)
                        };

                        android.app.AlertDialog.Builder builder =
                                new android.app.AlertDialog.Builder(ConversationListActivity.this);
                        builder.setTitle("Change group");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setItems(photoOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (selectedConvo.getUserNamesInConversation().size() >=
                                                Constants.MAX_CONVO_PARTICIPANTS) {
                                            Toast.makeText(getApplicationContext(),
                                                    "This conversation is already crowded!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            startNextActivity(selectedConvo, AddUserToConversationActivity.class);
                                        }
                                        break;
                                    case 1:
                                        confirmLeaveConversation(selectedConvo);
                                        break;
                                    case 2:
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                return true;
            }
        });
    }

    private void setViewsBasedOnConversationStatus(Conversation conversation, View v) {
        String title = "Oops";
        String imageText = conversation.getNextUserNameToTell().substring(0, 1);
        String nextTurnDescription = "Next Up: " + conversation.getNextUserNameToTell();

        int conversationStatus = conversation.currentConversationStatus(currentUserName);

        switch (conversationStatus) {
            case Constants.USER_TURN_TO_TELL:
                title = "Tell:    " + conversation.proposedPromptsTagAsString();
                imageText = "ME";
                nextTurnDescription = "You have a story to tell!";
                break;

            case Constants.USER_TURN_TO_SEND_PROMPTS:
                title = "You need to send topics!";
                imageText = "ME";
                nextTurnDescription = "You need to send some prompts!";
                break;

            case Constants.USER_TURN_TO_HEAR:
                title = "Hear:    " + conversation.getCurrentPrompt().getTag();
                break;

            case Constants.USER_WAITING_FOR_PROMPTS:
                title = "Waiting for topics.";
                break;

            case Constants.USER_WAITING_FOR_STORY:
                title = "Waiting for a story.";
                break;

            case Constants.USER_WAITING_FOR_OTHERS:
                title = "Waiting for everyone else to listen.";
                break;
        }

        String conversationParticipants = "Conversation with:  "
                + conversation.otherConversationParticipants(currentUserName);
        String storyDuration = conversation.recordingDurationAsFormattedString();

        ((TextView) v.findViewById(R.id.conversation_title)).setText(title);
        ((TextView) v.findViewById(R.id.conversation_profile_image)).setText(imageText);
        ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(nextTurnDescription);
        ((TextView) v.findViewById(R.id.conversation_participants)).setText(conversationParticipants);
        ((TextView) v.findViewById(R.id.conversation_story_duration)).setText(storyDuration);

        (v.findViewById(R.id.conversation_time_since_action)).setVisibility(View.GONE);
    }

    private void determineActivityToStart(Conversation conversation){
        int conversationStatus = conversation.currentConversationStatus(currentUserName);

        switch (conversationStatus) {
            case Constants.USER_TURN_TO_TELL:
                Log.i("PickedAConvo!", "My turn to tell");
                startNextActivity(conversation, PickTopicToRecordActivity.class);
                break;

            case Constants.USER_TURN_TO_SEND_PROMPTS:
                Log.i("PickedAConvo!", "My turn to send prompts");
                startNextActivity(conversation, ChooseTopicsToSendActivity.class);
                break;

            case Constants.USER_TURN_TO_HEAR:
                Log.i("PickedAConvo!", "My turn to hear");
                startNextActivity(conversation, ListenToStoryActivity.class);
                break;

            case Constants.USER_WAITING_FOR_PROMPTS:
                //TODO: startWait/PingActivity();
                Log.i("PickedAConvo!", "Waiting for prompts");
                break;

            case Constants.USER_WAITING_FOR_STORY:
                //TODO: startWait/PingActivity();
                Log.i("PickedAConvo!", "Waiting for story");
                break;

            case Constants.USER_WAITING_FOR_OTHERS:
                //TODO: startWait/PingActivity();
                Log.i("PickedAConvo!", "Waiting for others to hear story");
                break;

            default:
                Log.i("PickedAConvo!", "Default selected");
                break;
        }
    }


    private void confirmLeaveConversation(final Conversation conversation){
        AlertDialog.Builder alert = new AlertDialog.Builder(ConversationListActivity.this);
        alert.setTitle("You don't want to talk anymore?");
        alert.setMessage("Are you sure you want to leave this conversation?");

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteConversation(conversation);
                dialog.dismiss();

            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void deleteConversation(Conversation conversation){
        if(conversation.isOnlyTwoPeople()) {
            removeEntireConversation(conversation);
        } else {
            removeCurrentUserFromConversation(conversation);
        }
    }

    private void removeEntireConversation(Conversation conversation) {
        String fbStorageFilePathToRecording = conversation.getFbStorageFilePathToRecording();

        if (!fbStorageFilePathToRecording.equals("none")) {
            removeRecordingAndConvo(conversation);
        } else {
            removeConvoOnly(conversation);
        }
    }

    private void removeRecordingAndConvo(final Conversation conversation){
        String FBStorageFilePath = conversation.getFbStorageFilePathToRecording();

        FirebaseStorage storage = FirebaseStorage.getInstance();


        StorageReference sRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        StorageReference audioFileStorageRef = sRef.child(FBStorageFilePath);

        audioFileStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Recording deleted", "FB Storage recoding file deleted.");

                removeConvoOnly(conversation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Recording deleted", "ERROR - FB Storage recoding file NOT deleted.");

                removeConvoOnly(conversation);
            }
        });
    }

    private void removeConvoOnly(Conversation conversation) {
        HashMap<String, Object> mapOfDataToDelete = new HashMap<String, Object>();

        ArrayList<String> userNamesInConversation = conversation.getUserNamesInConversation();

        mapOfDataToDelete.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                selectedConvoPushId, null);

        for (String userNames : userNamesInConversation) {
            mapOfDataToDelete.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userNames + "/" + selectedConvoPushId, null);
        }

        baseRef.updateChildren(mapOfDataToDelete, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FBDeleteConvo", "Error deleting conversatoin");
                }
                Log.i("FBDeleteConvo", "Convo deleted successfully");
            }
        });
    }

    private void removeCurrentUserFromConversation(Conversation conversation) {
        conversation.removeUserFromConversation(currentUserName);

        ArrayList<String> userNamesInConversation = conversation.getUserNamesInConversation();

        HashMap<String, Object> mapOfDataToDelete = new HashMap<String, Object>();
        mapOfDataToDelete.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                selectedConvoPushId + "/" + currentUserName, null);

        mapOfDataToDelete.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                + currentUserName + "/" + selectedConvoPushId, null);


        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userNames : userNamesInConversation) {
            mapOfDataToDelete.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userNames + "/" + selectedConvoPushId, itemToAddHashMap);
        }


        baseRef.updateChildren(mapOfDataToDelete, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FBDeleteConvo", "Error removing user from conversation");
                }
                Log.i("FBDeleteConvo", "User removed from convo successfully");
            }
        });
    }

    private void startNextActivity(Conversation conversation, Class classToStart){
        Intent intent = new Intent(this, classToStart);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
    }

    private void startNewConversation(){
        Intent intent = new Intent(this, StartNewConversationActivity.class);
        startActivity(intent);
    }

    private void logOutFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startSplashScreenActivity();
                    }
                });
    }

    private void startSplashScreenActivity(){
        Intent intent = new Intent(this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean existsUserProfile(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void setUserNameToPrefManager() {
        prefManager.setUserNameToPreferences(currentUserName);
    }

    private void getUserNameForNewProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to ONanON!");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.layout_new_user_info,
                (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                currentUserName = input.getText().toString();
                checkIfUserNameIsUnique();
            }
        });

        builder.show();
    }

    private void checkIfUserNameIsUnique() {
        DatabaseReference userRef = baseRef.child(Constants.FB_LOCATION_USERS).child(currentUserName);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (existsUserProfile(snapshot)) {
                    Toast.makeText(ConversationListActivity.this, "That User Name already exists!", Toast.LENGTH_SHORT);
                    getUserNameForNewProfile();
                } else {
                    createUserInFirebaseHelper();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void createUserInFirebaseHelper() {

        mUserProfilePicUrl = "XXXXXX";
        /* Create a HashMap version of the user to add */
        User newUser = new User(currentUserName, mUserEmail, currentUserUID, mUserProfilePicUrl);
        HashMap<String, Object> newUserMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(newUser, Map.class);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Constants.FB_LOCATION_USERS + "/" + currentUserName,
                newUserMap);
        childUpdates.put("/" + Constants.FB_LOCATION_UID_MAPPINGS + "/"
                + currentUserUID, currentUserName);

        baseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setUserNameToPrefManager();
                setFirebaseListToUserName();
                showUserNameInTextView();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }
}
