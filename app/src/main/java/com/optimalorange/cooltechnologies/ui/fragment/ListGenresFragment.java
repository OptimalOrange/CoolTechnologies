package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VideosRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.ui.ListVideosActivity;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.ui.view.VideoCardViewBuilder;
import com.optimalorange.cooltechnologies.util.ItemsCountCalculater;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 分类
 */
//TODO reCreat问题（横竖屏转换、接完电话回来时）
//TODO 点击时的视觉表现（动画等）
public class ListGenresFragment extends SwipeRefreshFragment {

    /**
     * @see {@literal http://open.youku.com/docs/api_searches.html#schemas-video-category}
     */
    private static final String YOUKU_API_SCHEMAS_VIDEO_CATEGORY
            = "https://openapi.youku.com/v2/schemas/video/category.json";

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    private NetworkChecker mNetworkChecker;

    /**
     * 类型列表及每种类型对应的视频
     */
    private Pair<ArrayList<String>, ArrayList<List<Video>>> mGenres;

    private final RequestsManager mRequestsManager = new RequestsManager();

    /**
     * 状态属性：网络联通性。true表示已连接网络；false表示网络已断开。
     */
    private boolean mIsConnected = false;

    private BroadcastReceiver mNetworkReceiver;


    private View mMainContentView;

    private RecyclerView mRecyclerView;

    private View mEmptyView;

    private View mNoConnectionView;

    private RecyclerView.Adapter mAdapter;

    private ItemsCountCalculater.Result mItemsCountAndDimension;

    //--------------------------------------------------------------------------
    // Network Request to Youku
    //--------------------------------------------------------------------------

    private final Request mVideoCategorySchemaRequest = new JsonObjectRequest(
            Request.Method.GET,
            YOUKU_API_SCHEMAS_VIDEO_CATEGORY,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    // see YOUKU_API_SCHEMAS_VIDEO_CATEGORY
                    JSONObject techCategory = null;
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("categories");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject currentObject = jsonArray.optJSONObject(i);
                            if (currentObject == null) {
                                continue;
                            }
                            if (CATEGORY_LABEL_OF_TECH.equals(currentObject.optString("label"))) {
                                techCategory = currentObject;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (techCategory != null) {
                        JSONArray genresJson = techCategory.optJSONArray("genres");
                        final int length = genresJson != null ? genresJson.length() : 0;
                        ArrayList<String> genres = new ArrayList<>(length);
                        ArrayList<List<Video>> videos = new ArrayList<>(length);
                        final Pair<ArrayList<String>, ArrayList<List<Video>>> newGenres
                                = new Pair<>(genres, videos);
                        for (int i = 0; i < length; i++) {
                            try {
                                String genre = genresJson.getJSONObject(i).getString("label");
                                genres.add(genre);
                                videos.add(null);
                                mRequestsManager.addRequest(
                                        buildQueryVideosRequest(genre, newGenres, i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        setGenres(newGenres);
                    } else {
                        setGenres(null);
                    }
                    mRequestsManager.addRequestRespondeds();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                    mRequestsManager.addRequestErrors();
                }
            }
    ).setTag(this);

    /**
     * @param genre  类型
     * @param genres {@link #mGenres}
     * @param index  在{@code genres}中的索引
     * @see #mGenres
     */
    private Request buildQueryVideosRequest(final String genre,
            final Pair<ArrayList<String>, ArrayList<List<Video>>> genres, final int index) {
        if (mItemsCountAndDimension == null) {
            throw new IllegalStateException("calculateItemsCount before query");
        }
        return new VideosRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setGenre(genre)
                .setPeriod(VideosRequest.Builder.PERIOD.MONTH)
                .setCount(mItemsCountAndDimension.getCount())
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        genres.second.set(index, videos);
                        mAdapter.notifyItemChanged(index);
                        mRequestsManager.addRequestRespondeds();
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mRequestsManager.addRequestErrors();
                    }
                })
                .build()
                .setTag(this);
    }

    //--------------------------------------------------------------------------
    // Override Methods of Fragment
    //--------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mNetworkChecker = NetworkChecker.newInstance(getActivity());
        // Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setIsConnected(mNetworkChecker.isConnected());
            }
        };
        getActivity().registerReceiver(mNetworkReceiver, filter);
        // update isConnected state now
        setIsConnected(mNetworkChecker.isConnected());
        applyIsConnected();
        mAdapter = new MyAdapter();
        mItemsCountAndDimension = calculateItemsCount(getActivity());
    }

    @Override
    public View onCreateChildView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_genres, container, false);
        mMainContentView = rootView.findViewById(R.id.main_content);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mEmptyView = rootView.findViewById(android.R.id.empty);
        mNoConnectionView = rootView.findViewById(R.id.no_connection);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set NoConnectionView
        mNoConnectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConnection();
            }
        });

        // set RecyclerView
        // use RecyclerView.setHasFixedSize(true) to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        applyGenres();
        applyIsConnected();

        if (mIsConnected && genresIsEmpty()) {
            setRefreshing(true);
            startLoad(); // important! must do this later than calculateItemsCount
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
        mRecyclerView = null;
        mMainContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        cancelLoad();
        mItemsCountAndDimension = null;
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
        restartLoad();
    }

    @Override
    protected boolean canChildScrollUp() {
        return mRecyclerView.getVisibility() == View.VISIBLE &&
                mRecyclerView.canScrollVertically(-1);
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    private void startLoad() {
        if (mIsConnected) {
            mRequestsManager.addRequest(mVideoCategorySchemaRequest);
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

    public void setGenres(Pair<ArrayList<String>, ArrayList<List<Video>>> genres) {
        mGenres = genres;
        applyGenres();
    }

    public boolean genresIsEmpty() {
        return mGenres == null || mGenres.first.isEmpty();
    }

    private void applyGenres() {
        mAdapter.notifyDataSetChanged();
        final boolean isEmpty = genresIsEmpty();
        if (mEmptyView != null) {
            mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 跳转到网络设置
     */
    private boolean setConnection() {
        return NetworkChecker.openWirelessSettings(getActivity());
    }

    private static ItemsCountCalculater.Result calculateItemsCount(Context context) {
        Resources resources = context.getResources();
        float margin = resources.getDimension(R.dimen.card_margin_half);
        float cardWidthMin = resources.getDimension(R.dimen.card_width_min);
        float cardWidthMax = resources.getDimension(R.dimen.card_width_max);
        return ItemsCountCalculater
                .calculateItemsCountAndDimension(
                        cardWidthMin, cardWidthMax, margin * 2,
                        resources.getDisplayMetrics().widthPixels - margin * 2,
                        ItemsCountCalculater.Preference.SMALLER_OR_MORE_ITEMS);
    }

    //--------------------------------------------------------------------------
    // 内部类
    //--------------------------------------------------------------------------

    /**
     * {@link Request Requests}管理器。用于统计Requests状态。
     */
    private class RequestsManager {

        private int mRequests = 0;

        private int mRequestRespondeds = 0;

        private int mRequestErrors = 0;

        private int mRequestCancelleds = 0;

        /**
         * 初始化总{@link Request}数为0
         */
        private void reset() {
            mRequests = mRequestRespondeds = mRequestErrors = mRequestCancelleds = 0;
        }

        /**
         * 添加{@link Request}数
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

    //-------------------------------------
    // Adapter
    //-------------------------------------

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.list_item_genre, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            addCardViews(inflater, viewHolder);
            return viewHolder;
        }

        private void addCardViews(LayoutInflater inflater, ViewHolder viewHolder) {
            ArrayList<VideoCardViewBuilder.VideoCardViewHolder> cardViews =
                    new ArrayList<>(mItemsCountAndDimension.getCount());
            for (int i = mItemsCountAndDimension.getCount(); i >= 1; i--) {
                VideoCardViewBuilder.VideoCardViewHolder cardViewHolder =
                        new VideoCardViewBuilder()
                                .setInflater(inflater)
                                .setParent(viewHolder.mVideosContainer)
                                .setAttachToParent(true)
                                .setWidth(mItemsCountAndDimension.getDimension())
                                .build();

                cardViewHolder.mImageView
                        .setDefaultImageResId(R.drawable.ic_image_view_placeholder);
                cardViewHolder.mImageView.setErrorImageResId(R.drawable.ic_image_view_placeholder);

                cardViews.add(cardViewHolder);
            }
            viewHolder.mCardViews = cardViews;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final String title = mGenres.first.get(position);
            holder.mTitleTextView.setText(title);
            holder.mTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ListVideosActivity.class);
                    intent.putExtra(ListVideosActivity.EXTRA_KEY_GENRE, title);
                    startActivity(intent);
                }
            });
            List<Video> videos = mGenres.second.get(position);
            int cardViewsIndex = 0;
            if (videos != null) {
                final boolean shouldShowImages = shouldShowImage();
                for (final Video video : videos) {
                    bindVideoCardView(
                            holder.mCardViews.get(cardViewsIndex), video, shouldShowImages);
                    cardViewsIndex++;
                }
            }
            for (; cardViewsIndex < holder.mCardViews.size(); cardViewsIndex++) {
                holder.mCardViews.get(cardViewsIndex).clearAllViewsContent();
            }
        }

        private boolean shouldShowImage() {
            return true;
        }

        private void bindVideoCardView(
                final VideoCardViewBuilder.VideoCardViewHolder videoCardView,
                final Video video,
                final boolean showImage) {
            videoCardView.bindVideo(video, showImage, mVolleySingleton.getImageLoader());
            videoCardView.mRootCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PlayVideoActivity.class);
                    FavoriteBean favoriteBean = new FavoriteBean(video);
                    intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO, favoriteBean);
                    startActivity(intent);
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return genresIsEmpty() ? 0 : mGenres.first.size();
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mItemView;

        public TextView mTitleTextView;

        public LinearLayout mVideosContainer;

        public ArrayList<VideoCardViewBuilder.VideoCardViewHolder> mCardViews;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mTitleTextView = (TextView) itemView.findViewById(R.id.title_textView);
            mVideosContainer = (LinearLayout) itemView.findViewById(R.id.videos_container);
        }
    }


}
