package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListGenresFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PromotionFragment;
import com.optimalorange.cooltechnologies.util.Const;
import com.umeng.update.UmengUpdateAgent;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BaseActivity {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    /**
     * Pagers列表（有序）
     */
    private static final int[] FRAGMENT_IDS_ORDER_BY_POSITION = {
            R.id.fragment_promotion,
            R.id.fragment_videos,
            R.id.fragment_genres,
            R.id.fragment_favorite,
            R.id.fragment_history
    };

    /**
     * 默认Pager的位置
     */
    private static final int DEFAULT_POSITION = 1;

    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;

    private NetworkChecker mNetworkChecker;

    private MyFragmentPagerAdapter mAdapter;

    private ViewPager mPager;

    private String mUserToken;

    private boolean mIsLogin;

    //--------------------------------------------------------------------------
    // 覆写Activity的生命周期方法
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //友盟自动更新
        UmengUpdateAgent.update(this);
        //set preferences' default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mDefaultSharedPreferencesSingleton = DefaultSharedPreferencesSingleton.getInstance(this);
        mNetworkChecker = NetworkChecker.newInstance(this);

        mUserToken = mDefaultSharedPreferencesSingleton.retrieveString("user_token", "");
        mIsLogin = !mUserToken.isEmpty();

        setContentView(R.layout.activity_main);

        mAdapter = new MyFragmentPagerAdapter(
                this,
                getFragmentManager(),
                FRAGMENT_IDS_ORDER_BY_POSITION
        );
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        // Bind the indicators to the ViewPager
        TitlePageIndicator mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        // goto default pager
        mPager.setCurrentItem(DEFAULT_POSITION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof HistoryFragment) {
            ((HistoryFragment) fragment).refreshData();
        }
    }

    @Override
    protected void onDestroy() {
        mPager = null;
        mAdapter = null;
        mNetworkChecker = null;
        super.onDestroy();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            mPager.setCurrentItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, mPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.action_login);
        if (mIsLogin) {
            loginItem.setIcon(R.drawable.logout);
            loginItem.setTitle(R.string.action_logout);
        } else {
            loginItem.setIcon(R.drawable.login);
            loginItem.setTitle(R.string.action_login);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_login:
                if (mIsLogin) {
                    mDefaultSharedPreferencesSingleton.saveString("user_token", "");
                    mIsLogin = false;
                    invalidateOptionsMenu();
                    Toast.makeText(this, R.string.action_logout_success, Toast.LENGTH_SHORT).show();
                } else {
                    if (!mNetworkChecker.isConnected()) {
                        Toast.makeText(this, R.string.action_login_no_net, Toast.LENGTH_SHORT)
                                .show();
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
                    mIsLogin = true;
                    invalidateOptionsMenu();
                    Fragment fragment = getCurrentFragment();
                    if (fragment instanceof FavoriteFragment) {
                        ((FavoriteFragment) fragment).getJsonData();
                    }
                }
        }
    }

    //--------------------------------------------------------------------------
    // 新声明方法
    //--------------------------------------------------------------------------

    private Fragment getCurrentFragment() {
        return mAdapter.getItem(mPager.getCurrentItem());
    }

    //--------------------------------------------------------------------------
    // 嵌套类
    //--------------------------------------------------------------------------

    /**
     * 本类不是线程安全的。请仅在主线程中使用。
     */
    private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private final Context mContext;

        private final int[] mFragmentIdsOrderByPosition;

        private final Map<Integer, FragmentHolder> mIdFragmentMap;

        public MyFragmentPagerAdapter(
                Context context, FragmentManager fm, int[] fragmentIdsOrderByPosition) {
            super(fm);
            mContext = context;
            mFragmentIdsOrderByPosition =
                    Arrays.copyOf(fragmentIdsOrderByPosition, fragmentIdsOrderByPosition.length);
            // 参考 http://stackoverflow.com/questions/15844035/best-hashmap-initial-capacity-while-indexing-a-list#15844186
            mIdFragmentMap = new HashMap<>(mFragmentIdsOrderByPosition.length, 1);
        }

        @Override
        public int getCount() {
            return mFragmentIdsOrderByPosition.length;
        }

        /**
         * @see #getItemById(int)
         */
        @NonNull
        @Override
        public CharSequence getPageTitle(int position) {
            return getItemById((int) getItemId(position)).title;
        }

        /**
         * @see #getItemById(int)
         */
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return getItemById((int) getItemId(position)).fragment;
        }

        @Override
        public long getItemId(int position) {
            return mFragmentIdsOrderByPosition[position];
        }

        /**
         * 取得指定ID的{@link Fragment}和它的Title。
         * <p>每次用相同的id调用此方法，都会返回相同的实例。</p>
         *
         * @see MyFragmentPagerAdapter
         */
        public FragmentHolder getItemById(int id) {
            if (!mIdFragmentMap.containsKey(id)) {
                mIdFragmentMap.put(id, createItemById(id));
            }
            return mIdFragmentMap.get(id);
        }

        private FragmentHolder createItemById(int id) {
            Fragment fragment;
            String title;
            switch (id) {
                case R.id.fragment_videos:
                    fragment = new ListVideosFragment();
                    title = mContext.getString(R.string.popular);
                    break;
                case R.id.fragment_genres:
                    fragment = new ListGenresFragment();
                    title = mContext.getString(R.string.genre);
                    break;
                case R.id.fragment_favorite:
                    fragment = new FavoriteFragment();
                    title = mContext.getString(R.string.favorites);
                    break;
                case R.id.fragment_history:
                    fragment = new HistoryFragment();
                    title = mContext.getString(R.string.history);
                    break;
                case R.id.fragment_promotion:
                    fragment = new PromotionFragment();
                    title = mContext.getString(R.string.promotion);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fragment id: " + id);
            }
            return new FragmentHolder(fragment, title);
        }

        private static class FragmentHolder {

            @NonNull
            public final Fragment fragment;

            @NonNull
            public final String title;

            private FragmentHolder(@NonNull Fragment fragment, @NonNull String title) {
                this.fragment = fragment;
                this.title = title;
            }
        }
    }

}
