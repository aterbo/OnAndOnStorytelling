package com.onanon.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ATerbo on 7/28/16.
 */
public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "ONanON_Pref";

    private static final String IS_FIRST_TIME_LAUNCH_KEY = "IsFirstTimeLaunch";
    private static final String CURRENT_USER_NAME_KEY = "currentUser";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH_KEY, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH_KEY, true);
    }

    public void setUserNameToPreferences(String currentUserName){
        editor.putString(CURRENT_USER_NAME_KEY, currentUserName);
        editor.commit();
    }

    public String getUserNameFromSharedPreferences(){
        return pref.getString(CURRENT_USER_NAME_KEY, "");
    }

    public boolean isUserNameInSharedPreferencesEmpty(){
        String currentUserName = pref.getString(CURRENT_USER_NAME_KEY, "");
        if (currentUserName.isEmpty() || currentUserName == "") {
            return true;
        } else {
            return false;
        }
    }
}
