package com.aterbo.tellme;

import android.content.Context;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

/**
 * Created by ATerbo on 3/14/16.
 */
public class FBHelper {

    Context context;
    public FBHelper(Context context){
        this.context = context;
    }

    public void logUserIntoServerViaEmail(String email, String password){
        Firebase ref = new Firebase(context.getResources().getString(R.string.firebase_url));
        ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                System.out.println("Error logging in");
            }
        });
    }


    public void addNewUserToServer(String email, String password){
        Firebase ref = new Firebase(context.getResources().getString(R.string.firebase_url));
        ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added: " + result.get("uid"));
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Error adding new user");
            }
        });
    }



}
