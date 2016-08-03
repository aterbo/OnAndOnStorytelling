package com.onanon.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecordStoryActivity extends AppCompatActivity {

    private AudioRecorder myRecorder;
    private MediaPlayer myPlayer;
    private Conversation conversation;
    private Chronometer recordingDurationCounter;
    private String outputFile = null;
    private ToggleButton playbackButton;
    private ToggleButton recordButton;
    private Button resetControlButton;
    private Button finishAndSendButton;
    private TextView recordingStatus;
    private TextView recordingDuration;
    private String selectedConvoPushId;
    private long recordingStartTime;
    private long recordingEndTime;
    private long cumulativeRecordingTime;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_story);

        cumulativeRecordingTime = 0;
        initializeViews();
        setRecordingButtonListener();
        setPlaybackButtonListener();
        getConversation();
        createRecordingFile();
        showConversationDetails();
        buildRecorder();

    }

    private void initializeViews(){
        recordingStatus = (TextView) findViewById(R.id.recording_status_indicator);
        recordingDuration = (TextView) findViewById(R.id.recording_duration);
        recordingDurationCounter = (Chronometer) findViewById(R.id.recording_time_counter);
        recordButton = (ToggleButton)findViewById(R.id.record_button);
        playbackButton = (ToggleButton) findViewById(R.id.playback_control_button);
        resetControlButton = (Button)findViewById(R.id.reset_control_button);
        finishAndSendButton = (Button)findViewById(R.id.finish_and_send_button);
        playbackButton.setEnabled(false);
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void buildRecorder() {

    }

    private void createRecordingFile(){
        File file = getFileForRecording();

        deleteOldFile(file);

        createNewFile(file);
    }

    private void deleteOldFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private void createNewFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private File getFileForRecording() {
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
        outputFile = tempFile.getPath();

        return tempFile;
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getLastUserNameToTell().replace(",",".") + " asks");

        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    private void setRecordingButtonListener(){
        recordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
    }

    private void setPlaybackButtonListener(){
        playbackButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startPlayback();
                } else {
                    stopPlayback();
                }
            }
        });
    }

    private void startRecording(){

        recordingDuration.setVisibility(View.GONE);
        recordingStatus.setVisibility(View.GONE);

        myRecorder = AudioRecorderBuilder.with(this)
                .fileName(outputFile)
                .config(AudioRecorder.MediaRecorderConfig.DEFAULT)
                .loggable()
                .build();

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
        recordingStatus.setText("Recording");
        recordingStatus.setVisibility(View.VISIBLE);
        playbackButton.setEnabled(false);
        resetControlButton.setEnabled(false);
    }

    private void startRecordingDurationCounter() {
        recordingStartTime = System.nanoTime();
        recordingDurationCounter.setVisibility(View.VISIBLE);
        recordingDurationCounter.setBase(SystemClock.elapsedRealtime() - cumulativeRecordingTime*1000);
        recordingDurationCounter.setText(recordingDurationAsFormattedString(cumulativeRecordingTime));
        recordingDurationCounter.start();
    }

    private void stopRecording(){
        stopRecordingDurationCounter();

        // Wait a half second to stop recording
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myRecorder.pause(new AudioRecorder.OnPauseListener() {
                    @Override
                    public void onPaused(String activeRecordFileName) {
                        Log.i("PausedRecording", outputFile);
                        Log.i("PausedRecording", activeRecordFileName);

                        addUpTotalRecordingTime();
                        changeButtonsToPostRecordingOptions();
                    }

                    @Override
                    public void onException(Exception e) {
                        // error
                    }
                });
            }
        }, 800);
    }

    private void stopRecordingDurationCounter() {
        recordingEndTime = System.nanoTime();
        recordingDurationCounter.stop();
    }

    private void addUpTotalRecordingTime() {
        long recordingDurationOfLastSegment = getRecordingTimeInSeconds();
        cumulativeRecordingTime = cumulativeRecordingTime + recordingDurationOfLastSegment;
    }

    private long getRecordingTimeInSeconds(){
        return ((recordingEndTime - recordingStartTime)/1000000000);
    }

    private void changeButtonsToPostRecordingOptions() {
        recordingStatus.setText("Recording Paused");
        resetControlButton.setEnabled(true);
        recordingDurationCounter.setVisibility(View.GONE);
        recordingDuration.setVisibility(View.VISIBLE);
        recordingDuration.setText("Story Length: " +
                recordingDurationAsFormattedString(cumulativeRecordingTime));
        playbackButton.setEnabled(true);
        finishAndSendButton.setEnabled(true);
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

    public void resetRecording(View view){
        //myRecorder.cancel();
        cumulativeRecordingTime = 0;
        deleteRecordingFile();
        createRecordingFile();
        recordingDurationCounter.setVisibility(View.INVISIBLE);
        recordingDuration.setVisibility(View.GONE);
        recordingStatus.setText("");
        playbackButton.setEnabled(false);
        resetControlButton.setEnabled(false);
        finishAndSendButton.setEnabled(false);
    }

    private void startPlayback() {
        File file = new File(outputFile);
        if (file.exists()) {

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

        Log.i("FilePlayback", "File Exists, attempting to play");
        } else {
            Log.i("FilePlayback", "File does not exist!");
        }

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
    }

    public void sendRecordingClick(View view){
        progressDialog = Utils.getSpinnerDialog(this);
        setRecordingTime();
        saveRecordingToFirebaseStorage();
    }

    private void setRecordingTime() {
        conversation.setStoryRecordingDuration(cumulativeRecordingTime);
    }

    private void saveRecordingToFirebaseStorage(){
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        StorageReference audioRecordingsRef = storageRef.child("audioStories");
        StorageReference currentConversationRef = audioRecordingsRef.child(selectedConvoPushId);

        String fileNameOnServer = Utils.getDateTimeAsString() + ".mp4";
        StorageReference recordingStorageRef = currentConversationRef.child(fileNameOnServer);

        conversation.setFbStorageFilePathToRecording(recordingStorageRef.getPath());

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

    private void updateConversationAfterRecording(){
        conversation.changeNextPlayer();
        conversation.setAllUsersAsHaveNotListenedButLastToTell();

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
                Log.i("FIREBASEUpdateCONVO", "Convo updated to Firebase successfully");

                progressDialog.dismiss();
                Toast.makeText(RecordStoryActivity.this, R.string.recording_sent_notice, Toast.LENGTH_SHORT);
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
        finish();
    }
}
