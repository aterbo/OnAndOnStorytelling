package com.aterbo.tellme;

import android.content.Context;
import android.util.Log;

import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.ConversationSummary;
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

    private Context context;
    private Firebase baseRef;
    private Firebase userRef;
    private Firebase userConvosRef;
    private Firebase convoParticipantsRef;
    private String mUserName, mUserEmail, mUserID;

    public FBHelper(Context context){
        this.context = context;
        baseRef = new Firebase(context.getResources().getString(R.string.firebase_url));
        userRef = baseRef.child("users");
        userConvosRef = baseRef.child("userConvos");
        convoParticipantsRef = baseRef.child("convoParticipants");
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


    public void addNewUserToServer(final String userName, final String email, final String password){
        baseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added: " + result.get("uid"));
                mUserEmail = email.replace(".", ",");
                mUserName = userName;
                mUserID = (String) result.get("uid");

                createUserInFirebaseHelper();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                System.out.println("Error adding new user");
            }
        });
    }

    private void createUserInFirebaseHelper() {
        HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();

        /* Create a HashMap version of the user to add */
        User newUser = new User(mUserName, mUserEmail, mUserID);
        HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(newUser, Map.class);

        /* Add the user and UID to the update map */
        userAndUidMapping.put("/" + Constants.FIREBASE_LOCATION_USERS + "/" + mUserEmail,
                newUserMap);
        userAndUidMapping.put("/" + Constants.FIREBASE_LOCATION_UID_MAPPINGS + "/"
                + mUserID, mUserEmail);

        /* Try to update the database; if there is already a user, this will fail */
        baseRef.updateChildren(userAndUidMapping, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    /* Try just making a uid mapping */
                    baseRef.child(Constants.FIREBASE_LOCATION_UID_MAPPINGS)
                            .child(mUserID).setValue(mUserEmail);
                    Log.i("FIREBASELOGIN", "Error adding user to Firebase");
                }
                Log.i("FIREBASELOGIN", "User added to Firebase");
            }
        });
    }

    public String getCurrentUserID(){
        AuthData authData = baseRef.getAuth();
        if (authData != null) {
            return authData.getUid();
        } else {
            return "";
        }
    }

    public void addNewConversation(String currentUserEmail, User selectedUser,
                                   ArrayList<Prompt> selectedPromptsList){
        Firebase newRef = convoParticipantsRef.push();
        String convoId = newRef.getKey();
        String selectedUserEmail = selectedUser.getEmail();

        HashMap<String, Object> convoEmails = new HashMap<String, Object>();

        Prompt noCurrentPrompt = new Prompt("", "");

        ArrayList<String> usersInConversationEmails = new ArrayList<>();
        usersInConversationEmails.add(currentUserEmail.replace(".",","));
        usersInConversationEmails.add(selectedUserEmail.replace(".",","));

        ConversationSummary itemToAddObject = new ConversationSummary(usersInConversationEmails,
                selectedUserEmail, 2, noCurrentPrompt, selectedPromptsList);
        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(itemToAddObject, Map.class);

        convoEmails.put("/" + Constants.FIREBASE_LOCATION_CONVO_PARTICIPANTS + "/" + convoId + "/"
                + currentUserEmail.replace(".",","), "creator");
        convoEmails.put("/" + Constants.FIREBASE_LOCATION_CONVO_PARTICIPANTS + "/" + convoId + "/"
                + selectedUserEmail.replace(".",","), "recipient");

        for (String userEmail : usersInConversationEmails) {
            convoEmails.put("/" + Constants.FIREBASE_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + convoId, itemToAddHashMap);
        }

        baseRef.updateChildren(convoEmails, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASECREATENEWCONVO", "Error adding convo to Firebase");
                }
                Log.i("FIREBASECREATENEWCONVO", "Convo added to Firebase successfully");
            }
        });
    }

    public void updateConversationAfterRecording(ConversationSummary conversation, String convoPushId){

        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userEmail : conversation.getUsersInConversationEmails()) {
            convoInfoToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + convoPushId, conversationToAddHashMap);
        }

        baseRef.updateChildren(convoInfoToUpdate, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");
            }
        });
    }
}