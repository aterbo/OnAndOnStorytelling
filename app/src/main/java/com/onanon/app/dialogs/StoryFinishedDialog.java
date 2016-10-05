package com.onanon.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Conversation;
import com.onanon.app.classes.Prompt;

import java.util.ArrayList;

/**
 * Created by ATerbo on 10/4/16.
 */
public class StoryFinishedDialog extends DialogFragment {

    private Conversation conversation;
    private String conversationPushId;
    private DatabaseReference baseRef, mNumberOfPromptsRef;

    private ProgressDialog progressDialog;

    public static WaitingForPromptsDialog newInstance(Conversation conversation, String conversationPushId) {
        WaitingForPromptsDialog frag = new WaitingForPromptsDialog();
        Bundle args = new Bundle();
        args.putParcelable("conversation", conversation);
        args.putString("convoPushId", conversationPushId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_after_story, null))
                .setPositiveButton(R.string.send_text_response, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // This will be overridden for data validations
                    }
                })
                .setNegativeButton(R.string.continue_without_response, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StoryFinishedDialog.this.getDialog().cancel();
                    }
                })
                .setNeutralButton(R.string.listen_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            final EditText responseEditText = (EditText) d.findViewById(R.id.entered_response);

            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;
                    String enteredData = responseEditText.getText().toString();

                    if (enteredData.length()>1) {
                        wantToCloseDialog = true;
                    }
                    if (wantToCloseDialog) {
                        d.dismiss();
                    } else {
                        Toast.makeText(getContext(), "You didn't enter a response!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
