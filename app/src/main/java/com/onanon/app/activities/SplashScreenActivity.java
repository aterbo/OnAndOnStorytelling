package com.onanon.app.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashScreenActivity extends AppCompatActivity {


    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int RC_SIGN_IN = 100;

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
            moveToConversationListActivity();
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
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher)
                .setProviders(AuthUI.EMAIL_PROVIDER,
                        AuthUI.GOOGLE_PROVIDER,
                        AuthUI.FACEBOOK_PROVIDER)
                .build();
        startActivityForResult(intent, RC_SIGN_IN);
    }



    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            moveToConversationListActivity();
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
