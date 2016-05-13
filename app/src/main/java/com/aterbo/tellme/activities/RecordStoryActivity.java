package com.aterbo.tellme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.Utils.Utils;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.VisualizerView;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Handler;

public class RecordStoryActivity extends AppCompatActivity {

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private Conversation conversation;
    private Chronometer recordingDurationCounter;
    private String outputFile = null;
    private Button playbackControlButton;
    private Button recordingStatusButton;
    private Button finishAndSendButton;
    private TextView recordingStatus;
    private TextView recordingDuration;
    private String selectedConvoPushId;
    private String encodedRecording;
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

    }

    private void initializeViews(){
        recordingStatus = (TextView) findViewById(R.id.recording_status_indicator);
        recordingDuration = (TextView) findViewById(R.id.recording_duration);
        recordingDurationCounter = (Chronometer) findViewById(R.id.recording_time_counter);
        playbackControlButton = (Button)findViewById(R.id.playback_control_button);
        recordingStatusButton = (Button)findViewById(R.id.recording_control_button);
        finishAndSendButton = (Button)findViewById(R.id.finish_and_send_button);
        playbackControlButton.setEnabled(false);
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void setRecordingDetails(){
        if (Utils.isExternalStorageWritable()) {
            String fileName = getRandomFileName();
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
            outputFile += "/" + fileName + ".3gp";
        }
    }

    private String getRandomFileName(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getLastPlayersEmail().replace(",",".") + " says");

        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    public void recordingControlClick(View view){
        String recordingStatus = recordingStatusButton.getText().toString();

        if (recordingStatus.equals("Start Recording")){
            startRecording();
        } else if (recordingStatus.equals("Stop Recording")){
            stopRecording();

        } else if (recordingStatus.equals("Reset")){
            resetRecording();
        }
    }

    private void startRecording(){
        try {
            myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB); //Optimized for speech coding
            myRecorder.setOutputFile(outputFile);
            myRecorder.setAudioChannels(1);

            myRecorder.prepare();
            recordingStartTime = System.nanoTime();
            myRecorder.start();

            recordingDurationCounter.setVisibility(View.VISIBLE);
            recordingDurationCounter.setBase(SystemClock.elapsedRealtime());
            recordingDurationCounter.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

        recordingStatusButton.setText("Stop Recording");
        recordingStatus.setText("Recording");
        playbackControlButton.setEnabled(false);
    }

    private void stopRecording(){
        try {
            recordingEndTime = System.nanoTime();
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;
            recordingDurationCounter.stop();
        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }

        recordingStatusButton.setText("Reset");
        recordingStatus.setText("Recording finished");
        recordingDurationCounter.setVisibility(View.GONE);
        recordingDuration.setVisibility(View.VISIBLE);
        recordingDuration.setText("Story Length: " +
                recordingDurationAsFormattedString(getRecordingTimeInSeconds()));
        playbackControlButton.setEnabled(true);
        finishAndSendButton.setEnabled(true);
    }

    private void resetRecording(){
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
        saveRecordingToConversation();
        conversation.clearProposedTopics();
        conversation.changeNextPlayer();
        updateConversationAfterRecording();
    }

    private void saveRecordingToConversation(){
        convertAudioFileToString();
        setRecordingTime();
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


    public void updateConversationAfterRecording(){
        showProgressDialog();
        Firebase baseRef = new Firebase(Constants.FB_LOCATION);

        Firebase newRecordingRef = baseRef.child(Constants.FB_LOCATION_RECORDINGS).push();
        String recordingPushId = newRecordingRef.getKey();
        conversation.setStoryRecordingPushId(recordingPushId);

        HashMap<String, Object> convoInfoToUpdate = new HashMap<String, Object>();

        HashMap<String, Object> conversationToAddHashMap =
                (HashMap<String, Object>) new ObjectMapper().convertValue(conversation, Map.class);

        for (String userEmail : conversation.getUsersInConversationEmails()) {
            convoInfoToUpdate.put("/" + Constants.FB_LOCATION_USER_CONVOS + "/"
                    + userEmail + "/" + selectedConvoPushId, conversationToAddHashMap);
        }

        convoInfoToUpdate.put("/" + Constants.FB_LOCATION_RECORDINGS + "/" + recordingPushId,
                encodedRecording);

        baseRef.updateChildren(convoInfoToUpdate, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updatedto Firebase successfully");

                dismissProgressDialog();
                showToastFromStringResource(R.string.recording_sent_notice);
                moveToNextActivity();
            }
        });
    }

    private void convertAudioFileToString(){
        File file = new File(outputFile);
        byte[] bytes = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        encodedRecording = Base64.encodeToString(bytes, 0);
    }

    private void moveToNextActivity(){

        Intent intent = new Intent(this, ChooseTopicsToSendActivity.class);
        intent.putExtra(Constants.CONVERSATION_INTENT_KEY, conversation);
        intent.putExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY, selectedConvoPushId);
        startActivity(intent);
    }

    public String recordingDurationAsFormattedString(long storyRecordingDuration){
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

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading story...");
        progressDialog.setTitle("Uploading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        progressDialog.dismiss();
    }
}
