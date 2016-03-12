package com.aterbo.tellme.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aterbo.tellme.R;
import com.aterbo.tellme.classes.Conversation;

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
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToTell.getProposedPromptsTagString());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToTell.getUsersNameAsString());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToTell.getTimeSinceLastAction());
                    (convertView.findViewById(R.id.conversation_story_duration)).setVisibility(View.GONE);
                    break;
                case TYPE_CONVO_TO_HEAR:
                    final Conversation convoToHear = (Conversation) getItem(position);
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToHear.getCurrentPrompt().getTagText());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToHear.getUsersNameAsString());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToHear.getTimeSinceLastAction());
                    ((TextView) convertView.findViewById(R.id.conversation_story_duration)).setText(convoToHear.getStoryDuration());
                    break;
                case TYPE_CONVO_TO_WAIT_FOR:
                    final Conversation convoToWaitFor = (Conversation) getItem(position);
                    ((TextView) convertView.findViewById(R.id.conversation_title)).setText(convoToWaitFor.getProposedPromptsTagString());
                    ((TextView) convertView.findViewById(R.id.conversation_participants)).setText(convoToWaitFor.getUsersNameAsString());
                    ((TextView) convertView.findViewById(R.id.conversation_time_since_action)).setText(convoToWaitFor.getTimeSinceLastAction());
                    (convertView.findViewById(R.id.conversation_story_duration)).setVisibility(View.GONE);
                    break;
            }

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
        } else if (((Conversation)object).getStatus() == 0) {
            return TYPE_CONVO_TO_TELL;
        } else if (((Conversation)object).getStatus() == 1) {
            return TYPE_CONVO_TO_HEAR;
        } else if (((Conversation)object).getStatus() == 2) {
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
