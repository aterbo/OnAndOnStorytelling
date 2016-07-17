package com.onanon.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lassana.recorder.AudioRecorder;
import com.github.lassana.recorder.AudioRecorderBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecordStoryActivity extends AppCompatActivity {

    private AudioRecorder myRecorder;
    private MediaPlayer myPlayer;
    private Conversation conversation;
    private Chronometer recordingDurationCounter;
    private String outputFile = null;
    private String afterPauseString;
    private File recordingFile;
    private Button playbackControlButton;
    private Button recordingStatusButton;
    private Button resetControlButton;
    private Button finishAndSendButton;
    private TextView recordingStatus;
    private TextView recordingDuration;
    private String selectedConvoPushId;
    private long recordingStartTime;
    private long recordingEndTime;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_story);

        initializeViews();
        getConversation();
        setRecordingDetails();
        showConversationDetails();
        buildRecorder();

    }

    private void initializeViews(){
        recordingStatus = (TextView) findViewById(R.id.recording_status_indicator);
        recordingDuration = (TextView) findViewById(R.id.recording_duration);
        recordingDurationCounter = (Chronometer) findViewById(R.id.recording_time_counter);
        playbackControlButton = (Button)findViewById(R.id.playback_control_button);
        recordingStatusButton = (Button)findViewById(R.id.recording_control_button);
        resetControlButton = (Button)findViewById(R.id.reset_control_button);
        finishAndSendButton = (Button)findViewById(R.id.finish_and_send_button);
        playbackControlButton.setEnabled(false);
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void buildRecorder() {
        myRecorder = AudioRecorderBuilder.with(this)
                .fileName(outputFile)
                .config(AudioRecorder.MediaRecorderConfig.DEFAULT)
                .loggable()
                .build();
    }

    private void setRecordingDetails(){
        String fileName = getRandomFileName();
        outputFile = getFilesDir().getAbsolutePath();
        outputFile += "/" + fileName + ".mp4";
    }

    private String getRandomFileName(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getLastPlayersUserName().replace(",",".") + " says");

        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    public void recordingControlClick(View view){
        String recordingStatus = recordingStatusButton.getText().toString();

        if (recordingStatus.equals("Start Recording")){
            startRecording();
        } else if (recordingStatus.equals("Stop Recording")){
            stopRecording();

        } else if (recordingStatus.equals("Continue Recording")){
            startRecording();
        }
    }


    private void startRecording(){
        myRecorder.start(new AudioRecorder.OnStartListener() {
            @Override
            public void onStarted() {
                // started
                startRecordingDurationCounter();
                changeButtonsToRecordingOptions();
            }

            @Override
            public void onException(Exception e) {
                // error
                Log.i("AudioRecorder", "ERROR With continuous audio recorder!");
            }
        });


    }

    private void changeButtonsToRecordingOptions() {
        recordingStatusButton.setText("Stop Recording");
        recordingStatus.setText("Recording");
        playbackControlButton.setEnabled(false);
    }

    private void startRecordingDurationCounter() {
        recordingStartTime = System.nanoTime();
        recordingDurationCounter.setVisibility(View.VISIBLE);
        recordingDurationCounter.setBase(SystemClock.elapsedRealtime());
        recordingDurationCounter.start();
    }

    private void stopRecording(){

        myRecorder.pause(new AudioRecorder.OnPauseListener() {
            @Override
            public void onPaused(String activeRecordFileName) {

                afterPauseString = activeRecordFileName;
                Log.i("PausedRecording", outputFile);
                Log.i("PausedRecording", activeRecordFileName);
                stopRecordingDurationCounter();
                changeButtonsToPostRecordingOptions();
            }

            @Override
            public void onException(Exception e) {
                // error
            }
        });
    }

    private void stopRecordingDurationCounter() {
        recordingEndTime = System.nanoTime();
        recordingDurationCounter.stop();
    }

    private void changeButtonsToPostRecordingOptions() {
        recordingStatusButton.setText("Continue Recording");
        recordingStatus.setText("Recording Pause");
        resetControlButton.setEnabled(true);

        recordingDurationCounter.setVisibility(View.GONE);
        recordingDuration.setVisibility(View.VISIBLE);
        recordingDuration.setText("Story Length: " +
                recordingDurationAsFormattedString(getRecordingTimeInSeconds()));
        playbackControlButton.setEnabled(true);
        finishAndSendButton.setEnabled(true);
    }

    private void resumeRecording() {

    }

    private String recordingDurationAsFormattedString(long storyRecordingDuration){
        if (storyRecordingDuration != 0) {
            final int MINUTES_IN_AN_HOUR = 60;
            final int SECONDS_IN_A_MINUTE = 60;
            int totalSeconds = (int) storyRecordingDuration;

            int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
            int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
            int minutes = totalMinutes % MINUTES_IN_AN_HOUR;

            if (seconds < 10){
                return minutes + ":0" + seconds;
            }
            return minutes + ":" + seconds;

        } else {
            return "";
        }
    }

    private void resetRecording(){
        deleteRecordingFile();
        setRecordingDetails();
        recordingStatusButton.setText("Start Recording");
        recordingDurationCounter.setVisibility(View.INVISIBLE);
        recordingDuration.setVisibility(View.GONE);
        recordingStatus.setText("");
        playbackControlButton.setEnabled(false);
    }

    public void playbackControlClick(View view){
        Button playbackControlButton = (Button)findViewById(R.id.playback_control_button);
        String playbackStatus = playbackControlButton.getText().toString();

        if (playbackStatus.equals("Play Recording")){
            startPlayback();
        } else if (playbackStatus.equals("Stop Playback")){
            stopPlayback();
        }
    }

    private void startPlayback() {
        try{
            myPlayer = new MediaPlayer();
                myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();
            myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayback();
                }
            });

            recordingStatus.setText("Playing");

        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackControlButton.setText("Stop Playback");
    }

    private void stopPlayback() {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
                recordingStatus.setText("");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackControlButton.setText("Play Recording");
    }

    public void sendRecordingClick(View view){
        setRecordingTime();
        saveRecordingToFirebaseStorage();
    }

    private void saveRecordingToFirebaseStorage(){
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        StorageReference audioRecordingsRef = storageRef.child("audioStories");
        StorageReference currentConversationRef = audioRecordingsRef.child(selectedConvoPushId);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
        String strDate = sdf.format(c.getTime());

        String fileNameOnServer = strDate + ".mp4";
        StorageReference recordingStorageRef = currentConversationRef.child(fileNameOnServer);

        conversation.setStoryRecordingPushId(recordingStorageRef.getPath());

        Log.i("StorageUpload", "String ref: " + recordingStorageRef.getPath());


        Uri file = Uri.fromFile(new File(outputFile));

        UploadTask uploadTask = recordingStorageRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.i("StorageUpload", "Error uploading file to storage.");
                deleteRecordingFile();
                conversation.clearProposedTopics();
                updateConversationAfterRecording();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i("StorageUpload", "File uploaded to storage.");
                deleteRecordingFile();
                conversation.clearProposedTopics();
                updateConversationAfterRecording();
            }
        });
    }

    private void setRecordingTime(){
        conversation.setStoryRecordingDuration(getRecordingTimeInSeconds());
    }

    private long getRecordingTimeInSeconds(){
        return ((recordingEndTime - recordingStartTime)/1000000000);
    }

    private void showToastFromStringResource(int stringResourceId) {
        Toast.makeText(this, getResources().getString(stringResourceId), Toast.LENGTH_SHORT).show();
    }

    private void updateConversationAfterRecording(){
        progressDialog = Utils.getSpinnerDialog(this);

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userName : conversation.getUserNamesInConversation()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userName + "/" + selectedConvoPushId, conversationToAddHashMap);
        }

        baseRef.updateChildren(convoInfoToUpdate, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");


                progressDialog.dismiss();
                showToastFromStringResource(R.string.recording_sent_notice);
                moveToNextActivity();
            }
        });
    }

    private void deleteRecordingFile(){
        File file = new File(outputFile);
        boolean isDeleteSuccessful = file.delete();

        if (isDeleteSuccessful) {
            Log.i("Recording deleted", "Temporary recoding file deleted.");
        } else {
        Log.i("Recording NOT Deleted", "Error deleting temporary recording file.");
        }
        outputFile = null;
    }

    private void moveToNextActivity(){
        Intent intent = new Intent(this, ChooseTopicsToSendActivity.class);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
    }
}
