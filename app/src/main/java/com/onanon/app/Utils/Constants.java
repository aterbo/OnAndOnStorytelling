package com.onanon.app.Utils;

/**
 * Created by ATerbo on 3/24/16.
 */
public class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where user lists are stored (ie "userLists")
     */

    public static final String FB_LOCATION_USERS = "users";
    public static final String FB_LOCATION_USER_CONVOS = "userConvos";
    public static final String FB_LOCATION_CONVO_PARTICIPANTS = "convoParticipants";
    public static final String FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS = "numberOfPrompts";
    public static final String FB_LOCATION_PROMPTS = "prompts";
    public static final String FB_LOCATION_UID_MAPPINGS = "uidMappings";

    public static final int NUMBER_OF_PROMPT_OPTIONS = 3;

    public static final String CONVERSATION_INTENT_KEY = "conversation";
    public static final String USER_NAME_INTENT_KEY = "currentUserName";
    public static final String INITIATING_ACTIVITY_INTENT_KEY = "initiatingActivity";
    public static final String CONVERSATION_PUSH_ID_INTENT_KEY = "conversationPushId";

    /**
     * Constants for skipping media players forward and backward.
     */
    public static final int SPLASH_SCREEN = 0;
    public static final int CONVO_LIST = 1;

    public static final int shortSkipTime = 5000;
    public static  final int longSkipTime = 30000;

    /**
     * Keys for determining player status
     */

    public static final int USER_TURN_TO_TELL = 10;
    public static final int USER_TURN_TO_SEND_PROMPTS = 20;
    public static final int USER_TURN_TO_HEAR = 30;
    public static final int USER_WAITING_FOR_PROMPTS = 40;
    public static final int USER_WAITING_FOR_STORY = 50;
    public static final int USER_WAITING_FOR_OTHERS = 60;
    public static final int INCORRECT_RESULT = 70;
}
