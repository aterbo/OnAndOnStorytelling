package com.aterbo.tellme;

import android.content.Context;

import com.aterbo.tellme.SQLite.DBHelper;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.Prompt;
import com.aterbo.tellme.classes.User;

import java.util.ArrayList;

/**
 * Created by ATerbo on 2/12/16.
 */
public class SupplyTestSQLiteData {

    private Context context;

    public SupplyTestSQLiteData(Context context){
        this.context = context;
    }

    public void buildTestSQLiteDB(){
        DBHelper db = new DBHelper(context);
        ArrayList<Conversation> testList = getTestConvos();
        db.addListOfConversations(testList);
    }

    private ArrayList<Conversation> getTestConvos(){
        ArrayList<Conversation> testList = new ArrayList<>();
        ArrayList<User> userTestList = new ArrayList<>();

        userTestList.add(new User("Jim Bob"));
        testList.add(new Conversation("Fun, a car, and food.", "2 hours", "", userTestList, 0,
                new Prompt("Describe a fun day", "Fun day")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Billy Boy"));
        testList.add(new Conversation("A Bad Day.", "3 hours", "3:42", userTestList, 1,
                new Prompt("Describe a bad day", "Bad day")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mom"));
        userTestList.add(new User("Dad"));
        testList.add(new Conversation("Life, death, and taxes", "1 day", "", userTestList, 0,
                new Prompt("Describe a good day", "Good Life")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mary Magdaline"));
        testList.add(new Conversation("Good times.", "45 min", "6:32", userTestList, 1,
                new Prompt("Describe a good time", "Good times")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Pete"));
        userTestList.add(new User("Dan"));
        testList.add(new Conversation("Future, The Moon Landing, or Politics", "5 hours", "", userTestList, 2,
                new Prompt("Describe what the future looks like", "The future")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Sam Storyteller"));
        testList.add(new Conversation("A belly laugh", "3 days", "1:20", userTestList, 1,
                new Prompt("Tell me about your last belly laugh", "A belly laugh")));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mike Straub"));
        testList.add(new Conversation("The Past, The President, or a book", "3 days", "", userTestList, 2,
                new Prompt("What were you like in the past?", "The past")));

        return testList;
    }
}
