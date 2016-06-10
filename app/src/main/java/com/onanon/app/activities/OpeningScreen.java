package com.onanon.app.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;

public class OpeningScreen extends AppCompatActivity {

    private String currentUserUID, currentUserName;
    private String loginEmail, loginPass;
    private DatabaseReference baseRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private Button logInButton;
    private Button createUserButton;
    private Button allowRecordAudioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initializeViews();
        initializeFirebase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        setUpFirebaseAuthListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        requestAppPermissions();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void requestAppPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(OpeningScreen.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(OpeningScreen.this, "ONanON needs access to the microphone to record " +
                        "your stories!",
                        Toast.LENGTH_LONG).show();
            }

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.



                } else {
                    logInButton.setClickable(false);
                    logInButton.setAlpha(.5f);
                    createUserButton.setClickable(false);
                    createUserButton.setAlpha(.5f);
                    allowRecordAudioButton.setVisibility(View.VISIBLE);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void allowRecordAudioButtonPressed(View view) {
        logInButton.setClickable(true);
        logInButton.setAlpha(1f);
        createUserButton.setClickable(true);
        createUserButton.setAlpha(1f);
        allowRecordAudioButton.setVisibility(View.GONE);
        requestAppPermissions();
    }

    private void setUpFirebaseAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    currentUserUID = user.getUid();

                    getUserNameFromUID();
                    Log.d("AuthStateListen", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("AuthStateListen", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void initializeViews() {
        logInButton = (Button) findViewById(R.id.log_in_button);
        createUserButton = (Button) findViewById(R.id.create_user_button);
        allowRecordAudioButton = (Button) findViewById(R.id.allow_record_audio_button);
    }

    private void initializeFirebase(){
        baseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void getUserNameFromUID(){
        DatabaseReference promptRef = baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS).child(currentUserUID);

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                currentUserName = (String) snapshot.getValue();

                saveUserNameToPreferences();
                startConversationListActivity();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                progressDialog.dismiss();
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void startConversationListActivity(){
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }

        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }

    public void logInButtonPressed(View view){
        progressDialog = Utils.getSpinnerDialog(this);

        final EditText emailInput = (EditText) findViewById(R.id.emailEditText);
        final EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);

        loginEmail = emailInput.getText().toString().trim();
        loginPass = passwordInput.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(loginEmail, loginPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Signin", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("Signin", "signInWithEmail", task.getException());
                            progressDialog.dismiss();
                            Toast.makeText(OpeningScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void saveUserNameToPreferences(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.CURRENT_USER_NAME_KEY, currentUserName);
        // Commit the edits!
        editor.commit();
    }

    public void createUserButtonPressed(View view){
        Intent intent = new Intent(this, AddNewUserActivity.class);
        startActivity(intent);
    }
}
