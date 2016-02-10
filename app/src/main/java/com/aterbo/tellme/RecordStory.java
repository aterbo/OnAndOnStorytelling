package com.aterbo.tellme;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class RecordStory extends AppCompatActivity {

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    private Button playbackControlButton;
    private TextView recordingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_story);

        recordingStatus = (TextView) findViewById(R.id.recording_status_indicator);
        // store it to sd card
        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/javacodegeeksRecording.3gpp";

        playbackControlButton = (Button)findViewById(R.id.playback_control_button);
        playbackControlButton.setEnabled(false);

    }

    public void recordingControlClick(View view){
        Button recordingStatusButton = (Button)findViewById(R.id.recording_control_button);
        String recordingStatus = recordingStatusButton.getText().toString();

        if (recordingStatus.equals("Start Recording")){
            startRecording();
            recordingStatusButton.setText("Stop Recording");

        } else if (recordingStatus.equals("Stop Recording")){
            stopRecording();
            recordingStatusButton.setText("Reset");

        } else if (recordingStatus.equals("Reset")){
            resetRecording();
            recordingStatusButton.setText("Start Recording");
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

        recordingStatus.setText("Recording Point: Recording");
        playbackControlButton.setEnabled(false);
    }

    private void stopRecording(){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;

            playbackControlButton.setEnabled(true);
            recordingStatus.setText("Recording Point: Stop recording");

        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }
    }

    private void resetRecording(){
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
                recordingStatus.setText("Recording Point: Stopping");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackControlButton.setText("Play Recording");
    }

}
