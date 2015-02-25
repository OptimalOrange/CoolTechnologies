package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 本应用所有{@link android.app.Activity Activity}的父类。
 */
public abstract class BaseActivity extends ActionBarActivity {

    private boolean mShowSettingsMenuItem = true;

    private Toolbar mActionBarToolbar;

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

    // copy from https://github.com/google/iosched/blob/b3c3ae2a5b41e28c07383644d78e5c076288322f/android/src/main/java/com/google/samples/apps/iosched/ui/BaseActivity.java
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    // copy from https://github.com/google/iosched/blob/b3c3ae2a5b41e28c07383644d78e5c076288322f/android/src/main/java/com/google/samples/apps/iosched/ui/BaseActivity.java
    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

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
