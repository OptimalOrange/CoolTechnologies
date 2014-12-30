package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.StaggeredGridView;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.ui.view.PullRefreshLayout;
import com.optimalorange.cooltechnologies.util.Utils;
import com.optimalorange.cooltechnologies.network.VideosRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
 * Created by WANGZHENGZE on 2014/11/20.
 * 热门
 */
public class ListVideosFragment extends Fragment {

    // Fragment初始化参数

    /**
     * 应当显示的Video的genre（类型，示例：手机）<br/>
     * Type: String
     *
     * @see #newInstance(String genre)
     */
    public static final String ARGUMENT_KEY_GENRE =
            ListVideosFragment.class.getName() + ".argument.KEY_GENRE";

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    /**
     * 应当显示的Video的genre（类型，示例：手机）。null表示显示所有类别的Video。
     *
     * @see #ARGUMENT_KEY_GENRE
     */
    @Nullable
    private String mGenre;

    private int mPage = 1;

    private StaggeredGridView mGridView;

    private PullRefreshLayout mPullRefreshLayout;

    private ItemsAdapter mItemsAdapter;

    private LinkedList<Video> mListVideos = new LinkedList<Video>();

    /**
     * 获取Video（见entity包中Video）
     *
     * @return VideoRequest
     */
    private VideosRequest buildQueryVideosRequest() {
        VideosRequest.Builder builder = new VideosRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setPage(mPage)
                .setPeriod(VideosRequest.Builder.PERIOD.WEEK)
                .setOrderby(VideosRequest.Builder.ORDER_BY.VIEW_COUNT)
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        for (Video mVideo : videos) {
                            mListVideos.add(mVideo);
                            if (mItemsAdapter != null) {
                                mItemsAdapter.notifyDataSetChanged();
                            }
                            if (mPullRefreshLayout != null) {
                                mPullRefreshLayout.setRefreshing(false);
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

        //如果没设置mGenre就用默认的，如果设置了mGenre就请求相应的类型Video
        if (mGenre != null) {
            builder.setGenre(mGenre);
        }
        return builder.build();
    }

    /**
     * 用于 创建设置有指定参数的新{@link ListVideosFragment}实例的 工厂方法
     *
     * @param genre 应当显示的Video的genre（类型，示例：手机）
     * @return 设置有指定参数的新实例
     * @see #ARGUMENT_KEY_GENRE
     */
    public static ListVideosFragment newInstance(String genre) {
        ListVideosFragment fragment = new ListVideosFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_KEY_GENRE, genre);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 如果设置过Arguments，应用之
        if (getArguments() != null) {
            mGenre = getArguments().getString(ARGUMENT_KEY_GENRE);
        }
        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mVolleySingleton.addToRequestQueue(buildQueryVideosRequest());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list_videos, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridView = (StaggeredGridView) view.findViewById(R.id.grid_view);
        mPullRefreshLayout = (PullRefreshLayout) view.findViewById(R.id.pull_refresh_layout);
        mItemsAdapter = new ItemsAdapter(mListVideos, mVolleySingleton.getImageLoader());
        mGridView.setAdapter(mItemsAdapter);

        mPullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //每次刷新时去除所有Video
                mListVideos.clear();
                //重新请求第一页的内容
                mPage = 1;
                mItemsAdapter.notifyDataSetChanged();
                //开始请求刷新Video
                mVolleySingleton.addToRequestQueue(buildQueryVideosRequest());
            }
        });

        //item点击监听，点击进行播放视频
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mIntent = new Intent(getActivity(), PlayVideoActivity.class);
                FavoriteBean favoriteBean = new FavoriteBean(mListVideos.get(i));
                mIntent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO_ID, favoriteBean);
                startActivity(mIntent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));

    }

    /**
     * 热门视频的图片墙的适配器
     *
     * @author Zhou Peican
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
                mVolleySingleton.addToRequestQueue(buildQueryVideosRequest());
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
