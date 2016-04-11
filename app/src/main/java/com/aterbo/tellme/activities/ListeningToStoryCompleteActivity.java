package com.aterbo.tellme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ListeningToStoryCompleteActivity extends AppCompatActivity {

    private Conversation conversation;
    private String selectedConvoPushId;
    private String recordingPushId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_to_story_complete);
        getConversation();
        setViews();
        eliminateCurrentStory();
        }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("conversation");
        selectedConvoPushId = intent.getStringExtra("conversationPushId");
    }

    private void eliminateCurrentStory(){
        recordingPushId = conversation.getStoryRecordingFilePath();
        conversation.setStoryRecordingFilePath("none");
        conversation.setCurrentPrompt(new Prompt("null", "null"));
    }

    private void setViews() {
        ((TextView) findViewById(R.id.sender_text)).setText("You just listened to a story from " +
                conversation.userEmailsAsString() + " about");
        ((TextView) findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    public void goBackToMainScreenButtonClick(View view){
        Toast.makeText(this, "Now that you've heard this story, " + conversation.userEmailsAsString()
                + " wants to hear a story from you!"
                , Toast.LENGTH_LONG).show();

        updateConversationAfterListening();
    }


    public void updateConversationAfterListening(){
        Firebase baseRef = new Firebase(Constants.FB_LOCATION);

        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userEmail : conversation.getUsersInConversationEmails()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + selectedConvoPushId, conversationToAddHashMap);
        }

        convoInfoToUpdate.put("/" + Constants.FB_LOCATION_RECORDINGS + "/" + recordingPushId,
                null);

        baseRef.updateChildren(convoInfoToUpdate, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");
                goBackToMainScreen();
            }
        });
    }


    private void goBackToMainScreen(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}
