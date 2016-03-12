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
        ArrayList<Prompt> proposedPromptTestList = new ArrayList<>();

        userTestList.add(new User("Jim Bob"));
        proposedPromptTestList.add(new Prompt("Describe a fun day", "Fun day"));
        proposedPromptTestList.add(new Prompt("Describe your first car", "First Car"));
        proposedPromptTestList.add(new Prompt("Describe your favorite food", "Favorite Food"));
        testList.add(new Conversation("Fun, a car, and food.", "2 hours", "", userTestList, 0,
                new Prompt("Describe a fun day", "Fun day"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Billy Boy"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Tell me about your mom", "Mother"));
        proposedPromptTestList.add(new Prompt("Tell me about your biggest fear", "Fear"));
        proposedPromptTestList.add(new Prompt("Describe your favorite food", "Favorite Food"));
        testList.add(new Conversation("A Bad Day.", "3 hours", "3:42", userTestList, 1,
                new Prompt("Describe a bad day", "Bad day"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mom"));
        userTestList.add(new User("Dad"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Tell me a story", "A story"));
        proposedPromptTestList.add(new Prompt("Tell me a regret", "Regret"));
        proposedPromptTestList.add(new Prompt("Describe pure joy", "Joy"));
        testList.add(new Conversation("Life, death, and taxes", "1 day", "", userTestList, 0,
                new Prompt("Describe a good day", "Good Life"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mary Contrary"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Tell me a story", "A story"));
        proposedPromptTestList.add(new Prompt("Tell me about a bicycle", "Regret"));
        proposedPromptTestList.add(new Prompt("Describe pure joy", "Joy"));
        testList.add(new Conversation("Good times.", "45 min", "6:32", userTestList, 1,
                new Prompt("Describe a good time", "Good times"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Pete"));
        userTestList.add(new User("Dan"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Describe what the future looks like", "The Future"));
        proposedPromptTestList.add(new Prompt("Tell me about politics", "Politics"));
        proposedPromptTestList.add(new Prompt("Do you think the moon landing happened?", "The Moon Landing"));
        testList.add(new Conversation("Future, The Moon Landing, or Politics", "5 hours", "", userTestList, 2,
                new Prompt("Describe what the future looks like", "The future"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Sam Storyteller"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Describe what the future looks like", "The Future"));
        proposedPromptTestList.add(new Prompt("Tell me about politics", "Politics"));
        proposedPromptTestList.add(new Prompt("Do you think the moon landing happened?", "The Moon Landing"));
        testList.add(new Conversation("A belly laugh", "3 days", "1:20", userTestList, 1,
                new Prompt("Tell me about your last belly laugh", "A belly laugh"), proposedPromptTestList));

        userTestList = new ArrayList<>();
        userTestList.add(new User("Mike Straub"));
        proposedPromptTestList = new ArrayList<>();
        proposedPromptTestList.add(new Prompt("Tell me a story", "A story"));
        proposedPromptTestList.add(new Prompt("Tell me about a bicycle", "Regret"));
        proposedPromptTestList.add(new Prompt("Describe pure joy", "Joy"));
        testList.add(new Conversation("The Past, The President, or a book", "3 days", "", userTestList, 1,
                new Prompt("What were you like in the past?", "The past"), proposedPromptTestList));

        return testList;
    }
}
