package com.aterbo.tellme.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;

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
    private Prompt storyPrompt;
    private Uri speechUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_story);

        getConversation();
        showConversationDetails();
        getPromptData();
        getstoryUri();
        showPromptTextInTextView();
        initializeViews();
        setToggleButton();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra("selectedConversation");
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
        senderText.setText(conversation.getUsersNameAsString() + " answered");

        TextView recordingLength = (TextView)findViewById(R.id.story_duration);
        recordingLength.setText(conversation.getStoryDuration());
    }

    private void getPromptData(){
        storyPrompt = conversation.getCurrentPrompt();
    }

    private void getstoryUri(){
        speechUri = Uri.parse(conversation.getStoryFilePath());
    }


    private void showPromptTextInTextView(){
        ((TextView)findViewById(R.id.prompt_text)).setText(storyPrompt.getText());
    }

    private void initializeViews(){
        mPlayer = MediaPlayer.create(this, speechUri);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        finalTime = mPlayer.getDuration();
        duration = (TextView) findViewById(R.id.story_duration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
    }

    private void setToggleButton(){
        playPauseButton = (ToggleButton) findViewById(R.id.media_play);
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
                    storyComplete();
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

    private void storyComplete(){
        stopPlayback();
        Intent intent = new Intent(this, ListeningToStoryCompleteActivity.class);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
    }

    private Prompt getDummyPromptData(){
        return new Prompt("Tell me a story about an immigrant.", "An immigrant");
    }
}
