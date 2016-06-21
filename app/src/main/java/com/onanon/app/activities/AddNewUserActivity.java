package com.onanon.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.User;

import java.util.HashMap;
import java.util.Map;

public class AddNewUserActivity extends AppCompatActivity {

    private DatabaseReference baseRef;
    private String mUserName, mUserEmail, mUserID, mUserProfilePicUrl, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;

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

        getEnteredData();
        mAuth = FirebaseAuth.getInstance();
        setUpFirebaseAuthListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void getEnteredData() {
        Intent intent = getIntent();
        String loginEmail = intent.getStringExtra("userEmail");
        if (!loginEmail.isEmpty()){
            final EditText emailInput = (EditText) findViewById(R.id.emailEditText);
            emailInput.setText(loginEmail);
        }
        String loginPass = intent.getStringExtra("userPass");
        if (!loginPass.isEmpty()){
            final EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);
            passwordInput.setText(loginPass);
        }
    }

    private void setUpFirebaseAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mUserID = user.getUid();
                    mUserProfilePicUrl = "xxxxxx";

                    createUserInFirebaseHelper();
                    Log.d("AuthStateListen", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("AuthStateListen", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public void addUserConfirm(View view){
        final EditText usernameInput = (EditText) findViewById(R.id.userNameEditText);
        final EditText emailInput = (EditText) findViewById(R.id.emailEditText);
        final EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);
        Log.i("ADDUSER", usernameInput.getText().toString() + passwordInput.getText().toString());


        mUserName = usernameInput.getText().toString().trim();
        mUserEmail = emailInput.getText().toString().trim();
        mPassword = passwordInput.getText().toString().trim();

        if (isPasswordAtLeastSixCharLong()) {
            progressDialog = Utils.getSpinnerDialog(this);
            baseRef = FirebaseDatabase.getInstance().getReference();
            addNewUserToServer();
        } else {
            Toast.makeText(AddNewUserActivity.this, "Password must be at least 6 characters.",
                    Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
        }
    }



    private void addNewUserToServer() {
        mAuth.createUserWithEmailAndPassword(mUserEmail, mPassword).addOnCompleteListener(this,
            new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("AddUser", "createUserWithEmail:onComplete:" + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Toast.makeText(AddNewUserActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
            }
        });
    }

    private boolean isPasswordAtLeastSixCharLong(){
        if (mPassword.length()>=6){
            return true;
        } else {
            return false;
        }
    }

    private void createUserInFirebaseHelper() {
        HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();

        /* Create a HashMap version of the user to add */
        User newUser = new User(mUserName, mUserEmail, mUserID, mUserProfilePicUrl);
        HashMap<String, Object> newUserMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(newUser, Map.class);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Constants.FB_LOCATION_USERS + "/" + mUserName,
                newUserMap);
        childUpdates.put("/" + Constants.FB_LOCATION_UID_MAPPINGS + "/"
                + mUserID, mUserName);

        baseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                saveUserNameToPreferences();
                goBackToStartList();
            }
        });

    }

    private void saveUserNameToPreferences(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.CURRENT_USER_NAME_KEY, mUserName);
        // Commit the edits!
        editor.commit();
    }

    private void goBackToStartList(){
        progressDialog.dismiss();
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}
