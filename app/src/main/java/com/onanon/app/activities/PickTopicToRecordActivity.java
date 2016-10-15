package com.onanon.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class PickTopicToRecordActivity extends AppCompatActivity {

    private Conversation conversation;
    private ArrayList<Prompt> promptOptionsList;
    private Button topicOption1;
    private Button topicOption2;
    private Button topicOption3;
    private View topOr;
    private View bottomOr;
    private String selectedConvoPushId;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_topic_to_record);
        getConversation();
        showConversationDetails();

        initializeViews();
        getPrompts();
        setPromptsToButtons();

    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_user_name);
        senderText.setText(conversation.getLastUserNameToTell());
    }

    private void initializeViews() {
        topicOption1 = (Button)findViewById(R.id.record_topic_option_1);
        topicOption2 = (Button)findViewById(R.id.record_topic_option_2);
        topicOption3 = (Button)findViewById(R.id.record_topic_option_3);
        promptOptionsList = new ArrayList<>();
        setProfilePicture();
    }

    private void setProfilePicture(){

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userIconRef = baseRef.child(Constants.FB_LOCATION_USERS)
                .child(conversation.getLastUserNameToTell()).child("profilePhotoUrl");
        context = this;

        userIconRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String convoIconUrl = (String) snapshot.getValue();
                ImageView profilePic = (ImageView) findViewById(R.id.conversation_icon);

                if (convoIconUrl != null && !convoIconUrl.isEmpty() && !convoIconUrl.contains(Constants.NO_PHOTO_KEY)) {
                    //Profile has picture

                    if (Character.toString(convoIconUrl.charAt(0)).equals("\"")) {
                        convoIconUrl = convoIconUrl.substring(1, convoIconUrl.length()-1);
                    }
                } else {
                    //profile does not have picture
                    convoIconUrl = "";
                }
                Glide.with(PickTopicToRecordActivity.this)
                        .load(convoIconUrl)
                        .bitmapTransform(new CropCircleTransformation(context))
                        .placeholder(R.drawable.word_treatment_512_84)
                        .fallback(R.drawable.word_treatment_512_84)
                        .dontAnimate()
                        .into(profilePic);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getPrompts(){
        promptOptionsList = conversation.returnProposedPromptsAsList();
    }

    private void setPromptsToButtons(){
        topicOption1.setText(promptOptionsList.get(0).getText());
        topicOption2.setText(promptOptionsList.get(1).getText());
        topicOption3.setText(promptOptionsList.get(2).getText());
    }

    public void topic1(View view){
        Prompt chosenTopic = promptOptionsList.get(0);
        conversation.setCurrentPrompt(chosenTopic);
        startNextActivity();
    }

    public void topic2(View view){
        Prompt chosenTopic = promptOptionsList.get(1);
        conversation.setCurrentPrompt(chosenTopic);
        startNextActivity();
    }

    public void topic3(View view){
        Prompt chosenTopic = promptOptionsList.get(2);
        conversation.setCurrentPrompt(chosenTopic);
        startNextActivity();
    }

    private void startNextActivity(){
        Intent intent = new Intent(this, RecordStoryActivity.class);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
        finish();
    }
}
