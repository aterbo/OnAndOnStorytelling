package com.aterbo.tellme.SQLite;

/**
 * Created by ATerbo on 3/11/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;

import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version control
    private static final int VERSION_LAUNCH = 1;

    private static final int DATABASE_VERSION = VERSION_LAUNCH;

    // Database Name
    private static final String DATABASE_NAME = "conversationList";
    private static final String NO_USER = "XXXXX";
    private static final String NO_PROPOSED_PROMPT = "XXXX";

    //Constructor code
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Enable foreign keys
    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Create database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.ConversationDBTable.CREATE_CONVO_TABLE);
    }

    //upgrade database on version change(currently drops table)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // NOTE: This switch statement is designed to handle cascading database
        // updates, starting at the current version and falling through to all
        // future upgrade cases. Only use "break;" when you want to drop and
        // recreate the entire database.
        int version = oldVersion;

        switch (version) {
            case VERSION_LAUNCH:
        }

        Log.d(LOG, "after upgrade logic, at version " + version);
        if (version != DATABASE_VERSION) {
            Log.w(LOG, "Error with upgrade");
        }
    }


    //CRUD - Create operations
    public int addConversation(Conversation conversation) {
        // get reference of the MealDB database
        long newId;
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted in meal table
        ContentValues values = new ContentValues();
        values.put(DBContract.ConversationDBTable.COLUMN_TITLE, conversation.getTitle());
        values.put(DBContract.ConversationDBTable.COLUMN_TIME_SINCE_LAST_ACTION,  conversation.getTimeSinceLastAction());
        values.put(DBContract.ConversationDBTable.COLUMN_STORY_DURATION,  conversation.getStoryDuration());
        values.put(DBContract.ConversationDBTable.COLUMN_STORY_FILE_PATH,  conversation.getStoryFilePath());
        values.put(DBContract.ConversationDBTable.COLUMN_STATUS_FLAG, conversation.getStatus());
        values.put(DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT, conversation.getCurrentPrompt().getPromptText());
        values.put(DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT_TAG, conversation.getCurrentPrompt().getTagText());

        if(conversation.hasProposedPrompts()){
            Prompt prompt = conversation.getProposedPromptByIndex(0);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_1, prompt.getTagText());
            prompt = conversation.getProposedPromptByIndex(1);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_2, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_2, prompt.getTagText());
            prompt = conversation.getProposedPromptByIndex(2);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_3, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_3, prompt.getTagText());
        } else {
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_1, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_2, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_2, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_3, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_3, NO_PROPOSED_PROMPT);
        }

        int numberOfUsers = conversation.getNumberOfUsers();
        if(numberOfUsers>0) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, conversation.getUser(0).getName());
        } else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, NO_USER);
        }
        if(numberOfUsers>1) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, conversation.getUser(1).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, NO_USER);
        }
        if(numberOfUsers>2) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, conversation.getUser(2).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, NO_USER);
        }
        if(numberOfUsers>3) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, conversation.getUser(3).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, NO_USER);
        }
        if(numberOfUsers>4) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, conversation.getUser(4).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, NO_USER);
        }

        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_1, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_2, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_3, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_4, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_5, NO_USER);

        // insert meal
        newId = db.insert(DBContract.ConversationDBTable.CONVO_TABLE, null, values);

        // close database transaction
        db.close();

        return (int) newId;
    }

    public ArrayList<Conversation> getConversationList() {
        ArrayList<Conversation> conversations = new ArrayList();

        String query = "SELECT  * FROM " + DBContract.ConversationDBTable.CONVO_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // if results !=null, parse the first one
        if (cursor.moveToFirst()) {
            do {
                Conversation conversation = new Conversation();
                conversation.setSqlIdNumber(cursor.getInt(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable._ID)));
                conversation.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_TITLE)));
                conversation.setTimeSinceLastAction(cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_TIME_SINCE_LAST_ACTION)));
                conversation.setStoryDuration(cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_STORY_DURATION)));
                conversation.setStoryFilePath(cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_STORY_FILE_PATH)));
                conversation.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_STATUS_FLAG)));
                conversation.setSqlIdNumber(cursor.getInt((cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable._ID))));

                String promptText = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT));
                String promptTag = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT_TAG));
                conversation.setCurrentPrompt(new Prompt(promptText, promptTag));

                //get Proposed Prompts
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1)).equals(NO_PROPOSED_PROMPT)) {
                    conversation.setToProposedPrompts(new Prompt(
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1)),
                            cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_1))));
                    conversation.setToProposedPrompts(new Prompt(
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_2)),
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_2))));
                    conversation.setToProposedPrompts(new Prompt(
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_3)),
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_3))));
                }
                //Pull all matching users
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_1)).equals(NO_USER)) {
                    conversation.addUserToConversation(
                            new User(cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_NAME_1))));
                }
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_2)).equals(NO_USER)){
                    conversation.addUserToConversation(new User(cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_NAME_2))));
                }
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_3)).equals(NO_USER)){
                    conversation.addUserToConversation(new User(cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_NAME_3))));
                }
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_4)).equals(NO_USER)){
                    conversation.addUserToConversation(new User(cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_NAME_4))));
                }
                if(!cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_5)).equals(NO_USER)){
                    conversation.addUserToConversation(new User(cursor.getString(cursor.getColumnIndexOrThrow(
                            DBContract.ConversationDBTable.COLUMN_NAME_5))));
                }
                conversations.add(conversation);
            } while (cursor.moveToNext());

            cursor.close();
            db.close();
        }
        return conversations;
    }

    public int updateConversation(Conversation conversation) {

        // get reference of the MealDB database
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(DBContract.ConversationDBTable.COLUMN_TITLE, conversation.getTitle());
        values.put(DBContract.ConversationDBTable.COLUMN_TIME_SINCE_LAST_ACTION, conversation.getTimeSinceLastAction());
        values.put(DBContract.ConversationDBTable.COLUMN_STORY_DURATION, conversation.getStoryDuration());
        values.put(DBContract.ConversationDBTable.COLUMN_STORY_FILE_PATH, conversation.getStoryFilePath());
        values.put(DBContract.ConversationDBTable.COLUMN_STATUS_FLAG, conversation.getStatus());
        values.put(DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT, conversation.getCurrentPrompt().getPromptText());
        values.put(DBContract.ConversationDBTable.COLUMN_CURRENT_PROMPT_TAG, conversation.getCurrentPrompt().getTagText());


        if(conversation.hasProposedPrompts()){
            Prompt prompt = conversation.getProposedPromptByIndex(0);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_1, prompt.getTagText());
            prompt = conversation.getProposedPromptByIndex(1);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_2, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_2, prompt.getTagText());
            prompt = conversation.getProposedPromptByIndex(2);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_3, prompt.getPromptText());
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_3, prompt.getTagText());
        } else {
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_1, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_1, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_2, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_2, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_3, NO_PROPOSED_PROMPT);
            values.put(DBContract.ConversationDBTable.COLUMN_PROPOSED_PROMPT_TAG_3, NO_PROPOSED_PROMPT);
        }

        int numberOfUsers = conversation.getNumberOfUsers();
        if(numberOfUsers>0) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, conversation.getUser(0).getName());
        } else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, NO_USER);
        }
        if(numberOfUsers>1) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, conversation.getUser(1).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, NO_USER);
        }
        if(numberOfUsers>2) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, conversation.getUser(2).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, NO_USER);
        }
        if(numberOfUsers>3) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, conversation.getUser(3).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, NO_USER);
        }
        if(numberOfUsers>4) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, conversation.getUser(4).getName());
        }else{
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, NO_USER);
        }

        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_1, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_2, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_3, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_4, NO_USER);
        values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_5, NO_USER);

        // update
        int i = db.update(DBContract.ConversationDBTable.CONVO_TABLE, values, DBContract.ConversationDBTable._ID +
                " = ?", new String[]{String.valueOf(conversation.getSqlIdNumber())});

        db.close();
        return i;
    }

    // Deleting single meal
    public void deleteConversation(Conversation conversation) {

        // get reference of the MealDB database
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DBContract.ConversationDBTable.CONVO_TABLE, DBContract.ConversationDBTable._ID
                + " = ?", new String[]{String.valueOf(conversation.getSqlIdNumber())});
        db.close();
    }

    public void addListOfConversations(ArrayList<Conversation> conversations){
        for (Conversation conversation : conversations) {
            addConversation(conversation);
        }
    }
}
