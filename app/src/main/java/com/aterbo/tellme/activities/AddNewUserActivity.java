package com.aterbo.tellme.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.aterbo.tellme.FBHelper;
import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class AddNewUserActivity extends AppCompatActivity {

    private Firebase baseRef;
    private String mUserName, mUserEmail, mUserID, mUserProfilePicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void addUserConfirm(View view){
        final EditText usernameInput = (EditText) findViewById(R.id.userNameEditText);
        final EditText emailInput = (EditText) findViewById(R.id.emailEditText);
        final EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);
        Log.i("ADDUSER", usernameInput.getText().toString() + passwordInput.getText().toString());

        baseRef = new Firebase(Constants.FB_LOCATION);
        addNewUserToServer(usernameInput.getText().toString(), emailInput.getText().toString(),
                passwordInput.getText().toString());
    }



    public void addNewUserToServer(final String userName, final String email, final String password){
        baseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added to firebase login: " + result.get("uid"));

                logUserIn(userName, email, (String) result.get("uid"), password);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Error adding new user");
            }
        });
    }

    private void logUserIn(final String userName, final String email, final String userId, final String password){
        baseRef = new Firebase(Constants.FB_LOCATION);
        baseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());

                mUserEmail = email.replace(".", ",");
                mUserName = userName;
                mUserID = userId;
                mUserProfilePicUrl = (String) authData.getProviderData().get("profileImageURL");

                createUserInFirebaseHelper();
                Log.i("FIREBASELOGIN", "User logged in to Firebase");
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
            }
        });
    }

    private void createUserInFirebaseHelper() {
        HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();

        /* Create a HashMap version of the user to add */
        User newUser = new User(mUserName, mUserEmail, mUserID, mUserProfilePicUrl);
        HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(newUser, Map.class);

        /* Add the user and UID to the update map */
        userAndUidMapping.put("/" + Constants.FB_LOCATION_USERS + "/" + mUserName,
                newUserMap);
        userAndUidMapping.put("/" + Constants.FB_LOCATION_UID_MAPPINGS + "/"
                + mUserID, mUserName);

        /* Try to update the database; if there is already a user, this will fail */
        baseRef.updateChildren(userAndUidMapping, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    /* Try just making a uid mapping */
                    baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS)
                            .child(mUserID).setValue(mUserName);
                    Log.i("FIREBASELOGIN", "Error adding user to Firebase");
                }
                Log.i("FIREBASELOGIN", "User added to Firebase");
            }
        });

        goBackToStartList();
    }

    private void goBackToStartList(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}
