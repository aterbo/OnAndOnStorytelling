package com.aterbo.tellme.Utils;

/**
 * Created by ATerbo on 3/24/16.
 */
public class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where user lists are stored (ie "userLists")
     */
    public static final String FB_LOCATION = "https://tell-me.firebaseio.com";
    public static final String FB_LOCATION_USERS = "users";
    public static final String FB_LOCATION_RECORDINGS = "recordings";
    public static final String FB_LOCATION_USER_CONVOS = "userConvos";
    public static final String FB_LOCATION_CONVO_PARTICIPANTS = "convoParticipants";
    public static final String FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS = "numberOfPrompts";
    public static final String FB_LOCATION_PROMPTS = "prompts";
    public static final String FB_LOCATION_UID_MAPPINGS = "uidMappings";

    public static final int NUMBER_OF_PROMPT_OPTIONS = 3;

    public static final String CONVERSATION_INTENT_KEY = "conversation";
    public static final String USER_NAME_INTENT_KEY = "currentUserName";
    public static final String CONVERSATION_PUSH_ID_INTENT_KEY = "conversationPushId";
    public static final String CURRENT_USER_NAME_KEY = "currentUser";
    public static final String SHARED_PREFS_FILE = "preferences";

}
