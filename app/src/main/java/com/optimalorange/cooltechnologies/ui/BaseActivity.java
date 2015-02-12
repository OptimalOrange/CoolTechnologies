package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 本应用所有{@link Activity}的父类。
 */
public abstract class BaseActivity extends Activity {

    private boolean mShowSettingsMenuItem = true;

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
