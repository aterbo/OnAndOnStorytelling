package com.aterbo.tellme;

import android.content.Context;

import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;
import java.util.Random;

/**
 * Created by ATerbo on 3/14/16.
 */
public class FBHelper {

    private Context context;
    private Firebase baseRef;

    public FBHelper(Context context){
        this.context = context;
        baseRef = new Firebase(context.getResources().getString(R.string.firebase_url));
    }

    public void logUserIntoServerViaEmail(String email, String password){
        baseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
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
        baseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added: " + result.get("uid"));
                baseRef.child("users").child((String) result.get("uid")).setValue(new User("Test", "TEST!" ));
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Error adding new user");
            }
        });
    }

    public String getCurrentUserName(){
        AuthData authData = baseRef.getAuth();
        if (authData != null) {
            return authData.getUid();
        } else {
            return "";
        }
    }

    public void addNewConversation(Conversation newConversation){
        String firebasePath = (getCurrentUserName() + "--" + (newConversation.getUser(0).getName()).replace(".",""));

        Firebase uploadRef =  baseRef.child("groups").child(firebasePath);
        uploadRef.child("").setValue(newConversation);
    }

    public void getRandomPrompt(){
        Random random = new Random();
        int randomNumber = random.nextInt(8 - 1) + 1;
        Firebase prompts = baseRef.child("prompts").child(Integer.toString(randomNumber));

        prompts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                Prompt prompt = new Prompt((String) snapshot.child("text").getValue(),
                        (String) snapshot.child("tag").getValue());
                //sendPromptSomewhereSomehow(prompt);
                send(prompt);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private Prompt send(Prompt prompt){

        return prompt;
    }

}