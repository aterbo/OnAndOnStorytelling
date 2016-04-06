package com.aterbo.tellme.classes;

/**
 * Created by ATerbo on 3/24/16.
 */
public class ConvoLite {

    private String title;
    private String currentPrompt;

    public ConvoLite(){}


    public ConvoLite(String title, String currentPrompt) {
        this.title = title;
        this.currentPrompt = currentPrompt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrentPrompt() {
        return currentPrompt;
    }

    public void setCurrentPrompt(String currentPrompt) {
        this.currentPrompt = currentPrompt;
    }


}
