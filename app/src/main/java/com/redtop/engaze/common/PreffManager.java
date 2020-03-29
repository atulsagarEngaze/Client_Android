package com.redtop.engaze.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;

public class PreffManager {
    public static void setPref(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setPrefBoolean(String key, Boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setPrefLong(String key, Long value, Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }


    public static <T> void setPrefArrayList(String key, ArrayList<T> value, Context context)  {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(key, ObjectSerializer.serialize(value));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        editor.commit();
    }


    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getPrefArrayList(String key, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        try {
            return (ArrayList<T>)ObjectSerializer.deserialize(preferences.getString(key, null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getPref(String key, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static Boolean getPrefBoolean(String key, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }

    public static Long getPrefLong(String key, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getLong(key, 0);
    }

    public static void removePref(String key, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        if(preferences.getString(key, null) != null)
            preferences.edit().remove(key).apply();
    }
}
