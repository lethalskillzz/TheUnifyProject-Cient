package net.theunifyproject.lethalskillzz.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

/**
 * Created by Ibrahim on 21/10/2015.
 */
public class PrefManager {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "UNIfyProject";

    //Settings Shared Preferences Keys
    private static final String KEY_SETTING_PROMPT = "prompt";
    private static final String KEY_SETTING_VIBRATE = "vibrate";
    private static final String KEY_SETTING_SOUND = "sound";


    // Other Shared Preferences Keys
    private static final String KEY_REG_STAGE = "regStage";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_NEW_MOBILE_NUMBER = "new_mobile_number";
    private static final String KEY_SESSION_ID = "sessionId";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_COURSE = "course";
    private static final String KEY_LEVEL = "level";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setPrompt(boolean prompt) {
        editor.putBoolean(KEY_SETTING_PROMPT, prompt);
        editor.commit();
    }

    public boolean getPrompt() { return pref.getBoolean(KEY_SETTING_PROMPT, false); }


    public void setSound(boolean sound) {
        editor.putBoolean(KEY_SETTING_SOUND, sound);
        editor.commit();
    }

    public boolean getSound() { return pref.getBoolean(KEY_SETTING_SOUND, false); }


    public void setVibrate(boolean vibrate) {
        editor.putBoolean(KEY_SETTING_VIBRATE, vibrate);
        editor.commit();
    }

    public boolean getVibrate() { return pref.getBoolean(KEY_SETTING_VIBRATE, false); }



    public void setRegStage(Integer regStage) {
        editor.putInt(KEY_REG_STAGE, regStage);
        editor.commit();
    }

    public Integer getRegStage() { return pref.getInt(KEY_REG_STAGE, 0); }




    public void setSessionId(String sessionIdId) {
        editor.putString(KEY_SESSION_ID, sessionIdId);
        editor.commit();
    }

    public String getSessionId() { return pref.getString(KEY_SESSION_ID, null); }

    public String getUsername() { return pref.getString(KEY_USERNAME, null); }



    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }

    public String getMobileNumber() { return pref.getString(KEY_MOBILE_NUMBER, null); }


    public void setNewMobileNumber(String mobileNumber) {
        editor.putString(KEY_NEW_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }

    public String getNewMobileNumber() { return pref.getString(KEY_NEW_MOBILE_NUMBER, null); }



    public void storeUserDetails(String sessionId, String name, String username, String mobile,
                                 String location, String course, String level, boolean isInit) {

        editor.putString(KEY_SESSION_ID, sessionId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_LOCATION, location);
        editor.putString(KEY_COURSE, course);
        editor.putString(KEY_LEVEL, level);
        if(isInit) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putBoolean(KEY_SETTING_PROMPT, true);
            editor.putBoolean(KEY_SETTING_SOUND, true);
            editor.putBoolean(KEY_SETTING_VIBRATE, true);
        }
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> profile = new HashMap<>();
        profile.put("sessionId", pref.getString(KEY_SESSION_ID, null));
        profile.put("name", pref.getString(KEY_NAME, null));
        profile.put("username", pref.getString(KEY_USERNAME, null));
        profile.put("mobile", pref.getString(KEY_MOBILE, null));
        profile.put("location", pref.getString(KEY_LOCATION, null));
        profile.put("course", pref.getString(KEY_COURSE, null));
        profile.put("level", pref.getString(KEY_LEVEL, null));
        return profile;
    }

}
