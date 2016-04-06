package com.aterbo.tellme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;

import org.w3c.dom.Text;

public class ListeningToStoryCompleteActivity extends AppCompatActivity {

    private Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_to_story_complete);
        getConversation();
        changeConversationStatus();
        setViews();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("conversation");
    }

    private void changeConversationStatus(){
        conversation.setTimeSinceLastAction("now");
        conversation.setStatusToTell();
        //TODO:Update Conversation Status in FB
    }

    private void setViews(){
        ((TextView)findViewById(R.id.sender_text)).setText("You just listened to a story from " +
                conversation.getUsersNameAsString() + " about");
        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getPromptText());
    }

    public void goBackToMainScreenButtonClick(View view){
        Toast.makeText(this, "Now that you've heard this story, " + conversation.getUsersNameAsString()
                + " wants to hear a story from you!"
                , Toast.LENGTH_LONG).show();

        Prompt newPrompt = getNewPrompts();
        updateConversationPrompt(newPrompt);
        goBackToMainScreen();
    }

    private Prompt getNewPrompts(){
        return getDummyPrompts();
    }

    private Prompt getDummyPrompts(){
        return new Prompt("Tell me a story about an immigrant.", "An immigrant");
    }

    private void updateConversationPrompt(Prompt newPrompt){
        conversation.setCurrentPrompt(newPrompt);
        //TODO: Set prompt to FB
    }

    private void goBackToMainScreen(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}
