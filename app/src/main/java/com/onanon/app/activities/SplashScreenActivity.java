package com.onanon.app.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.classes.User;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {


    private String currentUserName, currentUserUID, mUserEmail, mUserProfilePicUrl;
    private DatabaseReference baseRef;
    private PrefManager prefManager;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int RC_SIGN_IN = 100;
    private boolean flag;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        prefManager = new PrefManager(this);
        runIntroSlidesIfNeeded();
        mAuth = FirebaseAuth.getInstance();
        baseRef = FirebaseDatabase.getInstance().getReference();

        Button logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.GONE);
        logInButton.setClickable(false);
    }

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
    public void onResume() {
        super.onResume();
        flag = true;

        if (isPermissionsGranted()) {
            buildAuthStateListener();
        } else {
            requestAppPermissions();
        }
    }

    private void runIntroSlidesIfNeeded() {
        if (prefManager.isFirstTimeLaunch()) {
            prefManager.setFirstTimeLaunch(false);

            Intent intent = new Intent(SplashScreenActivity.this, IntroSliderActivity.class);
            intent.putExtra(Constants.INITIATING_ACTIVITY_INTENT_KEY, Constants.SPLASH_SCREEN);
            startActivity(intent);
            finish();
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
        if (isShowExplanationRequired()){
            showPermissionsExplanationDialog("You need to allow access to the microphone to record " +
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

    private boolean isShowExplanationRequired(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            return true;
        }  else {
            return false;
        }
    }

    private void showPermissionsExplanationDialog(String message,
                                                  DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
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
                    buildAuthStateListener();
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
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher)
                .setProviders(AuthUI.EMAIL_PROVIDER,
                        AuthUI.GOOGLE_PROVIDER)
                .build();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.i("handleSignInResponse", "Signed in");
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

    private void buildAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && flag) {
                    Log.i("onAuthStateChange2", "onAuthStateChanged:signed_in:" + user.getUid());

                    flag = false;

                    currentUserUID = user.getUid();
                    mUserEmail = user.getEmail();

                    Uri photoUrl = user.getPhotoUrl();
                    if (photoUrl != null) {
                        mUserProfilePicUrl = photoUrl.toString();
                    } else {
                        mUserProfilePicUrl = Constants.NO_PHOTO_KEY;
                    }
                    checkIfUserAccountExistsInFB();
                } else {
                    flag = false;
                    showButtons();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }
    
    private void checkIfUserAccountExistsInFB() {

        Button logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.GONE);
        logInButton.setClickable(false);

        DatabaseReference uIdRef = baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS).child(currentUserUID);

        uIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (existsDataSnapshop(snapshot)) {
                    Log.i("checkIfUserExists", "Data Snapshot Exists");
                    currentUserName = (String) snapshot.getValue();
                    prefManager.setUserNameToPreferences(currentUserName);
                    moveToConversationListActivity();
                } else {
                    setUpNewFBUserEntry("User Name");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private boolean existsDataSnapshop(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null && dataSnapshot.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void setUpNewFBUserEntry(String hintMessage) {
        Log.i("setUpNewFBUser", "Started Activity");


        Button logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.GONE);
        logInButton.setClickable(false);

        findViewById(R.id.add_user_name_text).setVisibility(View.VISIBLE);

        final EditText userNameInput = (EditText) findViewById(R.id.user_name_input);
        final TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout);
        final Button setUserNameButton = (Button) findViewById(R.id.create_user_name_button);

        setUserNameButton.setClickable(true);
        setUserNameButton.setEnabled(true);
        setUserNameButton.setVisibility(View.VISIBLE);
        textInputLayout.setVisibility(View.VISIBLE);
        textInputLayout.setHint(hintMessage);
        textInputLayout.setHintAnimationEnabled(true);

        userNameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    currentUserName = userNameInput.getText().toString();
                    if (currentUserName.length() > 2) {
                        setUserNameButton.setEnabled(false);
                        checkIfUserNameIsUnique();
                    } else {
                        textInputLayout.setHint("That user name is too short.");
                    }
                }
                return false;
            }
        });

        setUserNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUserName = userNameInput.getText().toString().trim();
                if (currentUserName.length() > 2) {
                    setUserNameButton.setEnabled(false);
                    checkIfUserNameIsUnique();
                } else {
                    textInputLayout.setHint("That user name is too short.");
                }
            }
        });
    }

    private void checkIfUserNameIsUnique() {
        DatabaseReference userRef = baseRef.child(Constants.FB_LOCATION_USER_NAME_KEY_LIST)
                .child(userNameAsKey());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (existsDataSnapshop(snapshot)) {
                    setUpNewFBUserEntry("That user name is taken.");
                } else {
                    createUserInFirebaseHelper();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("Error getting user data from Firebase after login. " +
                        "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private String userNameAsKey() {
        return currentUserName.toLowerCase().replace(" ", "");
    }

    private void createUserInFirebaseHelper() {

        /* Create a HashMap version of the user to add */
        User newUser = new User(currentUserName, mUserEmail, currentUserUID, mUserProfilePicUrl);
        HashMap<String, Object> newUserMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(newUser, Map.class);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Constants.FB_LOCATION_USERS + "/" + currentUserName,
                newUserMap);
        childUpdates.put("/" + Constants.FB_LOCATION_USER_NAME_KEY_LIST + "/" +
                userNameAsKey(), true);
        childUpdates.put("/" + Constants.FB_LOCATION_UID_MAPPINGS + "/"
                + currentUserUID, currentUserName);

        baseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                prefManager.setUserNameToPreferences(currentUserName);
                moveToConversationListActivity();
            }
        });

    }


    private void moveToConversationListActivity(){
        removeAuthStateListeners();
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }

    private void removeAuthStateListeners() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeAuthStateListeners();
    }
}
