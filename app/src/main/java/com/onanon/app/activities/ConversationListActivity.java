package com.onanon.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onanon.app.Utils.NetworkUtil;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.dialogs.EditProfileDialogFragment;
import com.onanon.app.dialogs.ViewHistoryDialog;
import com.onanon.app.dialogs.WaitingForPromptsDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ConversationListActivity extends AppCompatActivity {

    private String currentUserName;
    private String selectedConvoPushId;
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
        if (!prefManager.isFcmTokenCurrent() && !prefManager.getFcmTokenFromSharedPreferences().isEmpty()) {
            updateFcmTokenInFBDatabase();
        }
    }

    private void getUserName(){
        if (!prefManager.isUserNameInSharedPreferencesEmpty()) {
            currentUserName = prefManager.getUserNameFromSharedPreferences();
            showUserNameInTextView();
            setFirebaseListToUserName();
            setConversationResponses();
        } else {
            logOutFromFirebase();
        }
    }

    private void updateFcmTokenInFBDatabase() {
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference tokenRef = baseRef.child(Constants.FB_LOCATION_FCM_TOKENS).child(currentUserName);
        String token = prefManager.getFcmTokenFromSharedPreferences();

        tokenRef.setValue(token, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FCM Token", "Error updating token to FB");
                    prefManager.setIsFcmTokenCurrentToPreferences(false);
                } else {
                    prefManager.setIsFcmTokenCurrentToPreferences(true);
                }
                Log.i("FCM Token", "Token updated to Firebase successfully");
            }
        });
    }

    private void showUserNameInTextView(){
        ((TextView)findViewById(R.id.current_user_indicator)).setText("Hello, " + currentUserName);
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
        switch (item.getItemId()) {
            case R.id.add_conversation_menu:
                startNewConversation();
                return true;
            case R.id.log_out_menu:
                prefManager.setUserNameToPreferences("");
                logOutFromFirebase();
                return true;
            case R.id.edit_profile_menu:
                editProfileMenu();
                return true;
            case R.id.see_intro_slides_menu:
                Intent intent = new Intent(ConversationListActivity.this, IntroSliderActivity.class);
                intent.putExtra(Constants.INITIATING_ACTIVITY_INTENT_KEY, Constants.CONVO_LIST);
                startActivity(intent);
                return true;
            case R.id.invite_friends:
                Utils.composeMmsMessage(getString(R.string.invite_sms_message_text), this);
                return true;
            case R.id.send_comments:
                Utils.composeEmail(new String[]{getString(R.string.andy_onanon_email)},
                        getString(R.string.comments), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editProfileMenu(){
        EditProfileDialogFragment editProfileDialogFragment=new EditProfileDialogFragment();
        editProfileDialogFragment.show(getSupportFragmentManager(), "EditProfileDialog");
    }

    private void setFirebaseListToUserName() {
        final ListView listView = (ListView) this.findViewById(R.id.conversation_list);

        listView.setEmptyView(findViewById(android.R.id.empty));

        //Get footer height at the current screen density.
        final int footerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48, getResources().getDisplayMetrics());

        View footer = new View(this);
        footer.setLayoutParams(new AbsListView.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, footerHeight));
        listView.addFooterView(footer, null, false);

        mListAdapter = new FirebaseListAdapter<Conversation>(this, Conversation.class,
                R.layout.layout_conversation_list_item,
                baseRef.child(Constants.FB_LOCATION_USER_CONVOS).child(currentUserName)) {

            @Override
            protected void populateView(View v, final Conversation conversation, int position) {
                setViewsBasedOnConversationStatus(conversation, v);
                final int clickedPosition = position;

                final ImageButton popUpBtn = (ImageButton)v.findViewById(R.id.pop_up_button);
                popUpBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (conversation != null) {
                            selectedConvoPushId = mListAdapter.getRef(clickedPosition).getKey();
                        }

                        final PopupMenu popup = new PopupMenu(ConversationListActivity.this, popUpBtn);
                        popup.getMenuInflater().inflate(R.menu.menu_conversation_list_popup, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                int i = item.getItemId();
                                if (i == R.id.add_someone) {
                                    if (conversation.getUserNamesInConversation().size() >=
                                            Constants.MAX_CONVO_PARTICIPANTS) {
                                        Toast.makeText(getApplicationContext(),
                                                "This conversation is already crowded!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        startNextActivity(conversation, AddUserToConversationActivity.class);
                                    }
                                    return true;
                                }
                                else if (i == R.id.view_history) {
                                    ViewHistoryDialog viewHistoryDialog =
                                            ViewHistoryDialog.newInstance(selectedConvoPushId);
                                    viewHistoryDialog.show(getSupportFragmentManager(), "ViewHistoryDialog");
                                    return true;
                                }
                                else if (i == R.id.leave_convo) {
                                    confirmLeaveConversation(conversation);
                                    return true;
                                }
                                else {
                                    return onMenuItemClick(item);
                                }
                            }
                        });
                        popup.show();
                    }
                });
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
    }

    private void setViewsBasedOnConversationStatus(Conversation conversation, View v) {
        String title = "Oops";
        v.findViewById(R.id.item_layout).setBackgroundResource(0);

        int conversationStatus = conversation.currentConversationStatus(currentUserName);

        switch (conversationStatus) {
            case Constants.USER_TURN_TO_TELL:
                title = "Talk about " + conversation.proposedPromptsTagAsString();
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_circle_144_70);
                break;

            case Constants.USER_TURN_TO_SEND_PROMPTS:
                title = "You need to send topics to " + conversation.getNextUserNameToTell() + "!";
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_circle_144_70);
                break;

            case Constants.USER_TURN_TO_HEAR:
                title = "Hear " + conversation.getLastUserNameToTell() + " on "
                        + conversation.getCurrentPrompt().getTag() + ". \n"
                        + conversation.recordingDurationAsFormattedString();
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_arrow_144_70);
                break;

            case Constants.USER_WAITING_FOR_PROMPTS:
                title = "Waiting for topics from " + conversation.getLastUserNameToTell();
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_none_144_70);
                break;

            case Constants.USER_WAITING_FOR_STORY:
                title = "Waiting for a story from " + conversation.getNextUserNameToTell();
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_none_144_70);
                break;

            case Constants.USER_WAITING_FOR_OTHERS:
                title = "Waiting for others to listen.";
                ((ImageView)v.findViewById(R.id.conversation_profile_image)).setImageResource(R.drawable.status_none_144_70);
                break;
        }

        String conversationParticipants = "With "
                + conversation.otherConversationParticipants(currentUserName);
        String lastAction = Utils.calcTimeFromMillisToNow(conversation.getDateLastActionOccurred());

        ((TextView) v.findViewById(R.id.conversation_title)).setText(title);
        ((TextView) v.findViewById(R.id.conversation_participants)).setText(conversationParticipants);
        ((TextView) v.findViewById(R.id.conversation_time_since_action)).setText(lastAction);
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
                getRandomPromptsOrWait(conversation);
                break;

            case Constants.USER_WAITING_FOR_STORY:
                //TODO: startWait/PingActivity();
                Toast.makeText(this, conversation.getNextUserNameToTell() +
                        " owes you a story!", Toast.LENGTH_LONG).show();
                Log.i("PickedAConvo!", "Waiting for story");
                break;

            case Constants.USER_WAITING_FOR_OTHERS:
                //TODO: startWait/PingActivity();
                ArrayList<String> usersToHearArray = conversation.getUserNamesHaveNotHeardStory();
                String usersToHear = "";
                for (String userName : usersToHearArray) {
                    usersToHear = usersToHear + ", " + userName;
                }
                usersToHear = usersToHear.substring(1);
                String verb;
                if (usersToHear.contains(",")) {
                    verb = " have ";
                } else {
                    verb = " has ";
                }

                Toast.makeText(this, usersToHear + verb + "to hear the current story!", Toast.LENGTH_LONG).show();
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
        removeEntireConversation(conversation);
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

    private void startNextActivity(Conversation conversation, Class classToStart){
        if (NetworkUtil.isConnectedToNetwork(this)) {
            Intent intent = new Intent(this, classToStart);
            intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
            intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
            startActivity(intent);
        }
    }

    private void startNewConversation(){
        if (NetworkUtil.isConnectedToNetwork(this)) {
            Intent intent = new Intent(this, StartNewConversationActivity.class);
            startActivity(intent);
        }
    }

    private void getRandomPromptsOrWait(Conversation conversation) {
        WaitingForPromptsDialog waitingForPromptsDialog = WaitingForPromptsDialog.newInstance(
                conversation, selectedConvoPushId);
        waitingForPromptsDialog.show(getSupportFragmentManager(), "WaitingForPromptsDialog");
    }

    private void setConversationResponses() {
        DatabaseReference responsesForUserRef = baseRef.child(Constants.FB_LOCATION_RESPONSES)
                .child(currentUserName);

        responsesForUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Button responseButton = (Button)findViewById(R.id.reactions_button);
                    responseButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void reactionsButtonClick(View view) {
        Intent intent = new Intent(this, ReactionsActivity.class);
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
