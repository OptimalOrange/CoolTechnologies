package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 收藏
 */
public class FavoriteFragment extends Fragment {

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_favorite, container, false);
        }

        return v;
    }
}
