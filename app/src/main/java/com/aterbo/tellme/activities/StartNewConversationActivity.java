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

import com.aterbo.tellme.FBHelper;
import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StartNewConversationActivity extends AppCompatActivity {

    FirebaseListAdapter<User> mListAdapter;
    ListView mListView;
    private Firebase mUsersRef;
    String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        currentUserEmail = intent.getStringExtra("currentUserEmail");

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

    public void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_display_all_users);

        mListAdapter = new FirebaseListAdapter<User>(this, User.class,
                android.R.layout.two_line_list_item, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ((TextView)v.findViewById(android.R.id.text1)).setText(model.getEmail().replace(",", "."));
                ((TextView)v.findViewById(android.R.id.text2)).setText(model.getUserName());
            }
        };
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = mListAdapter.getItem(position);
                Log.i("SELECTED USER", selectedUser.getEmail());
                if (isSelectedUserCurrentUser(selectedUser)) {
                    Toast.makeText(getApplicationContext(), "You can only talk to yourself if you're crazy.", Toast.LENGTH_SHORT).show();
                } else {
                    makeANewConversationWith(selectedUser);
                }
            }
        });
    }

    private boolean isSelectedUserCurrentUser(User selectedUser){
        if (selectedUser.getEmail().equals(currentUserEmail)){
            return true;
        }
        return false;
    }

    private void makeANewConversationWith(User selectedUser){
        Firebase baseRef = new Firebase(Constants.FB_LOCATION);
        Firebase convoParticipantsRef = baseRef.child(Constants.FB_LOCATION_CONVO_PARTICIPANTS);

        Firebase newConversationRef = convoParticipantsRef.push();
        final String newConversationPushId = newConversationRef.getKey();
        String selectedUserEmail = selectedUser.getEmail();

        HashMap<String, Object> convoEmails = new HashMap<String, Object>();

        Prompt noCurrentPrompt = new Prompt();
        ArrayList<Prompt> promptArrayList = new ArrayList<>();
        promptArrayList.add(new Prompt());
        promptArrayList.add(new Prompt());
        promptArrayList.add(new Prompt());

        ArrayList<String> usersInConversationEmails = new ArrayList<>();
        usersInConversationEmails.add(currentUserEmail.replace(".",","));
        usersInConversationEmails.add(selectedUserEmail.replace(".",","));

        final Conversation conversation = new Conversation(usersInConversationEmails,
                selectedUserEmail, noCurrentPrompt, promptArrayList);

        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        convoEmails.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                newConversationPushId + "/" + currentUserEmail.replace(".",","), "creator");
        convoEmails.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                newConversationPushId + "/" + selectedUserEmail.replace(".",","), "recipient");

        for (String userEmail : usersInConversationEmails) {
            convoEmails.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + newConversationPushId, itemToAddHashMap);
        }

        baseRef.updateChildren(convoEmails, new Firebase.CompletionListener() {
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
        intent.putExtra("conversation", conversation);
        intent.putExtra("selectedConversationPushId", selectedConvoPushId);
        startActivity(intent);
    }
}
