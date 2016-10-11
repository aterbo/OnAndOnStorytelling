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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Prompt;
import com.onanon.app.classes.Response;
import com.onanon.app.classes.User;
import com.onanon.app.dialogs.StoryFinishedDialog;
import com.onanon.app.dialogs.ViewResponseDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReactionsActivity extends AppCompatActivity implements ViewResponseDialog.ViewResponseListener {

    private FirebaseListAdapter<Response> mListAdapter;
    private ListView mListView;
    private String currentUserName;
    private Response selectedResponse;
    private String selectedResponsePushId;

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

        mListView.setEmptyView(findViewById(R.id.responses_empty));

        mListAdapter = Utils.getResponseListAdaptor(this, currentUserName);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedResponse = mListAdapter.getItem(position);
                selectedResponsePushId = mListAdapter.getRef(position).getKey();

                Log.i("Selected Response", selectedResponse.getResponse());
                ViewResponseDialog viewResponseDialog = ViewResponseDialog
                        .newInstance(selectedResponse, currentUserName);
                viewResponseDialog.show(getSupportFragmentManager(), "ViewResponseDialog");
            }
        });
    }

    @Override
    public void saveResponseClick() {
        Log.i("Selected Response", "Save " + selectedResponse.getResponse());
    }

    @Override
    public void deleteResponseClick() {
        Log.i("Selected Response", "Delete " + selectedResponse.getResponse());
        deleteResponseFromFB();
    }

    private void deleteResponseFromFB() {
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference selectedReactionsRef = baseRef
                .child(Constants.FB_LOCATION_RESPONSES)
                .child(currentUserName)
                .child(selectedResponsePushId);

        selectedReactionsRef.setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateResponse", "Error deleting response from Firebase");
                }
                Log.i("FIREBASEUpdateResponses", "Response deleted successfully");
            }
        });
    }
}
