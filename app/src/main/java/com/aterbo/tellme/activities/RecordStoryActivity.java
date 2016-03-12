package com.aterbo.tellme.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;

import java.io.IOException;

public class RecordStoryActivity extends AppCompatActivity {

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private Conversation conversation;
    private String outputFile = null;
    private Button playbackControlButton;
    private Button recordingStatusButton;
    private Button finishAndSendButton;
    private TextView recordingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_story);

        getConversation();
        showConversationDetails();

        recordingStatus = (TextView) findViewById(R.id.recording_status_indicator);
        // store it to sd card
        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/javacodegeeksRecording.3gpp";

        playbackControlButton = (Button)findViewById(R.id.playback_control_button);
        recordingStatusButton = (Button)findViewById(R.id.recording_control_button);
        finishAndSendButton = (Button)findViewById(R.id.finish_and_send_button);
        playbackControlButton.setEnabled(false);

        Intent intent = getIntent();
        String chosenTopic = intent.getStringExtra("ChosenTopic");
        ((TextView)findViewById(R.id.prompt_text)).setText(chosenTopic);

    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("selectedConversation");
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getUsersNameAsString() + " says");
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
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myRecorder.setOutputFile(outputFile);

            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

        recordingStatusButton.setText("Stop Recording");
        recordingStatus.setText("Recording Point: Recording");
        playbackControlButton.setEnabled(false);
    }

    private void stopRecording(){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;
        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }

        recordingStatusButton.setText("Reset");
        recordingStatus.setText("Recording Point: Stop recording");
        playbackControlButton.setEnabled(true);
        finishAndSendButton.setEnabled(true);
    }

    private void resetRecording(){
        recordingStatusButton.setText("Start Recording");
        recordingStatus.setText("Recording Point: -");
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

            recordingStatus.setText("Recording Point: Playing");

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
                recordingStatus.setText("Recording Point: -");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackControlButton.setText("Play Recording");
    }

    public void sendRecordingClick(View view){
        //TODO: Figure out how the hell to send this to someone.
        showToastFromStringResource(R.string.recording_sent_notice);
        Intent intent = new Intent(this, ChooseTopicsToSendActivity.class);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
    }

    private void showToastFromStringResource(int stringResourceId) {
        Toast.makeText(this, getResources().getString(stringResourceId), Toast.LENGTH_LONG).show();
    }
}
