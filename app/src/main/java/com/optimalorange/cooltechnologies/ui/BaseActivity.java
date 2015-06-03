package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.LinkedList;
import java.util.List;

/**
 * 本应用所有{@link android.app.Activity Activity}的父类。
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Durations for certain animations we use:
    protected static final int HEADER_HIDE_ANIM_DURATION = 300;

    /**
     * THRESHOLD for calculate approximate currentY and deltaY of {@link #onMainContentScrolled}
     */
    final static int ITEMS_THRESHOLD = 0;

    private boolean mShowSettingsMenuItem = true;

    private Toolbar mActionBarToolbar;

    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;

    private int mActionBarAutoHideSensivity = 0;

    private int mActionBarAutoHideMinY = 0;

    private int mActionBarAutoHideSignal = 0;

    private boolean mActionBarShown = true;

    private int mProgressBarTopWhenActionBarShown;

    private int mProgressBarTopWhenActionBarHidden = 0;

    private List<SwipeRefreshLayout> mSwipeRefreshLayouts = new LinkedList<>();

    private final AbsListView.OnScrollListener mOnScrollListenerForAbsListView =
            new AbsListView.OnScrollListener() {
                int lastFvi = 0;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0 : Integer.MAX_VALUE,
                            lastFvi - firstVisibleItem > 0 ? Integer.MIN_VALUE :
                                    lastFvi == firstVisibleItem ? 0 : Integer.MAX_VALUE
                    );
                    lastFvi = firstVisibleItem;
                }
            };

    private final RecyclerView.OnScrollListener mOnScrollListenerForRecyclerView =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0 : Integer.MAX_VALUE, dy);
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean superResult = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean superResult = super.onPrepareOptionsMenu(menu);
        MenuItem settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setEnabled(mShowSettingsMenuItem);
        settingsMenuItem.setVisible(mShowSettingsMenuItem);
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    public void registerSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if (!mSwipeRefreshLayouts.contains(swipeRefreshLayout)) {
            mSwipeRefreshLayouts.add(swipeRefreshLayout);
            updateSwipeRefreshProgressBarTop();
        }
    }

    public void deregisterSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if (mSwipeRefreshLayouts.contains(swipeRefreshLayout)) {
            mSwipeRefreshLayouts.remove(swipeRefreshLayout);
        }
    }

    protected void setProgressBarTop(
            int progressBarTopWhenActionBarShown, int progressBarTopWhenActionBarHidden) {
        mProgressBarTopWhenActionBarShown = progressBarTopWhenActionBarShown;
        mProgressBarTopWhenActionBarHidden = progressBarTopWhenActionBarHidden;
        updateSwipeRefreshProgressBarTop();
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayouts.isEmpty()) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = mActionBarShown ?
                mProgressBarTopWhenActionBarShown : mProgressBarTopWhenActionBarHidden;
        for (SwipeRefreshLayout swipeRefreshLayout : mSwipeRefreshLayouts) {
            swipeRefreshLayout.setProgressViewOffset(false,
                    top + progressBarStartMargin, top + progressBarEndMargin);
        }
    }

    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private void initActionBarAutoHide() {
        if (!mActionBarAutoHideEnabled) {
            mActionBarAutoHideEnabled = true;
            mActionBarAutoHideMinY = getResources().getDimensionPixelSize(
                    R.dimen.action_bar_auto_hide_min_y);
            mActionBarAutoHideSensivity = getResources().getDimensionPixelSize(
                    R.dimen.action_bar_auto_hide_sensivity);
        }
    }

    /**
     * 取得用于触发Action Bar auto hide的OnScrollListener
     */
    public AbsListView.OnScrollListener getOnScrollListenerForAbsListView() {
        initActionBarAutoHide();
        return mOnScrollListenerForAbsListView;
    }

    /**
     * 取得用于触发Action Bar auto hide的OnScrollListener
     */
    public RecyclerView.OnScrollListener getOnScrollListenerForRecyclerView() {
        initActionBarAutoHide();
        return mOnScrollListenerForRecyclerView;
    }

    /**
     * Indicates that the main content has scrolled (for the purposes of showing/hiding
     * the action bar for the "action bar auto hide" effect). currentY and deltaY may be exact
     * (if the underlying view supports it) or may be approximate indications:
     * deltaY may be INT_MAX to mean "scrolled forward indeterminately" and INT_MIN to mean
     * "scrolled backward indeterminately".  currentY may be 0 to mean "somewhere close to the
     * start of the list" and INT_MAX to mean "we don't know, but not at the start of the list"
     */
    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity;
        autoShowOrHideActionBar(shouldShow);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        updateSwipeRefreshProgressBarTop();
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {}

    /**
     * 有新添加的菜单项
     */
    private boolean hasDeclaredMenuItem() {
        return mShowSettingsMenuItem;
    }

    /**
     * 设置是否显示“设置”菜单项
     *
     * @param show true：显示，false：不显示
     */
    public void showSettingsMenuItem(boolean show) {
        if (mShowSettingsMenuItem != show) {
            mShowSettingsMenuItem = show;
            invalidateOptionsMenu();
        }
    }
}
