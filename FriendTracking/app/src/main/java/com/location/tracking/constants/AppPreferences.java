package com.location.tracking.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Created by Sreejith on 9/3/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AppPreferences {

    private static final boolean BOOLEAN_PREFERENCES_DEFAULT_VALUE = false;
    private static final int INTEGER_PREFERENCES_DEFAULT_VALUE = 0;
    private static final String STRING_PREFERENCES_DEFAULT_VALUE = "";
    private static final float FLOAT_PREFERENCES_DEFAULT_VALUE = 0;


    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(Context context,String key) {
        return getPreferences(context).getString(key, STRING_PREFERENCES_DEFAULT_VALUE);
    }

    public static void putString(Context context, String key, String value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getInteger(Context context, String key) {
        return getPreferences(context).getInt(key, INTEGER_PREFERENCES_DEFAULT_VALUE);
    }

    public static void putInteger(Context context, String key, int value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void putFloat(Context context, String key, float value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static float getFloat(Context context, String key) {
        return getPreferences(context).getFloat(key, FLOAT_PREFERENCES_DEFAULT_VALUE);
    }

    public static boolean getBoolean(Context context, String key) {
        return getPreferences(context).getBoolean(key, BOOLEAN_PREFERENCES_DEFAULT_VALUE);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public static void cleardata(Context context){
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

}
