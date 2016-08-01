package com.onanon.app.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation implements Parcelable{

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Conversation> CREATOR = new Parcelable.Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    private ArrayList<String> userNamesInConversation;
    private ArrayList<String> userNamesHaveHeardStory;
    private ArrayList<String> userNamesHaveNotHeardStory;
    private String nextUserNameToTell;
    private String lastUserNameToTell;
    private String fbStorageFilePathToRecording;
    private Prompt proposedPrompt1;
    private Prompt proposedPrompt2;
    private Prompt proposedPrompt3;
    private Prompt currentPrompt;
    private long storyRecordingDuration;
    private long dateLastStoryRecorded;
    private long dateLastActionOccurred;

    public Conversation() { }

    public Conversation(ArrayList<String> userNamesInConversation, String nextToTellUserName,
                        String lastPlayersUserNames, Prompt currentPrompt, ArrayList<Prompt> proposedPrompts){
        this.userNamesInConversation = userNamesInConversation;
        this.userNamesHaveHeardStory = new ArrayList<>();
        userNamesHaveHeardStory.add("none");
        this.userNamesHaveNotHeardStory = new ArrayList<>();
        userNamesHaveNotHeardStory.add("none");
        this.nextUserNameToTell = nextToTellUserName;
        this.lastUserNameToTell = lastPlayersUserNames;
        this.currentPrompt = currentPrompt;
        this.proposedPrompt1 = proposedPrompts.get(0);
        this.proposedPrompt2 = proposedPrompts.get(1);
        this.proposedPrompt3 = proposedPrompts.get(2);
        fbStorageFilePathToRecording = "none";
        storyRecordingDuration = 0;
        this.dateLastStoryRecorded = 0;
        this.dateLastActionOccurred = System.currentTimeMillis();
    }

    //Parcelabler.com
    protected Conversation(Parcel in) {
        if (in.readByte() == 0x01) {
            userNamesInConversation = new ArrayList<String>();
            in.readList(userNamesInConversation, String.class.getClassLoader());
        } else {
            userNamesInConversation = null;
        }
        if (in.readByte() == 0x01) {
            userNamesHaveHeardStory = new ArrayList<String>();
            in.readList(userNamesHaveHeardStory, String.class.getClassLoader());
        } else {
            userNamesHaveHeardStory = null;
        }
        if (in.readByte() == 0x01) {
            userNamesHaveNotHeardStory = new ArrayList<String>();
            in.readList(userNamesHaveNotHeardStory, String.class.getClassLoader());
        } else {
            userNamesHaveNotHeardStory = null;
        }
        nextUserNameToTell = in.readString();
        lastUserNameToTell = in.readString();
        fbStorageFilePathToRecording = in.readString();
        proposedPrompt1 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt2 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt3 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        currentPrompt = (Prompt) in.readValue(Prompt.class.getClassLoader());
        storyRecordingDuration = in.readLong();
        dateLastStoryRecorded = in.readLong();
        dateLastActionOccurred = in.readLong();
    }

    public ArrayList<String> getUserNamesInConversation() {
        return userNamesInConversation;
    }

    public void setUserNamesInConversation(ArrayList<String> userNamesInConversation) {
        this.userNamesInConversation = userNamesInConversation;
    }

    public ArrayList<String> getUserNamesHaveHeardStory() {
        return userNamesHaveHeardStory;
    }

    public void setUserNamesHaveHeardStory(ArrayList<String> userNamesHaveHeardStory) {
        this.userNamesHaveHeardStory = userNamesHaveHeardStory;
    }

    public ArrayList<String> getUserNamesHaveNotHeardStory() {
        return userNamesHaveNotHeardStory;
    }

    public void setUserNamesHaveNotHeardStory(ArrayList<String> userNamesHaveNotHeardStory) {
        this.userNamesHaveNotHeardStory = userNamesHaveNotHeardStory;
    }

    public long getDateLastStoryRecorded() {
        return dateLastStoryRecorded;
    }

    public void setDateLastStoryRecorded(long dateLastStoryRecorded) {
        this.dateLastStoryRecorded = dateLastStoryRecorded;
    }

    public long getDateLastActionOccurred() {
        return dateLastActionOccurred;
    }

    public void setDateLastActionOccurred(long dateLastActionOccurred) {
        this.dateLastActionOccurred = dateLastActionOccurred;
    }

    public Prompt getProposedPrompt1() {
        return proposedPrompt1;
    }

    public void setProposedPrompt1(Prompt proposedPrompt1) {
        this.proposedPrompt1 = proposedPrompt1;
    }

    public Prompt getProposedPrompt2() {
        return proposedPrompt2;
    }

    public void setProposedPrompt2(Prompt proposedPrompt2) {
        this.proposedPrompt2 = proposedPrompt2;
    }

    public Prompt getProposedPrompt3() {
        return proposedPrompt3;
    }

    public void setProposedPrompt3(Prompt proposedPrompt3) {
        this.proposedPrompt3 = proposedPrompt3;
    }

    public String getNextUserNameToTell() {
        return nextUserNameToTell;
    }

    public void setNextUserNameToTell(String nextUserNameToTell) {
        this.nextUserNameToTell = nextUserNameToTell;
    }

    public String getLastUserNameToTell() {
        return lastUserNameToTell;
    }

    public void setLastUserNameToTell(String lastUserNameToTell){
        this.lastUserNameToTell = lastUserNameToTell;
    }

    public String getUserNames(int userIndex){
        return userNamesInConversation.get(userIndex);
    }

    public void addUserNameToConversation(String userName){
        userNamesInConversation.add(userName);
    }

    public String userNamesAsString(){
        String userNames = "";
        for (String name : userNamesInConversation){
            userNames = userNames + name.replace(".",",") + ", ";
        }

        if (userNames.endsWith(", ")) {
            userNames = userNames.substring(0, userNames.length() - 2);
        }

        return userNames;
    }

    public Prompt getCurrentPrompt(){
        return currentPrompt;
    }

    public void setCurrentPrompt(Prompt currentPrompt){
        this.currentPrompt = currentPrompt;
    }

    public String getFbStorageFilePathToRecording(){
        return fbStorageFilePathToRecording;
    }

    public void setFbStorageFilePathToRecording(String fbStorageFilePathToRecording){
        this.fbStorageFilePathToRecording = fbStorageFilePathToRecording;
    }

    public long getStoryRecordingDuration() {
        return storyRecordingDuration;
    }

    public void setStoryRecordingDuration(long storyRecordingDuration) {
        this.storyRecordingDuration = storyRecordingDuration;
    }

    public String proposedPromptsTagAsString(){
        return proposedPrompt1.getTag() + ", " +
                proposedPrompt2.getTag() + ", or " +
                proposedPrompt3.getTag();
    }

    public ArrayList<Prompt> getProposedPromptsAsList() {
        ArrayList<Prompt> proposedPromptsList = new ArrayList<>();
        proposedPromptsList.add(proposedPrompt1);
        proposedPromptsList.add(proposedPrompt2);
        proposedPromptsList.add(proposedPrompt3);

        return proposedPromptsList;
    }

    public void changeNextPlayer(){
        lastUserNameToTell = nextUserNameToTell;
        int currentUserIndex = userNamesInConversation.indexOf(nextUserNameToTell);
        int userListSize = userNamesInConversation.size();

        if (currentUserIndex+1 >= userListSize){
            nextUserNameToTell = userNamesInConversation.get(0);
        } else if (currentUserIndex + 1 < userListSize) {
            nextUserNameToTell = userNamesInConversation.get(currentUserIndex+1);
        }
    }

    public void setAllUsersAsHaveNotListenedButLastToTell(){
        userNamesHaveHeardStory = new ArrayList<>();
        userNamesHaveHeardStory.add("none");
        userNamesHaveNotHeardStory = new ArrayList<>();
        for(String userNames : userNamesInConversation) {
            if (!userNames.equals(lastUserNameToTell)) {
                userNamesHaveNotHeardStory.add(userNames);
            }
        }
    }

    public void markUserAsHasHeardStory(String currentUserName) {
        userNamesHaveNotHeardStory.remove(currentUserName);
        if (userNamesHaveNotHeardStory.isEmpty()){
            userNamesHaveNotHeardStory.add("none");
        }

        userNamesHaveHeardStory.add(currentUserName);
        userNamesHaveHeardStory.remove("none");
    }

    public void clearProposedTopics(){
        proposedPrompt1 = null;
        proposedPrompt2 = null;
        proposedPrompt3 = null;
    }

    public String recordingDurationAsFormattedString(){
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

    public String otherConversationParticipants(String currentUserName){
        String participantsString = "";
        for (String userName : userNamesInConversation) {
            if(!userName.equals(currentUserName)) {
                participantsString = participantsString + ", " + userName;
            }
        }

        return participantsString.substring(2, participantsString.length());
    }

    public String determineTitle(String currentUserName){
        if (isUserTurnToTell(currentUserName)) {
            return "Tell:    " + proposedPromptsTagAsString();

        } else if (isUserTurnToSendPrompts(currentUserName)) {
            return "You need to send topics!";

        } else if (isUserTurnToHear(currentUserName)) {
            return "Hear:    " + currentPrompt.getText();

        } else if (isUserWaitingForPrompts(currentUserName)) {
            return "Waiting for topics.";

        } else if (isUserWaitingForStory(currentUserName)) {
            return "Waiting for a story.";

        } else if (isUserWaitingForOthersToHear()) {
            return "Waiting for everyone else to listen.";
        }
        return "Oops";
    }

    public boolean isUserTurnToTell(String currentUserName) {
        if(isCurrentUserNextToTell(currentUserName)
                && haveAllUsersHeardStory()
                && !isStoryRecorded()
                && isProposedPromptSelected()) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isUserTurnToSendPrompts(String currentUserName) {
        if(isCurrentUserLastToTell(currentUserName)
                && !isProposedPromptSelected()) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isUserTurnToHear(String currentUserName) {
        if(isStoryRecorded()
                && hasCurrentUserHeardStory(currentUserName)) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isUserWaitingForPrompts(String currentUserName) {
        if(isCurrentUserNextToTell(currentUserName)
                && !isProposedPromptSelected()) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isUserWaitingForStory(String currentUserName) {
        if(!isCurrentUserNextToTell(currentUserName)
                && !isStoryRecorded()) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isUserWaitingForOthersToHear() {
        if(isStoryRecorded()
                && !haveAllUsersHeardStory()) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isCurrentUserNextToTell(String currentUserName){
        if(getNextUserNameToTell().equals(currentUserName)){
            return true;
        } else{
            return false;
        }
    }

    public boolean isCurrentUserLastToTell(String currentUserName){
        if(getLastUserNameToTell().equals(currentUserName)){
            return true;
        } else{
            return false;
        }
    }

    public boolean haveAllUsersHeardStory() {
        if (userNamesHaveHeardStory.contains("none")) {
            return true;
        } else{
            return false;
        }
    }

    public boolean hasCurrentUserHeardStory(String currentUserName) {
        if (getUserNamesHaveNotHeardStory().contains(currentUserName)) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isStoryRecorded() {
        if(!fbStorageFilePathToRecording.equals("none")) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isProposedPromptSelected() {
        if(proposedPrompt1 != null) {
            return true;
        } else{
            return false;
        }
    }

    public boolean isOnlyTwoPeople() {
        if (userNamesInConversation.size() == 2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (userNamesInConversation == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(userNamesInConversation);
        }
        if (userNamesHaveHeardStory == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(userNamesHaveHeardStory);
        }
        if (userNamesHaveNotHeardStory == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(userNamesHaveNotHeardStory);
        }
        dest.writeString(nextUserNameToTell);
        dest.writeString(lastUserNameToTell);
        dest.writeString(fbStorageFilePathToRecording);
        dest.writeValue(proposedPrompt1);
        dest.writeValue(proposedPrompt2);
        dest.writeValue(proposedPrompt3);
        dest.writeValue(currentPrompt);
        dest.writeLong(storyRecordingDuration);
        dest.writeLong(dateLastStoryRecorded);
        dest.writeLong(dateLastActionOccurred);
    }

    //Mapper
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userNamesInConversation", userNamesInConversation);
        result.put("userNamesHaveHeardStory", userNamesHaveHeardStory);
        result.put("userNamesHaveNotHeardStory", userNamesHaveNotHeardStory);
        result.put("nextUserNameToTell", nextUserNameToTell);
        result.put("lastUserNameToTell", lastUserNameToTell);
        result.put("fbStorageFilePathToRecording", fbStorageFilePathToRecording);
        result.put("proposedPrompt1", proposedPrompt1);
        result.put("proposedPrompt2", proposedPrompt2);
        result.put("proposedPrompt3", proposedPrompt3);
        result.put("currentPrompt", currentPrompt);
        result.put("storyRecordingDuration", storyRecordingDuration);
        result.put("dateLastStoryRecorded", dateLastStoryRecorded);
        result.put("dateLastActionOccurredChanged", dateLastActionOccurred);

        return result;
    }
}
