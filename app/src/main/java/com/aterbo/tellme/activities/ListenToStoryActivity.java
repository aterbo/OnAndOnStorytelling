package com.aterbo.tellme.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.Utils.Utils;
import com.aterbo.tellme.classes.Conversation;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//http://examples.javacodegeeks.com/android/android-mediaplayer-example/

public class ListenToStoryActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    public TextView duration;
    private Conversation conversation;
    private double timeElapsed = 0;
    private double finalTime = 0;
    private int shortSkipTime = 5000;
    private int longSkipTime = 30000;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;
    private ToggleButton playPauseButton;
    private Uri speechUri;
    private String selectedConvoPushId;
    private String localTempFilePath;
    private String encodedRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_story);

        playPauseButton = (ToggleButton) findViewById(R.id.media_play);
        playPauseButton.setClickable(false);
        getConversation();
        getRecording();
        showConversationDetails();
        showPromptTextInTextView();
        initializeViews();
        setToggleButton();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("selectedConversation");
        selectedConvoPushId = intent.getStringExtra("selectedConversationPushId");
    }

    private void getRecording(){
        Firebase recordingRef = new Firebase(Constants.FB_LOCATION + "/" +
                Constants.FB_LOCATION_RECORDINGS + "/" + conversation.getStoryRecordingPushId());

        recordingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                encodedRecording = dataSnapshot.getValue(String.class);
                convertRecordingToTempFile();
                getStoryUri();
                setUpMediaPlayer();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getLastPlayersEmail().replace(",",".") + " answered");

        TextView recordingLength = (TextView)findViewById(R.id.story_duration);
        recordingLength.setText("StoryLength");
    }

    private void getStoryUri(){
        speechUri = Uri.parse(localTempFilePath);
    }

    private void convertRecordingToTempFile(){
        byte[] decoded = Base64.decode(encodedRecording, 0);
        String outputFile;

        if (Utils.isExternalStorageWritable()) {
            String fileName = UUID.randomUUID().toString().replaceAll("-", "");
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
            outputFile += "/" + fileName + ".3gp";

            try
            {
                File tempFile = new File(outputFile);
                FileOutputStream os = new FileOutputStream(tempFile, true);
                os.write(decoded);
                os.close();
                localTempFilePath = tempFile.getPath();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void showPromptTextInTextView(){
        ((TextView)findViewById(R.id.prompt_text)).setText(conversation.getCurrentPrompt().getText());
    }

    private void initializeViews(){
        duration = (TextView) findViewById(R.id.story_duration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);
    }

    private void setUpMediaPlayer(){

        mPlayer = MediaPlayer.create(this, speechUri);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        finalTime = mPlayer.getDuration();
        seekbar.setMax((int) finalTime);
        playPauseButton.setClickable(true);
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

    private void play() {
        try{
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    askIfUserWantsToListenAgain();
                }
            });
            timeElapsed = mPlayer.getCurrentPosition();
            seekbar.setProgress((int) timeElapsed);
            durationHandler.postDelayed(updateSeekBarTime, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (mPlayer!=null){
                timeElapsed = mPlayer.getCurrentPosition();
                //set seekbar progress
                seekbar.setProgress((int) timeElapsed);
                //set time remaing
                double timeRemaining = finalTime - timeElapsed;
                duration.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                        TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) -
                                TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

                //repeat yourself that again in 100 miliseconds
                durationHandler.postDelayed(this, 100);
            }
        }
    };

    private void pause() {
        mPlayer.pause();
    }

    public void shortForward(View view){
        forward(shortSkipTime);
    }

    public void longForward(View view){
        forward(longSkipTime);
    }

    public void shortRewind(View view){
        rewind(shortSkipTime);
    }

    public void longRewind(View view){
        rewind(longSkipTime);
    }

    private void forward(int forwardSkipTime) {
        if ((timeElapsed + forwardSkipTime) <= finalTime) {
            timeElapsed = timeElapsed + forwardSkipTime;

            mPlayer.seekTo((int) timeElapsed);
        }
    }

    private void rewind(int backwardSkipTime) {
        if ((timeElapsed - backwardSkipTime) >= 0) {
            timeElapsed = timeElapsed - backwardSkipTime;

            mPlayer.seekTo((int) timeElapsed);
        }
    }

    private void stopPlayback() {
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
        stopPlayback();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Listen again?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetPlayer();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                moveToNextActivity();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetPlayer(){
        timeElapsed = 0;
        finalTime = 0;
        playPauseButton.setChecked(false);
        seekbar.setProgress(0);
        setUpMediaPlayer();
        initializeViews();
        setToggleButton();

    }

    private void moveToNextActivity(){
        Intent intent = new Intent(this, ListeningToStoryCompleteActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("conversationPushId", selectedConvoPushId);
        startActivity(intent);
    }
}
