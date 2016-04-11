package com.aterbo.tellme.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.aterbo.tellme.FBHelper;
import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

public class ConversationListActivity extends FirebaseLoginBaseActivity {

    private String currentUserEmail;
    private String selectedConvoPushId;
    private Firebase baseRef;
    FirebaseListAdapter<Conversation> mListAdapter;

    @Override
    public Firebase getFirebaseRef() {
        // TODO: Return your Firebase ref
        return baseRef;
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the authentication provider
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the user
        System.out.println("Error logging in");
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        // TODO: Handle successful login
        System.out.println("User ID: ");
        Log.i("LOGGEDIN", "YAAAAAAAAAAY");
        Log.i("LOGGEDIN", authData.getUid());
        currentUserEmail = authData.getProviderData().get("email").toString();
        Log.i("LOGGEDIN", currentUserEmail);
        setFirebaseListToUserEmail();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        initializeFirebase();

        setSupportActionBar(toolbar);

        setFloatingActionButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setEnabledAuthProvider(AuthProviderType.PASSWORD);
    }

    private void initializeFirebase(){
        Firebase.setAndroidContext(this);
        baseRef = new Firebase(Constants.FB_LOCATION);
    }

    private void setFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewConversation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFirebaseListToUserEmail() {
        final ListView listView = (ListView) this.findViewById(R.id.conversation_list);
        mListAdapter = new FirebaseListAdapter<Conversation>(this, Conversation.class,
                R.layout.layout_conversation_list_item, baseRef.child("userConvos").child(currentUserEmail.replace(".",","))) {
            @Override
            protected void populateView(View v, Conversation conversation, int position) {

                String title = determineTitle(conversation);
                ((TextView) v.findViewById(R.id.conversation_title)).setText(title);
                ((TextView) v.findViewById(R.id.conversation_next_turn)).setText("Next Up: " + conversation.getNextPlayersEmail());
                ((TextView) v.findViewById(R.id.conversation_time_since_action)).setVisibility(View.GONE);
                (v.findViewById(R.id.conversation_story_duration)).setVisibility(View.GONE);
            }
        };

        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation selectedConvo = mListAdapter.getItem(position);
                if (selectedConvo != null) {
                    selectedConvoPushId = mListAdapter.getRef(position).getKey();
                    determineActivityToStart(selectedConvo);
                }
            }
        });
    }

    private void determineActivityToStart(Conversation conversation){

        if (isUserTurnToTell(conversation)) {
            Log.i("PickedAConvo!", "My turn to tell");
            startTellActivity(conversation);

        } else if (isUserTurnToHear(conversation)) {
            startListenActivity(conversation);
            Log.i("PickedAConvo!", "My turn to hear");

        } else if (isUserWaiting(conversation)) {
            //TODO: startWait/PingActivity();
            Log.i("PickedAConvo!", "My turn to hear");
        }
    }

    private String determineTitle(Conversation conversation){
        if (isUserTurnToTell(conversation)) {
            return conversation.proposedPromptsTagAsString();

        } else if (isUserTurnToHear(conversation)) {
            return conversation.getCurrentPrompt().getText();

        } else if (isUserWaiting(conversation)) {
            return "Waiting!";
        }
        return "Oops";
    }

    private boolean isUserTurnToTell(Conversation conversation) {
        if(conversation.getNextPlayersEmail().equals(currentUserEmail.replace(".",","))
                && conversation.getStoryRecordingPushId().equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserTurnToHear(Conversation conversation) {
        if(conversation.getNextPlayersEmail().equals(currentUserEmail.replace(".",","))
                && !conversation.getStoryRecordingPushId().equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isUserWaiting(Conversation selectedConvo) {
        if(!selectedConvo.getNextPlayersEmail().equals(currentUserEmail.replace(".",","))) {
            return true;
        } else{
            return false;
        }
    }

    private void startTellActivity(Conversation conversation){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        intent.putExtra("selectedConversation", conversation);
        intent.putExtra("selectedConversationPushId", selectedConvoPushId);
        startActivity(intent);
    }

    private void startListenActivity(Conversation conversation){
        Intent intent = new Intent(this, ListenToStoryActivity.class);
        intent.putExtra("selectedConversation", conversation);
        intent.putExtra("selectedConversationPushId", selectedConvoPushId);
        startActivity(intent);
    }

    public void logIn(View view){
        showFirebaseLoginPrompt();
    }

    public void addNewUser(View view){
        AlertDialog addNewUserDialog = addNewUserDialog("Add New User");
        addNewUserDialog.show();
    }
    private void startNewConversation(){
        Intent intent = new Intent(this, StartNewConversationActivity.class);
        intent.putExtra("currentUserEmail", currentUserEmail.replace(".",","));
        startActivity(intent);
    }


    public AlertDialog addNewUserDialog(String message) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.login, null);
        final AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
        failAlert.setTitle("Registration Failed");
        failAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add New User");
        alert.setMessage(message);
        alert.setView(textEntryView);
        alert.setPositiveButton("Create User", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.userNameEditText);
                    final EditText emailInput = (EditText) textEntryView.findViewById(R.id.emailEditText);
                    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.passwordEditText);
                    Log.i("ADDUSER", usernameInput.getText().toString() + passwordInput.getText().toString());

                    FBHelper fbHelper = new FBHelper();
                    fbHelper.addNewUserToServer(usernameInput.getText().toString(),
                            emailInput.getText().toString(),
                            passwordInput.getText().toString());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        return alert.create();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }
}
