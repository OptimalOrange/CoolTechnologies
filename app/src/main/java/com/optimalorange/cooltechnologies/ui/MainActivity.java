package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.fragment.FavoriteFragment;
import com.optimalorange.cooltechnologies.ui.fragment.HistoryFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListCategoriesFragment;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;
import com.optimalorange.cooltechnologies.ui.fragment.PromotionFragment;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private static final int[] FRAGMENT_IDS_ORDER_BY_POSITION = {
            FragmentConfig.FRAGMENT_ID_POPULAR,
            FragmentConfig.FRAGMENT_ID_CATEGORIES,
            FragmentConfig.FRAGMENT_ID_FAVORITE,
            FragmentConfig.FRAGMENT_ID_HISTORY,
            FragmentConfig.FRAGMENT_ID_PROMOTION
    };

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(
                getFragmentManager(),
                getResources().getStringArray(R.array.titles_in_cool_videos)
        ));
        // Bind the indicators to the ViewPager
        TitlePageIndicator mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] mPageTitles;

        public MyPagerAdapter(FragmentManager fm, String[] pageTitles) {
            super(fm);
            mPageTitles = pageTitles;
        }

        @Override
        public int getCount() {
            return mPageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentConfig.getInstance().getFragment((int) getItemId(position));
        }

        @Override
        public long getItemId(int position) {
            return FRAGMENT_IDS_ORDER_BY_POSITION[position];
        }
    }

    private static class FragmentConfig {

        public static final int FRAGMENT_ID_POPULAR = 0;

        public static final int FRAGMENT_ID_CATEGORIES = 1;

        public static final int FRAGMENT_ID_FAVORITE = 2;

        public static final int FRAGMENT_ID_HISTORY = 3;

        public static final int FRAGMENT_ID_PROMOTION = 4;

        public static FragmentConfig instance;

        public static FragmentConfig getInstance() {
            if (instance == null) {
                instance = new FragmentConfig();
            }
            return instance;
        }

        public Fragment getFragment(int id) {
            Fragment newFragment = null;
            switch (id) {
                case FRAGMENT_ID_POPULAR:
                    newFragment = new ListVideosFragment();
                    break;
                case FRAGMENT_ID_CATEGORIES:
                    newFragment = new ListCategoriesFragment();
                    break;
                case FRAGMENT_ID_FAVORITE:
                    newFragment = new FavoriteFragment();
                    break;
                case FRAGMENT_ID_HISTORY:
                    newFragment = new HistoryFragment();
                    break;
                case FRAGMENT_ID_PROMOTION:
                    newFragment = new PromotionFragment();
                    break;
            }
            return newFragment;
        }
    }

}
