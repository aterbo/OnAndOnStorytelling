package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation implements Parcelable{

    private ArrayList<String> userNamesInConversation;
    private String nextPlayersUserName;
    private String lastPlayersUserName;
    private String storyRecordingPushId;
    private Prompt proposedPrompt1;
    private Prompt proposedPrompt2;
    private Prompt proposedPrompt3;
    private Prompt currentPrompt;
    private long storyRecordingDuration;

    public Conversation() { }

    public Conversation(ArrayList<String> userNamesInConversation, String nextPlayersUserName,
                        String lastPlayersUserNames, Prompt currentPrompt, ArrayList<Prompt> proposedPrompts){
        this.userNamesInConversation = userNamesInConversation;
        this.nextPlayersUserName = nextPlayersUserName;
        this.lastPlayersUserName = lastPlayersUserNames;
        this.currentPrompt = currentPrompt;
        this.proposedPrompt1 = proposedPrompts.get(0);
        this.proposedPrompt2 = proposedPrompts.get(1);
        this.proposedPrompt3 = proposedPrompts.get(2);
        storyRecordingPushId = "none";
        storyRecordingDuration = 0;
    }

    public Prompt getProposedPrompt3() {
        return proposedPrompt3;
    }

    public void setProposedPrompt3(Prompt proposedPrompt3) {
        this.proposedPrompt3 = proposedPrompt3;
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

    public ArrayList<String> getUserNamesInConversation() {
        return userNamesInConversation;
    }

    public void setUserNamesInConversation(ArrayList<String> userNamesInConversation) {
        this.userNamesInConversation = userNamesInConversation;
    }

    public String getNextPlayersUserName() {
        return nextPlayersUserName;
    }

    public void setNextPlayersUserName(String nextPlayersUserName) {
        this.nextPlayersUserName = nextPlayersUserName;
    }

    public String getLastPlayersUserName() {
        return lastPlayersUserName;
    }

    public void setLastPlayersUserName(String lastPlayersUserName){
        this.lastPlayersUserName = lastPlayersUserName;
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

    public String getStoryRecordingPushId(){
        return storyRecordingPushId;
    }

    public void setStoryRecordingPushId(String storyRecordingPushId){
        this.storyRecordingPushId = storyRecordingPushId;
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

    public void changeNextPlayer(){
        makeLastPlayerCurrentNextPlayer();
        int counter = 0;
        String holderUserName;

        do {
            holderUserName = userNamesInConversation.get(counter);
            counter = counter + 1;
        } while(holderUserName.equals(nextPlayersUserName));
        nextPlayersUserName = holderUserName;
    }

    private void makeLastPlayerCurrentNextPlayer(){
        lastPlayersUserName = nextPlayersUserName;
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

    //Parcelabler.com
    protected Conversation(Parcel in) {
        if (in.readByte() == 0x01) {
            userNamesInConversation = new ArrayList<String>();
            in.readList(userNamesInConversation, String.class.getClassLoader());
        } else {
            userNamesInConversation = null;
        }
        nextPlayersUserName = in.readString();
        lastPlayersUserName = in.readString();
        storyRecordingPushId = in.readString();
        proposedPrompt1 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt2 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt3 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        currentPrompt = (Prompt) in.readValue(Prompt.class.getClassLoader());
        storyRecordingDuration = in.readLong();
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
        dest.writeString(nextPlayersUserName);
        dest.writeString(lastPlayersUserName);
        dest.writeString(storyRecordingPushId);
        dest.writeValue(proposedPrompt1);
        dest.writeValue(proposedPrompt2);
        dest.writeValue(proposedPrompt3);
        dest.writeValue(currentPrompt);
        dest.writeLong(storyRecordingDuration);
    }

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
}
