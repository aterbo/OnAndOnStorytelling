package com.onanon.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onanon.app.R;

/**
 * Created by ATerbo on 10/4/16.
 */
public class StoryFinishedDialog extends DialogFragment {

    private AlertDialog alertDialog;
    private String responseText;

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
                        mListener.continueWithoutResponseClick();
                    }
                })
                .setNeutralButton(R.string.listen_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.listenAgainClick();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        alertDialog = (AlertDialog)getDialog();
        if(alertDialog != null)
        {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            final EditText responseEditText = (EditText) alertDialog.findViewById(R.id.entered_response);

            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;
                    responseText = responseEditText.getText().toString();

                    if (responseText.length()>1) {
                        wantToCloseDialog = true;
                    }

                    if (wantToCloseDialog) {
                        //uploadResponseToFB(enteredData);
                        alertDialog.dismiss();
                        mListener.continueWithResponseClick(responseText);
                    } else {
                        Toast.makeText(getContext(), "You didn't enter a response!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface StoryFinishedListener {
        public void continueWithResponseClick(String responseText);
        public void continueWithoutResponseClick();
        public void listenAgainClick();
    }

    // Use this instance of the interface to deliver action events
    private StoryFinishedListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if (context instanceof StoryFinishedListener) {
            mListener = (StoryFinishedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
