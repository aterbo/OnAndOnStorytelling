package com.aterbo.tellme;

import android.content.Context;
import android.util.Log;

import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ATerbo on 3/14/16.
 */
public class FBHelper {

    private Firebase baseRef;
    private String mUserName, mUserEmail, mUserID, mUserPass, mUserProfilePicUrl;

    public FBHelper(){
        baseRef = new Firebase(Constants.FB_LOCATION);
    }

    public void addNewUserToServer(final String userName, final String email, final String password){
        baseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added to firebase login: " + result.get("uid"));
                mUserEmail = email.replace(".", ",");
                mUserName = userName;
                mUserPass = password;
                mUserID = (String) result.get("uid");

                logUserIn();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Error adding new user");
            }
        });
    }

    private void logUserIn(){
        baseRef.authWithPassword(mUserEmail, mUserPass, new Firebase.AuthResultHandler(){
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());

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
        userAndUidMapping.put("/" + Constants.FB_LOCATION_USERS + "/" + mUserEmail,
                newUserMap);
        userAndUidMapping.put("/" + Constants.FB_LOCATION_UID_MAPPINGS + "/"
                + mUserID, mUserEmail);

        /* Try to update the database; if there is already a user, this will fail */
        baseRef.updateChildren(userAndUidMapping, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    /* Try just making a uid mapping */
                    baseRef.child(Constants.FB_LOCATION_UID_MAPPINGS)
                            .child(mUserID).setValue(mUserEmail);
                    Log.i("FIREBASELOGIN", "Error adding user to Firebase");
                }
                Log.i("FIREBASELOGIN", "User added to Firebase");
            }
        });
    }
}