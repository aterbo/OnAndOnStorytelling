package com.onanon.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.classes.Conversation;

/**
 * Created by ATerbo on 10/4/16.
 */
public class WaitingForPromptsDialog extends DialogFragment {

    private Conversation conversation;

    public static WaitingForPromptsDialog newInstance(Conversation conversation) {
        WaitingForPromptsDialog frag = new WaitingForPromptsDialog();
        Bundle args = new Bundle();
        args.putParcelable("conversation", conversation);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        conversation = getArguments().getParcelable("conversation");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Waiting for prompts");
        builder.setMessage(conversation.getLastUserNameToTell() + " needs to send you some prompts.\n\n" +
                "Do you want to wait or do you want three random prompts from the database?");

        builder.setPositiveButton("Get Random Prompts", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setNegativeButton("Wait", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

}
