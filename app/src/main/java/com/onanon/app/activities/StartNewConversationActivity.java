package com.onanon.app.activities;

import android.app.ProgressDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;
import com.onanon.app.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StartNewConversationActivity extends AppCompatActivity {

    private FirebaseListAdapter<User> mListAdapter;
    private ListView mListView;
    private DatabaseReference baseRef, mUsersRef, mNumberOfPromptsRef;
    private String currentUserName;

    private ArrayList<Prompt> promptOptionsList;
    private int promptCountTracker = 0;
    private int numberOfPromptsOnServer;
    private int numberOfPromptsToGet;
    private ProgressDialog progressDialog;

    private ValueEventListener mNumberOfPromptsRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_new_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = Utils.getSpinnerDialog(this);

        baseRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = baseRef.child(Constants.FB_LOCATION_USERS);

        promptOptionsList = new ArrayList<>();
        getUserNameFromSharedPreferences();
        getPromptOptionsList();

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

        if (mNumberOfPromptsRefListener != null) {
            mNumberOfPromptsRef.removeEventListener(mNumberOfPromptsRefListener);
        }
    }

    private void getUserNameFromSharedPreferences(){
        PrefManager prefManager = new PrefManager(this);
        currentUserName = prefManager.getUserNameFromSharedPreferences();
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
        DatabaseReference convoParticipantsRef = baseRef.child(Constants.FB_LOCATION_CONVO_PARTICIPANTS);

        DatabaseReference newConversationRef = convoParticipantsRef.push();
        final String newConversationPushId = newConversationRef.getKey();
        String selectedUserName = selectedUser.getUserName();

        HashMap<String, Object> convoUserNames = new HashMap<String, Object>();

        Prompt noCurrentPrompt = new Prompt();
        ArrayList<Prompt> promptArrayList = promptOptionsList;

        ArrayList<String> userNamesInConversation = new ArrayList<>();
        userNamesInConversation.add(currentUserName);
        userNamesInConversation.add(selectedUserName);

        final Conversation conversation = new Conversation(userNamesInConversation,
                currentUserName, selectedUserName, noCurrentPrompt, promptArrayList);

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

        baseRef.updateChildren(convoUserNames, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");

                moveToNextActivity(conversation, newConversationPushId);
            }
        });
    }

    private void getPromptOptionsList(){
        setNumberOfPromptsFBListener();
    }

    private void setNumberOfPromptsFBListener(){
        mNumberOfPromptsRef = baseRef.child(Constants.FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS);

        mNumberOfPromptsRefListener = mNumberOfPromptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                Long tempNumber = (Long) snapshot.getValue();
                numberOfPromptsOnServer = tempNumber.intValue();
                getRandomPromptList();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getRandomPromptList(){
        numberOfPromptsToGet = 3;

        ArrayList<Integer> randNumList = getRandomArrayListOfIntegers(numberOfPromptsToGet);

        for (int promptId : randNumList) {
            getRandomPrompt(promptId);
        }
    }

    private ArrayList<Integer> getRandomArrayListOfIntegers(int numberToGet){

        ArrayList<Integer> list = new ArrayList<>();
        Random random = new Random();

        while (list.size() < numberToGet) {
            Integer nextRandom = random.nextInt(numberOfPromptsOnServer);
            if(!list.contains(nextRandom)) {
                list.add(nextRandom);
            }
        }
        return list;
    }

    private void getRandomPrompt(int promptIdNumber){
        DatabaseReference promptRef = baseRef.child(Constants.FB_LOCATION_PROMPTS)
                .child(Integer.toString(promptIdNumber));

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                promptOptionsList.add(snapshot.getValue(Prompt.class));

                if (promptOptionsList.size() == numberOfPromptsToGet) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void moveToNextActivity(Conversation conversation, String selectedConvoPushId){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
        finish();
    }
}
