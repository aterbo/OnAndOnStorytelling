package com.aterbo.tellme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Prompt;

import java.util.ArrayList;

public class ChooseTopicsToSendActivity extends AppCompatActivity {

    Button sendTopicOption1;
    Button sendTopicOption2;
    ArrayList<Prompt> chosenPromptList;
    ArrayList<Prompt> promptOptionsList;
    int promptCountTracker = 0;
    final static int TOTAL_ROUNDS_OF_PROMPTS_TO_PRESENT = 3;
    final static int NUMBER_OF_PROMPTS_PRESENTED_PER_ROUND = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_topics_to_send);

        initializeViews();
        getPromptOptionsList();
        askForRoundOfPrompts();
    }

    private void initializeViews() {
        sendTopicOption1 = (Button)findViewById(R.id.send_topic_option_1);
        sendTopicOption2 = (Button)findViewById(R.id.send_topic_option_2);
        chosenPromptList = new ArrayList<>();
        promptOptionsList = new ArrayList<>();
    }

    private void getPromptOptionsList(){
        promptOptionsList = generateDummyPromptList();
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

    private void questioningComplete(){
        //TODO: figure out how to send this to someone!
        Toast.makeText(this, "PROMPTS SELECTED!!!!!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }

    private void setOptionsToButtons(){
        String promptOption1 = promptOptionsList.get(promptCountTracker).getPromptText();
        promptCountTracker++;
        String promptOption2 = promptOptionsList.get(promptCountTracker).getPromptText();
        promptCountTracker++;

        sendTopicOption1.setText(promptOption1);
        sendTopicOption2.setText(promptOption2);
    }

    public void sendTopic1(View view){
        addChosenPromptToList(promptCountTracker - 2);
        askForRoundOfPrompts();
    }

    public void sendTopic2(View view){
        addChosenPromptToList(promptCountTracker - 1);
        askForRoundOfPrompts();
    }

    private void addChosenPromptToList(int indexNumber){
        chosenPromptList.add(promptOptionsList.get(indexNumber));
        Toast.makeText(this, promptOptionsList.get(indexNumber).getPromptText(), Toast.LENGTH_LONG).show();
    }

    private ArrayList<Prompt> generateDummyPromptList(){
        ArrayList<Prompt> dummyList = new ArrayList<>();
        dummyList.add(new Prompt("What is on the top of your bucket list?"));
        dummyList.add(new Prompt("What is the most creative profanity you've ever heard? Or create your own?"));
        dummyList.add(new Prompt("Do you follow logic?"));
        dummyList.add(new Prompt("Do you have a photographic memory?"));
        dummyList.add(new Prompt("Who helped you in life?"));
        dummyList.add(new Prompt("Would you describe yourself as left-wing or right-wing?"));

        return dummyList;
    }
}
