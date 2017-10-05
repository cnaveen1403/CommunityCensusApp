package com.zolipe.communitycensus.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.zolipe.communitycensus.database.GDatabaseHelper;
import com.zolipe.communitycensus.model.State;

import java.util.ArrayList;

public class AppData {
    private static final String SQLITE_NAME = "CommunityCensus";

    // for saving and geeting the result....
    public static boolean saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SQLITE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static boolean saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SQLITE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SQLITE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static void clearPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SQLITE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
//        editor.apply();
        editor.commit();
    }

}
