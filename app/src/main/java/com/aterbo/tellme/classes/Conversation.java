package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation implements Parcelable {

    private static int STATUS_TO_TELL = 0;
    private static int STATUS_TO_HEAR = 1;
    private static int STATUS_WAITING = 2;

    private String title;
    private String timeSinceLastAction;
    private String storyDuration;
    private String storyFilePath;
    private int statusFlag;
    private int sqlIdNumber;

    private ArrayList<User> usersInConversation;
    private ArrayList<Prompt> proposedPrompts;
    private Prompt currentPrompt;


    public Conversation() {
        usersInConversation = new ArrayList<>();
        proposedPrompts = new ArrayList<>();
    }

    public Conversation(String title, String timeSinceLastAction, String storyDuration, String storyFilePath,
                        ArrayList<User> usersInConversation, int statusFlag, Prompt currentPrompt,
                        ArrayList<Prompt> proposedPrompts){
        this.title = title;
        this.timeSinceLastAction = timeSinceLastAction;
        this.storyDuration = storyDuration;
        this.storyFilePath = storyFilePath;
        this.usersInConversation = usersInConversation;
        this.statusFlag = statusFlag;
        this.currentPrompt = currentPrompt;
        this.proposedPrompts = proposedPrompts;
    }

    public Conversation(String title, String timeSinceLastAction, String storyDuration,
                        ArrayList<User> usersInConversation, int statusFlag, Prompt currentPrompt,
                        ArrayList<Prompt> proposedPrompts, int sqlIdNumber, String storyFilePath){
        this.title = title;
        this.timeSinceLastAction = timeSinceLastAction;
        this.storyDuration = storyDuration;
        this.usersInConversation = usersInConversation;
        this.statusFlag = statusFlag;
        this.currentPrompt = currentPrompt;
        this.proposedPrompts = proposedPrompts;
        this.sqlIdNumber = sqlIdNumber;
        this.storyFilePath = storyFilePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimeSinceLastAction() {
        return timeSinceLastAction;
    }

    public void setTimeSinceLastAction(String timeSinceLastAction) {
        this.timeSinceLastAction = timeSinceLastAction;
    }

    public String getStoryDuration() {
        return storyDuration;
    }

    public void setStoryDuration(String storyDuration) {
        this.storyDuration = storyDuration;
    }

    public ArrayList<User> getUsersInConversation() {
        return usersInConversation;
    }

    public void setUsersInConversation(ArrayList<User> usersInConversation) {
        this.usersInConversation = usersInConversation;
    }

    public User getUser(int userIndex){
        return usersInConversation.get(userIndex);
    }

    public void addUserToConversation(User user){
        usersInConversation.add(user);
    }

    public int getNumberOfUsers(){
        return usersInConversation.size();
    }

    public String getUsersNameAsString(){
        String userNames = "";
        for (User user : usersInConversation){
            userNames = userNames + user.getUserName() + ", ";
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

    public int getStatus(){
        return statusFlag;
    }

    public void setStatus(int statusFlag){
        this.statusFlag = statusFlag;
    }



    public void setStatusToTell(){
        statusFlag = STATUS_TO_TELL;
    }

    public void setStatusToHear(){
        statusFlag = STATUS_TO_HEAR;
    }

    public void setStatusToWaiting(){
        statusFlag = STATUS_WAITING;
    }


    public int getSqlIdNumber() {
        return sqlIdNumber;
    }

    public void setSqlIdNumber(int sqlIdNumber) {
        this.sqlIdNumber = sqlIdNumber;
    }

    public void setToProposedPrompts(Prompt prompt){
        proposedPrompts.add(prompt);
    }

    public void setProposedPrompts(ArrayList<Prompt> proposedPrompts){
        this.proposedPrompts = proposedPrompts;
    }

    public ArrayList<Prompt> getProposedPrompts(){
        return proposedPrompts;
    }

    public Prompt getProposedPromptByIndex(int promptIndex){
        return proposedPrompts.get(promptIndex);
    }

    public void clearProposedPrompts(){
        proposedPrompts.clear();
    }

    public boolean hasProposedPrompts(){
        return !proposedPrompts.isEmpty();
    }

    public String getProposedPromptsTagString(){
        return proposedPrompts.get(0).getTag() + ", " +
                proposedPrompts.get(1).getTag() + ", or " +
                proposedPrompts.get(2).getTag();
    }

    public String getStoryFilePath() {
        return storyFilePath;
    }

    public void setStoryFilePath(String storyFilePath) {
        this.storyFilePath = storyFilePath;
    }

    //Parcelabler
    protected Conversation(Parcel in) {
        title = in.readString();
        timeSinceLastAction = in.readString();
        storyDuration = in.readString();
        storyFilePath = in.readString();
        if (in.readByte() == 0x01) {
            usersInConversation = new ArrayList<User>();
            in.readList(usersInConversation, User.class.getClassLoader());
        } else {
            usersInConversation = null;
        }
        statusFlag = in.readInt();
        sqlIdNumber = in.readInt();
        if (in.readByte() == 0x01) {
            proposedPrompts = new ArrayList<Prompt>();
            in.readList(proposedPrompts, Prompt.class.getClassLoader());
        } else {
            proposedPrompts = null;
        }
        currentPrompt = (Prompt) in.readValue(Prompt.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(timeSinceLastAction);
        dest.writeString(storyDuration);
        dest.writeString(storyFilePath);
        if (usersInConversation == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(usersInConversation);
        }
        dest.writeInt(statusFlag);
        dest.writeInt(sqlIdNumber);
        if (proposedPrompts == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(proposedPrompts);
        }
        dest.writeValue(currentPrompt);
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
