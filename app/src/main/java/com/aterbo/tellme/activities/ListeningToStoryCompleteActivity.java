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
import com.aterbo.tellme.classes.ConversationSummary;
import com.aterbo.tellme.classes.Prompt;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ListeningToStoryCompleteActivity extends AppCompatActivity {

    private ConversationSummary conversation;
    private String selectedConvoPushId;
    ArrayList<Prompt> randomPromptList;
    int numberOfPrompts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        randomPromptList = new ArrayList<>();
        setContentView(R.layout.activity_listening_to_story_complete);
        getConversation();
        setNumberOfPromptsFBListener();
        setViews();
        updateConversationPrompt(new Prompt("null", "null"));
        changeConversationNextPlay();
        eliminateCurrentStory();
        }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("conversation");
        selectedConvoPushId = intent.getStringExtra("conversationPushId");
    }

    private void changeConversationNextPlay() {
        conversation.changeNextPlayer();
    }

    private void eliminateCurrentStory(){
        conversation.setStoryRecordingFilePath("none");
    }

    private void setViews(){
        ((TextView)findViewById(R.id.sender_text)).setText("You just listened to a story from " +
                conversation.userEmailsAsString() + " about");
        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    public void goBackToMainScreenButtonClick(View view){
        Toast.makeText(this, "Now that you've heard this story, " + conversation.userEmailsAsString()
                + " wants to hear a story from you!"
                , Toast.LENGTH_LONG).show();
        conversation.setProposedPrompt1(randomPromptList.get(0));
        conversation.setProposedPrompt1(randomPromptList.get(1));
        conversation.setProposedPrompt1(randomPromptList.get(2));

        updateConversationAfterListening();
    }

    private void updateConversationPrompt(Prompt newPrompt){
        conversation.setCurrentPrompt(newPrompt);
    }

    public void updateConversationAfterListening(){
        Firebase baseRef = new Firebase(Constants.FIREBASE_LOCATION);

        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userEmail : conversation.getUsersInConversationEmails()) {
            convoInfoToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + selectedConvoPushId, conversationToAddHashMap);
        }

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

    private void getThreeRandomPrompts(){
        int[] randNumList = new int[3];
        randNumList = getThreeRandomPromptIDNumbers();

        for (int promptId :
                randNumList) {
            getRandomPrompt(promptId);
        }
    }

    private void setNumberOfPromptsFBListener(){
        Firebase ref = new Firebase(Constants.FIREBASE_LOCATION + "/" + Constants.FIREBASE_LOCATION_TOTAL_NUMBER_OF_PROMPTS);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                Long tempNumber = (Long) snapshot.getValue();
                numberOfPrompts = tempNumber.intValue();
                getThreeRandomPrompts();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    public void getRandomPrompt(int promptIdNumber){

        Firebase promptRef = new Firebase(Constants.FIREBASE_LOCATION + "/"
                + Constants.FIREBASE_LOCATION_PROMPTS + "/" + promptIdNumber);

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                randomPromptList.add(snapshot.getValue(Prompt.class));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private int[] getThreeRandomPromptIDNumbers(){
        Random rand = new Random();
        int num1 = rand.nextInt((numberOfPrompts) + 1);
        int num2 = rand.nextInt((numberOfPrompts) + 1);
        while (num2 == num1){
            num2 = rand.nextInt((numberOfPrompts) + 1);
        }
        int num3 = rand.nextInt((numberOfPrompts) + 1);
        while (num3 == num2 || num3 == num1){
            num3 = rand.nextInt((numberOfPrompts) + 1);
        }
        int[] randNumList = {num1, num2, num3};
        return randNumList;
    }
}
