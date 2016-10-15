package com.onanon.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.Utils;
import com.onanon.app.classes.Response;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        response = getArguments().getParcelable("response");
        currentUserName = getArguments().getString("currentUserName");

        View v = inflater.inflate(R.layout.dialog_view_response, container, false);

        // Watch for button clicks.
        Button save_button = (Button)v.findViewById(R.id.save_response);
        save_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                ViewResponseDialog.this.dismiss();
                mListener.saveResponseClick();
            }
        });

        // Watch for button clicks.
        Button delete_button = (Button)v.findViewById(R.id.delete_response);
        delete_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                ViewResponseDialog.this.dismiss();
                mListener.deleteResponseClick();
            }
        });

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

        ((TextView) v.findViewById(R.id.response_from_to)).setText(
                fromToString);
        ((TextView) v.findViewById(R.id.response_date)).setText(
                Utils.converSystemTimeToDateAsString(response.getDateResponseSubmitted()));
        ((TextView) v.findViewById(R.id.response_title)).setText(
                "RE: " + response.getPromptRespondingTo().getText());
        ((TextView)v.findViewById(R.id.response)).setText(response.getResponse());

        String profilePicUrl = response.getResponderProfilePicUrl();

        ImageView profilePic = ((ImageView) v.findViewById(R.id.response_sender_profile_image));

        if (profilePicUrl != null && !profilePicUrl.isEmpty() && !profilePicUrl.contains(Constants.NO_PHOTO_KEY)) {
            //Profile has picture

            if (Character.toString(profilePicUrl.charAt(0)).equals("\"")) {
                profilePicUrl = profilePicUrl.substring(1, profilePicUrl.length()-1);
            }
        } else {
            //profile does not have picture
            profilePicUrl = "";
        }
        Glide.with(getActivity())
                .load(profilePicUrl)
                .placeholder(R.drawable.alberticon)
                .fallback(R.drawable.alberticon)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(profilePic);

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

