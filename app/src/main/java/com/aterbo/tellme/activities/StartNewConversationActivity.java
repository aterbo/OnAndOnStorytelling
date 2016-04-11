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
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;
import java.util.Random;

public class StartNewConversationActivity extends AppCompatActivity {

    FirebaseListAdapter<User> mListAdapter;
    ListView mListView;
    private Firebase mUsersRef;
    ArrayList<Prompt> randomPromptList;
    String currentUserEmail;
    int numberOfPrompts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        currentUserEmail = intent.getStringExtra("currentUserEmail");

        setNumberOfPromptsFBListener();
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
    randomPromptList = new ArrayList<>();
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
        FBHelper fbHelper = new FBHelper(this);
        fbHelper.addNewConversation(currentUserEmail, selectedUser, randomPromptList);
    }

    private void getRandomPrompts(){
        int[] randNumList;
        randNumList = getThreeRandomPromptIDNumbers();

        for (int promptId :
                randNumList) {
            getRandomPrompt(promptId);
        }
    }

    private void setNumberOfPromptsFBListener(){
        Firebase ref = new Firebase(Constants.FB_LOCATION + "/" + Constants.FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                numberOfPrompts = ((Long) snapshot.getValue()).intValue();
                getRandomPrompts();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    public void getRandomPrompt(int promptIdNumber){

        Firebase promptRef = new Firebase(Constants.FB_LOCATION + "/"
                + Constants.FB_LOCATION_PROMPTS + "/" + promptIdNumber);

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                randomPromptList.add(snapshot.getValue(Prompt.class));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    private int[] getThreeRandomPromptIDNumbers(){
        Random rand = new Random();
        int num1, num2, num3;
        num1 = rand.nextInt((numberOfPrompts) + 1);
        do {
            num2 = rand.nextInt((numberOfPrompts) + 1);
        } while (num2 == num1);
        do {
            num3 = rand.nextInt((numberOfPrompts) + 1);
        } while (num3 == num1 || num3 == num2);


        int[] randNumList = {num1, num2, num3};
        return randNumList;
    }
}
