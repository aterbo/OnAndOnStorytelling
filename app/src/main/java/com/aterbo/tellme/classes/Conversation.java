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
    private ArrayList<User> usersInConversation;
    private int statusFlag;

    private ArrayList<Prompt> promptsAnswered;
    private Prompt currentPrompt;


    public Conversation() {    }
    public Conversation(String title, String timeSinceLastAction, String storyDuration,
                        ArrayList<User> usersInConversation, int statusFlag){
        this.title = title;
        this.timeSinceLastAction = timeSinceLastAction;
        this.storyDuration = storyDuration;
        this.usersInConversation = usersInConversation;
        this.statusFlag = statusFlag;
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

    public String getUsersNameAsString(){
        String userNames = "";
        for (User user : usersInConversation){
            userNames = userNames + user.getName() + ", ";
        }

        if (userNames.endsWith(", ")) {
            userNames = userNames.substring(0, userNames.length() - 2);
        }

        return userNames;
    }

    public int getStatus(){
        return statusFlag;
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


    //Parcelabler
    protected Conversation(Parcel in) {
        title = in.readString();
        timeSinceLastAction = in.readString();
        storyDuration = in.readString();
        if (in.readByte() == 0x01) {
            usersInConversation = new ArrayList<User>();
            in.readList(usersInConversation, User.class.getClassLoader());
        } else {
            usersInConversation = null;
        }
        statusFlag = in.readInt();
        if (in.readByte() == 0x01) {
            promptsAnswered = new ArrayList<Prompt>();
            in.readList(promptsAnswered, Prompt.class.getClassLoader());
        } else {
            promptsAnswered = null;
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
        if (usersInConversation == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(usersInConversation);
        }
        dest.writeInt(statusFlag);
        if (promptsAnswered == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(promptsAnswered);
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
