package com.optimalorange.cooltechnologies.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.listener.OnRecyclerViewScrollListener;
import com.optimalorange.cooltechnologies.network.SearchRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * 搜索页面
 *
 */
public class SearchActivity extends BaseActivity {

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private String mKeyWord;

    private int mPage = 1;

    private VolleySingleton mVolleySingleton;

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private LinkedList<Video> mListVideos = new LinkedList<Video>();

    private MaterialProgressBar mProgressBar;

    /** 是否有正在获取Video */
    private boolean mIsQueryingVideos = false;

    private OnRecyclerViewScrollListener mScrollListener;

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
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        //为下一次请求获取Video翻页
                        mPage++;
                        mIsQueryingVideos = false;
                        mProgressBar.setVisibility(View.GONE);
                        if (videos.size() == 0) {
                            removeListener();
                            Toast.makeText(SearchActivity.this, getString(R.string.at_last),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mIsQueryingVideos = false;
                        mProgressBar.setVisibility(View.GONE);
                    }
                });

        return builder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(this);

        mProgressBar = (MaterialProgressBar) findViewById(R.id.progressbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(mListVideos, mVolleySingleton.getImageLoader());
        mRecyclerView.setAdapter(mAdapter);

        initListener();

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
            mAdapter.notifyDataSetChanged();
        }

        //搜索请求
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mKeyWord = intent.getStringExtra(SearchManager.QUERY);
            mVolleySingleton.addToRequestQueue(buildSearchVideosRequest());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void initListener() {
        mScrollListener = new OnRecyclerViewScrollListener() {
            @Override
            public void onBottom() {
                super.onBottom();
                if (!mIsQueryingVideos) {
                    mIsQueryingVideos = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    mVolleySingleton.addToRequestQueue(buildSearchVideosRequest());
                }
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void removeListener() {
        if (mScrollListener != null && mRecyclerView != null){
            mRecyclerView.removeOnScrollListener(mScrollListener);
            mScrollListener = null;
        }
    }

    /**
     * 搜索结果适配器
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private LinkedList<Video> mVideos;

        private ImageLoader mImageLoader;

        public MyAdapter(LinkedList<Video> mVideos, ImageLoader mImageLoader) {
            this.mVideos = mVideos;
            this.mImageLoader = mImageLoader;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_video, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            //加载图片
            mImageLoader.get(mVideos.get(position).getThumbnail_v2(),
                    ImageLoader.getImageListener(holder.thumbnail,
                            R.drawable.ic_image_view_placeholder,
                            R.drawable.ic_image_view_placeholder));
            //显示播放时长
            holder.duration.setText(Utils.getDurationString(mVideos.get(position).getDuration()));
            //显示视频标题
            holder.title.setText(mVideos.get(position).getTitle());
            //显示播放次数（这里使用字符串资源格式化）
            holder.viewCount.setText(String.format(getString(R.string.view_count),
                    Utils.formatViewCount(mVideos.get(position).getView_count(),
                            SearchActivity.this)));

            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowVideoDetailActivity.start(
                            v.getContext(), mListVideos.get(position).getId());
                }
            });

        }

        @Override
        public int getItemCount() {
            return mVideos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView thumbnail;

            TextView duration;

            TextView title;

            TextView viewCount;

            public ViewHolder(View v) {
                super(v);
                thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
                duration = (TextView) v.findViewById(R.id.duration);
                title = (TextView) v.findViewById(R.id.title);
                viewCount = (TextView) v.findViewById(R.id.view_count);
            }
        }
    }
}
