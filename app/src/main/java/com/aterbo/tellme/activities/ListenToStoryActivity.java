package com.aterbo.tellme.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aterbo.tellme.R;

public class ListenToStoryActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    private String soundFile = null;
    private Button playbackControlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_story);

        soundFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/javacodegeeksRecording.3gpp";

        playbackControlButton = (Button)findViewById(R.id.playback_control_button);

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
            mPlayer = MediaPlayer.create(ListenToStoryActivity.this, R.raw.home);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayback();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackControlButton.setText("Stop Playback");
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

        playbackControlButton.setText("Play Recording");
    }

}
