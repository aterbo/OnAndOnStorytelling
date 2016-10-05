package com.onanon.app.classes;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by ATerbo on 10/5/16.
 */
public class Response {

    public Response() { }

    public Response(String originalTellerUserName, String responderUserName, String response,
                    String originalConversationPushId, Prompt promptRespondingTo,
                    long dateResponseSubmitted, long typeOfResponse) {
        this.originalTellerUserName = originalTellerUserName;
        this.responderUserName = responderUserName;
        this.response = response;
        this.originalConversationPushId = originalConversationPushId;
        this.promptRespondingTo = promptRespondingTo;
        this.dateResponseSubmitted = dateResponseSubmitted;
        this.typeOfResponse = typeOfResponse;
    }

    private String originalTellerUserName;
    private String responderUserName;
    private String response;
    private String originalConversationPushId;
    private Prompt promptRespondingTo;
    private long dateResponseSubmitted;
    private long typeOfResponse;

    public static long TEXT_RESPONSE = 0;
    public static long AUDIO_RESPONSE = 1;
    public static long VIDEO_RESPONSE = 2;

    public String getOriginalTellerUserName() {
        return originalTellerUserName;
    }

    public void setOriginalTellerUserName(String originalTellerUserName) {
        this.originalTellerUserName = originalTellerUserName;
    }

    public String getResponderUserName() {
        return responderUserName;
    }

    public void setResponderUserName(String responderUserName) {
        this.responderUserName = responderUserName;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOriginalConversationPushId() {
        return originalConversationPushId;
    }

    public void setOriginalConversationPushId(String originalConversationPushId) {
        this.originalConversationPushId = originalConversationPushId;
    }

    public Prompt getPromptRespondingTo() {
        return promptRespondingTo;
    }

    public void setPromptRespondingTo(Prompt promptRespondingTo) {
        this.promptRespondingTo = promptRespondingTo;
    }

    public long getDateResponseSubmitted() {
        return dateResponseSubmitted;
    }

    public void setDateResponseSubmitted(long dateResponseSubmitted) {
        this.dateResponseSubmitted = dateResponseSubmitted;
    }

    public long getTypeOfResponse() {
        return typeOfResponse;
    }

    public void setTypeOfResponse(long typeOfResponse) {
        this.typeOfResponse = typeOfResponse;
    }

    //Mapper
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("originalTellerUserName", originalTellerUserName);
        result.put("responderUserName", responderUserName);
        result.put("response", response);
        result.put("originalConversationPushId", originalConversationPushId);
        result.put("promptRespondingTo", promptRespondingTo);
        result.put("dateResponseSubmitted", dateResponseSubmitted);
        result.put("typeOfResponse", typeOfResponse);

        return result;
    }
}
