package com.onanon.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ATerbo on 10/4/16.
 */
public class WaitingForPromptsDialog extends DialogFragment {

    private Conversation conversation;
    private String conversationPushId;
    private DatabaseReference baseRef, mNumberOfPromptsRef;

    private ArrayList<Prompt> promptOptionsList;
    private int numberOfPromptsOnServer;
    private int numberOfPromptsToGet;
    private ProgressDialog progressDialog;

    private ValueEventListener mNumberOfPromptsRefListener;

    public static WaitingForPromptsDialog newInstance(Conversation conversation, String conversationPushId) {
        WaitingForPromptsDialog frag = new WaitingForPromptsDialog();
        Bundle args = new Bundle();
        args.putParcelable("conversation", conversation);
        args.putString("convoPushId", conversationPushId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        conversation = getArguments().getParcelable("conversation");
        conversationPushId = getArguments().getString("convoPushId");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Waiting for prompts");
        builder.setMessage(conversation.getLastUserNameToTell() + " needs to send you some prompts.\n\n" +
                "Do you want to wait or do you want three random prompts from the database?");

        builder.setPositiveButton("Get Random Prompts", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog = Utils.getSpinnerDialog(getContext());
                baseRef = FirebaseDatabase.getInstance().getReference();
                promptOptionsList = new ArrayList<>();
                getPromptOptionsList();
            }
        });

        builder.setNegativeButton("Wait", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }


    private void getPromptOptionsList(){
        setNumberOfPromptsFBListener();
    }

    private void setNumberOfPromptsFBListener(){
        mNumberOfPromptsRef = baseRef.child(Constants.FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS);

        mNumberOfPromptsRefListener = mNumberOfPromptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                Long tempNumber = (Long) snapshot.getValue();
                numberOfPromptsOnServer = tempNumber.intValue();
                getRandomPromptList();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getRandomPromptList(){
        numberOfPromptsToGet = 3;

        ArrayList<Integer> randNumList = getRandomArrayListOfIntegers(numberOfPromptsToGet);

        for (int promptId : randNumList) {
            getRandomPrompt(promptId);
        }
    }

    private ArrayList<Integer> getRandomArrayListOfIntegers(int numberToGet){

        ArrayList<Integer> list = new ArrayList<>();
        Random random = new Random();

        while (list.size() < numberToGet) {
            Integer nextRandom = random.nextInt(numberOfPromptsOnServer);
            if(!list.contains(nextRandom)) {
                list.add(nextRandom);
            }
        }
        return list;
    }

    private void getRandomPrompt(int promptIdNumber){
        DatabaseReference promptRef = baseRef.child(Constants.FB_LOCATION_PROMPTS)
                .child(Integer.toString(promptIdNumber));

        promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                promptOptionsList.add(snapshot.getValue(Prompt.class));

                if (promptOptionsList.size() == numberOfPromptsToGet) {
                    setPromptsToFirebase();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    private void setPromptsToFirebase(){
        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> prompt1HashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(promptOptionsList.get(0), Map.class);
        HashMap<String, Object> prompt2HashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(promptOptionsList.get(1), Map.class);
        HashMap<String, Object> prompt3HashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(promptOptionsList.get(2), Map.class);

        //Update the prompts in the conversation only
        for (String userName : conversation.getUserNamesInConversation()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/proposedPrompt1",
                    prompt1HashMap);
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/proposedPrompt2",
                    prompt2HashMap);
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/proposedPrompt3",
                    prompt3HashMap);

            //Update time last action occurred
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/dateLastActionOccurred",
                    Utils.getSystemTimeAsLong());
        }

        baseRef.updateChildren(convoInfoToUpdate, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");

                increaseRandomTopicsRequestedCounter();
            }
        });
    }

    private void increaseRandomTopicsRequestedCounter() {

        DatabaseReference counterRef = baseRef.child(Constants.FB_LOCATION_STATISTICS)
                .child(Constants.FB_COUNTER_RANDOM_TOPICS_REQUESTED);
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
                progressDialog.dismiss();
            }
        });
    }

}
