package com.optimalorange.cooltechnologies.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.optimalorange.cooltechnologies.R;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 热门
 */
public class HotFragment extends Fragment {

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.hot_fragment_layout, container, false);
        }

        return v;
    }
}
