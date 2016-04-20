package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation implements Parcelable{

    private ArrayList<String> usersInConversationEmails;
    private String nextPlayersEmail;
    private String lastPlayersEmail;
    private String storyRecordingPushId;
    private Prompt proposedPrompt1;
    private Prompt proposedPrompt2;
    private Prompt proposedPrompt3;
    private Prompt currentPrompt;
    private long storyRecordingDuration;

    public Conversation() { }

    public Conversation(ArrayList<String> usersInConversationEmails, String nextPlayersEmail,
                        String lastPlayersEmail, Prompt currentPrompt, ArrayList<Prompt> proposedPrompts){
        this.usersInConversationEmails = usersInConversationEmails;
        this.nextPlayersEmail = nextPlayersEmail;
        this.lastPlayersEmail = lastPlayersEmail;
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

    public ArrayList<String> getUsersInConversationEmails() {
        return usersInConversationEmails;
    }

    public void setUsersInConversationEmails(ArrayList<String> usersInConversationEmails) {
        this.usersInConversationEmails = usersInConversationEmails;
    }

    public String getNextPlayersEmail() {
        return nextPlayersEmail;
    }

    public void setNextPlayersEmail(String nextPlayersEmail) {
        this.nextPlayersEmail = nextPlayersEmail;
    }

    public String getLastPlayersEmail() {
        return lastPlayersEmail;
    }

    public void setLastPlayersEmail(String lastPlayersEmail){
        this.lastPlayersEmail = lastPlayersEmail;
    }

    public String getUserEmails(int userIndex){
        return usersInConversationEmails.get(userIndex);
    }

    public void addUserEmailToConversation(String userEmail){
        usersInConversationEmails.add(userEmail);
    }

    public String userEmailsAsString(){
        String userEmails = "";
        for (String email : usersInConversationEmails){
            userEmails = userEmails + email.replace(".",",") + ", ";
        }

        if (userEmails.endsWith(", ")) {
            userEmails = userEmails.substring(0, userEmails.length() - 2);
        }

        return userEmails;
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
        String holderEmail;

        do {
            holderEmail = usersInConversationEmails.get(counter);
            counter = counter + 1;
        } while(holderEmail.equals(nextPlayersEmail));
        nextPlayersEmail = holderEmail;
    }

    public void makeLastPlayerCurrentNextPlayer(){
        lastPlayersEmail = nextPlayersEmail;
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
            usersInConversationEmails = new ArrayList<String>();
            in.readList(usersInConversationEmails, String.class.getClassLoader());
        } else {
            usersInConversationEmails = null;
        }
        nextPlayersEmail = in.readString();
        lastPlayersEmail = in.readString();
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
        if (usersInConversationEmails == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(usersInConversationEmails);
        }
        dest.writeString(nextPlayersEmail);
        dest.writeString(lastPlayersEmail);
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
