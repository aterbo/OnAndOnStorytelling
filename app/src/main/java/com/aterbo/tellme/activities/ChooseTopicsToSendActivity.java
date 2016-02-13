package com.aterbo.tellme.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aterbo.tellme.R;

public class ChooseTopicsToSendActivity extends AppCompatActivity {

    Button sendTopicOption1;
    Button sendTopicOption2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_topics_to_send);

        initializeViews();
        set
    }

    private void initializeViews() {
        sendTopicOption1 = (Button)findViewById(R.id.send_topic_option_1);
        sendTopicOption2 = (Button)findViewById(R.id.send_topic_option_2);
    }

    public void sendTopic1(View view){

    }

    public void sendTopic2(View view){

    }
}
