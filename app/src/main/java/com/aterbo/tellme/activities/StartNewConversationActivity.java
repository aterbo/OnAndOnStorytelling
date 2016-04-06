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

import java.util.ArrayList;

public class StartNewConversationActivity extends AppCompatActivity {

    FirebaseListAdapter<User> mListAdapter;
    ListView mListView;
    private Firebase mUsersRef;
    ArrayList<Prompt> masterPromptList;
    String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        currentUserEmail = intent.getStringExtra("currentUserEmail");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    mUsersRef = new Firebase(getResources().getString(R.string.firebase_url) + "/" +
            Constants.FIREBASE_LOCATION_USERS);
    initializeScreen();
    masterPromptList = new ArrayList<>();
    getRandomPrompt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
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
                continueWithNewConvo(selectedUser);
            }
        });
    }

    private void continueWithNewConvo(User selectedUser){
        //TODO: Make PromptList Selector

        ArrayList<Prompt> selectedPromptsList = new ArrayList<>();
        selectedPromptsList.add(masterPromptList.get(0));
        selectedPromptsList.add(masterPromptList.get(1));
        selectedPromptsList.add(masterPromptList.get(2));

        FBHelper fbHelper = new FBHelper(this);
        fbHelper.addNewConversation(currentUserEmail, selectedUser, selectedPromptsList);

        //startTellActivity(newConversation);
    }


    public void getRandomPrompt(){

        Firebase baseRef = new Firebase(this.getResources().getString(R.string.firebase_url));
        Firebase promptRef = baseRef.child("prompts");

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                int counter = 1;
                while (counter < 8) {
                    Prompt prompt = new Prompt((String) snapshot.child(Integer.toString(counter)).child("text").getValue(),
                            (String) snapshot.child(Integer.toString(counter)).child("tag").getValue());
                    //sendPromptSomewhereSomehow(prompt);
                    masterPromptList.add(prompt);
                    counter++;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }
}
