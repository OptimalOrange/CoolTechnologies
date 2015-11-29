package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimalorange.cooltechnologies.BuildConfig;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.network.DestroyFavoriteRequest;
import com.optimalorange.cooltechnologies.network.GetMyFavoriteRequest;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.LoginableBaseActivity;
import com.optimalorange.cooltechnologies.ui.ShowVideoDetailActivity;
import com.optimalorange.cooltechnologies.ui.entity.Empty;
import com.optimalorange.cooltechnologies.ui.entity.FavoriteFooter;
import com.optimalorange.cooltechnologies.ui.entity.Loading;
import com.optimalorange.cooltechnologies.ui.entity.Video;
import com.optimalorange.cooltechnologies.ui.viewholder.RecyclerEmptyViewHolder;
import com.optimalorange.cooltechnologies.ui.viewholder.RecyclerFavoriteFooterViewHolder;
import com.optimalorange.cooltechnologies.ui.viewholder.RecyclerFavoriteViewHolder;
import com.optimalorange.cooltechnologies.ui.viewholder.RecyclerLoadingViewHolder;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gq.baijie.classbasedviewadapter.android.adapter.ClassBasedRecyclerViewAdapter;
import gq.baijie.classbasedviewadapter.android.adapter.DataSet;
import gq.baijie.classbasedviewadapter.android.adapter.ViewHolderFactoryRegister;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 收藏
 */
//TODO set click listener
public class FavoriteFragment extends SwipeRefreshFragment {

    private static final String DEFAULT_CATEGORY_LABEL = "科技";

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;

    private NetworkChecker mNetworkChecker;

    private List<Loading> mLoadingDataSet;

    private FavoritesDataSet mFavoritesDataSet;

    private boolean mIsLoadingFavorites = false;

    private final ClassBasedRecyclerViewAdapter adapter = new ClassBasedRecyclerViewAdapter();

    private ViewHolder vh;


    private final LoginableBaseActivity.OnLoginStatusChangeListener mOnLoginStatusChangeListener =
            new LoginableBaseActivity.OnLoginStatusChangeListener() {
                @Override
                public void onLoginStatusChanged(boolean hasLoggedIn) {
                    if (hasLoggedIn) {
                        setRefreshable(true);
                        onRefresh();
                    } else {
                        setRefreshable(false);
                    }
                }
            };


    private void initProperties() {
        Loading loading = new Loading();
        Empty empty = new Empty();
        FavoriteFooter haveMoreFooter = new FavoriteFooter();
        FavoriteFooter noMoreFooter = new FavoriteFooter();
        loading.hint = getString(R.string.favorite_new_loading);
        empty.hint = getString(R.string.favorite_no_fav);
        haveMoreFooter.hint = getString(R.string.favorite_view_more);
        noMoreFooter.hint = getString(R.string.at_last);
        haveMoreFooter.listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJsonData();
            }
        };
        mLoadingDataSet = Collections.singletonList(loading);
        mFavoritesDataSet = new FavoritesDataSet();
        mFavoritesDataSet.empty = empty;
        mFavoritesDataSet.haveMoreFooter = haveMoreFooter;
        mFavoritesDataSet.noMoreFooter = noMoreFooter;

        final ViewHolderFactoryRegister register = adapter.getRegister();
        register.registerViewHolderFactory(new RecyclerLoadingViewHolder.Factory());
        register.registerViewHolderFactory(new RecyclerEmptyViewHolder.Factory());
        register.registerViewHolderFactory(new RecyclerFavoriteFooterViewHolder.Factory());
        register.registerViewHolderFactory(new RecyclerFavoriteViewHolder.Factory() {
            @Override
            public void bindViewHolder(RecyclerFavoriteViewHolder holder, final Video value) {
                super.bindViewHolder(holder, value);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowVideoDetailActivity.start(v.getContext(), value.id);
                    }
                });
            }
        });

        adapter.setDataSet(mLoadingDataSet);
        adapter.notifyDataSetChanged();
    }

    private void initState() {
        mIsLoadingFavorites = false;
    }

    public void resetState() {
        mIsLoadingFavorites = false; //TODO cancel pending requests
        adapter.setDataSet(mLoadingDataSet);
        adapter.notifyDataSetChanged();
        mFavoritesDataSet.unsetFavorites();
    }

    public void addFavorites(Favorites added) {
        if (mFavoritesDataSet.favorites != null) {
            mFavoritesDataSet.addFavorites(added, adapter);
        } else {
            final Favorites newFavorites = new Favorites(new ArrayList<Video>());
            newFavorites.add(added);
            mFavoritesDataSet.favorites = newFavorites;
            adapter.setDataSet(mFavoritesDataSet);
            adapter.notifyDataSetChanged();
        }
    }

    public void removeFavorites(int position) {
        if (mFavoritesDataSet.favorites == null) {
            throw new IllegalStateException("");
        }
        mFavoritesDataSet.remove(position, adapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mYoukuClientId = getString(R.string.youku_client_id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkChecker = NetworkChecker.newInstance(getActivity());

        initProperties();
        initState();
    }

    @Override
    protected View onCreateChildView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        vh = new ViewHolder(rootView);
        return rootView;
    }

    @Override
    public void onRefresh() {
        getNewData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDefaultSharedPreferencesSingleton =
                DefaultSharedPreferencesSingleton.getInstance(getActivity());
        ((LoginableBaseActivity) getActivity())
                .addLoginStatusChangeListener(mOnLoginStatusChangeListener);
        if (((LoginableBaseActivity) getActivity()).hasLoggedIn()) {
            setRefreshable(true);
        } else {
            setRefreshable(false);
        }

        vh.favorites.setLayoutManager(new LinearLayoutManager(vh.favorites.getContext()));
        vh.favorites.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // load data if not do it yet
        if (mFavoritesDataSet.favorites == null && !mIsLoadingFavorites) {
            vh.favorites.setVisibility(View.GONE);
            getNewData();
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
        ((LoginableBaseActivity) getActivity())
                .removeLoginStatusChangeListener(mOnLoginStatusChangeListener);
        vh.favorites.setAdapter(null);
        vh = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mNetworkChecker = null;
        super.onDestroy();
    }

    private void getNewData() {
        resetState();

        getJsonData();
    }

    public void getJsonData() {
        // 避免重复发送
        if (mIsLoadingFavorites) {
            return;
        }
        if (vh.favorites.getVisibility() == View.GONE) {
            setHint(R.string.favorite_new_loading);
        }
        if (mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
            if (!mNetworkChecker.isConnected()) {
                setHint(R.string.favorite_hint_no_net);
                return;
            }
            String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
            int nextPage;
            if (mFavoritesDataSet.favorites == null) {
                nextPage = 1;
            } else {
                nextPage = mFavoritesDataSet.favorites.getCurrentPage() + 1;
            }
            GetMyFavoriteRequest favoriteRequest = buildGetMyFavoriteRequest(token, nextPage, 10);
            mIsLoadingFavorites = true;
            mVolleySingleton.addToRequestQueue(favoriteRequest);
        } else {
            setHint(R.string.favorite_hint_no_login);
        }
    }

    private void setHint(int res) {
        if (vh != null) {
            vh.mainHint.setText(getString(res));
            vh.mainHint.setVisibility(View.VISIBLE);
            vh.favorites.setVisibility(View.GONE);
            vh.mainHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNewData();
                }
            });
        }
    }

    private void hideHint() {
        if (vh != null) {
            vh.mainHint.setVisibility(View.GONE);
            vh.favorites.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected boolean canChildScrollUp() {
        return vh.favorites.getVisibility() == View.VISIBLE &&
                vh.favorites.canScrollVertically(-1);
    }


    //TODO add delete feathure on UI
    private void sendDeleteRequest(String id, final int index) {
        if (!mNetworkChecker.isConnected()) {
            Toast.makeText(getActivity(), R.string.favorite_delete_no_net, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
        if (!mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
            Toast.makeText(getActivity(), R.string.favorite_delete_no_login, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mVolleySingleton.addToRequestQueue(buildDestroyFavoriteRequest(token, id, index));
    }

    private GetMyFavoriteRequest buildGetMyFavoriteRequest(String token, int page, int count) {
        final GetMyFavoriteRequestHandler handler =
                new GetMyFavoriteRequestHandler(new WeakReference<>(this));
        return new GetMyFavoriteRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setAccess_token(token)
                .setPage(page)
                .setCount(count)
                .setResponseListener(handler)
                .setErrorListener(handler)
                .build();
    }

    /**
     * 创建取消收藏的请求
     */
    private DestroyFavoriteRequest buildDestroyFavoriteRequest(
            String token, String videoId, int videoIndexInListView) {
        final DestroyFavoriteRequestHandler handler = new DestroyFavoriteRequestHandler(
                videoIndexInListView, new WeakReference<>(getContext()), new WeakReference<>(this));
        return new DestroyFavoriteRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(videoId)
                .setAccess_token(token)
                .setResponseListener(handler)
                .setErrorListener(handler)
                .build();
    }

    private static class GetMyFavoriteRequestHandler
            implements Response.Listener<JSONObject>, Response.ErrorListener {

        private final WeakReference<FavoriteFragment> mOwner;

        private GetMyFavoriteRequestHandler(WeakReference<FavoriteFragment> owner) {
            mOwner = owner;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            new RuntimeException(error).printStackTrace();

            final FavoriteFragment owner = mOwner.get();
            if (owner != null) {
                onFinished(owner);
                //TODO show error info
            }
        }

        @Override
        public void onResponse(JSONObject response) {
            final FavoriteFragment owner = mOwner.get();
            if (owner != null) {
                try {
                    doHandle(response, owner);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onFinished(owner);
            }
        }

        //!!!注意!!! owner为null的一种可能情况是系统临时destroy了它。此情况下，应当在recreate时重置状态。
        private void onFinished(@NonNull FavoriteFragment owner) {
            owner.mIsLoadingFavorites = false;
            owner.setRefreshing(false);
            owner.hideHint();
        }

        private static void doHandle(JSONObject response, FavoriteFragment owner)
                throws JSONException {
            owner.addFavorites(convertToFavoriteInfo(response));
        }

        private static Favorites convertToFavoriteInfo(JSONObject jsonObject)
                throws JSONException {
            JSONArray videoArray = jsonObject.getJSONArray("videos");
            Favorites favorites = new Favorites(convertNeededVideos(videoArray));
            favorites.setCurrentReadCountIncludingUnneeded(videoArray.length());
            favorites.setTotal(jsonObject.getInt("total"));
            favorites.setCurrentPage(jsonObject.getInt("page"));
            return favorites;
        }

        private static List<Video> convertNeededVideos(JSONArray videoArray)
                throws JSONException {
            List<Video> result = new LinkedList<>();
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject itemObject = videoArray.getJSONObject(i);
                if (itemObject.getString("category").equals(DEFAULT_CATEGORY_LABEL)) {
                    result.add(convertToFavorite(itemObject));
                }
            }
            return result;
        }

        private static Video convertToFavorite(JSONObject jsonObject) throws JSONException {
            Video result = new Video();
            result.title = jsonObject.getString("title");
            result.link = jsonObject.getString("link");
            result.thumbnail = jsonObject.getString("thumbnail");
            result.duration = jsonObject.getString("duration");
            result.id = jsonObject.getString("id");
            return result;
        }

    }

    private static class DestroyFavoriteRequestHandler
            implements Response.Listener<JSONObject>, Response.ErrorListener {

        private final int mVideoIndexInListView;

        private final WeakReference<Context> mContextWeakReference;

        private final WeakReference<FavoriteFragment> mOwner;

        private DestroyFavoriteRequestHandler(
                int videoIndexInListView,
                WeakReference<Context> contextWeakReference,
                WeakReference<FavoriteFragment> owner) {
            mVideoIndexInListView = videoIndexInListView;
            mContextWeakReference = contextWeakReference;
            mOwner = owner;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            final Context context = mContextWeakReference.get();
            if (context != null) {
                final String message = context.getString(R.string.favorite_delete_fail);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onResponse(JSONObject response) {
            final Context context = mContextWeakReference.get();
            final FavoriteFragment owner = mOwner.get();
            if (owner != null) {
                owner.removeFavorites(mVideoIndexInListView);
            }
            if (context != null) {
                final String message = context.getString(R.string.favorite_delete_success);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static class FavoritesDataSet implements DataSet {

        @Nullable
        private Favorites favorites;

        private Empty empty;

        private FavoriteFooter haveMoreFooter;

        private FavoriteFooter noMoreFooter;


        @NonNull
        private Favorites getNonNullFavorites() {
            if (favorites != null) {
                return favorites;
            } else {
                throw new IllegalStateException("haven't init FavoritesDataSet");
            }
        }

        @Override
        public int size() {
            // getNonNullFavorites().isEmpty() 时，get(0)为empty；否则，最后有某种footer
            return getNonNullFavorites().getInterestingFavorites().size() + 1;
        }

        //TODO 改善
        @Override
        public Object get(int position) {
            final List<Video> interestingFavorites =
                    getNonNullFavorites().getInterestingFavorites();
            if (position < interestingFavorites.size()) {
                return interestingFavorites.get(position);
            } else {
                if (!getNonNullFavorites().isEmpty()) {
                    return getFavoriteFooter(position);
                } else {
                    return empty;
                }
            }
        }

        private FavoriteFooter getFavoriteFooter(int position) {
            if (BuildConfig.DEBUG) {
                // This if block will be auto deleted when release
                if (position != getNonNullFavorites().getInterestingFavorites().size()) { //NOPMD
                    throw new IllegalStateException();
                }
            }
            if (!getNonNullFavorites().allRead()) {
                return haveMoreFooter;
            } else {
                return noMoreFooter;
            }
        }

        public void unsetFavorites() {
            favorites = null;
        }

        public void addFavorites(Favorites added, RecyclerView.Adapter adapter) {
            Favorites nonNullFavorites = getNonNullFavorites();
            if (nonNullFavorites.isEmpty()) {
                nonNullFavorites.add(added);
                adapter.notifyDataSetChanged();
            } else {
                final int sizeBeforeAdd = nonNullFavorites.getInterestingFavorites().size();
                nonNullFavorites.add(added);
                adapter.notifyItemRangeInserted(
                        sizeBeforeAdd, added.getInterestingFavorites().size());
                if (nonNullFavorites.allRead()) {
                    // footer改为noMoreFooter
                    adapter.notifyItemChanged(nonNullFavorites.getInterestingFavorites().size());
                }
            }
        }

        public void remove(int position, RecyclerView.Adapter adapter) {
            Favorites nonNullFavorites = getNonNullFavorites();
            nonNullFavorites.remove(position);
            if (nonNullFavorites.isEmpty()) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRemoved(position);
            }
        }

    }

    private static class Favorites {

        private int total;

        private int currentPage;

        private int currentReadCountIncludingUnneeded;

        @NonNull
        private final List<Video> interestingFavorites;

        public Favorites(@NonNull List<Video> interestingFavorites) {
            this.interestingFavorites = interestingFavorites;
        }

        // TODO 由于不明原因，有些收藏可能读取不到，会导致此方法出问题
        public boolean allRead() {
            if (BuildConfig.DEBUG) {
                // This if block will be auto deleted when release
                if (currentReadCountIncludingUnneeded > total) { //NOPMD
                    final String message = "currentReadCountIncludingUnneeded > total: " +
                            "currentReadCountIncludingUnneeded=" + currentReadCountIncludingUnneeded
                            + ", total=" + total;
                    throw new AssertionError(message);
                }
            }
            return currentReadCountIncludingUnneeded >= total;
        }

        public boolean isEmpty() {
            if (total == 0) {
                return true;
            } else {
                if (BuildConfig.DEBUG) {
                    // This if block will be auto deleted when release
                    if (total < 0) { //NOPMD
                        throw new AssertionError("total < 0");
                    }
                }
                //TODO ------------------------- test this trick -----------------------
                return allRead() && getInterestingFavorites().isEmpty();
            }
        }

        @NonNull
        public List<Video> getInterestingFavorites() {
            return Collections.unmodifiableList(interestingFavorites);
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        /**
         * 危险：不安全地影响状态
         */
        public void setCurrentReadCountIncludingUnneeded(int currentReadCountIncludingUnneeded) {
            this.currentReadCountIncludingUnneeded = currentReadCountIncludingUnneeded;
        }

        /**
         * 危险：不安全地影响状态
         */
        public void setTotal(int total) {
            this.total = total;
        }

        //TODO check illegal argument
        //TODO check state
        public void add(Favorites added) {
            //TODO check total != added.total?
            total = added.total;

            //TODO check currentPage + 1 != added.currentPage?
            //TODO 处理 多请求并发条件下的 乱序问题
            currentPage = added.currentPage;

            //TODO 重复response？
            currentReadCountIncludingUnneeded += added.currentReadCountIncludingUnneeded;
            interestingFavorites.addAll(added.getInterestingFavorites());
        }

        //TODO check illegal argument
        //TODO check state
        public void remove(int index) {
            total--;
            currentReadCountIncludingUnneeded--;
            interestingFavorites.remove(index);
        }

    }

    static class ViewHolder {

        RecyclerView favorites;

        TextView mainHint;

        private ViewHolder(View root) {
            favorites = (RecyclerView) root.findViewById(R.id.favorites);
            mainHint = (TextView) root.findViewById(R.id.main_hint);
        }

    }

}
