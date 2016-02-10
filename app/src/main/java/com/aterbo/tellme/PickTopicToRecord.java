package com.aterbo.tellme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PickTopicToRecord extends AppCompatActivity {

    String chosenTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_topic_to_record);
        chosenTopic = new String();
    }

    public void topic1(View view){
        chosenTopic = ((Button)findViewById(R.id.topic_1)).getText().toString();
        startNextActivity();
    }

    public void topic2(View view){
        chosenTopic = ((Button)findViewById(R.id.topic_2)).getText().toString();
        startNextActivity();
    }

    public void topic3(View view){
        chosenTopic = ((Button)findViewById(R.id.topic_3)).getText().toString();
        startNextActivity();
    }

    private void startNextActivity(){
        Intent intent = new Intent(this, RecordStory.class);
        intent.putExtra("ChosenTopic", chosenTopic);
        startActivity(intent);
    }
}
