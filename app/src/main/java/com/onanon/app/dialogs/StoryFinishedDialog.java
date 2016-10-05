package com.onanon.app.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.Utils.Utils;
import com.onanon.app.activities.ConversationListActivity;
import com.onanon.app.activities.ListenToStoryActivity;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ATerbo on 10/4/16.
 */
public class StoryFinishedDialog extends DialogFragment {

    private Conversation conversation;
    private String conversationPushId, localTempFilePath, currentUserName;
    private DatabaseReference baseRef;
    private HashMap<String, Object> convoInfoToUpdate;

    private ProgressDialog progressDialog;

    private AlertDialog alertDialog;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static StoryFinishedDialog newInstance(
            Conversation conversation, String conversationPushId, String localTempFilePath) {
        StoryFinishedDialog frag = new StoryFinishedDialog();
        Bundle args = new Bundle();
        args.putParcelable("conversation", conversation);
        args.putString("convoPushId", conversationPushId);
        args.putString("localTempFilePath", localTempFilePath);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        conversation = getArguments().getParcelable("conversation");
        conversationPushId = getArguments().getString("convoPushId");
        localTempFilePath = getArguments().getString("localTempFilePath");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_after_story, null))
                .setPositiveButton(R.string.send_text_response, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // This will be overridden for data validations
                    }
                })
                .setNegativeButton(R.string.continue_without_response, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog = Utils.getSpinnerDialog(getContext());
                        finishListeningToStory();
                    }
                })
                .setNeutralButton(R.string.listen_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        baseRef = FirebaseDatabase.getInstance().getReference();
        PrefManager prefManager = new PrefManager(getContext());
        currentUserName = prefManager.getUserNameFromSharedPreferences();

        alertDialog = (AlertDialog)getDialog();
        if(alertDialog != null)
        {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            final EditText responseEditText = (EditText) alertDialog.findViewById(R.id.entered_response);

            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;
                    String enteredData = responseEditText.getText().toString();

                    if (enteredData.length()>1) {
                        wantToCloseDialog = true;
                    }

                    if (wantToCloseDialog) {
                        progressDialog = Utils.getSpinnerDialog(getContext());
                        uploadResponseToFB(enteredData);
                    } else {
                        Toast.makeText(getContext(), "You didn't enter a response!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void uploadResponseToFB(String response) {
        //TODO: Upload response to FB.
        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    private void finishListeningToStory() {
        deleteLocalStoryAudioFile();
        convoInfoToUpdate = new HashMap<String, Object>();

        conversation.markUserAsHasHeardStory(currentUserName);
        if (conversation.haveAllUsersHeardStory()){
            deleteFBStorageStoryAudioFile();
        } else {
            updateConversationAfterListening();
        }
    }

    private void deleteLocalStoryAudioFile(){
        File file = new File(localTempFilePath);
        boolean isDeleteSuccessful = file.delete();

        if (isDeleteSuccessful) {
            Log.i("Recording deleted", "Temporary recoding file deleted.");
        } else {
            Log.i("Recording NOT Deleted", "Error deleting temporary recording file.");
        }
    }

    private void deleteFBStorageStoryAudioFile(){
        String FBStorageFilePath = conversation.getFbStorageFilePathToRecording();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        StorageReference audioFileStorageRef = sRef.child(FBStorageFilePath);

        audioFileStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Recording deleted", "FB Storage recoding file deleted.");


                deleteConversationReferencesToFileinHashMap();
                updateConversationAfterListening();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Recording deleted", "ERROR - FB Storage recoding file NOT deleted.");

                deleteConversationReferencesToFileinHashMap();
                updateConversationAfterListening();
            }
        });
    }

    private void deleteConversationReferencesToFileinHashMap(){
        conversation.setFbStorageFilePathToRecording("none");
        conversation.setCurrentPrompt(new Prompt("null", "null"));
        conversation.setStoryRecordingDuration(0);


        HashMap<String, Object> nullPromptToAddToHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(
                        new Prompt("null", "null"), Map.class);

        for (String userName : conversation.getUserNamesInConversation()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/fbStorageFilePathToRecording",
                    "none");


            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/currentPrompt",
                    nullPromptToAddToHashMap);


            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/storyRecordingDuration",
                    0);
        }
    }

    private void updateConversationAfterListening(){

        for (String userName : conversation.getUserNamesInConversation()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/userNamesHaveHeardStory",
                    conversation.getUserNamesHaveHeardStory());

            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                            + userName + "/" + conversationPushId + "/userNamesHaveNotHeardStory",
                    conversation.getUserNamesHaveNotHeardStory());

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
                increaseHeardCounter();
            }
        });
    }

    private void increaseHeardCounter() {

        DatabaseReference counterRef = baseRef.child(Constants.FB_LOCATION_STATISTICS)
                .child(Constants.FB_COUNTER_RECORDINGS_HEARD);
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
                goBackToMainScreen();
            }
        });
    }

    private void goBackToMainScreen(){
        Intent intent = new Intent(context, ConversationListActivity.class);
        getActivity().startActivity(intent);
    }
}
