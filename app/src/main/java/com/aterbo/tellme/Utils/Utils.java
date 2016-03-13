package com.aterbo.tellme.Utils;

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

}
