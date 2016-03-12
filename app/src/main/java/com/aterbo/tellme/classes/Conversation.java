package com.aterbo.tellme.classes;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation {

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
}
