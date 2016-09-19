package com.onanon.app.dialogs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.activities.ChooseTopicsToSendActivity;

/**
 * Created by ATerbo on 9/18/16.
 */
public class EditProfileDialogFragment extends DialogFragment {

    private ImageView profilePicView;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_edit_profile, container);
        getDialog().setTitle("Hello");
        profilePicView = (ImageView) view.findViewById(R.id.profile_photo);
        setProfileImageFromFirebase();

        Button changeProfilePicButton = (Button)view.findViewById(R.id.change_profile_pic_button);
        changeProfilePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                changeProfilePic();
            }
        });

        Button cancelButton = (Button)view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                dismiss();
            }
        });

        return view;
    }

    private void setProfileImageFromFirebase() {
        PrefManager prefManager = new PrefManager(getActivity());
        String currentUserName = prefManager.getUserNameFromSharedPreferences();

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userIconRef = baseRef.child(Constants.FB_LOCATION_USERS)
                .child(currentUserName).child("profilePhotoUrl");

        userIconRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String convoIconUrl = (String) snapshot.getValue();

                if (convoIconUrl != null && !convoIconUrl.isEmpty() && !convoIconUrl.contains(Constants.NO_PHOTO_KEY)) {
                    //Profile has picture

                    if (Character.toString(convoIconUrl.charAt(0)).equals("\"")) {
                        convoIconUrl = convoIconUrl.substring(1, convoIconUrl.length()-1);
                    }
                } else {
                    //profile does not have picture
                    convoIconUrl = "";
                }
                Glide.with(getActivity()).load(convoIconUrl).placeholder(R.drawable.word_treatment_512_84)
                        .fallback(R.drawable.word_treatment_512_84).dontAnimate().into(profilePicView);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void changeProfilePic() {


        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");
        photoPickerIntent.putExtra("scale", true);
        photoPickerIntent.putExtra("outputX", 96);
        photoPickerIntent.putExtra("outputY", 96);
        photoPickerIntent.putExtra("aspectX", 1);
        photoPickerIntent.putExtra("aspectY", 1);
        photoPickerIntent.putExtra("return-data", true);
        photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        getActivity().startActivityForResult(photoPickerIntent, Constants.REQ_CODE_PICK_IMAGE);
    }

/*
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case Constants.REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageReturnedIntent!=null) {
                        Bundle extras = imageReturnedIntent.getExtras();
                        Bitmap selectedBitmap = extras.getParcelable("data");
                        imageR = (ImageView) findViewById(R.id.image);
                        imageR.setImageBitmap(selectedBitmap);
                    }
                }
        }
        */
}
