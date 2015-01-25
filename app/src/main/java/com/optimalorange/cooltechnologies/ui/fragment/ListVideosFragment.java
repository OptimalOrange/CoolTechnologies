package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.StaggeredGridView;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VideosRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.util.Utils;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
public class ListVideosFragment extends SwipeRefreshFragment {

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

    private NetworkChecker mNetworkChecker;

    /** 网络请求管理器 */
    private final RequestsManager mRequestsManager = new RequestsManager();

    /**
     * 状态属性：网络联通性。true表示已连接网络；false表示网络已断开。
     */
    private boolean mIsConnected = false;

    private BroadcastReceiver mNetworkReceiver;

    private View mNoConnectionView;

    private View mEmptyView;

    private View mMainContentView;

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
                        }
                        applyVideos();
                        mRequestsManager.addRequestRespondeds();
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mRequestsManager.addRequestErrors();
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
        //检测网络是否连接
        mNetworkChecker = NetworkChecker.newInstance(getActivity());
        /* 注册网络监听 */
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setIsConnected(mNetworkChecker.isConnected());
            }
        };
        getActivity().registerReceiver(mNetworkReceiver, filter);
        //根据网络状态设置显示的view
        setIsConnected(mNetworkChecker.isConnected());
        applyIsConnected();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateChildView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_videos, container, false);
        mMainContentView = rootView.findViewById(R.id.main_content);
        mGridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);
        mEmptyView = rootView.findViewById(android.R.id.empty);
        mNoConnectionView = rootView.findViewById(R.id.no_connection);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mItemsAdapter = new ItemsAdapter(mListVideos, mVolleySingleton.getImageLoader());
        mGridView.setAdapter(mItemsAdapter);

        /* 点击设置网络 */
        mNoConnectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConnection();
            }
        });

        applyVideos();
        applyIsConnected();
        if (mIsConnected) {
            setRefreshing(true);
            startLoad();
        }

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
    public void onDestroyView() {
        mNoConnectionView = null;
        mEmptyView = null;
        mGridView.setAdapter(null);
        mGridView = null;
        mMainContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        cancelLoad();
        mItemsAdapter = null;
        if (mNetworkReceiver != null) {
            getActivity().unregisterReceiver(mNetworkReceiver);
        }
        mNetworkReceiver = null;
        mNetworkChecker = null;
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        //每次刷新时去除所有Video
        mListVideos.clear();
        //重新请求第一页的内容
        mPage = 1;
        mItemsAdapter.notifyDataSetChanged();
        restartLoad();
    }

    @Override
    protected boolean canChildScrollUp() {
        return mGridView.getVisibility() == View.VISIBLE &&
                mGridView.canScrollVertically(-1);
    }

    private void startLoad() {
        if (mIsConnected) {
            mRequestsManager.addRequest(buildQueryVideosRequest());
        }
    }

    private void restartLoad() {
        cancelLoad();
        startLoad();
    }

    private void cancelLoad() {
        mVolleySingleton.getRequestQueue().cancelAll(this);
        mRequestsManager.reset();
    }

    private void onLoadFinished() {
        setRefreshing(false);
    }

    public void setIsConnected(boolean isConnected) {
        if (mIsConnected != isConnected) {
            mIsConnected = isConnected;
            applyIsConnected();
        }
    }

    private void applyIsConnected() {
        if (mNoConnectionView != null) {
            mNoConnectionView.setVisibility(mIsConnected ? View.GONE : View.VISIBLE);
        }
        if (mMainContentView != null) {
            mMainContentView.setVisibility(mIsConnected ? View.VISIBLE : View.GONE);
        }
        setRefreshable(mIsConnected);
    }

    public boolean videosIsEmpty() {
        return mListVideos == null || mListVideos.size() == 0;
    }

    private void applyVideos() {
        if (mItemsAdapter != null) {
            mItemsAdapter.notifyDataSetChanged();
        }
        final boolean isEmpty = videosIsEmpty();
        if (mEmptyView != null) {
            mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private boolean setConnection() {
        return NetworkChecker.openWirelessSettings(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
     * {@link com.android.volley.Request Requests}管理器。用于统计Requests状态。
     */
    private class RequestsManager {

        private int mRequests = 0;

        private int mRequestRespondeds = 0;

        private int mRequestErrors = 0;

        private int mRequestCancelleds = 0;

        /**
         * 初始化总{@link com.android.volley.Request}数为0
         */
        private void reset() {
            mRequests = mRequestRespondeds = mRequestErrors = mRequestCancelleds = 0;
        }

        /**
         * 添加{@link com.android.volley.Request}数
         *
         * @return 添加后，总Request数
         */
        public int addRequest(Request request) {
            mVolleySingleton.addToRequestQueue(request);
            return mRequests++;
        }

        /**
         * 添加收到响应的{@link Request}数
         *
         * @return 添加后，总收到响应的Request数
         */
        public int addRequestRespondeds() {
            int result = mRequestRespondeds++;
            checkIsAllRequestsFinished();
            return result;
        }

        /**
         * 添加失败的{@link Request}数
         *
         * @return 添加后，总失败的Request数
         */
        public int addRequestErrors() {
            int result = mRequestErrors++;
            checkIsAllRequestsFinished();
            return result;
        }

        /**
         * 添加取消的{@link Request}数
         *
         * @return 添加后，总取消的Request数
         */
        public int addRequestCancelleds() {
            int result = mRequestCancelleds++;
            checkIsAllRequestsFinished();
            return result;
        }

        public int getRequestFinisheds() {
            return mRequestRespondeds + mRequestErrors;
        }

        public boolean isAllRequestsFinished() {
            return mRequests == getRequestFinisheds() + mRequestCancelleds;
        }

        private void checkIsAllRequestsFinished() {
            if (isAllRequestsFinished()) {
                onLoadFinished();
            }
        }
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
                        .inflate(R.layout.list_item_video, parent, false);
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
