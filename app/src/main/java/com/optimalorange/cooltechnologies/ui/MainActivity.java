package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListGenresFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PromotionFragment;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends LoginableBaseActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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

    /**
     * 状态属性：被重创建了
     *
     * @see #onRestoreInstanceState(android.os.Bundle)
     */
    private boolean mHasBeenReinitialized = false;

    private MyFragmentPagerAdapter mAdapter;

    private ViewPager mPager;

    //--------------------------------------------------------------------------
    // 覆写Activity的生命周期方法
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalSetup();

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
        // setOnPageChangeListener
        mIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (FRAGMENT_IDS_ORDER_BY_POSITION[position] == R.id.fragment_promotion) {
                    showLoginOrLogoutMenuItem(false);
                } else {
                    showLoginOrLogoutMenuItem(true);
                }
            }
        });
        // goto default pager
        mPager.setCurrentItem(DEFAULT_POSITION);
    }

    @Override
    protected void onDestroy() {
        mPager = null;
        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mHasBeenReinitialized = true;
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            mPager.setCurrentItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, mPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    //--------------------------------------------------------------------------
    // 新声明方法
    //--------------------------------------------------------------------------

    private void globalSetup() {
        //有Fragment，禁止默认的页面统计方式，不自动统计Activity
        MobclickAgent.openActivityDurationTrack(false);
        //发送策略
        MobclickAgent.updateOnlineConfig(this);
        //日志加密设置
        AnalyticsConfig.enableEncrypt(true);
        //友盟自动更新
        UmengUpdateAgent.update(this);
        //set preferences' default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    private Fragment getCurrentFragment() {
        return mAdapter.getItem(mPager.getCurrentItem());
    }

    //--------------------------------------------------------------------------
    // 嵌套类
    //--------------------------------------------------------------------------

    /**
     * 本类不是线程安全的。请仅在主线程中使用。
     */
    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private final Context mContext;

        private final int[] mFragmentIdsOrderByPosition;

        private final Map<Integer, ItemHolder> mIdFragmentMap;

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
         * {@inheritDoc}
         * <p><b>Note</b>: the return value will be changed after Configuration Changes (updated in
         * {@link #instantiateItem(android.view.ViewGroup, int)})</p>
         * <p><b>Note</b>: after Configuration Changes and before
         * {@link #instantiateItem(android.view.ViewGroup, int)} finished, the return value likely
         * is <b>not</b> the one actually in {@link android.support.v4.view.ViewPager}</p>
         *
         * @see #getItemById(int)
         */
        @NonNull
        @Override
        public Fragment getItem(int position) {
            ItemHolder itemHolder = getItemById((int) getItemId(position));
            if (mHasBeenReinitialized && !itemHolder.hasBeenInstantiated) {
                Log.w(LOG_TAG, String.format("Activity has been re-initialized "
                        + "but the result of getItem(%d) hasn't been instantiated", position));
                Log.w(LOG_TAG, String.format(
                        "getItem(%d) is called by %s",
                        position,
                        Thread.currentThread().getStackTrace()[3]));
            }
            return itemHolder.fragment;
        }

        @Override
        public long getItemId(int position) {
            return mFragmentIdsOrderByPosition[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment item = (Fragment) super.instantiateItem(container, position);
            ItemHolder itemHolder = getItemById((int) getItemId(position));
            // after Configuration Changes, "item" is a fragment recreated by system and
            // "fragmentHolder.fragment" is a new instance, so fragmentHolder.fragment != item
            if (itemHolder.fragment != item) {
                itemHolder.fragment = item;
            }
            itemHolder.hasBeenInstantiated = true;
            return item;
        }

        /**
         * 取得指定ID的{@link Fragment}和它的Title。
         * <p>一般情况下，每次用相同的id调用此方法，都会返回相同的实例。</p>
         * <p><b>Note</b>：Configuration Changes会导致此方法（相同id）返回不同实例，
         * 并且其内部的fragment会在{@link #instantiateItem(android.view.ViewGroup, int)}更新。</p>
         * <p><b>Note</b>：在Configuration Changes之后，
         * {@link #instantiateItem(android.view.ViewGroup, int)}完成之前，本方法返回的
         * {@link ItemHolder ItemHolder}内的{@link ItemHolder#fragment fragment}很可能与
         * {@link ViewPager}中的不一致。</p>
         *
         * @see MyFragmentPagerAdapter
         */
        @NonNull
        public ItemHolder getItemById(int id) {
            if (!mIdFragmentMap.containsKey(id)) {
                mIdFragmentMap.put(id, createItemById(id));
            }
            return mIdFragmentMap.get(id);
        }

        private ItemHolder createItemById(int id) {
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

            return new ItemHolder(fragment, title);
        }

        private class ItemHolder {

            /**
             * 已经由{@link #instantiateItem(android.view.ViewGroup, int)}实例化过。
             * {@link #fragment}与{@link ViewPager}中的{@link Fragment fragment}一致。
             */
            public boolean hasBeenInstantiated = false;

            @NonNull
            public Fragment fragment;

            @NonNull
            public String title;

            private ItemHolder(@NonNull Fragment fragment, @NonNull String title) {
                this.fragment = fragment;
                this.title = title;
            }
        }
    }

}
