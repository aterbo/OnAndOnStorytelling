package com.aterbo.tellme.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.SQLite.DBHelper;
import com.aterbo.tellme.SupplyTestSQLiteData;
import com.aterbo.tellme.adaptors.ConversationListAdaptor;
import com.aterbo.tellme.alertdialogs.PingStorytellerDialog;
import com.aterbo.tellme.classes.Conversation;
import com.firebase.client.Firebase;

import java.util.ArrayList;

public class ConversationListActivity extends AppCompatActivity {

    ArrayList<Conversation> conversations;
    int toHearSeparatorPosition;
    int toWaitForSeparatorPosition;
    int toTellSeparatorPosition;
    String chosenUserToAddNew;

    ListView conversationListView;
    ConversationListAdaptor conversationListAdaptor;
    ArrayList<Object> objectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        initializeFirebase();
        setSupportActionBar(toolbar);

        setFloatingActionButton();
        getConversationsFromDB();
        constructConversationList();
        setListAdaptor();

    }

    private void initializeFirebase(){
        Firebase.setAndroidContext(this);
    }

    private void setFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewConversation();
            }
        });
    }

    private void setListAdaptor() {
        conversationListAdaptor = new ConversationListAdaptor(objectList, this);
        conversationListView = (ListView)findViewById(R.id.conversation_list);
        conversationListView.setAdapter(conversationListAdaptor);
        conversationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                respondToListClick(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    private void respondToListClick(int position){
        if (position<(toHearSeparatorPosition)){
            startTellActivity(position);
        } else if (position<(toWaitForSeparatorPosition)){
            startListenActivity(position);
        } else {
            DialogFragment newFragment = PingStorytellerDialog.newInstance();
            newFragment.show(getFragmentManager(), "ping");
        }
    }

    private void constructConversationList(){
        objectList = new ArrayList<>();

        setAllSeparators();

        int status;

        for (Conversation conversation : conversations){
            status = conversation.getStatus();
            switch (status) {
                case 0:
                    objectList.add(1, conversation);
                    toHearSeparatorPosition = toHearSeparatorPosition + 1;
                    toWaitForSeparatorPosition = toWaitForSeparatorPosition + 1;
                    break;
                case 1:
                    objectList.add(toHearSeparatorPosition+1, conversation);
                    toWaitForSeparatorPosition = toWaitForSeparatorPosition + 1;
                    break;
                case 2:
                    objectList.add(toWaitForSeparatorPosition+1, conversation);
                    break;
            }
        }
    }

    private void getConversationsFromDB(){
        DBHelper db = new DBHelper(this);
        conversations = db.getConversationList();
        if(conversations.size()<=1) {
            SupplyTestSQLiteData testListData = new SupplyTestSQLiteData(this);
            testListData.buildTestSQLiteDB();
            conversations = db.getConversationList();
        }
    }

    private void setAllSeparators() {
        toTellSeparatorPosition = 0;
        toHearSeparatorPosition = 1;
        toWaitForSeparatorPosition = 2;
        addSeparator(toTellSeparatorPosition, "Stories to tell");
        addSeparator(toHearSeparatorPosition, "Stories to hear");
        addSeparator(toWaitForSeparatorPosition, "Stories to wait for");
    }

    private void addSeparator(int position, String separatorText){
        objectList.add(position, new String(separatorText));
    }

    private void startTellActivity(int position){
        Intent intent = new Intent(this, PickTopicToRecordActivity.class);
        intent.putExtra("selectedConversation", (Conversation)objectList.get(position));
        startActivity(intent);
    }

    private void startListenActivity(int position){
        Intent intent = new Intent(this, ListenToStoryActivity.class);
        intent.putExtra("selectedConversation", (Conversation)objectList.get(position));
        startActivity(intent);
    }

    public void goToLoginScreen(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void startNewConversation(){

        final CharSequence[] items = {
                "andy.terbovich@gmail.com", "test@test.com", "a@a.com"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose User to Talk To")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        chosenUserToAddNew = items[which].toString();
                    }
                });
        AlertDialog pickUserAlert = builder.create();
        pickUserAlert.show();
    }
}
