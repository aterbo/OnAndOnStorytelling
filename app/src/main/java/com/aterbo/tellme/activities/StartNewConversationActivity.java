package com.aterbo.tellme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseListAdapter;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartNewConversationActivity extends AppCompatActivity {

    private FirebaseListAdapter<User> mListAdapter;
    private ListView mListView;
    private Firebase mUsersRef;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        currentUserName = intent.getStringExtra(Constants.USER_NAME_INTENT_KEY);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will eventually let you add a new friend to your " +
                        "contact list", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

    mUsersRef = new Firebase(Constants.FB_LOCATION+ "/" + Constants.FB_LOCATION_USERS);
    initializeScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }

    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_display_all_users);

        mListAdapter = new FirebaseListAdapter<User>(this, User.class,
                android.R.layout.two_line_list_item, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ((TextView)v.findViewById(android.R.id.text1)).setText(model.getUserName());
                ((TextView)v.findViewById(android.R.id.text2)).setText(model.getEmail().replace(",", "."));
            }
        };
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = mListAdapter.getItem(position);
                Log.i("SELECTED USER", selectedUser.getUserName());
                if (isSelectedUserCurrentUser(selectedUser)) {
                    Toast.makeText(getApplicationContext(), "You can only talk to yourself if you're crazy.", Toast.LENGTH_SHORT).show();
                } else {
                    makeANewConversationWith(selectedUser);
                }
            }
        });
    }

    private boolean isSelectedUserCurrentUser(User selectedUser){
        if (selectedUser.getUserName().equals(currentUserName)){
            return true;
        }
        return false;
    }

    private void makeANewConversationWith(User selectedUser){
        Firebase baseRef = new Firebase(Constants.FB_LOCATION);
        Firebase convoParticipantsRef = baseRef.child(Constants.FB_LOCATION_CONVO_PARTICIPANTS);

        Firebase newConversationRef = convoParticipantsRef.push();
        final String newConversationPushId = newConversationRef.getKey();
        String selectedUserName = selectedUser.getUserName();

        HashMap<String, Object> convoUserNames = new HashMap<String, Object>();

        Prompt noCurrentPrompt = new Prompt();
        ArrayList<Prompt> promptArrayList = new ArrayList<>();
        promptArrayList.add(new Prompt());
        promptArrayList.add(new Prompt());
        promptArrayList.add(new Prompt());

        ArrayList<String> userNamesInConversation = new ArrayList<>();
        userNamesInConversation.add(currentUserName);
        userNamesInConversation.add(selectedUserName);

        final Conversation conversation = new Conversation(userNamesInConversation,
                selectedUserName, currentUserName, noCurrentPrompt, promptArrayList);

        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        convoUserNames.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                newConversationPushId + "/" + currentUserName, "creator");
        convoUserNames.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                newConversationPushId + "/" + selectedUserName, "recipient");

        for (String userNames : userNamesInConversation) {
            convoUserNames.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userNames + "/" + newConversationPushId, itemToAddHashMap);
        }

        baseRef.updateChildren(convoUserNames, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASECREATENEWCONVO", "Error adding convo to Firebase");
                }
                Log.i("FIREBASECREATENEWCONVO", "Convo added to Firebase successfully");
                moveToNextActivity(conversation, newConversationPushId);
            }
        });
    }


    private void moveToNextActivity(Conversation conversation, String selectedConvoPushId){
        Intent intent = new Intent(this, ChooseTopicsToSendActivity.class);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
    }
}
