package com.onanon.app.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Prompt;
import com.onanon.app.classes.Response;
import com.onanon.app.classes.User;

import java.util.ArrayList;

public class ReactionsActivity extends AppCompatActivity {

    private FirebaseListAdapter<Response> mListAdapter;
    private ListView mListView;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reactions);
        getUserNameFromSharedPreferences();
        initializeScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_friends:
                Utils.composeMmsMessage(getString(R.string.invite_sms_message_text), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getUserNameFromSharedPreferences(){
        PrefManager prefManager = new PrefManager(this);
        currentUserName = prefManager.getUserNameFromSharedPreferences();
    }

    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_reactions);

        mListAdapter = Utils.getResponseListAdaptor(this, currentUserName);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Response selectedReaction = mListAdapter.getItem(position);
                Log.i("SELECTED USER", selectedReaction.getResponse());
            }
        });
    }
}
