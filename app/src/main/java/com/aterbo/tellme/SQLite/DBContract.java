package com.aterbo.tellme.SQLite;

import android.provider.BaseColumns;

import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.User;

import java.util.ArrayList;

/**
 * Created by ATerbo on 3/11/16.
 */
public class DBContract {
    //Variables for table create
    public static final String ID_TYPE = " INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";
    public static final String COMMA_SEP = ", ";

    public void DBContract() {
    }

    //Data for meal table
    public class ConversationDBTable implements BaseColumns {

        //Table definitions
        public static final String CONVO_TABLE = "myconversations";

        //Column names for Conversation Table
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TIME_SINCE_LAST_ACTION = "timesince";
        public static final String COLUMN_STORY_DURATION = "duration";
        public static final String COLUMN_STATUS_FLAG = "statusflag";
        public static final String COLUMN_CURRENT_PROMPT = "currentprompt";
        public static final String COLUMN_CURRENT_PROMPT_TAG = "currentprompttag";
        public static final String COLUMN_NAME_1 = "name1";
        public static final String COLUMN_USER_NAME_1 = "username1";
        public static final String COLUMN_NAME_2 = "name2";
        public static final String COLUMN_USER_NAME_2 = "username2";
        public static final String COLUMN_NAME_3= "name3";
        public static final String COLUMN_USER_NAME_3 = "username3";
        public static final String COLUMN_NAME_4 = "name4";
        public static final String COLUMN_USER_NAME_4 = "username4";
        public static final String COLUMN_NAME_5 = "name5";
        public static final String COLUMN_USER_NAME_5 = "username5";


        //Create Table String
        public static final String CREATE_CONVO_TABLE = "CREATE TABLE " +
                CONVO_TABLE + " (" +
                _ID + ID_TYPE + COMMA_SEP +
                COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                COLUMN_TIME_SINCE_LAST_ACTION + TEXT_TYPE + COMMA_SEP +
                COLUMN_STORY_DURATION + TEXT_TYPE + COMMA_SEP +
                COLUMN_STATUS_FLAG + INT_TYPE + COMMA_SEP +
                COLUMN_CURRENT_PROMPT + TEXT_TYPE + COMMA_SEP +
                COLUMN_CURRENT_PROMPT_TAG + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_1 + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME_1 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME_2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_3 + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME_3 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME_4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME_5 + TEXT_TYPE + " )";
    }

    //Conversation Table
    //PUT VARIABLES FOR ALL TABLE COLUMNS IN {}
    public static final String[] CONVO_COLUMNS = {
            ConversationDBTable._ID,
            ConversationDBTable.COLUMN_TITLE,
            ConversationDBTable.COLUMN_TIME_SINCE_LAST_ACTION,
            ConversationDBTable.COLUMN_STORY_DURATION,
            ConversationDBTable.COLUMN_STATUS_FLAG,
            ConversationDBTable.COLUMN_CURRENT_PROMPT,
            ConversationDBTable.COLUMN_CURRENT_PROMPT_TAG,
            ConversationDBTable.COLUMN_NAME_1,
            ConversationDBTable.COLUMN_USER_NAME_1,
            ConversationDBTable.COLUMN_NAME_2,
            ConversationDBTable.COLUMN_USER_NAME_2,
            ConversationDBTable.COLUMN_NAME_3,
            ConversationDBTable.COLUMN_USER_NAME_3,
            ConversationDBTable.COLUMN_NAME_4,
            ConversationDBTable.COLUMN_USER_NAME_4,
            ConversationDBTable.COLUMN_NAME_5,
            ConversationDBTable.COLUMN_USER_NAME_5};

    /*
    //Photo Table data
    public class UserDBTable implements BaseColumns {
        //Table definitions
        public static final String USER_TABLE = "users";

        //Column names for photo Table
        public static final String COLUMN_CONVERSATION_ID = "convoid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_USER_NAME = "username";

        //Create Table String -- including Foreign Key reference to table
        public static final String CREATE_USER_TABLE = "CREATE TABLE " +
                USER_TABLE + " (" +
                _ID + ID_TYPE + COMMA_SEP +
                COLUMN_CONVERSATION_ID + INT_TYPE + COMMA_SEP +
                COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_USER_NAME + TEXT_TYPE + COMMA_SEP +
                "FOREIGN KEY(" + COLUMN_CONVERSATION_ID + ") REFERENCES " +
                ConversationDBTable.CONVO_TABLE + "(" + ConversationDBTable._ID + ") )";
    }

    //User Table
    //PUT VARIABLES FOR ALL TABLE COLUMNS IN {}
    public static final String[] USER_COLUMNS = {
            UserDBTable._ID,
            UserDBTable.COLUMN_CONVERSATION_ID,
            UserDBTable.COLUMN_NAME,
            UserDBTable.COLUMN_USER_NAME};

    public static final String RAW_QUERY_STRING = "SELECT * FROM " + ConversationDBTable.CONVO_TABLE
            + " INNER JOIN " + UserDBTable.USER_TABLE + " ON " +
            ConversationDBTable.CONVO_TABLE + "." + ConversationDBTable._ID + " = " +
            UserDBTable.USER_TABLE + "." + UserDBTable.COLUMN_CONVERSATION_ID;
    */
}

