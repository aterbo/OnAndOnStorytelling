package com.onanon.app.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onanon.app.R;
import com.onanon.app.Utils.Constants;
import com.onanon.app.Utils.PrefManager;
import com.onanon.app.Utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by ATerbo on 9/18/16.
 */
public class EditProfileDialogFragment extends android.support.v4.app.DialogFragment {

    private ImageView profilePicView;
    private Uri mCropImageUri;
    private String currentUserName;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PrefManager prefManager = new PrefManager(getActivity());
        currentUserName = prefManager.getUserNameFromSharedPreferences();

        View view = inflater.inflate(R.layout.dialog_edit_profile, container);
        getDialog().setTitle("Hello, " + currentUserName);
        profilePicView = (ImageView) view.findViewById(R.id.profile_photo);
        setProfileImageFromFirebase();

        Button changeProfilePicButton = (Button)view.findViewById(R.id.change_profile_pic_button);
        changeProfilePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeProfilePic();
            }
        });

        Button doneButton = (Button)view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void setProfileImageFromFirebase() {

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

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
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
                progressDialog = Utils.getSpinnerDialog(getContext());
                Uri resultUri = result.getUri();
                uploadPhotoToFirebase(resultUri);
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
                .setRequestedSize(Constants.PROFILE_PIC_DIMENSIONS,Constants.PROFILE_PIC_DIMENSIONS,
                        CropImageView.RequestSizeOptions.RESIZE_FIT)
                .start(getContext(), this);
    }

    private void uploadPhotoToFirebase(Uri photoUri) {

        if (currentUserName == null) {
            PrefManager prefManager = new PrefManager(getActivity());
            currentUserName = prefManager.getUserNameFromSharedPreferences();
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl("gs://firebase-tell-me.appspot.com");
        StorageReference profilePicturesRef = storageRef.child("profilePictures");
        StorageReference currentUserRef = profilePicturesRef.child(currentUserName);
        String photoFileName = currentUserName + ".jpeg";

        StorageReference photoStorageRef = currentUserRef.child(photoFileName);


        Log.i("StorageUpload", "String ref: " + photoStorageRef.getPath());

        UploadTask uploadTask = photoStorageRef.putFile(photoUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.i("StorageUpload", "Error uploading file to storage.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i("StorageUpload", "File uploaded to storage.");
                updateUserOnFirebase(downloadUrl);
            }
        });
    }

    private void updateUserOnFirebase(Uri photoDownloadUrl){
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userIconRef = baseRef.child(Constants.FB_LOCATION_USERS)
                .child(currentUserName).child("profilePhotoUrl");

        userIconRef.setValue(photoDownloadUrl.toString(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.i("FIREBASEUpdateCONVO", "Error updating convo to Firebase");
                }
                Log.i("FIREBASEUpdateCONVO", "Convo updated to Firebase successfully");

                setProfileImageFromFirebase();

            }
        });
    }

}
