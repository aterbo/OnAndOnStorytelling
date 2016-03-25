package com.aterbo.tellme;

import android.content.Context;

import com.aterbo.tellme.SQLite.DBHelper;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Created by ATerbo on 3/14/16.
 */
public class FBHelper {

    private Context context;
    private Firebase baseRef;
    private Firebase userRef;
    private Firebase groupRef;

    public FBHelper(Context context){
        this.context = context;
        baseRef = new Firebase(context.getResources().getString(R.string.firebase_url));
        userRef = baseRef.child("users");
        groupRef = baseRef.child("groups");
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


    public void addNewUserToServer(final String email, String password){
        baseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("New user added: " + result.get("uid"));
                User newUser = new User(email.replace(".", ""), (String) result.get("uid"));

                userRef.child((String) result.get("uid")).setValue(newUser);
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
        Firebase newGroup = groupRef.push();
        newGroup.setValue(newConversation);
        //build out users list under user path and under new conversation path
        for (User user : newConversation.getUsersInConversation()){
            userRef.child(user.getUserName()).child("groups").child(newGroup.getKey()).setValue(true);
            newGroup.child("participants").child(user.getUserName()).setValue(true);
        }
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

    public void setUserGroupListener(){
        Firebase userGroup = userRef.child(getCurrentUserName()).child("groups");
        userGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Conversation conversation = parseFBPathIntoConversation(dataSnapshot);
                setNewConversationToDB(conversation);
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
            User user = new User((String) child.child("name").getValue(),
                    (String) child.child("userName").getValue());
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

    private void setNewConversationToDB(Conversation conversation){
        DBHelper db = new DBHelper(context);
        db.addConversation(conversation);

    }
}