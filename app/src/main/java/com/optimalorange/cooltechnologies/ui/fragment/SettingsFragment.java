package com.optimalorange.cooltechnologies.ui.fragment;


import com.optimalorange.cooltechnologies.R;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onPause();
    }

}
