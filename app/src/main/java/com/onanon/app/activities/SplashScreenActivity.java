package com.onanon.app.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.onanon.app.R;

public class SplashScreenActivity extends AppCompatActivity {


    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isPermissionsGranted()) {
            checkIfLoggedIn();
        } else {
            requestAppPermissions();
        }
    }

    private void checkIfLoggedIn(){
        if (isUserLoggedIn()) {
            moveToConversationListActivity();
        } else {
            moveToUserLogInActivity();
        }
    }

    private boolean isUserLoggedIn(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.i("isLoggedIn", "User is logged in.");
            return true;
        } else {
            Log.i("isLoggedIn", "User is not logged in.");
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
                    moveToUserLogInActivity();
                } else {
                    requestAppPermissions();
                }
                return;
            }
        }
    }

    private void moveToUserLogInActivity(){
        Log.i("isLoggedIn", "Move to Log In.");
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }


    private void moveToConversationListActivity(){
        Log.i("isLoggedIn", "Move to Convo List.");
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
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
