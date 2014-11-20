package com.optimalorange.cooltechnologies.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 */
public class Utils {

    /**
     * 将string存入SharedPreferences
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 从SharedPreferences中取出string
     *
     * @param context  context
     * @param key      key
     * @param defValue defValue
     * @return string
     */
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defValue);
    }
}
