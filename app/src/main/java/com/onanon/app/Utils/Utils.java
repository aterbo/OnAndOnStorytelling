package com.onanon.app.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onanon.app.R;
import com.onanon.app.classes.Response;
import com.onanon.app.classes.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by ATerbo on 3/13/16.
 */
public class Utils {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static ProgressDialog getSpinnerDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Telling a tale");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;
    }

    public static String getDateTimeAsString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
        return sdf.format(c.getTime());
    }

    public static long getSystemTimeAsLong() {
        return System.currentTimeMillis();
    }

    public static String converSystemTimeToDateAsString(long milliSeconds)
    {
        String dateFormat = "MMM d, HH:mm";
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        return formatter.format(calendar.getTime());
    }

    public static String calcTimeFromMillisToNow(long milliSeconds) {
        long currentTime = System.currentTimeMillis();
        long millis = currentTime - milliSeconds;

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        StringBuilder sb = new StringBuilder(64);

        if (days >= 2) {
            sb.append(days);
            sb.append(" days");
        } else if (days >= 1) {
            sb.append(days);
            sb.append(" day");
        } else if (hours >= 2) {
            sb.append(hours);
            sb.append(" hours");
        } else if (hours >= 1) {
            sb.append(hours);
            sb.append(" hour");
        } else if (minutes >= 5) {
            sb.append(minutes);
            sb.append(" min");
        } else{
            sb.append("Just a few minutes");
        }

        sb.append(" ago");

        return(sb.toString());
    }

    public static FirebaseListAdapter<User> getUserListAdaptor(final Activity activity){

        DatabaseReference baseRef, mUsersRef;
        baseRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = baseRef.child(Constants.FB_LOCATION_USERS);

        FirebaseListAdapter userListAdaptor = new FirebaseListAdapter<User>(activity, User.class,
                R.layout.layout_user_list_item, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ((TextView) v.findViewById(R.id.user_name)).setText(model.getUserName());
                String profilePicUrl = model.getProfilePhotoUrl();
                ImageView profilePic = ((ImageView) v.findViewById(R.id.profile_photo));

                if (profilePicUrl != null && !profilePicUrl.isEmpty() && !profilePicUrl.contains(Constants.NO_PHOTO_KEY)) {
                    //Profile has picture

                    if (Character.toString(profilePicUrl.charAt(0)).equals("\"")) {
                        profilePicUrl = profilePicUrl.substring(1, profilePicUrl.length()-1);
                    }
                } else {
                    //profile does not have picture
                    profilePicUrl = "";
                }
                Glide.with(activity).load(profilePicUrl).placeholder(R.drawable.alberticon)
                        .fallback(R.drawable.alberticon).into(profilePic);
            }
        };

        return userListAdaptor;
    }

    public static FirebaseListAdapter<Response> getResponseListAdaptor(final Activity activity
            , final String currentUserName){

        DatabaseReference baseRef, mResponseRef, mUsersResponsesRef;
        baseRef = FirebaseDatabase.getInstance().getReference();
        mResponseRef = baseRef.child(Constants.FB_LOCATION_RESPONSES);
        mUsersResponsesRef = mResponseRef.child(currentUserName);

        FirebaseListAdapter responseListAdaptor = new FirebaseListAdapter<Response>(activity, Response.class,
                R.layout.layout_reactions_list_item, mUsersResponsesRef) {
            @Override
            protected void populateView(View v, Response model, int position) {
                ((TextView) v.findViewById(R.id.response_title))
                        .setText("RE: " + model.getPromptRespondingTo().getText());

                String from, to, fromToString;
                if (model.getResponderUserName()==currentUserName) {
                    from = "you";
                } else {
                    from = model.getResponderUserName();
                }
                if (model.getOriginalTellerUserName()==currentUserName) {
                    to = "you";
                } else {
                    to = model.getOriginalTellerUserName();
                }

                fromToString = "From " + from + " to " + to;

                ((TextView) v.findViewById(R.id.response_from_to)).setText(fromToString);

                ((TextView) v.findViewById(R.id.response_date))
                        .setText(converSystemTimeToDateAsString(model.getDateResponseSubmitted()));

                String profilePicUrl = model.getResponderProfilePicUrl();

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
                Glide.with(activity)
                        .load(profilePicUrl)
                        .placeholder(R.drawable.word_treatment_144_24)
                        .fallback(R.drawable.alberticon)
                        .bitmapTransform(new CropCircleTransformation(activity))
                        .dontAnimate()
                        .into(profilePic);
            }
        };
        return responseListAdaptor;
    }

    public static void composeMmsMessage(String message, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static void composeEmail(String[] addresses, String subject, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
