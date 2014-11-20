package com.optimalorange.cooltechnologies.util;

import android.app.Fragment;

import com.optimalorange.cooltechnologies.ui.fragment.ClassifyFragment;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HotFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PushFragment;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 */
public class FragmentConfig {
    public static FragmentConfig instance;

    public static final int FRAGMENT_ID_HOT = 0;
    public static final int FRAGMENT_ID_CLASSIFY = 1;
    public static final int FRAGMENT_ID_FAVORITE = 2;
    public static final int FRAGMENT_ID_HISTORY = 3;
    public static final int FRAGMENT_ID_PUSH = 4;

    public static FragmentConfig getInstance() {
        if (instance == null) {
            instance = new FragmentConfig();
        }
        return instance;
    }

    public Fragment getFragment(int id) {
        Fragment newFragment = null;
        switch (id) {
            case FRAGMENT_ID_HOT:
                newFragment = new HotFragment();
                break;
            case FRAGMENT_ID_CLASSIFY:
                newFragment = new ClassifyFragment();
                break;
            case FRAGMENT_ID_FAVORITE:
                newFragment = new FavoriteFragment();
                break;
            case FRAGMENT_ID_HISTORY:
                newFragment = new HistoryFragment();
                break;
            case FRAGMENT_ID_PUSH:
                newFragment = new PushFragment();
                break;
        }

        return newFragment;
    }

}
