package com.optimalorange.cooltechnologies.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.NumberFormat;
import java.util.Locale;

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

    /**
     * @see #formatViewCount(int, android.content.Context)
     */
    public static String formatViewCount(int view_count) {
        return formatViewCount(view_count, null);
    }

    /**
     * 格式化{@link com.optimalorange.cooltechnologies.entity.Video#getView_count() 总播放数}，
     * 如果默认{@link Locale}的{@link Locale#getLanguage() 语言}是中文，
     * 格式化为xx万的形式（view_count大于1万时，否则保持view_count原样）；
     * 默认语言不是中文时使用{@link NumberFormat#getIntegerInstance()}格式化。
     *
     * @param view_count {@link com.optimalorange.cooltechnologies.entity.Video Video}的
     *                   {@link com.optimalorange.cooltechnologies.entity.Video#getView_count()
     *                   总播放数}
     * @param context    用于获取默认{@link Locale}的上下文环境，如果为空。则使用{@link Locale#getDefault()}
     * @return 格式化后的结果
     */
    public static String formatViewCount(int view_count, Context context) {
        Locale locale;
        if (context == null) {
            locale = Locale.getDefault();
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if ("zh".equals(locale.getLanguage())) {
            return formatViewCountChinese(view_count);
        } else {
            return formatViewCountNonChinese(view_count);
        }
    }

    /**
     * @see #formatViewCount(int, android.content.Context)
     */
    private static String formatViewCountChinese(int view_count) {
        if (view_count >= 10_000) {
            return view_count / 10_000 + "万";
        } else {
            return String.valueOf(view_count);
        }
    }

    /**
     * @see #formatViewCount(int, android.content.Context)
     */
    private static String formatViewCountNonChinese(int view_count) {
        return NumberFormat.getIntegerInstance().format(view_count);
    }


    /**
     * 视频播放时长（秒）转换为（yy-mm-ss)
     *
     * @param duration int
     * @return String
     */
    public static String getDurationString(int duration) {
        int hours = duration / (60 * 60);
        int minutes = (duration - hours * (60 * 60)) / 60;
        int seconds = duration - hours * (60 * 60) - minutes * 60;
        String minutesStr = "", secondsStr = "";
        String hoursStr = "" + hours;
        if (minutes < 10) {
            minutesStr = "0" + minutes;
        } else {
            minutesStr = "" + minutes;
        }
        if (seconds < 10) {
            secondsStr = "0" + seconds;
        } else {
            secondsStr = "" + seconds;
        }
        if (hours == 0) {
            return minutesStr + ":" + secondsStr;
        } else {
            return hoursStr + ":" + minutesStr + ":" + secondsStr;
        }
    }
}
