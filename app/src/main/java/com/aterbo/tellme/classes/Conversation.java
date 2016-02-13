package com.aterbo.tellme.classes;

/**
 * Created by ATerbo on 2/12/16.
 */
public class Conversation {

    private String title;
    private String participant;
    private String timeSinceLastAction;
    private String storyDuration;


    public Conversation() {    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
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
}
