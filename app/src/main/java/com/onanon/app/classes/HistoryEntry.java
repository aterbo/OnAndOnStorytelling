package com.onanon.app.classes;

import com.google.firebase.database.Exclude;
import com.onanon.app.Utils.Utils;

import java.util.HashMap;

/**
 * Created by ATerbo on 10/11/16.
 */
public class HistoryEntry {

    private String storyTellerUserName;
    private Prompt promptRespondingTo;
    private long dateResponseRecorded;

    public HistoryEntry() {}

    public HistoryEntry(String storyTellerUserName, Prompt promptRespondingTo) {
        this.storyTellerUserName = storyTellerUserName;
        this.promptRespondingTo = promptRespondingTo;
        this.dateResponseRecorded = Utils.getSystemTimeAsLong();
    }

    public String getStoryTellerUserName() {
        return storyTellerUserName;
    }

    public void setStoryTellerUserName(String storyTellerUserName) {
        this.storyTellerUserName = storyTellerUserName;
    }

    public Prompt getPromptRespondingTo() {
        return promptRespondingTo;
    }

    public void setPromptRespondingTo(Prompt promptRespondingTo) {
        this.promptRespondingTo = promptRespondingTo;
    }

    public long getDateResponseRecorded() {
        return dateResponseRecorded;
    }

    public void setDateResponseRecorded(long dateResponseRecorded) {
        this.dateResponseRecorded = dateResponseRecorded;
    }

    //Mapper
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("storyTellerUserName", storyTellerUserName);
        result.put("promptRespondingTo", promptRespondingTo);
        result.put("dateResponseRecorded", dateResponseRecorded);

        return result;
    }
}
