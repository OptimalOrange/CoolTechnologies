package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.util.Const;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public abstract class LoginableBaseActivity extends BaseActivity {

    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;

    private NetworkChecker mNetworkChecker;

    private boolean mShowLoginOrLogoutMenuItem = true;

    /** 状态属性：已登录优酷账号 */
    private boolean mHasLoggedIn;

    private final List<OnLoginStatusChangeListener> mListeners = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDefaultSharedPreferencesSingleton = DefaultSharedPreferencesSingleton.getInstance(this);
        mNetworkChecker = NetworkChecker.newInstance(this);

        hasLoggedIn(mDefaultSharedPreferencesSingleton.hasLoggedIn());
    }

    @Override
    protected void onDestroy() {
        mNetworkChecker = null;
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean superResult = super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_or_logout, menu);
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean superResult = super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_login_or_logout);
        if (hasLoggedIn()) {
            item.setIcon(R.drawable.ic_perm_identity_fill_white_24dp);
            item.setTitle(R.string.action_logout);
        } else {
            item.setIcon(R.drawable.ic_perm_identity_white_24dp);
            item.setTitle(R.string.action_login);
        }
        item.setEnabled(mShowLoginOrLogoutMenuItem);
        item.setVisible(mShowLoginOrLogoutMenuItem);
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login_or_logout:
                if (hasLoggedIn()) {
                    int message;
                    if (mDefaultSharedPreferencesSingleton.commitSaveString("user_token", "")) {
                        hasLoggedIn(false);
                        message = R.string.action_logout_success;
                    } else {
                        message = R.string.action_logout_fail;
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } else {
                    if (!mNetworkChecker.isConnected()) {
                        Toast.makeText(this, R.string.action_login_no_net, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, Const.REQUEST_CODE_LOGIN_ACTIVITY);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Const.REQUEST_CODE_LOGIN_ACTIVITY:
                if (resultCode == Const.RESULT_CODE_SUCCESS_LOGIN_ACTIVITY) {
                    hasLoggedIn(true);
                }
                break;
        }
    }

    //--------------------------------------------------------------------------
    // 新声明方法
    //--------------------------------------------------------------------------


    public DefaultSharedPreferencesSingleton getDefaultSharedPreferencesSingleton() {
        return mDefaultSharedPreferencesSingleton;
    }

    public NetworkChecker getNetworkChecker() {
        return mNetworkChecker;
    }

    public boolean hasLoggedIn() {
        return mHasLoggedIn;
    }

    private void hasLoggedIn(boolean hasLoggedIn) {
        if (mHasLoggedIn != hasLoggedIn) {
            mHasLoggedIn = hasLoggedIn;
            invalidateOptionsMenu();
            for (OnLoginStatusChangeListener listener : mListeners) {
                listener.onLoginStatusChanged(hasLoggedIn);
            }
        }
    }

    public void addLoginStatusChangeListener(OnLoginStatusChangeListener listener) {
        mListeners.add(listener);
    }

    public void removeLoginStatusChangeListener(OnLoginStatusChangeListener listener) {
        mListeners.remove(listener);
    }

    /**
     * 有新添加的菜单项
     */
    private boolean hasDeclaredMenuItem() {
        return mShowLoginOrLogoutMenuItem;
    }

    /**
     * 设置是否显示“登录”或“注销”菜单项
     *
     * @param show true：显示，false：不显示
     */
    public void showLoginOrLogoutMenuItem(boolean show) {
        if (mShowLoginOrLogoutMenuItem != show) {
            mShowLoginOrLogoutMenuItem = show;
            invalidateOptionsMenu();
        }
    }

    //--------------------------------------------------------------------------
    // 嵌套类
    //--------------------------------------------------------------------------

    /**
     * 登录状态变更监听器
     */
    public static interface OnLoginStatusChangeListener {

        /**
         * 当登录状态改变时被触发
         *
         * @param hasLoggedIn 当前的登录状态：true表示已登录；false表示未登录。
         */
        public void onLoginStatusChanged(boolean hasLoggedIn);
    }

}
