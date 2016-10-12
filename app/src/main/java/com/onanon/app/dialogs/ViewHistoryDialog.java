package com.onanon.app.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.HistoryEntry;

/**
 * Created by ATerbo on 10/11/16.
 */
public class ViewHistoryDialog extends DialogFragment {
    ListView mListView;
    String conversationPushId;
    FirebaseListAdapter<HistoryEntry> mListAdapter;


    public static ViewHistoryDialog newInstance(String conversationPushId) {
        ViewHistoryDialog dialog = new ViewHistoryDialog();
        Bundle args = new Bundle();
        args.putString("conversationPushId", conversationPushId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        conversationPushId = getArguments().getString("conversationPushId");

        View v = inflater.inflate(R.layout.dialog_view_history, container,
                true);

        mListView = (ListView)v.findViewById(R.id.list_history);
        mListView.setEmptyView(v.findViewById(R.id.history_empty));
        setFirebaseListAdaptor();
        mListView.setAdapter(mListAdapter);

        return v;
    }

    private void setFirebaseListAdaptor(){
        DatabaseReference baseRef, mHistoryRef, mConversationHistoryRef;
        baseRef = FirebaseDatabase.getInstance().getReference();
        mHistoryRef = baseRef.child(Constants.FB_LOCATION_HISTORY);
        mConversationHistoryRef = mHistoryRef.child(conversationPushId);

        mListAdapter = new FirebaseListAdapter<HistoryEntry>(
                getActivity(), HistoryEntry.class,
                R.layout.layout_history_list_item, mConversationHistoryRef) {
            @Override
            protected void populateView(View v, HistoryEntry model, int position) {
                ((TextView) v.findViewById(R.id.prompt_history))
                        .setText(model.getPromptRespondingTo().getText());

                ((TextView) v.findViewById(R.id.sender_history)).setText(model.getStoryTellerUserName());

                ((TextView) v.findViewById(R.id.story_recorded_date))
                        .setText(Utils.converSystemTimeToDateAsString(model.getDateResponseRecorded()));
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }
    }
}
