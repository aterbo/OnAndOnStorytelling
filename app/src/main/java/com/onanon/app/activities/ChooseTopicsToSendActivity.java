package com.onanon.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChooseTopicsToSendActivity extends AppCompatActivity {

    private Button sendTopicOption1;
    private Button sendTopicOption2;
    private ArrayList<Prompt> promptOptionsList;
    private ArrayList<Prompt> selectedPromptsList;
    private String selectedConvoPushId;
    private Conversation conversation;
    private DatabaseReference baseRef;
    private int promptCountTracker = 0;
    private int numberOfPromptsOnServer;
    private int numberOfPromptsToGet;
    private final static int TOTAL_ROUNDS_OF_PROMPTS_TO_PRESENT = 3;
    private final static int NUMBER_OF_PROMPTS_PRESENTED_PER_ROUND = 2;

    private DatabaseReference mNumberOfPromptsRef;
    private ValueEventListener mNumberOfPromptsRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_topics_to_send);

        baseRef = FirebaseDatabase.getInstance().getReference();
        getConversation();
        initializeViews();
        getPromptOptionsList();
        setConversationToView();
        conversation.clearProposedTopics();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void initializeViews() {
        sendTopicOption1 = (Button)findViewById(R.id.send_topic_option_1);
        sendTopicOption2 = (Button)findViewById(R.id.send_topic_option_2);
        promptOptionsList = new ArrayList<>();
        selectedPromptsList = new ArrayList<>();
    }

    private void setConversationToView() {
        ((TextView) findViewById(R.id.next_storyteller_prompt)).setText(
                conversation.getLastPlayersUserName() + " is next to tell a story");
    }

    private void getPromptOptionsList(){
        setNumberOfPromptsFBListener();
    }

    private void askForRoundOfPrompts(){
        if (isQuestioningDone()) {
            questioningComplete();
        } else {
            setOptionsToButtons();
        }
    }

    private boolean isQuestioningDone(){
        if (promptCountTracker >= (TOTAL_ROUNDS_OF_PROMPTS_TO_PRESENT * NUMBER_OF_PROMPTS_PRESENTED_PER_ROUND)) {
            return true;
        } else {
            return false;
        }
    }

    private void setOptionsToButtons(){
        String promptOption1 = promptOptionsList.get(promptCountTracker).getText();
        promptCountTracker++;
        String promptOption2 = promptOptionsList.get(promptCountTracker).getText();
        promptCountTracker++;

        sendTopicOption1.setText(promptOption1);
        sendTopicOption2.setText(promptOption2);
    }

    private void updateConversation(){
        conversation.changeNextPlayer();
        conversation.setProposedPrompt1(selectedPromptsList.get(0));
        conversation.setProposedPrompt2(selectedPromptsList.get(1));
        conversation.setProposedPrompt3(selectedPromptsList.get(2));
    }


    public void sendTopic1(View view) {
        addChosenPromptToList(promptCountTracker - 2);
        askForRoundOfPrompts();
    }

    public void sendTopic2(View view){
        addChosenPromptToList(promptCountTracker - 1);
        askForRoundOfPrompts();
    }

    private void addChosenPromptToList(int indexNumber) {
        selectedPromptsList.add(promptOptionsList.get(indexNumber));
    }

    private void questioningComplete(){
        Toast.makeText(this, "PROMPTS SELECTED!", Toast.LENGTH_SHORT).show();
        updateConversation();
        updateConversationOnServer();
    }

    private void updateConversationOnServer(){
        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userName : conversation.getUserNamesInConversation()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userName + "/" + selectedConvoPushId, conversationToAddHashMap);
        }

        baseRef.updateChildren(convoInfoToUpdate, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");

                returnToConversationList();
            }
        });
    }

    private void returnToConversationList(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }


    private void getSixRandomPrompts(){
        numberOfPromptsToGet = NUMBER_OF_PROMPTS_PRESENTED_PER_ROUND * TOTAL_ROUNDS_OF_PROMPTS_TO_PRESENT;
        int[] randNumList = new int[numberOfPromptsToGet];
        randNumList = getSixRandomPromptIDNumbers();

        for (int promptId : randNumList) {
            getRandomPrompt(promptId);
        }
    }

    private void setNumberOfPromptsFBListener(){
        mNumberOfPromptsRef = baseRef.child(Constants.FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS);

        mNumberOfPromptsRefListener = mNumberOfPromptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                Long tempNumber = (Long) snapshot.getValue();
                numberOfPromptsOnServer = tempNumber.intValue();
                getSixRandomPrompts();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getRandomPrompt(int promptIdNumber){
        DatabaseReference promptRef = baseRef.child(Constants.FB_LOCATION_PROMPTS)
                .child(Integer.toString(promptIdNumber));

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                promptOptionsList.add(snapshot.getValue(Prompt.class));
                numberOfPromptsToGet--;
                if (numberOfPromptsToGet == 0) {
                    proceed();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private int[] getSixRandomPromptIDNumbers(){
        Random rand = new Random();
        int num1, num2, num3, num4, num5, num6;
        num1 = rand.nextInt((numberOfPromptsOnServer) + 1);
        do {
            num2 = rand.nextInt((numberOfPromptsOnServer) + 1);
        } while (num2 == num1);
        do {
            num3 = rand.nextInt((numberOfPromptsOnServer) + 1);
        } while (num3 == num1 || num3 == num2);
        do {
            num4 = rand.nextInt((numberOfPromptsOnServer) + 1);
        } while (num4 == num1 || num4 == num2 || num4 == num3);
        do {
            num5 = rand.nextInt((numberOfPromptsOnServer) + 1);
        } while (num5 == num1 || num5 == num2 || num5 == num3 || num5 == num4);
        do {
            num6 = rand.nextInt((numberOfPromptsOnServer) + 1);
        } while (num6 == num1 || num6 == num2 || num6 == num3 || num6 == num4 || num6 == num5);


        int[] randNumList = {num1, num2, num3, num4, num5, num6};
        return randNumList;
    }

    private void proceed(){
        findViewById(R.id.send_topic_option_1).setVisibility(View.VISIBLE);
        findViewById(R.id.or_section).setVisibility(View.VISIBLE);
        findViewById(R.id.send_topic_option_2).setVisibility(View.VISIBLE);
        askForRoundOfPrompts();
    }

    public void onDestroy() {
        super.onDestroy();

        mNumberOfPromptsRef.removeEventListener(mNumberOfPromptsRefListener);

    }
}