package com.aterbo.tellme;

import android.content.Context;
import android.util.Log;

import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.ConversationSummary;
import com.aterbo.tellme.classes.ConvoLite;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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


        ArrayList<String> usersInConversationEmails = new ArrayList<>();
        usersInConversationEmails.add(currentUserEmail.replace(".",","));
        usersInConversationEmails.add(selectedUserEmail.replace(".",","));

        ConversationSummary itemToAddObject = new ConversationSummary(usersInConversationEmails,
                selectedUserEmail, 2, -1, selectedPromptsList);
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

    public void setUserGroupListener(){
        Firebase userGroup = userRef.child(getCurrentUserID()).child("groups");
        userGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Conversation conversation = parseFBPathIntoConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                userRemovedFromConversation(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private Conversation parseFBPathIntoConversation(DataSnapshot dataSnapshot){
        Conversation conversation = new Conversation();
        conversation.setTitle((String) dataSnapshot.child("title").getValue());
        conversation.setTimeSinceLastAction((String) dataSnapshot.child("timeSinceLastAction").getValue());
        conversation.setStoryDuration((String) dataSnapshot.child("storyDuration").getValue());
        conversation.setStoryFilePath((String) dataSnapshot.child("storyFilePath").getValue());
        conversation.setStatus((int) dataSnapshot.child("statusFlag").getValue());
        conversation.setSqlIdNumber((int) dataSnapshot.child("sqlIdNumber").getValue());
        conversation.setUsersInConversation(parseUserList(dataSnapshot.child("usersInConversation")));
        conversation.setProposedPrompts(parsePromptList(dataSnapshot.child("proposedPrompts")));
        conversation.setCurrentPrompt(parsePrompt(dataSnapshot.child("currentPrompt")));

        return conversation;
    }

    private void userRemovedFromConversation(DataSnapshot dataSnapshot){

    }

    private ArrayList<User> parseUserList(DataSnapshot snapshot){
        ArrayList<User> userList = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            User user = new User((String) child.child("userName").getValue(),
                    (String) child.child("email").getValue(),
                    (String) child.child("userId").getValue());
            userList.add(user);
        }
        return userList;
    }

    private ArrayList<Prompt> parsePromptList(DataSnapshot snapshot){
        ArrayList<Prompt> promptList = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Prompt prompt = new Prompt((String) child.child("text").getValue(),
                    (String) child.child("tag").getValue());
            promptList.add(prompt);
        }
        return promptList;
    }

    private Prompt parsePrompt(DataSnapshot snapshot){
        Prompt currentPrompt = new Prompt((String) snapshot.child("text").getValue(),
                    (String) snapshot.child("tag").getValue());
        return currentPrompt;
    }


}