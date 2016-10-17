package com.onanon.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onanon.app.R;

/**
 * Created by ATerbo on 10/4/16.
 */
public class StoryFinishedDialog extends DialogFragment {

    private String responseText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_after_story, container,
                true);
        final EditText responseEditText = (EditText) v.findViewById(R.id.entered_response);
        // retrieve display dimensions
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        v.setMinimumWidth((int)(displayRectangle.width() * 0.95f));

        // Watch for button clicks.
        Button listenAgainButton = (Button)v.findViewById(R.id.listen_again_button);
        listenAgainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                StoryFinishedDialog.this.dismiss();
                mListener.listenAgainClick();
            }
        });

        // Watch for button clicks.
        final Button responseButton = (Button)v.findViewById(R.id.response_button);
        responseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    if (responseButton.getText()==getString(R.string.continue_without_response)) {
                        StoryFinishedDialog.this.dismiss();
                        mListener.continueWithoutResponseClick();
                    } else if (responseButton.getText()==getString(R.string.send_text_response)) {
                        StoryFinishedDialog.this.dismiss();
                        mListener.continueWithResponseClick(responseEditText.getText().toString());
                    } else {
                        Toast.makeText(getContext(), "Oops! Something went wrong", Toast.LENGTH_LONG);
                    }
                }
            });

        responseEditText.addTextChangedListener(new TextWatcher() {
            private void handleText() {
                // Grab the button
                if(responseEditText.getText().length() == 0) {
                    responseButton.setText(R.string.continue_without_response);
                } else {
                    responseButton.setText(R.string.send_text_response);
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                handleText();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        });

        responseButton.setText(R.string.continue_without_response);

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
