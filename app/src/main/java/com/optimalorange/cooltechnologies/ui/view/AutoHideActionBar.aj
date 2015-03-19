package com.optimalorange.cooltechnologies.ui.view;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.MainActivity;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListGenresFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PromotionFragment;

import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

public aspect AutoHideActionBar {

    private static final String LOG_TAG = AutoHideActionBar.class.getSimpleName();

    private interface SupportAutoHideActionBarEnable {}
    declare parents: (PromotionFragment || ListVideosFragment || ListGenresFragment
            || FavoriteFragment || HistoryFragment) implements SupportAutoHideActionBarEnable;

    private MainActivity SupportAutoHideActionBarEnable.mMainActivity = null;
    private int SupportAutoHideActionBarEnable.mActionBarHeight = -1;
    private int SupportAutoHideActionBarEnable.mPageIndicatorHeight = -1;
    private ViewGroup SupportAutoHideActionBarEnable.mScrollableView = null;

    private pointcut inFragmentPackage(): within(com.optimalorange.cooltechnologies.ui.fragment.*);

    after(SupportAutoHideActionBarEnable fragment): inFragmentPackage() && this(fragment)
            && execution(void onCreate(Bundle)) {
        final Activity activity = ((Fragment) fragment).getActivity();
        if (activity instanceof MainActivity) {
            fragment.mMainActivity = (MainActivity) activity;
            fragment.mActionBarHeight =
                    activity.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
            fragment.mPageIndicatorHeight =
                    activity.getResources().getDimensionPixelSize(R.dimen.page_indicator_height);
        } else {
            fragment.mMainActivity = null;
        }
    }

    after(SupportAutoHideActionBarEnable fragment) returning(View view): inFragmentPackage() &&
            this(fragment) && execution(View onCreateChildView(LayoutInflater, ViewGroup, Bundle)) {
        if (fragment.mMainActivity == null) {
            return;
        }
        fragment.mScrollableView = findScrollableView(fragment, view);

        final int paddingTop = fragment.mActionBarHeight + fragment.mPageIndicatorHeight;
        for (View container : findOtherContainers(fragment, view)) {
            setPaddingTop(container, paddingTop);
        }
        if (fragment.mScrollableView != null) {
            fragment.mScrollableView.setClipToPadding(false);
            setPaddingTop(fragment.mScrollableView, paddingTop);
            setOnScrollListener(fragment.mScrollableView, fragment.mMainActivity);
        }

    }

    before(SupportAutoHideActionBarEnable fragment):
            inFragmentPackage() && this(fragment) && execution(void onDestroyView()) {
        if (fragment.mScrollableView != null) {
            clearOnScrollListener(fragment.mScrollableView);
            fragment.mScrollableView = null;
        }
    }

    private static void setPaddingTop(View v, int paddingTop) {
        v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), v.getPaddingBottom());
    }

    @Nullable
    private static ViewGroup findScrollableView(Object fragment, View rootView) {
        int scrollableViewId;
        if (fragment instanceof PromotionFragment) {
            return null;
        } else if (fragment instanceof ListVideosFragment) {
            scrollableViewId = R.id.grid_view;
        } else if (fragment instanceof ListGenresFragment) {
            scrollableViewId = R.id.recycler_view;
        } else if (fragment instanceof FavoriteFragment || fragment instanceof HistoryFragment) {
            scrollableViewId = R.id.favorite_list;
        } else {
            throw new IllegalArgumentException("unknown fragment:" + fragment);
        }
        return (ViewGroup) rootView.findViewById(scrollableViewId);
    }

    @NonNull
    private static View[] findOtherContainers(Object fragment, View rootView) {
        if (fragment instanceof PromotionFragment) {
            return new View[]{rootView.findViewById(R.id.main_content)};
        } else if (fragment instanceof ListVideosFragment
                || fragment instanceof ListGenresFragment) {
            return new View[]{
                    rootView.findViewById(android.R.id.empty),
                    rootView.findViewById(R.id.no_connection)};
        } else if (fragment instanceof FavoriteFragment || fragment instanceof HistoryFragment) {
            return new View[]{rootView.findViewById(R.id.favorite_hint)};
        } else {
            throw new IllegalArgumentException("unknown fragment:" + fragment);
        }
    }

    private static void setOnScrollListener(View scrollableView, MainActivity mainActivity) {
        if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView)
                    .setOnScrollListener(mainActivity.getOnScrollListenerForRecyclerView());
        } else if (scrollableView instanceof AbsListView) {
            ((AbsListView) scrollableView)
                    .setOnScrollListener(mainActivity.getOnScrollListenerForAbsListView());
        } else {
            Log.w(LOG_TAG, "Can't set ScrollListener to scrollableView:" + scrollableView);
        }
    }

    private static void clearOnScrollListener(View scrollableView) {
        if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).setOnScrollListener(null);
        } else if (scrollableView instanceof AbsListView) {
            ((AbsListView) scrollableView).setOnScrollListener(null);
        } else {
            Log.w(LOG_TAG, "Can't clear ScrollListener of scrollableView:" + scrollableView);
        }
    }

}
