package com.aterbo.tellme.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.aterbo.tellme.FBHelper;
import com.aterbo.tellme.R;
import com.aterbo.tellme.Utils.Constants;
import com.aterbo.tellme.classes.User;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

public class PickUserActivity extends AppCompatActivity {

    FirebaseListAdapter<User> mListAdapter;
    ListView mListView;
    private Firebase mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }      });

    /**
     * Create Firebase references
     */
    mUsersRef = new Firebase(getResources().getString(R.string.firebase_url) + "/" +
            Constants.FIREBASE_LOCATION_USERS);

    /**
     * Link layout elements from XML and setup the toolbar
     */
    initializeScreen();

    /**
     * Set interactive bits, such as click events/adapters
     */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_display_all_users);

        mListAdapter = new FirebaseListAdapter<User>(this, User.class,
                android.R.layout.two_line_list_item, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ((TextView)v.findViewById(android.R.id.text1)).setText(model.getEmail().replace(",", "."));
                ((TextView)v.findViewById(android.R.id.text2)).setText(model.getUserName());
            }
        };
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = mListAdapter.getItem(position);
                if (selectedUser != null) {
                    String userEmail = selectedUser.getEmail();
                    Log.i("SELECTED USER", userEmail);
                    //TODO: Determine which type of conversation was picked and open activity based on PushId.
                }
            }

        });


    }

}
