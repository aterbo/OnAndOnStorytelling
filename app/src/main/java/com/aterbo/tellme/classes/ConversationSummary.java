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
    private ArrayList<Integer> proposedPromptsIdNumber;
    private int currentPromptID;

    public ConversationSummary() {
    }

    public ConversationSummary(ArrayList<String> usersInConversationEmails, String nextPlayersEmail,
                               int statusFlag, int currentPrompt, ArrayList<Integer> proposedPromptsIdNumber){
        this.usersInConversationEmails = usersInConversationEmails;
        this.nextPlayersEmail = nextPlayersEmail;
        this.statusFlag = statusFlag;
        this.currentPromptID = currentPrompt;
        this.proposedPromptsIdNumber = proposedPromptsIdNumber;
        setTitleBasedOnStatus();
    }

    public int getStatusFlag() {
        return statusFlag;
    }

    private void setTitleBasedOnStatus(){
        if (statusFlag == STATUS_TO_TELL){
            title = pullProposedPromptsTagString();
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

    public int getNumberOfUsers(){
        return usersInConversationEmails.size();
    }

    public String pullUserEmailsAsString(){
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

    public int getStatus(){
        return statusFlag;
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

    public void setToProposedPrompts(int prompt){
        proposedPromptsIdNumber.add(prompt);
    }

    public ArrayList<Integer> getProposedPromptsIdNumber(){
        return proposedPromptsIdNumber;
    }

    public void setProposedPromptsIdNumber(ArrayList<Integer> proposedPromptsIdNumber){
        this.proposedPromptsIdNumber = proposedPromptsIdNumber;
    }

    public int getProposedPromptIdByIndex(int promptIndex){
        return proposedPromptsIdNumber.get(promptIndex);
    }

    public void clearProposedPrompts(){
        proposedPromptsIdNumber.clear();
    }

    public boolean hasProposedPrompts(){
        return !proposedPromptsIdNumber.isEmpty();
    }

    public String pullProposedPromptsTagString(){
        return proposedPromptsIdNumber.get(0) + ", " +
                proposedPromptsIdNumber.get(1)+ ", or " +
                proposedPromptsIdNumber.get(2);
    }
}
