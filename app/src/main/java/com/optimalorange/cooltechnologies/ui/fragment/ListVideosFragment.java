package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.listener.OnRecyclerViewScrollListener;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.RequestsManager;
import com.optimalorange.cooltechnologies.network.VideosRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.ui.ShowVideoDetailActivity;
import com.optimalorange.cooltechnologies.util.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
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
    private RequestsManager mRequestsManager;

    /**
     * 状态属性：网络联通性。true表示已连接网络；false表示网络已断开。
     */
    private boolean mIsConnected = false;

    private BroadcastReceiver mNetworkReceiver;

    private View mNoConnectionView;

    private View mEmptyView;

    private View mMainContentView;


    /**
     * 应当显示的Video的genre（类型，示例：手机）。null表示显示所有类别的Video。
     *
     * @see #ARGUMENT_KEY_GENRE
     */
    @Nullable
    private String mGenre;

    private int mPage = 1;

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private LinkedList<Video> mListVideos = new LinkedList<Video>();

    private MaterialProgressBar mProgressBar;

    /** 是否有正在获取Video */
    private boolean mIsQueryingVideos = false;

    private OnRecyclerViewScrollListener mScrollListener;

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
                        //为下一次请求获取Video翻页
                        mPage++;
                        mIsQueryingVideos = false;
                        mProgressBar.setVisibility(View.GONE);
                        if (videos.size() == 0) {
                            removeListener();
                            Toast.makeText(getActivity(), getString(R.string.at_last),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mRequestsManager.addRequestErrors();
                        mIsQueryingVideos = false;
                        mProgressBar.setVisibility(View.GONE);
                    }
                });

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
        mRequestsManager = new RequestsManager(VolleySingleton.getInstance(getActivity()));
        mRequestsManager.setOnAllRequestsFinishedListener(
                new RequestsManager.OnAllRequestsFinishedListener() {
                    @Override
                    public void onAllRequestsFinished(RequestsManager requestsManager) {
                        onLoadFinished();
                    }
                });
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
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mEmptyView = rootView.findViewById(android.R.id.empty);
        mNoConnectionView = rootView.findViewById(R.id.no_connection);
        mProgressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressbar);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(
                mListVideos, mRequestsManager.getVolleySingleton().getImageLoader());
        mRecyclerView.setAdapter(mAdapter);

        initListener();


        /* 点击设置网络 */
        mNoConnectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConnection();
            }
        });

        applyVideos();
        applyIsConnected();
        if (mIsConnected && listVideosIsEmpty()) {
            setRefreshing(true);
            startLoad();
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
    public void onDestroyView() {
        mNoConnectionView = null;
        mEmptyView = null;
        mRecyclerView.setAdapter(null);
        removeListener();
        mRecyclerView = null;
        mMainContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        cancelLoad();
        mAdapter = null;
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
        mAdapter.notifyDataSetChanged();
        restartLoad();
        resetListener();
    }

    @Override
    protected boolean canChildScrollUp() {
        return mRecyclerView.getVisibility() == View.VISIBLE &&
                mRecyclerView.canScrollVertically(-1);
    }

    private void initListener() {
        mScrollListener = new OnRecyclerViewScrollListener() {
            @Override
            public void onBottom() {
                super.onBottom();
                if (!mIsQueryingVideos) {
                    mIsQueryingVideos = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRequestsManager.addRequest(buildQueryVideosRequest());
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

    private void resetListener(){
        removeListener();
        initListener();
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

    public boolean listVideosIsEmpty() {
        return mListVideos.size() == 0;
    }

    private void applyVideos() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
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
                (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));

    }

    /**
     * 热门视频的图片墙的适配器
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
                    Utils.formatViewCount(mVideos.get(position).getView_count(), getActivity())));

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
