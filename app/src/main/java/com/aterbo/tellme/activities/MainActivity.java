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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Conversation> toTellList;
    List<Conversation> toHearList;
    List<Conversation> toWaitForList;

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

    private List<Conversation> getJunkConversationList(){
        List<Conversation> testList = new ArrayList<>();

        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));
        testList.add(new Conversation(true));

        return testList;
    }

    private void constructConversationList(){

        toTellList = getJunkConversationList();
        toHearList = getJunkConversationList();
        toWaitForList = getJunkConversationList();

        addSeparator("Stories to tell");
        addContent(toTellList);
        addSeparator("Stories to hear");
        addContent(toHearList);
        addSeparator("Stories to wait for");
        addContent(toTellList);
    }

    private void addSeparator(String separatorText){
        objectList.add(new String(separatorText));
    }

    private void addContent(List<Conversation> listToAdd){
        for (Conversation conversation : listToAdd){
            objectList.add(conversation);
        }
    }

    public void startNextActivity(View view){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        startActivity(intent);
    }
}
