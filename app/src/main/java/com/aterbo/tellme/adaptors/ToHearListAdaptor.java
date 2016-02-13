package com.aterbo.tellme.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;
import com.aterbo.tellme.classes.SquareImageView;

import java.util.List;

/**
 * Created by ATerbo on 2/12/16.
 */
public class ToHearListAdaptor extends BaseAdapter {

    private List<Conversation> conversationList;
    private Context context;

    public ToHearListAdaptor(List<Conversation> conversationList, Context context) {
        this.conversationList = conversationList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.layout_conversation_list_item, viewGroup, false);

            viewHolder = setNewViewHolder(viewHolder, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Conversation conversation = conversationList.get(position);

        populateViewWithConversationText(viewHolder, conversation);

        return convertView;
    }

    private ViewHolder setNewViewHolder(ViewHolder viewHolder, View convertView){
        viewHolder.profileImage = (SquareImageView) convertView.findViewById(R.id.conversation_profile_image);
        viewHolder.title = (TextView) convertView.findViewById(R.id.conversation_title);
        viewHolder.participants = (TextView) convertView.findViewById(R.id.conversation_participants);
        viewHolder.timeSinceLastAction = (TextView) convertView.findViewById(R.id.conversation_time_since_action);
        viewHolder.storyDuration = (TextView) convertView.findViewById(R.id.conversation_story_duration);
        return viewHolder;
    }


    //ViewHolder for smooth scrolling
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
