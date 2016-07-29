package com.onanon.app.activities;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddUserToConversationActivity extends AppCompatActivity {

    private FirebaseListAdapter<User> mListAdapter;
    private ListView mListView;
    private DatabaseReference baseRef, mUsersRef;
    private String currentUserName;
    private String selectedConvoPushId;
    private Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_user_to_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        baseRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = baseRef.child(Constants.FB_LOCATION_USERS);

        getConversation();
        getUserNameFromSharedPreferences();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will eventually let you add a new friend to your " +
                        "contact list", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        initializeScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }

    private void getUserNameFromSharedPreferences(){
        PrefManager prefManager = new PrefManager(this);
        currentUserName = prefManager.getUserNameFromSharedPreferences();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
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
                } else if (isSelectedUserAlreadyInConversation(selectedUser)) {
                    Toast.makeText(getApplicationContext(), "That user is already in the conversation!.", Toast.LENGTH_SHORT).show();
                } else {
                    addUserToConversation(selectedUser);
                    updateConversationInFB(selectedUser);
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

    private boolean isSelectedUserAlreadyInConversation(User selectedUser){
        String selectedUserName = selectedUser.getUserName();
        if (conversation.getUserNamesInConversation().contains(selectedUserName)){
            return true;
        }
        return false;
    }

    private void addUserToConversation(User selectedUser) {
        String addedUserName = selectedUser.getUserName();
        conversation.getUserNamesInConversation().add(addedUserName);
        if (!conversation.getFbStorageFilePathToRecording().equals("none")) {
            conversation.markUserAsHasHeardStory(addedUserName);
        }
    }

    private void updateConversationInFB(User selectedUser){
        DatabaseReference convoParticipantsRef = baseRef.child(Constants.FB_LOCATION_CONVO_PARTICIPANTS);

        String selectedUserName = selectedUser.getUserName();

        HashMap<String, Object> convoUserNames = new HashMap<String, Object>();

        ArrayList<String> userNamesInConversation = conversation.getUserNamesInConversation();

        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        convoUserNames.put("/" + Constants.FB_LOCATION_CONVO_PARTICIPANTS + "/" +
                selectedConvoPushId + "/" + selectedUserName, "added");

        for (String userNames : userNamesInConversation) {
            convoUserNames.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userNames + "/" + selectedConvoPushId, itemToAddHashMap);
        }

        baseRef.updateChildren(convoUserNames, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");

                goBackToConversationList();
            }
        });
    }

    private void goBackToConversationList(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }
}

