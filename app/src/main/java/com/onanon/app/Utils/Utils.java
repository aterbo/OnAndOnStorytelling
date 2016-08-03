package com.onanon.app.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
        progressDialog.setMessage("");
        progressDialog.setTitle("Processing");
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
        String dateFormat = "HH:mm, MMM d";
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
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        StringBuilder sb = new StringBuilder(64);

        sb.append("Active ");

        if (days >= 2) {
            sb.append(days);
            sb.append(" Days");
        } else if (days >= 1) {
            sb.append(days);
            sb.append(" Day");
        } else if (hours >= 2) {
            sb.append(hours);
            sb.append(" Hours");
        } else if (hours >= 1) {
            sb.append(hours);
            sb.append(" Hour");
        } else if (minutes >= 5) {
            sb.append(minutes);
            sb.append(" Min");
        } else{
            sb.append("Just a few minutes");
        }

        sb.append(" ago");

        return(sb.toString());
    }
}
