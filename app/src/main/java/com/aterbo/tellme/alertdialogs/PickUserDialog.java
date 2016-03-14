package com.aterbo.tellme.alertdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.aterbo.tellme.R;

/**
 * Created by ATerbo on 3/13/16.
 */
public class PickUserDialog extends DialogFragment {

    final CharSequence[] items = {
            "andy.terbovich@gmail.com", "test@test.com", "a@a.com"
    };


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose User to Talk To")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
    }
}