package com.aterbo.tellme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.adaptors.ConversationListAdaptor;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.ConvoToHear;
import com.aterbo.tellme.classes.ConvoToTell;
import com.aterbo.tellme.classes.ConvoToWaitFor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<ConvoToTell> toTellList;
    ArrayList<ConvoToHear> toHearList;
    ArrayList<ConvoToWaitFor> toWaitForList;

    ListView conversationListView;
    ConversationListAdaptor conversationListAdaptor;
    ArrayList<Object> objectList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        objectList = new ArrayList<>();

        constructConversationList();

        conversationListAdaptor = new ConversationListAdaptor(objectList, this);
        conversationListView = (ListView)findViewById(R.id.conversation_list);
        conversationListView.setAdapter(conversationListAdaptor);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<ConvoToTell> getTestConvoToTell(){
        ArrayList<ConvoToTell> testList = new ArrayList<>();

        testList.add(new ConvoToTell(true));
        testList.add(new ConvoToTell(true));
        testList.add(new ConvoToTell(true));

        return testList;
    }

    private ArrayList<ConvoToHear> getTestConvoToHear(){
        ArrayList<ConvoToHear> testList = new ArrayList<>();

        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));
        testList.add(new ConvoToHear(true));

        return testList;
    }

    private ArrayList<ConvoToWaitFor> getTestConvoToWaitFor(){
        ArrayList<ConvoToWaitFor> testList = new ArrayList<>();


        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));
        testList.add(new ConvoToWaitFor(true));

        return testList;
    }

    private void constructConversationList(){

        toTellList = getTestConvoToTell();
        toHearList = getTestConvoToHear();
        toWaitForList = getTestConvoToWaitFor();

        addSeparator("Stories to tell");

        for (ConvoToTell conversation : toTellList){
            objectList.add(conversation);
        }
        addSeparator("Stories to hear");

        for (ConvoToHear conversation : toHearList){
            objectList.add(conversation);
        }

        addSeparator("Stories to wait for");

        for (ConvoToWaitFor conversation : toWaitForList){
            objectList.add(conversation);
        }

    }

    private void addSeparator(String separatorText){
        objectList.add(new String(separatorText));
    }

    public void startNextActivity(View view){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        startActivity(intent);
    }
}
