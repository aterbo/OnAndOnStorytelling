package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class ConversationSummary implements Parcelable{

    private static final int STATUS_TO_TELL = 0;
    private static final int STATUS_TO_HEAR = 1;
    private static final int STATUS_WAITING = 2;

    private String title;
    private int statusFlag;
    private ArrayList<String> usersInConversationEmails;
    private String nextPlayersEmail;
    private String storyRecordingFilePath;
    private Prompt proposedPrompt1;
    private Prompt proposedPrompt2;
    private Prompt proposedPrompt3;
    private Prompt currentPrompt;

    public ConversationSummary() { }

    public ConversationSummary(ArrayList<String> usersInConversationEmails, String nextPlayersEmail,
                               int statusFlag, Prompt currentPrompt, ArrayList<Prompt> proposedPrompts){
        this.usersInConversationEmails = usersInConversationEmails;
        this.nextPlayersEmail = nextPlayersEmail;
        this.statusFlag = statusFlag;
        this.currentPrompt = currentPrompt;
        this.proposedPrompt1 = proposedPrompts.get(0);
        this.proposedPrompt2 = proposedPrompts.get(1);
        this.proposedPrompt3 = proposedPrompts.get(2);
        storyRecordingFilePath = "none";
        setTitleBasedOnStatus();
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

    public int getStatusFlag() {
        return statusFlag;
    }

    private void setTitleBasedOnStatus(){
        if (statusFlag == STATUS_TO_TELL){
            title = proposedPromptsTagAsString();
        } else if (statusFlag == STATUS_TO_HEAR){
            title = currentPrompt.getText();
        }  else if (statusFlag == STATUS_WAITING){
            title = "I'm WAAAIII-TING.";
        }
    }

    public String getTitle() {
        return title;
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

    public String getStoryRecordingFilePath(){
        return storyRecordingFilePath;
    }

    public void setStoryRecordingFilePath(String storyRecordingFilePath){
        this.storyRecordingFilePath = storyRecordingFilePath;
    }

    public void setStatus(int statusFlag){
        this.statusFlag = statusFlag;
        setTitleBasedOnStatus();
    }



    public void setStatusToTell(){
        statusFlag = STATUS_TO_TELL;
        setTitleBasedOnStatus();
    }

    public void setStatusToHear(){
        statusFlag = STATUS_TO_HEAR;
        setTitleBasedOnStatus();
    }

    public void setStatusToWaiting(){
        statusFlag = STATUS_WAITING;
        setTitleBasedOnStatus();
    }


    public String proposedPromptsTagAsString(){
        return proposedPrompt1.getTag() + ", " +
                proposedPrompt2.getTag() + ", or " +
                proposedPrompt3.getTag();
    }

    public void changeNextPlayer(){
        int counter = 0;
        String holderEmail;

        do {
            holderEmail = usersInConversationEmails.get(counter);
            counter = counter + 1;
        } while(holderEmail.equals(nextPlayersEmail));
        nextPlayersEmail = holderEmail;
    }

    public void clearProposedTopics(){
        proposedPrompt1 = null;
        proposedPrompt2 = null;
        proposedPrompt3 = null;
    }

    //Parcelabler.com
    protected ConversationSummary(Parcel in) {
        title = in.readString();
        statusFlag = in.readInt();
        if (in.readByte() == 0x01) {
            usersInConversationEmails = new ArrayList<String>();
            in.readList(usersInConversationEmails, String.class.getClassLoader());
        } else {
            usersInConversationEmails = null;
        }
        nextPlayersEmail = in.readString();
        storyRecordingFilePath = in.readString();
        proposedPrompt1 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt2 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        proposedPrompt3 = (Prompt) in.readValue(Prompt.class.getClassLoader());
        currentPrompt = (Prompt) in.readValue(Prompt.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(statusFlag);
        if (usersInConversationEmails == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(usersInConversationEmails);
        }
        dest.writeString(nextPlayersEmail);
        dest.writeString(storyRecordingFilePath);
        dest.writeValue(proposedPrompt1);
        dest.writeValue(proposedPrompt2);
        dest.writeValue(proposedPrompt3);
        dest.writeValue(currentPrompt);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ConversationSummary> CREATOR = new Parcelable.Creator<ConversationSummary>() {
        @Override
        public ConversationSummary createFromParcel(Parcel in) {
            return new ConversationSummary(in);
        }

        @Override
        public ConversationSummary[] newArray(int size) {
            return new ConversationSummary[size];
        }
    };
}
