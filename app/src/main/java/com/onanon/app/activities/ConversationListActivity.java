package com.onanon.app.activities;

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

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.Conversation;


public class ConversationListActivity extends AppCompatActivity {

    private String currentUserName;
    private String selectedConvoPushId;
    private DatabaseReference baseRef;
    private FirebaseListAdapter<Conversation> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        baseRef = FirebaseDatabase.getInstance().getReference();

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

        } else if (isUserTurnToPickPrompts(conversation)) {
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

        } else if (isUserTurnToPickPrompts(conversation)) {
            return "You need to send topics!";

        } else if (isUserWaitingForPrompts(conversation)) {
            return "Waiting for topics.";

        } else if (isUserWaitingForStory(conversation)) {
            return "Waiting for a story.";
        }
        return "Oops";
    }

    private boolean isUserTurnToTell(Conversation conversation) {
        if(isCurrentUsersTurn(conversation)
                && !isStoryRecorded(conversation)
                && isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserTurnToHear(Conversation conversation) {
        if(isCurrentUsersTurn(conversation)
                && isStoryRecorded(conversation)
                && isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaitingForPrompts(Conversation conversation) {
        if(!isCurrentUsersTurn(conversation)
                && !isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaitingForStory(Conversation conversation) {
        if(!isCurrentUsersTurn(conversation)
                && isProposedPromptSelected(conversation)) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isCurrentUsersTurn(Conversation conversation){
        if(conversation.getNextPlayersUserName().equals(currentUserName)){
            return true;
        } else{
            return false;
        }
    }

    private boolean isStoryRecorded(Conversation conversation) {
        if(!conversation.getStoryRecordingPushId().equals("none")) {
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

    private boolean isUserTurnToPickPrompts(Conversation conversation) {
        if(isCurrentUsersTurn(conversation) &&
                !isProposedPromptSelected(conversation)) {
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
        FirebaseAuth.getInstance().signOut();
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
