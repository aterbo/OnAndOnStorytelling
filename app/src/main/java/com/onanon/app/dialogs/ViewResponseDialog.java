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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Response;

/**
 * Created by ATerbo on 10/5/16.
 */
public class ViewResponseDialog  extends DialogFragment {

    private Response response;
    private String currentUserName;

    public static ViewResponseDialog newInstance(Response response, String currentUserName) {
        ViewResponseDialog frag = new ViewResponseDialog();

        Bundle args = new Bundle();
        args.putParcelable("response", response);
        args.putString("currentUserName", currentUserName);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        response = getArguments().getParcelable("response");
        currentUserName = getArguments().getParcelable("currentUserName");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_view_response, null))
                .setTitle(R.string.viewing_response_title)
                .setPositiveButton(R.string.delete_response, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.deleteResponseClick();
                    }
                })
                .setNegativeButton(R.string.save_responses, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.saveResponseClick();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if(alertDialog != null)
        {
            String from, to, fromToString;
            if (response.getResponderUserName()==currentUserName) {
                from = "you";
            } else {
                from = response.getResponderUserName();
            }
            if (response.getOriginalTellerUserName()==currentUserName) {
                to = "you";
            } else {
                to = response.getOriginalTellerUserName();
            }

            fromToString = "From " + from + " to " + to;

            ((TextView) alertDialog.findViewById(R.id.response_from_to)).setText(
                    fromToString);
            ((TextView) alertDialog.findViewById(R.id.response_date)).setText(
                    Utils.calcTimeFromMillisToNow(response.getDateResponseSubmitted()));
            ((TextView) alertDialog.findViewById(R.id.response_title)).setText(
                    "RE: " + response.getPromptRespondingTo().getText());
            ((TextView)alertDialog.findViewById(R.id.response)).setText(response.getResponse());

            String profilePicUrl = response.getResponderProfilePicUrl();

            ImageView profilePic = ((ImageView) alertDialog.findViewById(R.id.response_sender_profile_image));

            if (profilePicUrl != null && !profilePicUrl.isEmpty() && !profilePicUrl.contains(Constants.NO_PHOTO_KEY)) {
                //Profile has picture

                if (Character.toString(profilePicUrl.charAt(0)).equals("\"")) {
                    profilePicUrl = profilePicUrl.substring(1, profilePicUrl.length()-1);
                }
            } else {
                //profile does not have picture
                profilePicUrl = "";
            }
            Glide.with(getActivity()).load(profilePicUrl).placeholder(R.drawable.alberticon)
                    .fallback(R.drawable.alberticon).into(profilePic);
        }
    }

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ViewResponseListener {
        public void deleteResponseClick();
        public void saveResponseClick();
    }

    // Use this instance of the interface to deliver action events
    private ViewResponseListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if (context instanceof ViewResponseListener) {
            mListener = (ViewResponseListener) context;
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

