package com.aterbo.tellme.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;


public class ConversationListActivity extends AppCompatActivity {

    private String currentUserName;
    private String selectedConvoPushId;
    private Firebase baseRef;
    private FirebaseListAdapter<Conversation> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        baseRef = new Firebase(Constants.FB_LOCATION);

        getUserNameFromSharedPreferences();
        setFirebaseListToUserName();
        showUserNameInTextView();

        setSupportActionBar(toolbar);

        setFloatingActionButton();
    }

    private void getUserNameFromSharedPreferences(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        currentUserName = settings.getString(Constants.CURRENT_USER_NAME_KEY, "");
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
                clearUserNameFromSharedPreferences();
                logOutFromFirebase();
                startOpeningScreenActivity();
                return true;
            case R.id.add_new_user_menu:
                addNewUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFirebaseListToUserName() {
        final ListView listView = (ListView) this.findViewById(R.id.conversation_list);
        mListAdapter = new FirebaseListAdapter<Conversation>(this, Conversation.class,
                R.layout.layout_conversation_list_item, baseRef.child("userConvos").child(currentUserName)) {
            @Override
            protected void populateView(View v, Conversation conversation, int position) {

                String title = determineTitle(conversation);
                if (conversation.getNextPlayersUserName().equals(currentUserName)) {
                    ((TextView) v.findViewById(R.id.conversation_profile_image)).setText("Me");
                    ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(
                            "You're up next!");
                } else {
                    String firstChar = conversation.getNextPlayersUserName().substring(0, 1);
                    ((TextView) v.findViewById(R.id.conversation_profile_image)).setText(firstChar);
                    ((TextView) v.findViewById(R.id.conversation_next_turn)).setText(
                            "Next Up: " + conversation.getNextPlayersUserName());
                }
                ((TextView) v.findViewById(R.id.conversation_title)).setText(title);
                ((TextView) v.findViewById(R.id.conversation_participants)).setText(
                        "Group:  " + otherConversationParticipants(conversation));
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

        } else if (doesUserNeedToPickTopics(conversation)) {
            startNextActivity(conversation, ChooseTopicsToSendActivity.class);
        }
        else if (isUserWaiting(conversation)) {
            //TODO: startWait/PingActivity();
            Log.i("PickedAConvo!", "My turn to hear");
        }
    }

    private String otherConversationParticipants(Conversation conversation){
        String participantsString = "";
        for (String userName :
                conversation.getUserNamesInConversation()) {
            if(!userName.equals(currentUserName)) {
                participantsString = participantsString + ", " + userName.replace(",",".");
            }
        }

        return participantsString.substring(2, participantsString.length());
    }

    private String determineTitle(Conversation conversation){
        if (isUserTurnToTell(conversation)) {
            return "Tell:    " + conversation.proposedPromptsTagAsString();

        } else if (isUserTurnToHear(conversation)) {
            return "Hear:    " + conversation.getCurrentPrompt().getText();

        } else if (doesUserNeedToPickTopics(conversation)) {
            return "You need to send topics!";

        } else if (isUserWaiting(conversation)) {
            return "Waiting for a story.";
        }
        return "Oops";
    }

    private boolean isUserTurnToTell(Conversation conversation) {
        if(conversation.getNextPlayersUserName().equals(currentUserName)
                && conversation.getStoryRecordingPushId().equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserTurnToHear(Conversation conversation) {
        if(conversation.getNextPlayersUserName().equals(currentUserName)
                && !conversation.getStoryRecordingPushId().equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaiting(Conversation selectedConvo) {
        if(!selectedConvo.getNextPlayersUserName().equals(currentUserName)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean doesUserNeedToPickTopics(Conversation selectedConvo) {
        if(selectedConvo.getLastPlayersUserName().equals(currentUserName) &&
                selectedConvo.getProposedPrompt1() == null) {
            return true;
        } else{
            return false;
        }
    }

    private void startNextActivity(Conversation conversation, Class classToStart){
        Intent intent = new Intent(this, classToStart);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
    }

    private void addNewUser(){
        Intent intent = new Intent(this, AddNewUserActivity.class);
        startActivity(intent);
    }
    private void startNewConversation(){
        Intent intent = new Intent(this, StartNewConversationActivity.class);
        startActivity(intent);
    }

    private void clearUserNameFromSharedPreferences(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.CURRENT_USER_NAME_KEY, "");
        // Commit the edits!
        editor.commit();
    }

    private void logOutFromFirebase(){
        baseRef.unauth();
    }

    private void startOpeningScreenActivity(){
        Intent intent = new Intent(this, OpeningScreen.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }
}
