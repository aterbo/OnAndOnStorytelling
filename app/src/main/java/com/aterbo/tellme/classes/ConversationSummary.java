package com.aterbo.tellme.classes;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class ConversationSummary{

    private static final int STATUS_TO_TELL = 0;
    private static final int STATUS_TO_HEAR = 1;
    private static final int STATUS_WAITING = 2;

    private String title;
    private int statusFlag;
    private ArrayList<String> usersInConversationEmails;
    private String nextPlayersEmail;
    private Prompt proposedPrompt1;
    private Prompt proposedPrompt2;
    private Prompt proposedPrompt3;
    private int currentPromptID;

    public ConversationSummary() { }

    public ConversationSummary(ArrayList<String> usersInConversationEmails, String nextPlayersEmail,
                               int statusFlag, int currentPrompt, ArrayList<Prompt> proposedPrompts){
        this.usersInConversationEmails = usersInConversationEmails;
        this.nextPlayersEmail = nextPlayersEmail;
        this.statusFlag = statusFlag;
        this.currentPromptID = currentPrompt;
        this.proposedPrompt1 = proposedPrompts.get(0);
        this.proposedPrompt2 = proposedPrompts.get(1);
        this.proposedPrompt3 = proposedPrompts.get(2);
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
            title = Integer.toString(getCurrentPromptID());
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

    public int getCurrentPromptID(){
        return currentPromptID;
    }

    public void setCurrentPromptID(int currentPromptID){
        this.currentPromptID = currentPromptID;
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
}
