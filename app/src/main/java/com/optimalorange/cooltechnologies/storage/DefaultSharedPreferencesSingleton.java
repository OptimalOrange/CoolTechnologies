package com.optimalorange.cooltechnologies.storage;

import com.optimalorange.cooltechnologies.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * 全局{@link SharedPreferences}单例。
 * <p><i>本类是线程安全的</i></p>
 *
 * @see SharedPreferences
 */
public class DefaultSharedPreferencesSingleton {

    private static DefaultSharedPreferencesSingleton mInstance;

    @NonNull
    private final Resources mResources;

    @NonNull
    private final SharedPreferences mDefaultSharedPreferences;


    public static DefaultSharedPreferencesSingleton getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new DefaultSharedPreferencesSingleton(context);
        }
        return mInstance;
    }

    private DefaultSharedPreferencesSingleton(@NonNull Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        context = context.getApplicationContext();
        mResources = context.getResources();
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 将value以String的形式存入
     * {@link PreferenceManager#getDefaultSharedPreferences DefaultSharedPreferences}
     *
     * @param key   关键字
     * @param value 值
     * @return 如果成功写入了value值，返回true
     * @see #applySaveString(String, String)
     * @see android.content.SharedPreferences.Editor#putString(String, String)
     * @see android.content.SharedPreferences.Editor#commit()
     */
    public boolean commitSaveString(String key, String value) {
        return mDefaultSharedPreferences.edit().putString(key, value).commit();
    }

    /**
     * 将value以String的形式存入
     * {@link PreferenceManager#getDefaultSharedPreferences DefaultSharedPreferences}
     *
     * @param key   关键字
     * @param value 值
     * @see #commitSaveString(String, String)
     * @see android.content.SharedPreferences.Editor#putString(String, String)
     * @see android.content.SharedPreferences.Editor#apply()
     */
    public void applySaveString(String key, String value) {
        mDefaultSharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * 从{@link PreferenceManager#getDefaultSharedPreferences DefaultSharedPreferences}中取出string值
     *
     * @param key      关键字
     * @param defValue 如果以key为关键字的首选项不存在，返回defValue
     * @return 如果存在的话，返回首选项值；不存在的话，返回defValue。
     * 如果以key为关键字的首选项不是String类型的，抛出{@link ClassCastException}异常。
     * @see SharedPreferences#getString(String key, String value)
     */
    public String retrieveString(String key, String defValue) {
        return mDefaultSharedPreferences.getString(key, defValue);
    }

    /**
     * 仅在WLAN下，显示图片
     *
     * @see R.string#pref_key_only_show_images_when_use_wlan
     */
    public boolean onlyShowImagesWhenUseWlan() {
        return mDefaultSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_key_only_show_images_when_use_wlan),
                mResources.getBoolean(R.bool.pref_default_value_only_show_images_when_use_wlan)
        );
    }

    /**
     * 仅在WLAN下，播放视频
     *
     * @see R.string#pref_key_only_play_video_when_use_wlan
     */
    public boolean onlyPlayVideoWhenUseWlan() {
        return mDefaultSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_key_only_play_video_when_use_wlan),
                mResources.getBoolean(R.bool.pref_default_value_only_play_video_when_use_wlan)
        );
    }

}
