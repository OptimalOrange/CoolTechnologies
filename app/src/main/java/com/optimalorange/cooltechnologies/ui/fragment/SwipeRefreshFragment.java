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

import java.lang.ref.WeakReference;

public abstract class SwipeRefreshFragment
        extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * 状态属性：可刷新状态
     */
    private boolean mRefreshable = true;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mChildView;

    private MenuItem mRefreshMenuItem;

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
        mChildView = onCreateChildView(inflater, container, savedInstanceState);
        mSwipeRefreshLayout = new SwipeRefreshLayout(getActivity()) {
            @Override
            public boolean canChildScrollUp() {
                return SwipeRefreshFragment.this.canChildScrollUp();
            }
        };
        mSwipeRefreshLayout.addView(
                mChildView,
                SwipeRefreshLayout.LayoutParams.MATCH_PARENT,
                SwipeRefreshLayout.LayoutParams.MATCH_PARENT);
        mSwipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        applyRefreshable();
        return mSwipeRefreshLayout;
    }

    @Override
    public void onDestroyView() {
        //TODO circle progress view won't be detached?
        // 参考自 http://stackoverflow.com/questions/27411397/new-version-of-swiperefreshlayout-causes-wrong-draw-of-views#27934636
        mSwipeRefreshLayout.clearAnimation(); // hide circle progress view
        mSwipeRefreshLayout.setOnRefreshListener(null);
        mSwipeRefreshLayout.removeView(mChildView);
        mSwipeRefreshLayout = null;
        mChildView = null;
        super.onDestroyView();
    }

    /**
     * 子类需调用此父类实现，以显示“刷新”{@link android.view.MenuItem}。
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
        mRefreshMenuItem = menu.findItem(R.id.action_refresh);
        applyRefreshable();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                setRefreshing(true);
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
     * 设置可刷新状态
     *
     * @param refreshable true表示当前可刷新；false表示当前不可刷新
     */
    public void setRefreshable(boolean refreshable) {
        if (mRefreshable != refreshable) {
            mRefreshable = refreshable;
            applyRefreshable();
        }
    }

    private void applyRefreshable() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(mRefreshable);
        }
        if (mRefreshMenuItem != null) {
            mRefreshMenuItem.setEnabled(mRefreshable);
            mRefreshMenuItem.setVisible(mRefreshable);
        }
    }

    /**
     * @see SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(final boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            //TODO bug of SwipeRefreshLayout
            // SwipeRefreshLayout indicator does not appear when the setRefreshing(true) is called
            // before the SwipeRefreshLayout.onMeasure(). This is a unreliable workaround from
            // http://stackoverflow.com/questions/26858692/swiperefreshlayout-setrefreshing-not-showing-indicator-initially
            mSwipeRefreshLayout.post(new Runnable() {
                private final WeakReference<SwipeRefreshLayout> mSwipeRefreshLayoutWeakReference =
                        new WeakReference<>(mSwipeRefreshLayout);

                @Override
                public void run() {
                    SwipeRefreshLayout swipeRefreshLayout = mSwipeRefreshLayoutWeakReference.get();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(refreshing);
                    }
                }
            });
        }
    }

    /**
     * @see SwipeRefreshLayout#canChildScrollUp()
     */
    protected boolean canChildScrollUp() {
        return mChildView.canScrollVertically(-1);
    }

    /**
     * 当{@link #onCreateView}时调用，返回{@link SwipeRefreshLayout}的内容。<br/>
     * SwipeRefreshLayout只能有一个直接子孩子，除非{@link SwipeRefreshFragment}的子类重新实现了
     * {@link #canChildScrollUp()}。
     *
     * @see SwipeRefreshLayout
     * @see #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    protected abstract View onCreateChildView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 请实现此方法，而不要使用{@link SwipeRefreshLayout#setOnRefreshListener}，否则“刷新”菜单项将会失效。<br/>
     * 请在完成刷新后调用{@link #setRefreshing(boolean) setRefreshing(false)}。
     */
    @Override
    public abstract void onRefresh();

}
