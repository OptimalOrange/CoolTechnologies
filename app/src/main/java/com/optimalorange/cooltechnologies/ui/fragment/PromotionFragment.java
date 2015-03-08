package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.MainActivity;
import com.optimalorange.cooltechnologies.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 推荐
 */
public class PromotionFragment extends Fragment {

    private int mActionBarHeight = -1;

    private int mPageIndicatorHeight = -1;

    private View v;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActionBarHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        mPageIndicatorHeight = getResources().getDimensionPixelSize(R.dimen.page_indicator_height);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_promotion, container, false);
        ViewGroup mainContent = (ViewGroup) v.findViewById(R.id.main_content);
        // set layout
        if (getActivity() instanceof MainActivity) {
            mainContent.setClipToPadding(false);
            int paddingTop = mActionBarHeight + mPageIndicatorHeight;
            Utils.setPaddingTop(mainContent, paddingTop);
        }
        return v;
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

    @Override
    public void onDestroyView() {
        v = null;
        super.onDestroyView();
    }
}
