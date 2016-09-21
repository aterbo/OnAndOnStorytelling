package com.onanon.app.Utils;

/**
 * Created by ATerbo on 3/24/16.
 */
public class Constants {

    public static final int MAX_CONVO_PARTICIPANTS = 4;
    public static final int NUMBER_OF_PROMPT_OPTIONS = 3;
    public static final int SHORT_SKIP_TIME = 5000;
    public static final int LONG_SKIP_TIME = 30000;
    public static final String NO_PHOTO_KEY = "NO_PHOTO";
    public static final int REQ_CODE_PICK_IMAGE = 1;
    public static final int PROFILE_PIC_DIMENSIONS = 288;

    /**
     * Firebase Location Constants
     */
    public static final String FB_LOCATION_USERS = "users";
    public static final String FB_LOCATION_USER_CONVOS = "userConvos";
    public static final String FB_LOCATION_CONVO_PARTICIPANTS = "convoParticipants";
    public static final String FB_LOCATION_TOTAL_NUMBER_OF_PROMPTS = "numberOfPrompts";
    public static final String FB_LOCATION_PROMPTS = "prompts";
    public static final String FB_LOCATION_UID_MAPPINGS = "uidMappings";
    public static final String FB_LOCATION_FCM_TOKENS = "fcmTokens";
    public static final String FB_LOCATION_USER_NAME_KEY_LIST = "userNamesKeyList";
    public static final String FB_COUNTER_RECORDING = "totalNumberOfRecordings";
    public static final String FB_COUNTER_CONVERSATIONS_STARTED = "totalNumberOfConversationsStarted";
    public static final String FB_COUNTER_RECORDINGS_HEARD = "totalNumberOfRecordingsHeard";

    /**
     * Intent Key and Activity Constants
     */
    public static final String CONVERSATION_INTENT_KEY = "conversation";
    public static final String USER_NAME_INTENT_KEY = "currentUserName";
    public static final String INITIATING_ACTIVITY_INTENT_KEY = "initiatingActivity";
    public static final String CONVERSATION_PUSH_ID_INTENT_KEY = "conversationPushId";
    public static final int SPLASH_SCREEN = 0;
    public static final int CONVO_LIST = 1;

    /**
     * Player and Convo Status Keys
     */
    public static final int USER_TURN_TO_TELL = 10;
    public static final int USER_TURN_TO_SEND_PROMPTS = 20;
    public static final int USER_TURN_TO_HEAR = 30;
    public static final int USER_WAITING_FOR_PROMPTS = 40;
    public static final int USER_WAITING_FOR_STORY = 50;
    public static final int USER_WAITING_FOR_OTHERS = 60;
    public static final int INCORRECT_RESULT = 70;
}
