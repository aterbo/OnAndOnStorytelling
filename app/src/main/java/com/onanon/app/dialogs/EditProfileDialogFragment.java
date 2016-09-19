package com.onanon.app.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.onanon.app.Manifest;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.activities.ChooseTopicsToSendActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by ATerbo on 9/18/16.
 */
public class EditProfileDialogFragment extends android.support.v4.app.DialogFragment {

    private ImageView profilePicView;
    private Uri mCropImageUri;

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
        Intent pickImageChooserIntent = CropImage.getPickImageChooserIntent(getContext());
        startActivityForResult(pickImageChooserIntent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }



    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                Uri resultUri = result.getUri();
                Toast.makeText(getContext(), "Result OK " + resultUri.toString(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), "Result Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(getContext(), "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setFixAspectRatio(true)
                .setAspectRatio(1,1)
                .setRequestedSize(96,96, CropImageView.RequestSizeOptions.RESIZE_FIT)
                .start(getContext(), this);
    }

}
