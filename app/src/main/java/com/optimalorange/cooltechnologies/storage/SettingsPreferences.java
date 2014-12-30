package com.optimalorange.cooltechnologies.storage;

import com.optimalorange.cooltechnologies.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class SettingsPreferences {

    private static SettingsPreferences mInstance;

    @NonNull
    private final Resources mResources;

    @NonNull
    private final SharedPreferences mDefaultSharedPreferences;


    public static SettingsPreferences getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new SettingsPreferences(context);
        }
        return mInstance;
    }

    private SettingsPreferences(@NonNull Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        context = context.getApplicationContext();
        mResources = context.getResources();
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean shouldShowImagesWhenUseMobileNetwork() {
        return mDefaultSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_key_show_images),
                mResources.getBoolean(R.bool.pref_default_value_show_images)
        );
    }

    public boolean shouldPlayVideoWhenUseMobileNetwork() {
        return mDefaultSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_key_play_video),
                mResources.getBoolean(R.bool.pref_default_value_play_video)
        );
    }

}
