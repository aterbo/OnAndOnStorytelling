package com.onanon.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;
import com.onanon.app.classes.VisualizerView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//http://examples.javacodegeeks.com/android/android-mediaplayer-example/
//Visualizer: http://android-er.blogspot.com/2015/02/create-audio-visualizer-for-mediaplayer.html

public class ListenToStoryActivity extends AppCompatActivity {

    private String currentUserName;
    private MediaPlayer mPlayer;
    private TextView duration;
    private Conversation conversation;
    private double timeElapsed = 0;
    private double finalTime = 0;
    private final int shortSkipTime = 5000;
    private final int longSkipTime = 30000;
    private final Handler durationHandler = new Handler();
    private SeekBar seekbar;
    private ToggleButton playPauseButton;
    private Uri speechUri;
    private String selectedConvoPushId;
    private String localTempFilePath;
    private ProgressDialog progressDialog;
    private StorageReference audioFileStorageRef;

    private VisualizerView mVisualizerView;
    private Visualizer mVisualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_story);

        getUserName();
        getConversation();
        getRecordingFromFirebaseStorage();
        initializeViews();
        showConversationDetails();
        setToggleButton();
    }

    private void getUserName(){
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFS_FILE, MODE_PRIVATE);
        currentUserName =  settings.getString(Constants.CURRENT_USER_NAME_KEY, "");
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
    }

    private void getRecordingFromFirebaseStorage(){
        String FBStorageFilePath = conversation.getFbStorageFilePathToRecording();

        FirebaseStorage storage = FirebaseStorage.getInstance();


        StorageReference sRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        audioFileStorageRef = sRef.child(FBStorageFilePath);

        File tempFile;

        if (Utils.isExternalStorageWritable()) {
            String fileName = UUID.randomUUID().toString().replaceAll("-", "");
            File tempFileDir = this.getFilesDir();
            tempFile = new File(tempFileDir, fileName + ".mp4");
            localTempFilePath = tempFile.getPath();
        } else {
            try{
            tempFile = File.createTempFile("recordings", "mp4");
            } catch (IOException exception){
                tempFile = new File("ERROR");
            }
        }

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
                // Handle any errors
            }
        });
    }

    private void initializeViews(){
        playPauseButton = (ToggleButton) findViewById(R.id.media_play);
        playPauseButton.setClickable(false);
        progressDialog = Utils.getSpinnerDialog(this);
        duration = (TextView) findViewById(R.id.story_duration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizer);
    }

    private void showConversationDetails(){
        TextView senderText = (TextView)findViewById(R.id.sender_text);
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
    }

    public void shortForward(View view){
        skip(+shortSkipTime);
    }

    public void longForward(View view){
        skip(+longSkipTime);
    }

    public void shortRewind(View view){
        skip(-shortSkipTime);
    }

    public void longRewind(View view){
        skip(-longSkipTime);
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
        builder.setPositiveButton("Back to Main Menu", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishListeningToStory();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Listen Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetPlayer();
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
        showConversationDetails();
        setToggleButton();
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void finishListeningToStory() {
        conversation.markUserAsHasHeardStory(currentUserName);
        deleteLocalStoryAudioFile();
        deleteFBStorageStoryAudioFile();
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
        audioFileStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Recording deleted", "FB Storage recoding file deleted.");

                eliminateCurrentStory();
                updateConversationAfterListening();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Recording deleted", "ERROR - FB Storage recoding file NOT deleted.");

                eliminateCurrentStory();
                updateConversationAfterListening();
            }
        });
    }

    private void eliminateCurrentStory(){
        conversation.setFbStorageFilePathToRecording("none");
        conversation.setCurrentPrompt(new Prompt("null", "null"));
        conversation.setStoryRecordingDuration(0);
    }

    private void updateConversationAfterListening(){
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

                goBackToMainScreen();
            }
        });
    }

    private void goBackToMainScreen(){
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }
}
