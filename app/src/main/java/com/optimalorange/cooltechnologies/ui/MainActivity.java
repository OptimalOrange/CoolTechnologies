package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListGenresFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PromotionFragment;
import com.optimalorange.cooltechnologies.util.Const;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.util.Utils;
import com.umeng.update.UmengUpdateAgent;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


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

    private ViewPager mPager;

    private String mUserToken;

    private boolean mIsLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //友盟自动更新
        UmengUpdateAgent.update(this);
        //set preferences' default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);

        mUserToken = Utils.getString(this, "user_token", "");
        mIsLogin = !mUserToken.isEmpty();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyFragmentPagerAdapter(
                this,
                getFragmentManager(),
                FRAGMENT_IDS_ORDER_BY_POSITION
        ));
        // Bind the indicators to the ViewPager
        TitlePageIndicator mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        // goto default pager
        mPager.setCurrentItem(DEFAULT_POSITION);
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
        } else {
            loginItem.setIcon(R.drawable.login);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_login:
                if (mIsLogin) {
                    Utils.saveString(this, "user_token", "");
                    mIsLogin = false;
                    invalidateOptionsMenu();
                    Toast.makeText(this, R.string.action_logout_success, Toast.LENGTH_SHORT).show();
                } else {
                    if (!NetworkChecker.isConnected(this)) {
                        Toast.makeText(this, R.string.action_login_no_net, Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    }
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, Const.REQUEST_CODE_LOGIN_ACTIVITY);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private Context mContext;

        private int[] mFragmentIdsOrderByPosition;

        public MyFragmentPagerAdapter(
                Context context, FragmentManager fm, int[] fragmentIdsOrderByPosition) {
            super(fm);
            mContext = context;
            mFragmentIdsOrderByPosition = fragmentIdsOrderByPosition;
        }

        @Override
        public int getCount() {
            return mFragmentIdsOrderByPosition.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getTitleById((int) getItemId(position));
        }

        @Override
        public Fragment getItem(int position) {
            return getItemById((int) getItemId(position));
        }

        @Override
        public long getItemId(int position) {
            return mFragmentIdsOrderByPosition[position];
        }

        private String getTitleById(int id) {
            switch (id) {
                case R.id.fragment_videos:
                    return mContext.getString(R.string.popular);
                case R.id.fragment_genres:
                    return mContext.getString(R.string.genre);
                case R.id.fragment_favorite:
                    return mContext.getString(R.string.favorites);
                case R.id.fragment_history:
                    return mContext.getString(R.string.history);
                case R.id.fragment_promotion:
                    return mContext.getString(R.string.promotion);
                default:
                    throw new IllegalArgumentException("Unknown fragment id: " + id);
            }
        }

        private Fragment getItemById(int id) {
            switch (id) {
                case R.id.fragment_videos:
                    return new ListVideosFragment();
                case R.id.fragment_genres:
                    return new ListGenresFragment();
                case R.id.fragment_favorite:
                    return new FavoriteFragment();
                case R.id.fragment_history:
                    return new HistoryFragment();
                case R.id.fragment_promotion:
                    return new PromotionFragment();
                default:
                    throw new IllegalArgumentException("Unknown fragment id: " + id);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof HistoryFragment) {
            ((HistoryFragment) fragment).refreshData();
        }
    }

    private Fragment getCurrentFragment() {
        final String name = makeFragmentName(
                R.id.pager, FRAGMENT_IDS_ORDER_BY_POSITION[mPager.getCurrentItem()]);
        return getFragmentManager().findFragmentByTag(name);
    }

    /**
     * copy from {@link FragmentPagerAdapter#makeFragmentName(int, long)}
     */
    // TODO shouldn't depend on others private code
    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
