package com.onanon.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;
import com.onanon.app.classes.Response;
import com.onanon.app.classes.VisualizerView;
import com.onanon.app.dialogs.StoryFinishedDialog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//http://examples.javacodegeeks.com/android/android-mediaplayer-example/
//Visualizer: http://android-er.blogspot.com/2015/02/create-audio-visualizer-for-mediaplayer.html

public class ListenToStoryActivity extends AppCompatActivity
        implements StoryFinishedDialog.StoryFinishedListener {

    private String currentUserName;
    private MediaPlayer mPlayer;
    private TextView duration;
    private Conversation conversation;
    private double timeElapsed = 0;
    private double finalTime = 0;
    private final Handler durationHandler = new Handler();
    private SeekBar seekbar;
    private ToggleButton playPauseButton;
    private Uri speechUri;
    private String conversationPushId;
    private String localTempFilePath;
    private ProgressDialog progressDialog;
    private StorageReference audioFileStorageRef;
    private DatabaseReference baseRef;
    private HashMap<String, Object> convoInfoToUpdate;
    private Button long_rewind_button;
    private Button rewind_button;
    private Button ff_button;
    private Button long_ff_button;

    private VisualizerView mVisualizerView;
    private Visualizer mVisualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_story);

        getUserName();
        getConversation();
        setFirebaseDetails();
        setProfilePicture();
        getRecordingFromFirebaseStorage();
        initializeViews();
        showConversationDetails();
        setToggleButton();
    }

    private void getUserName(){
        PrefManager prefManager = new PrefManager(this);
        currentUserName = prefManager.getUserNameFromSharedPreferences();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        conversationPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void setFirebaseDetails(){
        baseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setProfilePicture(){
        DatabaseReference userIconRef = baseRef.child(Constants.FB_LOCATION_USERS)
                .child(conversation.getLastUserNameToTell()).child("profilePhotoUrl");

        userIconRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String convoIconUrl = (String) snapshot.getValue();
                ImageView profilePic = (ImageView) findViewById(R.id.conversation_icon);

                if (convoIconUrl != null && !convoIconUrl.isEmpty() && !convoIconUrl.contains(Constants.NO_PHOTO_KEY)) {
                    //Profile has picture

                    if (Character.toString(convoIconUrl.charAt(0)).equals("\"")) {
                        convoIconUrl = convoIconUrl.substring(1, convoIconUrl.length()-1);
                    }
                } else {
                    //profile does not have picture
                    convoIconUrl = "";
                }
                Glide.with(ListenToStoryActivity.this).load(convoIconUrl).placeholder(R.drawable.word_treatment_512_84)
                        .fallback(R.drawable.word_treatment_512_84).dontAnimate().into(profilePic);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void getRecordingFromFirebaseStorage(){
        File tempFile = getFileForAudioDownloadDestination();

        String FBStorageFilePath = conversation.getFbStorageFilePathToRecording();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        audioFileStorageRef = sRef.child(FBStorageFilePath);

        audioFileStorageRef.getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                speechUri = Uri.parse(localTempFilePath);
                setUpMediaPlayer();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Toast.makeText(getApplicationContext(), "Error downloading audio file!", Toast.LENGTH_SHORT);
            }
        });
    }

    @NonNull
    private File getFileForAudioDownloadDestination() {
        File tempFile;

        if (Utils.isExternalStorageWritable()) {
            String fileName = UUID.randomUUID().toString().replaceAll("-", "");
            File tempFileDir = this.getFilesDir();
            tempFile = new File(tempFileDir, fileName + ".mp4");
        } else {
            try{
            tempFile = File.createTempFile("recordings", "mp4");
            } catch (IOException exception){
                tempFile = new File("ERROR");
            }
        }
        localTempFilePath = tempFile.getPath();

        return tempFile;
    }


    private void initializeViews(){
        playPauseButton = (ToggleButton) findViewById(R.id.media_play);
        playPauseButton.setClickable(false);
        progressDialog = Utils.getSpinnerDialog(this);
        duration = (TextView) findViewById(R.id.story_duration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizer);
        long_rewind_button = (Button) findViewById(R.id.media_long_rew);
        rewind_button = (Button) findViewById(R.id.media_short_rew);
        ff_button = (Button) findViewById(R.id.media_short_ff);
        long_ff_button = (Button) findViewById(R.id.media_long_ff);
        setControlButtonsEnabledAs(false);
    }

    private void setControlButtonsEnabledAs(Boolean enabledAs) {
        long_rewind_button.setEnabled(enabledAs);
        rewind_button.setEnabled(enabledAs);
        ff_button.setEnabled(enabledAs);
        long_ff_button.setEnabled(enabledAs);
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_user_name);
        senderText.setText(conversation.getLastUserNameToTell() + " answered");
        duration.setText(conversation.recordingDurationAsFormattedString());
        ((TextView) findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    private void setToggleButton(){
        playPauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (playPauseButton.isChecked()) {
                    play();
                } else {
                    pause();
                }
            }
        });
    }

    private void setUpMediaPlayer(){
        mPlayer = MediaPlayer.create(this, speechUri);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        finalTime = mPlayer.getDuration();
        seekbar.setMax((int) finalTime);
        playPauseButton.setClickable(true);

        setupVisualizerFxAndUI();
        mVisualizer.setEnabled(true);
    }

    private void setupVisualizerFxAndUI() {
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);

        progressDialog.dismiss();
    }

    private void play() {
        try{
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mVisualizer.setEnabled(false);
                    stopPlayback();
                    askIfUserWantsToListenAgain();
                }
            });
            timeElapsed = mPlayer.getCurrentPosition();
            seekbar.setProgress((int) timeElapsed);
            durationHandler.postDelayed(updateSeekBarTime, 100);
            setControlButtonsEnabledAs(true);
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mPlayer != null && fromUser){
                        mPlayer.seekTo(progress * 1000);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (mPlayer!=null){
                timeElapsed = mPlayer.getCurrentPosition();
                seekbar.setProgress((int) timeElapsed);
                double timeRemaining = finalTime - timeElapsed;
                duration.setText(formatMillisecondsAsTimeString((long) timeRemaining));

                //repeat again in 100 miliseconds
                durationHandler.postDelayed(this, 100);
            }
        }
    };

    private String formatMillisecondsAsTimeString (long timeInMilliseconds){
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds)));
    }

    private void pause() {
        mPlayer.pause();
        setControlButtonsEnabledAs(false);
    }

    public void shortForward(View view){
        skip(+Constants.SHORT_SKIP_TIME);
    }

    public void longForward(View view){
        skip(+Constants.LONG_SKIP_TIME);
    }

    public void shortRewind(View view){
        skip(-Constants.SHORT_SKIP_TIME);
    }

    public void longRewind(View view){
        skip(-Constants.LONG_SKIP_TIME);
    }

    private void skip(int skipTime) {
        double timeSkippedTo = timeElapsed + skipTime;

        if ((timeSkippedTo >=0) && (timeSkippedTo <= finalTime)) {
            timeElapsed = timeSkippedTo;
        } else if (timeSkippedTo > finalTime) {
            timeElapsed = finalTime;
        } else if (timeSkippedTo < 0) {
            timeElapsed = 0;
        }

        mPlayer.seekTo((int) timeElapsed);
    }

    private void stopPlayback() {
        setControlButtonsEnabledAs(false);
        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void askIfUserWantsToListenAgain() {
        StoryFinishedDialog storyFinishedDialog = new StoryFinishedDialog();
        storyFinishedDialog.show(getSupportFragmentManager(), "StoryFinishedDialog");
    }

    @Override
    public void continueWithResponseClick(String responseText) {
        // User touched the dialog's positive button
        progressDialog = Utils.getSpinnerDialog(this);
        getUserProfilePicUrl(responseText);
    }

    @Override
    public void continueWithoutResponseClick() {
        // User touched the dialog's negative button
        progressDialog = Utils.getSpinnerDialog(this);
        finishListeningToStory();
    }

    @Override
    public void listenAgainClick() {
        // User touched the dialog's neutral button
        resetPlayer();
    }

    private void resetPlayer(){
        timeElapsed = 0;
        finalTime = 0;
        playPauseButton.setChecked(false);
        seekbar.setProgress(0);
        setUpMediaPlayer();
        initializeViews();
        showConversationDetails();
        setToggleButton();
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void getUserProfilePicUrl(final String responseString) {
        DatabaseReference profilePicUrlRef = baseRef.child(Constants.FB_LOCATION_USERS)
                .child(currentUserName).child("profilePhotoUrl");
        profilePicUrlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profilePicUrl = dataSnapshot.getValue().toString();
                uploadResponseToFB(responseString, profilePicUrl);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadResponseToFB(String responseString, String profilePicUrl) {

        Response response = new Response(
                conversation.getLastUserNameToTell(), currentUserName, profilePicUrl, responseString,
                conversationPushId, conversation.getCurrentPrompt(), Utils.getSystemTimeAsLong(),
                Response.TEXT_RESPONSE);

        DatabaseReference responseRef = baseRef.child(Constants.FB_LOCATION_RESPONSES);

        HashMap<String, Object> responseHashMap = new HashMap<String, Object>();

        HashMap<String, Object> itemToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(response, Map.class);

        for (String userNames : conversation.otherConversationParticipantsArray(currentUserName)) {
            DatabaseReference newResponseRef = responseRef.child(userNames).push();
            String responsePushId = newResponseRef.getKey();
            responseHashMap.put("/" + userNames + "/" + responsePushId, itemToAddHashMap);
        }

        responseRef.updateChildren(responseHashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateResponse", "Error updating response to Firebase");
                }
                Log.i("FIREBASEUpdateResponse", "Response updated to Firebase successfully");
                finishListeningToStory();
            }
        });
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

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void goBackToMainScreen(){
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }


}
