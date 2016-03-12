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
        values.put(DBContract.ConversationDBTable.COLUMN_STATUS_FLAG, conversation.getStatus());

        int numberOfUsers = conversation.getNumberOfUsers();
        if(numberOfUsers>1) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, conversation.getUser(0).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_1, conversation.getUser(0).getUserName());
        }
        if(numberOfUsers>2) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, conversation.getUser(1).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_2, conversation.getUser(1).getUserName());
        }
        if(numberOfUsers>3) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, conversation.getUser(2).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_3, conversation.getUser(2).getUserName());
        }
        if(numberOfUsers>4) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, conversation.getUser(3).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_4, conversation.getUser(3).getUserName());
        }
        if(numberOfUsers>5) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, conversation.getUser(4).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_5, conversation.getUser(4).getUserName());
        }

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
                conversation.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_STATUS_FLAG)));

                //Pull all matching users
                String holderName = "";
                String holderUserName = "";
                holderName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_1));
                holderUserName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_USER_NAME_1));

                if(!holderName.isEmpty() || !holderUserName.isEmpty()){
                    User user = new User(holderName, holderUserName);
                    conversation.addUserToConversation(user);
                }
                holderName = "";
                holderUserName = "";
                holderName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_2));
                holderUserName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_USER_NAME_2));

                if(!holderName.isEmpty() || !holderUserName.isEmpty()){
                    User user = new User(holderName, holderUserName);
                    conversation.addUserToConversation(user);
                }
                holderName = "";
                holderUserName = "";
                holderName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_3));
                holderUserName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_USER_NAME_3));

                if(!holderName.isEmpty() || !holderUserName.isEmpty()){
                    User user = new User(holderName, holderUserName);
                    conversation.addUserToConversation(user);
                }
                holderName = "";
                holderUserName = "";
                holderName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_4));
                holderUserName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_USER_NAME_4));

                if(!holderName.isEmpty() || !holderUserName.isEmpty()){
                    User user = new User(holderName, holderUserName);
                    conversation.addUserToConversation(user);
                }
                holderName = "";
                holderUserName = "";
                holderName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_NAME_5));
                holderUserName = cursor.getString(cursor.getColumnIndexOrThrow(
                        DBContract.ConversationDBTable.COLUMN_USER_NAME_5));

                if(!holderName.isEmpty() || !holderUserName.isEmpty()){
                    User user = new User(holderName, holderUserName);
                    conversation.addUserToConversation(user);
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
        values.put(DBContract.ConversationDBTable.COLUMN_STATUS_FLAG, conversation.getStatus());

        int numberOfUsers = conversation.getNumberOfUsers();
        if(numberOfUsers>1) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_1, conversation.getUser(0).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_1, conversation.getUser(0).getUserName());
        }
        if(numberOfUsers>2) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_2, conversation.getUser(1).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_2, conversation.getUser(1).getUserName());
        }
        if(numberOfUsers>3) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_3, conversation.getUser(2).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_3, conversation.getUser(2).getUserName());
        }
        if(numberOfUsers>4) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_4, conversation.getUser(3).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_4, conversation.getUser(3).getUserName());
        }
        if(numberOfUsers>5) {
            values.put(DBContract.ConversationDBTable.COLUMN_NAME_5, conversation.getUser(4).getName());
            values.put(DBContract.ConversationDBTable.COLUMN_USER_NAME_5, conversation.getUser(4).getUserName());
        }

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
}
