package com.aterbo.tellme.adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aterbo.tellme.R;
import com.aterbo.tellme.activities.PickTopicToRecordActivity;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.ConvoToHear;
import com.aterbo.tellme.classes.ConvoToTell;
import com.aterbo.tellme.classes.ConvoToWaitFor;
import com.aterbo.tellme.classes.SquareImageView;

import java.util.ArrayList;

//http://cyrilmottier.com/2011/07/05/listview-tips-tricks-2-section-your-listview/

/**
 * Created by ATerbo on 2/12/16.
 */
public class ConversationListAdaptor extends BaseAdapter {

    private static final int TYPE_SEPARATOR = 0;
    private static final int TYPE_CONVO_TO_TELL = 1;
    private static final int TYPE_CONVO_TO_HEAR = 2;
    private static final int TYPE_CONVO_TO_WAIT_FOR = 3;

    private static final int ITEM_VIEW_TYPE_COUNT = 4;


    private ArrayList<Object> objectList = new ArrayList<>();
    private Context context;

    private LayoutInflater mInflater;

    public ConversationListAdaptor(ArrayList<Object> objectList, Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objectList = objectList;
        this.context = context;
    }


    @Override
    public int getCount() {
        return objectList.size();
    }

    @Override
    public Object getItem(int position) {
        return objectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final int type = getItemViewType(position);

        // First, let's create a new convertView if needed. You can also
        // create a ViewHolder to speed up changes if you want ;)
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    type == TYPE_SEPARATOR ? R.layout.layout_conversation_list_separator :
                            R.layout.layout_conversation_list_item, viewGroup, false);
        } else{

            // We can now fill the list item view with the appropriate data.
            switch (type){
                case TYPE_SEPARATOR:
                    ((TextView) convertView).setText((String) getItem(position));
                    break;
                case TYPE_CONVO_TO_TELL:
                    final Conversation convoToTell = (Conversation) getItem(position);
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToTell.getTitle());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToTell.getParticipant());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToTell.getTimeSinceLastAction());
                    (convertView.findViewById(R.id.conversation_story_duration)).setVisibility(View.GONE);
                    break;
                case TYPE_CONVO_TO_HEAR:
                    final Conversation convoToHear = (Conversation) getItem(position);
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToHear.getTitle());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToHear.getParticipant());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToHear.getTimeSinceLastAction());
                    ((TextView) convertView.findViewById(R.id.conversation_story_duration)).setText(convoToHear.getStoryDuration());
                    break;
                case TYPE_CONVO_TO_WAIT_FOR:
                    final Conversation convoToWaitFor = (Conversation) getItem(position);
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToWaitFor.getTitle());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToWaitFor.getParticipant());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToWaitFor.getTimeSinceLastAction());
                    (convertView.findViewById(R.id.conversation_story_duration)).setVisibility(View.GONE);
                    break;
            }

        }

        switch (type){
            case TYPE_SEPARATOR:
                convertView.setOnClickListener(null);
                break;
            case TYPE_CONVO_TO_TELL:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "TO TELL", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case TYPE_CONVO_TO_HEAR:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "TO HEAR", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case TYPE_CONVO_TO_WAIT_FOR:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "TO WAIT FOR", Toast.LENGTH_LONG).show();
                    }
                    });
                break;
            }

        return convertView;

    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = getItem(position);

        if (object instanceof String) {
            return TYPE_SEPARATOR;
        } else if (object instanceof ConvoToTell) {
            return TYPE_CONVO_TO_TELL;
        } else if (object instanceof ConvoToHear) {
            return TYPE_CONVO_TO_HEAR;
        } else if (object instanceof ConvoToWaitFor) {
            return TYPE_CONVO_TO_WAIT_FOR;
        } else{
            return TYPE_SEPARATOR;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        // A separator cannot be clicked !
        return getItemViewType(position) != TYPE_SEPARATOR;
    }
}
