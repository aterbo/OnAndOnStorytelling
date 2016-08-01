package com.onanon.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
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
import com.onanon.app.Utils.PrefManager;
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
        PrefManager prefManager = new PrefManager(this);
        currentUserName = prefManager.getUserNameFromSharedPreferences();
    }

    private void getConversation(){
        Intent intent  = getIntent();
        conversation = intent.getParcelableExtra(Constants.CONVERSATION_INTENT_KEY);
        selectedConvoPushId = intent.getStringExtra(Constants.CONVERSATION_PUSH_ID_INTENT_KEY);
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
                    stopPlayback();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Listen again?");
        builder.setPositiveButton("No, thank you!", new DialogInterface.OnClickListener() {
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
        deleteLocalStoryAudioFile();

        conversation.markUserAsHasHeardStory(currentUserName);
        if (haveAllUsersHeardStory()){
            deleteFBStorageStoryAudioFile();
        } else {
            updateConversationAfterListening();
        }
    }

    private boolean haveAllUsersHeardStory(){
        if (conversation.getUserNamesHaveNotHeardStory().contains("none")) {
            return true;
        } else {
            return false;
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
        audioFileStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Recording deleted", "FB Storage recoding file deleted.");

                deleteConversationReferencesToFile();
                updateConversationAfterListening();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Recording deleted", "ERROR - FB Storage recoding file NOT deleted.");

                deleteConversationReferencesToFile();
                updateConversationAfterListening();
            }
        });
    }

    private void deleteConversationReferencesToFile(){
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
        finish();
    }
}
