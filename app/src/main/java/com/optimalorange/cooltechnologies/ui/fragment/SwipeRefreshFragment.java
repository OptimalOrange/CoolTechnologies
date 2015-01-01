package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class SwipeRefreshFragment
        extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mSwipeRefreshLayout = new SwipeRefreshLayout(getActivity());
        mSwipeRefreshLayout.addView(
                onCreateViewInSwipeRefreshLayout(inflater, container, savedInstanceState),
                SwipeRefreshLayout.LayoutParams.MATCH_PARENT,
                SwipeRefreshLayout.LayoutParams.MATCH_PARENT);
        mSwipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return mSwipeRefreshLayout;
    }

    @Override
    public void onDestroyView() {
        mSwipeRefreshLayout = null;
        super.onDestroyView();
    }

    /**
     * 子类需调用此父类实现，以显示“刷新”{@link android.view.MenuItem}。
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    /**
     * @see SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    /**
     * 当{@link #onCreateView}时调用，返回{@link SwipeRefreshLayout}的内容。<br/>
     * SwipeRefreshLayout只能有一个直接子孩子。
     *
     * @see SwipeRefreshLayout
     * @see #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    protected abstract View onCreateViewInSwipeRefreshLayout(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 请实现此方法，而不要使用{@link SwipeRefreshLayout#setOnRefreshListener}，否则“刷新”菜单项将会失效。<br/>
     * 请在完成刷新后调用{@link #setRefreshing(boolean) setRefreshing(false)}。
     */
    @Override
    public abstract void onRefresh();

}
