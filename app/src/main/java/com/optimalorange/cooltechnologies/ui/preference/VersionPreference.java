package com.optimalorange.cooltechnologies.ui.preference;

import com.optimalorange.cooltechnologies.R;
import com.umeng.update.UmengUpdateAgent;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

public class VersionPreference extends Preference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VersionPreference(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public VersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VersionPreference(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        setTitle(R.string.check_for_update);
        try {
            final String versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
            setSummary(context.getString(R.string.version, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
        UmengUpdateAgent.forceUpdate(getContext());
    }
}
