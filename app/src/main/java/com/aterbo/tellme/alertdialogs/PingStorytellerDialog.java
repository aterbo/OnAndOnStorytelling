package com.aterbo.tellme.alertdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.aterbo.tellme.R;

/**
 * Created by ATerbo on 2/12/16.
 */
public class PingStorytellerDialog extends DialogFragment {

    public static PingStorytellerDialog newInstance() {
        PingStorytellerDialog frag = new PingStorytellerDialog();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String dialogText = "It has been 12 hours since Bob should have told his story. " +
                "Would you like to remind Bob that you want to hear from him?";

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.ping_title)
                .setCancelable(true)
                .setMessage(dialogText)
                .setPositiveButton(R.string.ping,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dismiss();
                        }
                    }
                )

                .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                    }
                )
                .create();
    }

}
