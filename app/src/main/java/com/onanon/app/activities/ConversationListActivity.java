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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.onanon.app.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.Conversation;

import java.util.ArrayList;
import java.util.HashMap;


public class ConversationListActivity extends AppCompatActivity {

    private String currentUserName;
    private String selectedConvoPushId;
    private String currentUserUID;
    private DatabaseReference baseRef;
    private FirebaseListAdapter<Conversation> mListAdapter;
    private PrefManager prefManager;

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
            getUserNameFromUID();
        } else {
            currentUserName = "Error";
            showUserNameInTextView();
        }
    }

    private void getUserNameFromUID(){
        DatabaseReference promptRef = baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS).child(currentUserUID);

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                currentUserName = (String) snapshot.getValue();

                prefManager.setUserNameToPreferences(currentUserName);

                setFirebaseListToUserName();
                showUserNameInTextView();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
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
                R.layout.layout_conversation_list_item, baseRef.child("userConvos").child(currentUserName)) {
            @Override
            protected void populateView(View v, Conversation conversation, int position) {

                String title = determineTitle(conversation);
                if (isUserTurnToTell(conversation)) {
                    ((TextView) v.findViewById(R.id.conversation_profile_image)).setText("Me");
                    ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(
                            "You have a story to tell!");
                } else if (isUserTurnToSendPrompts(conversation)) {
                    ((TextView) v.findViewById(R.id.conversation_profile_image)).setText("Me");
                    ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(
                            "You have to send some prompts!");
                } else {
                    String firstChar = conversation.getNextUserNameToTell().substring(0, 1);
                    ((TextView) v.findViewById(R.id.conversation_profile_image)).setText(firstChar);
                    ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(
                            "Next Up: " + conversation.getNextUserNameToTell());
                }
                ((TextView) v.findViewById(R.id.conversation_title)).setText(title);
                ((TextView) v.findViewById(R.id.conversation_participants)).setText(
                        "Conversation with:  " + otherConversationParticipants(conversation));
                ((TextView) v.findViewById(R.id.conversation_story_duration)).setText(
                        conversation.recordingDurationAsFormattedString());
                (v.findViewById(R.id.conversation_time_since_action)).setVisibility(View.GONE);
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

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ConversationListActivity.this);
                        builder.setTitle("Change group");
                        builder.setIcon(R.drawable.alberticon);
                        builder.setItems(photoOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (selectedConvo.getUserNamesInConversation().size() >= 4) {
                                            Toast.makeText(getApplicationContext(),
                                                    "This conversation is already crowded!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            startNextActivity(selectedConvo, AddUserToConversationActivity.class);
                                        }
                                        break;
                                    case 1:
                                        confirmDeleteConversation(selectedConvo);
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

    private void showUserNameInTextView(){
        ((TextView)findViewById(R.id.log_in_indicator)).setText("Logged in as: " + currentUserName);
    }

    private void determineActivityToStart(Conversation conversation){
        if (isUserTurnToTell(conversation)) {
            Log.i("PickedAConvo!", "My turn to tell");
            startNextActivity(conversation, PickTopicToRecordActivity.class);

        } else if (isUserTurnToHear(conversation)) {
            startNextActivity(conversation, ListenToStoryActivity.class);
            Log.i("PickedAConvo!", "My turn to hear");

        } else if (isUserTurnToSendPrompts(conversation)) {
            startNextActivity(conversation, ChooseTopicsToSendActivity.class);
        }
        else if (isUserWaitingForPrompts(conversation)) {
            //TODO: startWait/PingActivity();
            Log.i("PickedAConvo!", "Waiting for prompts");
        }
        else if (isUserWaitingForStory(conversation)) {
            //TODO: startWait/PingActivity();
            Log.i("PickedAConvo!", "Waiting for story");
        }
        else if (isUserWaitingForOthersToHear(conversation)) {
            //TODO: startWait/PingActivity();
            Log.i("PickedAConvo!", "Waiting for others to hear story");
        }
    }

    private String otherConversationParticipants(Conversation conversation){
        String participantsString = "";
        for (String userName : conversation.getUserNamesInConversation()) {
            if(!userName.equals(currentUserName)) {
                participantsString = participantsString + ", " + userName;
            }
        }

        return participantsString.substring(2, participantsString.length());
    }

    private String determineTitle(Conversation conversation){
        if (isUserTurnToTell(conversation)) {
            return "Tell:    " + conversation.proposedPromptsTagAsString();

        } else if (isUserTurnToSendPrompts(conversation)) {
            return "You need to send topics!";

        } else if (isUserTurnToHear(conversation)) {
            return "Hear:    " + conversation.getCurrentPrompt().getText();

        } else if (isUserWaitingForPrompts(conversation)) {
            return "Waiting for topics.";

        } else if (isUserWaitingForStory(conversation)) {
            return "Waiting for a story.";

        } else if (isUserWaitingForOthersToHear(conversation)) {
            return "Waiting for everyone else to listen.";
        }
        return "Oops";
    }

    private boolean isUserTurnToTell(Conversation conversation) {
        if(isCurrentUserNextToTell(conversation)
                && haveAllUsersHeardStory(conversation)
                && !isStoryRecorded(conversation)
                && isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserTurnToSendPrompts(Conversation conversation) {
        if(isCurrentUserLastToTell(conversation)
                && !isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserTurnToHear(Conversation conversation) {
        if(isStoryRecorded(conversation)
                && hasCurrentUserHeardStory(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaitingForPrompts(Conversation conversation) {
        if(isCurrentUserNextToTell(conversation)
                && !isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaitingForStory(Conversation conversation) {
        if(!isCurrentUserNextToTell(conversation)
                && !isStoryRecorded(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaitingForOthersToHear(Conversation conversation) {
        if(isStoryRecorded(conversation)
                && !haveAllUsersHeardStory(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isCurrentUserNextToTell(Conversation conversation){
        if(conversation.getNextUserNameToTell().equals(currentUserName)){
            return true;
        } else{
            return false;
        }
    }

    private boolean isCurrentUserLastToTell(Conversation conversation){
        if(conversation.getLastUserNameToTell().equals(currentUserName)){
            return true;
        } else{
            return false;
        }
    }

    private boolean haveAllUsersHeardStory(Conversation conversation) {
        if (conversation.getUserNamesHaveNotHeardStory().contains("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean hasCurrentUserHeardStory(Conversation conversation) {
        if (conversation.getUserNamesHaveNotHeardStory().contains(currentUserName)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isStoryRecorded(Conversation conversation) {
        if(!conversation.getFbStorageFilePathToRecording().equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isProposedPromptSelected(Conversation conversation) {
        if(conversation.getProposedPrompt1() != null) {
            return true;
        } else{
            return false;
        }
    }

    private void confirmDeleteConversation(final Conversation conversation){

        AlertDialog.Builder alert = new AlertDialog.Builder(ConversationListActivity.this);
        alert.setTitle("You don't want to talk anymore?");
        alert.setMessage("Are you sure you want to leave this conversation?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Delete", "Confirm Delete");
                deleteConversation(conversation);
                dialog.dismiss();

            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Delete", "Cancel Delete");

                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void deleteConversation(Conversation conversation){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }
}
