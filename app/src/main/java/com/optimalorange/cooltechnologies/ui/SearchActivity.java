package com.optimalorange.cooltechnologies.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.StaggeredGridView;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.util.SearchRequest;
import com.optimalorange.cooltechnologies.util.Utils;
import com.optimalorange.cooltechnologies.util.VolleySingleton;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * 搜索页面
 *
 * @author Zhou Peican
 */
public class SearchActivity extends Activity {

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private String mKeyWord;

    private int mPage = 1;

    private VolleySingleton mVolleySingleton;

    private StaggeredGridView mGridView;

    private ItemsAdapter mItemsAdapter;

    private LinkedList<Video> mListVideos = new LinkedList<Video>();

    private SearchRequest buildSearchVideosRequest() {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setKeyword(mKeyWord)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setPage(mPage)
                .setPeriod(SearchRequest.Builder.PERIOD.HISTORY)
                .setOrderby(SearchRequest.Builder.ORDER_BY.VIEW_COUNT)
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        for (Video mVideo : videos) {
                            mListVideos.add(mVideo);
                            if (mItemsAdapter != null) {
                                mItemsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        //为下一次请求获取Video翻页
        mPage++;

        return builder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(this);

        mGridView = (StaggeredGridView) findViewById(R.id.grid_view);
        mItemsAdapter = new ItemsAdapter(mListVideos, mVolleySingleton.getImageLoader());
        mGridView.setAdapter(mItemsAdapter);
        //item点击监听，点击进行播放视频
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mIntent = new Intent(SearchActivity.this, PlayVideoActivity.class);
                FavoriteBean favoriteBean = new FavoriteBean(mListVideos.get(i));
                mIntent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO_ID, favoriteBean);
                startActivity(mIntent);
            }
        });

        //处理搜索，当在actionbar上的SearchView输入完成点击搜索时
        // 系统会启动一个搜索请求，由本页面自身接收（singleTop）
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        /* 清除原有的搜索结果 */
        if (mListVideos.size() > 0) {
            mListVideos.clear();
            mItemsAdapter.notifyDataSetChanged();
        }

        //搜索请求
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mKeyWord = intent.getStringExtra(SearchManager.QUERY);
            mVolleySingleton.addToRequestQueue(buildSearchVideosRequest());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * 搜索结果适配器
     */
    private class ItemsAdapter extends BaseAdapter {

        private LinkedList<Video> mVideos;

        private ImageLoader mImageLoader;

        public ItemsAdapter(LinkedList<Video> mVideos, ImageLoader mImageLoader) {
            super();
            this.mVideos = mVideos;
            this.mImageLoader = mImageLoader;
        }

        @Override
        public int getCount() {
            return mVideos.size();
        }

        @Override
        public Object getItem(int position) {
            return mVideos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_videos, parent, false);
                vh = new ViewHolder();
                vh.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                vh.duration = (TextView) convertView.findViewById(R.id.duration);
                vh.title = (TextView) convertView.findViewById(R.id.title);
                vh.viewCount = (TextView) convertView.findViewById(R.id.view_count);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            //加载图片
            mImageLoader.get(mVideos.get(position).getThumbnail_v2(),
                    ImageLoader.getImageListener(vh.thumbnail,
                            R.drawable.ic_launcher, R.drawable.ic_launcher));
            //显示播放时长
            vh.duration.setText(Utils.getDurationString(mVideos.get(position).getDuration()));
            //显示视频标题
            vh.title.setText(mVideos.get(position).getTitle());
            //显示播放次数（这里使用字符串资源格式化）
            vh.viewCount.setText(String.format(getString(R.string.view_count),
                    Utils.formatViewCount(mVideos.get(position).getView_count(),
                            parent.getContext())));

            //当滑到末尾的位置时加载更多Video
            if (position == mListVideos.size() - 2) {
                mVolleySingleton.addToRequestQueue(buildSearchVideosRequest());
            }

            return convertView;
        }

        private class ViewHolder {

            ImageView thumbnail;

            TextView duration;

            TextView title;

            TextView viewCount;
        }

    }
}
