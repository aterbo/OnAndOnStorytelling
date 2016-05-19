package com.onanon.app.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

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
}
