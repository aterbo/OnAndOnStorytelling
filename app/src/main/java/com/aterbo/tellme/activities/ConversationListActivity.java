package com.aterbo.tellme.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.aterbo.tellme.FBHelper;
import com.aterbo.tellme.R;
import com.aterbo.tellme.SQLite.DBHelper;
import com.aterbo.tellme.adaptors.ConversationListAdaptor;
import com.aterbo.tellme.alertdialogs.PingStorytellerDialog;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends FirebaseLoginBaseActivity {

    ArrayList<Conversation> conversations;
    int toHearSeparatorPosition;
    int toWaitForSeparatorPosition;
    int toTellSeparatorPosition;
    ArrayList<Prompt> masterPromptList;

    ListView conversationListView;
    ConversationListAdaptor conversationListAdaptor;
    ArrayList<Object> objectList;
    private Firebase baseRef;

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
        Log.e("LOGGEDIN", "YAAAAAAAAAAY");
        Log.e("LOGGEDIN", authData.getUid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        initializeFirebase();
/*
        masterPromptList = new ArrayList<>();
        getRandomPrompt();
        setSupportActionBar(toolbar);

        setFloatingActionButton();
        getConversationsFromFB();
//        getConversationsFromDB();
        constructConversationList();
        setListAdaptor();
        */

    }

    @Override
    protected void onStart() {
        super.onStart();
        setEnabledAuthProvider(AuthProviderType.PASSWORD);
    }

    private void initializeFirebase(){
        Firebase.setAndroidContext(this);
        baseRef = new Firebase(this.getResources().getString(R.string.firebase_url));
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

    private void setListAdaptor() {
        conversationListAdaptor = new ConversationListAdaptor(objectList, this);
        conversationListView = (ListView)findViewById(R.id.conversation_list);
        conversationListView.setAdapter(conversationListAdaptor);
        conversationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                respondToListClick(position);
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

    private void respondToListClick(int position){
        if (position<(toHearSeparatorPosition)){
            Conversation selectedConversation = (Conversation)objectList.get(position);
            startTellActivity(selectedConversation);
        } else if (position<(toWaitForSeparatorPosition)){
            Conversation selectedConversation = (Conversation)objectList.get(position);
            startListenActivity(selectedConversation);
        } else {
            DialogFragment newFragment = PingStorytellerDialog.newInstance();
            newFragment.show(getFragmentManager(), "ping");
        }
    }

    private void constructConversationList(){
        objectList = new ArrayList<>();

        setAllSeparators();

        int status;

        for (Conversation conversation : conversations){
            status = conversation.getStatus();
            switch (status) {
                case 0:
                    objectList.add(1, conversation);
                    toHearSeparatorPosition = toHearSeparatorPosition + 1;
                    toWaitForSeparatorPosition = toWaitForSeparatorPosition + 1;
                    break;
                case 1:
                    objectList.add(toHearSeparatorPosition+1, conversation);
                    toWaitForSeparatorPosition = toWaitForSeparatorPosition + 1;
                    break;
                case 2:
                    objectList.add(toWaitForSeparatorPosition+1, conversation);
                    break;
            }
        }
    }

    private void getConversationsFromFB(){

    }

    private void getConversationsFromDB(){
        DBHelper db = new DBHelper(this);
        conversations = db.getConversationList();
        /*
        if(conversations.size()<=1) {
            SupplyTestData testListData = new SupplyTestData(this);
            testListData.buildTestSQLiteDB();
            conversations = db.getConversationList();
        } */
    }

    private void setAllSeparators() {
        toTellSeparatorPosition = 0;
        toHearSeparatorPosition = 1;
        toWaitForSeparatorPosition = 2;
        addSeparator(toTellSeparatorPosition, "Stories to tell");
        addSeparator(toHearSeparatorPosition, "Stories to hear");
        addSeparator(toWaitForSeparatorPosition, "Stories to wait for");
    }

    private void addSeparator(int position, String separatorText){
        objectList.add(position, new String(separatorText));
    }

    private void startTellActivity(Conversation conversation){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        intent.putExtra("selectedConversation", conversation);
        startActivity(intent);
    }

    private void startListenActivity(Conversation conversation){
        Intent intent = new Intent(this, ListenToStoryActivity.class);
        intent.putExtra("selectedConversation", conversation);
        startActivity(intent);
    }

    public void logIn(View view){
        showFirebaseLoginPrompt();
    }

    public void addNewUser(View view){
        AlertDialog addNewUserDialog = loginDialog("Add User");
        addNewUserDialog.show();
    }
    private void startNewConversation(){
        getUserList();
    }

    private void getUserList(){
        Firebase baseRef = new Firebase(this.getResources().getString(R.string.firebase_url));

        baseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                ArrayList<User> userList = parseUserList(snapshot);
                selectConversationPartner(userList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private ArrayList<User> parseUserList(DataSnapshot snapshot){
        ArrayList<User> userList = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            User user = new User((String) child.child("name").getValue(),
                    (String) child.child("userName").getValue());
            userList.add(user);
        }
        return userList;
    }

    private void selectConversationPartner(final ArrayList<User> userList) {
        List<String> nameList = new ArrayList<>();
        for (User user : userList){
            nameList.add(user.getName());
        }
        final CharSequence[] items = nameList.toArray(new String[nameList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose User to Talk To")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        User selectedUser = userList.get(which);
                        continueWithNewConvo(selectedUser);
                    }
                });
        AlertDialog pickUserAlert = builder.create();
        pickUserAlert.show();
    }

    private void continueWithNewConvo(User selectedUser){
        Conversation newConversation = makeConversation(selectedUser);

        /*
        DBHelper db = new DBHelper(this);
        db.addConversation(newConversation);
        */


        FBHelper fbHelper = new FBHelper(this);
        fbHelper.addNewConversation(newConversation);

        startTellActivity(newConversation);
    }

    private Conversation makeConversation(User selectedUser){
        ArrayList<User> chosenUserList = new ArrayList<>();
        chosenUserList.add(selectedUser);
        ArrayList<Prompt> dummyPromptList = new ArrayList<>();
        dummyPromptList.add(masterPromptList.get(0));
        dummyPromptList.add(masterPromptList.get(1));
        dummyPromptList.add(masterPromptList.get(2));

        Conversation conversation = new Conversation("TestTitle", "TestTimeSince", "TestDuration",
                "TestFilepath", chosenUserList,
                0, new Prompt("TestPrompt", "TestTag"), dummyPromptList);

        return conversation;
    }

    public void getRandomPrompt(){
        Firebase baseRef = new Firebase(this.getResources().getString(R.string.firebase_url));
        Firebase promptRef = baseRef.child("prompts");

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                int counter = 1;
                while(counter < 8) {
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

    public AlertDialog loginDialog(String message) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.login, null);
        final AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
        failAlert.setTitle("Login/ Register Failed");
        failAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Login/ Register");
        alert.setMessage(message);
        alert.setView(textEntryView);
        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.userNameEditText);
                    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.passwordEditText);
                    Log.i("ADDUSER", usernameInput.getText().toString() + passwordInput.getText().toString());

                    FBHelper fbHelper = new FBHelper(getApplicationContext());
                    fbHelper.addNewUserToServer(usernameInput.getText().toString(), passwordInput.getText().toString());
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

}
