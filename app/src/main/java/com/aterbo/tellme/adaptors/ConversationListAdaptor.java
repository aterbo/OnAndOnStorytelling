package com.aterbo.tellme.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.SquareImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ATerbo on 2/12/16.
 */
public class ConversationListAdaptor extends BaseAdapter {


    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;


    private ArrayList<Conversation> conversationList = new ArrayList<>();
    private Object[] OBJECTS;
    private Context context;

    private LayoutInflater mInflater;

    public ConversationListAdaptor(Object[] OBJECTS, Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.OBJECTS = OBJECTS;
        this.context = context;
    }


    @Override
    public int getCount() {
        return OBJECTS.length;
    }

    @Override
    public Object getItem(int position) {
        return OBJECTS[position];
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
        }

        // We can now fill the list item view with the appropriate data.
        if (type == TYPE_SEPARATOR) {
            ((TextView) convertView).setText((String) getItem(position));
        } else {
            final Conversation conversation = (Conversation) getItem(position);
            ((TextView) convertView.findViewById(R.id.conversation_title)).setText(conversation.getTitle());
            ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(conversation.getParticipant());
            ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(conversation.getTimeSinceLastAction());
            ((TextView) convertView.findViewById(R.id.conversation_story_duration)).setText(conversation.getStoryDuration());
        }

        return convertView;

    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (OBJECTS[position] instanceof String) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public boolean isEnabled(int position) {
        // A separator cannot be clicked !
        return getItemViewType(position) != TYPE_SEPARATOR;
    }


    private ViewHolder setNewViewHolder(ViewHolder viewHolder, View convertView){
        viewHolder.profileImage = (SquareImageView) convertView.findViewById(R.id.conversation_profile_image);
        viewHolder.title = (TextView) convertView.findViewById(R.id.conversation_title);
        viewHolder.participants = (TextView) convertView.findViewById(R.id.conversation_participants);
        viewHolder.timeSinceLastAction = (TextView) convertView.findViewById(R.id.conversation_time_since_action);
        viewHolder.storyDuration = (TextView) convertView.findViewById(R.id.conversation_story_duration);
        return viewHolder;
    }

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html
    //http://www.javacodegeeks.com/2013/09/android-viewholder-pattern-example.html
    static class ViewHolder {
        SquareImageView profileImage;
        TextView title;
        TextView participants;
        TextView timeSinceLastAction;
        TextView storyDuration;
    }

    private void populateViewWithConversationText(ViewHolder viewHolder, Conversation conversation){
        viewHolder.title.setText(conversation.getTitle());
        viewHolder.participants.setText(conversation.getParticipant());
        viewHolder.timeSinceLastAction.setText(conversation.getTimeSinceLastAction());
        viewHolder.storyDuration.setText(conversation.getStoryDuration());
    }
}
