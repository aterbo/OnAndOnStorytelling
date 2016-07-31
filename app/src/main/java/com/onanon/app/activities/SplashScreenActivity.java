package com.onanon.app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.classes.User;

import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {


    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int RC_SIGN_IN = 100;
    private String mUserName, mUserEmail, mUserID, mUserProfilePicUrl;
    private DatabaseReference baseRef;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        Toast.makeText(SplashScreenActivity.this, "Unknown Response.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    public void onStart() {
        super.onStart();

        runIntroSlidesIfNeeded();

        if (isPermissionsGranted()) {
            checkIfLoggedIn();
        } else {
            requestAppPermissions();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void runIntroSlidesIfNeeded() {
        PrefManager prefManager = new PrefManager(this);
        if (prefManager.isFirstTimeLaunch()) {
            prefManager.setFirstTimeLaunch(false);

            Intent intent = new Intent(SplashScreenActivity.this, IntroSliderActivity.class);
            intent.putExtra(Constants.INITIATING_ACTIVITY_INTENT_KEY, Constants.SPLASH_SCREEN);
            startActivity(intent);
            finish();
        }
    }

    private void checkIfLoggedIn(){
        if (isUserLoggedIn()) {
            ensureLogInProfileExistsOnServer();
        } else {
            showButtons();
        }
    }

    private boolean isUserLoggedIn(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPermissionsGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }  else {
            return false;
        }
    }

    private void requestAppPermissions(){
        if (isShowExplainationRequired()){
            showPermissionsExplainationDialog("You need to allow access to the microphone to record " +
                            "your awesome stories!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showPermissionsRequestDialog();
                        }
                    });
        } else {
            showPermissionsRequestDialog();
        }
    }

    private boolean isShowExplainationRequired(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            return true;
        }  else {
            return false;
        }
    }

    private void showPermissionsRequestDialog(){
        ActivityCompat.requestPermissions(SplashScreenActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkIfLoggedIn();
                } else {
                    requestAppPermissions();
                }
                return;
            }
        }
    }

    private void showButtons(){
        Button logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.VISIBLE);
        logInButton.setClickable(true);
    }

    public void logInButtonPressed(View view){
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder().build();
        startActivityForResult(intent, RC_SIGN_IN);
    }



    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ensureLogInProfileExistsOnServer();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(SplashScreenActivity.this, "Sign in cancelled.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(SplashScreenActivity.this, "Unknown sign in response.",
                Toast.LENGTH_SHORT).show();
    }

    private void ensureLogInProfileExistsOnServer() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        mUserID = currentUser.getUid();

        baseRef = FirebaseDatabase.getInstance().getReference();

        baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS).child(mUserID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(existsUserProfile(dataSnapshot)) {
                            mUserName = (String) dataSnapshot.getValue();
                            setUserNameToPrefManager();
                            moveToConversationListActivity();
                        } else {
                            mUserEmail = currentUser.getEmail();
                            getUserNameForNewProfile();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("SplashScreen", "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    private boolean existsUserProfile(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void setUserNameToPrefManager() {
        PrefManager prefManager = new PrefManager(this);
        prefManager.setUserNameToPreferences(mUserName);
    }

    private void getUserNameForNewProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to ONanON!");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.layout_new_user_info,
                (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mUserName = input.getText().toString();
                createUserInFirebaseHelper();
            }
        });

        builder.show();
    }

    private void createUserInFirebaseHelper() {

        mUserProfilePicUrl = "XXXXXX";
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
                setUserNameToPrefManager();
                moveToConversationListActivity();
            }
        });

    }

    private void moveToConversationListActivity(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }

    private void showPermissionsExplainationDialog(String message,
                                                   DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}
