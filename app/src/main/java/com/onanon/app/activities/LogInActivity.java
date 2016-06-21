package com.onanon.app.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

public class LogInActivity extends AppCompatActivity {

    private String currentUserUID, currentUserName;
    private String loginEmail, loginPass;
    private DatabaseReference baseRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
        if(isLogInInfoEntered()) {
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
                                Toast.makeText(LogInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    private boolean isLogInInfoEntered(){
        EditText emailInput = (EditText) findViewById(R.id.emailEditText);
        EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);

        if( emailInput.getText().toString() == null || emailInput.getText().toString().isEmpty()){
            return false;
        } else if (passwordInput.getText().toString() == null || passwordInput.getText().toString().isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    private void saveUserNameToPreferences(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.CURRENT_USER_NAME_KEY, currentUserName);
        // Commit the edits!
        editor.commit();
    }

    public void createUserButtonPressed(View view){
        final EditText emailInput = (EditText) findViewById(R.id.emailEditText);
        final EditText passwordInput = (EditText) findViewById(R.id.passwordEditText);

        loginEmail = emailInput.getText().toString().trim();
        loginPass = passwordInput.getText().toString().trim();

        Intent intent = new Intent(this, AddNewUserActivity.class);
        intent.putExtra("userEmail", loginEmail);
        intent.putExtra("userPass", loginPass);
        startActivity(intent);
    }
}
