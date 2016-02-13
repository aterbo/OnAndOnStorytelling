package com.aterbo.tellme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Prompt;

import java.util.ArrayList;

public class PickTopicToRecordActivity extends AppCompatActivity {

    Prompt chosenTopic;
    ArrayList<Prompt> promptOptionsList;
    Button topicOption1;
    Button topicOption2;
    Button topicOption3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_topic_to_record);

        initializeViews();
        getPrompts();
        setPromptsToButtons();
    }

    private void initializeViews() {
        topicOption1 = (Button)findViewById(R.id.record_topic_option_1);
        topicOption2 = (Button)findViewById(R.id.record_topic_option_2);
        topicOption3 = (Button)findViewById(R.id.record_topic_option_3);
        promptOptionsList = new ArrayList<>();
    }

    private void getPrompts(){
        promptOptionsList = generateDummyPromptList();
    }

    private void setPromptsToButtons(){
        topicOption1.setText(promptOptionsList.get(0).getPromptText());
        topicOption2.setText(promptOptionsList.get(1).getPromptText());
        topicOption3.setText(promptOptionsList.get(2).getPromptText());
    }

    public void topic1(View view){
        chosenTopic = promptOptionsList.get(0);
        startNextActivity();
    }

    public void topic2(View view){
        chosenTopic = promptOptionsList.get(1);
        startNextActivity();
    }

    public void topic3(View view){
        chosenTopic = promptOptionsList.get(2);
        startNextActivity();
    }

    private void startNextActivity(){
        Intent intent = new Intent(this, RecordStoryActivity.class);
        intent.putExtra("ChosenTopic", chosenTopic.getPromptText());
        startActivity(intent);
    }

    private ArrayList<Prompt> generateDummyPromptList(){
        ArrayList<Prompt> dummyList = new ArrayList<>();
        dummyList.add(new Prompt("Would you rather go to a greasy spoon diner or a fine dining restaurant?"));
        dummyList.add(new Prompt("Who did you last vote for?"));
        dummyList.add(new Prompt("What is one trait you've inherited from your mother?"));

        return dummyList;
    }
}
