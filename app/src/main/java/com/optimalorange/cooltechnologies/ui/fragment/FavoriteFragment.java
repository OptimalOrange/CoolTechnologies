package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimalorange.cooltechnologies.BuildConfig;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.adapter.FavoriteAdapter;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.network.DestroyFavoriteRequest;
import com.optimalorange.cooltechnologies.network.GetMyFavoriteRequest;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.LoginableBaseActivity;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 收藏
 */
public class FavoriteFragment extends SwipeRefreshFragment {

    private static final String DEFAULT_CATEGORY_LABEL= "科技";

    private String mYoukuClientId;

    private View v;
    private ListView favoriteListView;
    private VolleySingleton mVolleySingleton;
    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;
    private NetworkChecker mNetworkChecker;
    private TextView mTvHint;
    private boolean mIsCreated = false;

    private ArrayList<FavoriteBean> favoriteBeans;
    private FavoriteAdapter adapter;

    private int page = 1;
    private int total = 0;
    private int currentCount = 0;
    private TextView tvViewMore;
    private View footer;

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
    }

    @Override
    protected View onCreateChildView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_favorite, container, false);
        favoriteListView = (ListView) v.findViewById(R.id.favorite_list);
        mTvHint = (TextView) v.findViewById(R.id.favorite_hint);
        footer = inflater.inflate(R.layout.ll_favorite_footer, favoriteListView, false);
        tvViewMore = (TextView) footer.findViewById(R.id.tv_more);
        return v;
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
        if (((LoginableBaseActivity) getActivity()).hasLoggedIn()){
            setRefreshable(true);
        } else {
            setRefreshable(false);
        }

        favoriteListView.setVisibility(View.GONE);
        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO, favoriteBeans.get(position));
                startActivity(intent);
            }
        });

        favoriteBeans = new ArrayList<>();
        adapter = new FavoriteAdapter(getActivity(), favoriteBeans, mVolleySingleton.getImageLoader());

        tvViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) v).setText(getString(R.string.favorite_view_more_loading));
                getJsonData();
            }
        });
        favoriteListView.addFooterView(footer);
        favoriteListView.setAdapter(adapter);
        getNewData();
        mIsCreated = true;
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
        tvViewMore = null;
        footer = null;
        mTvHint = null;
        favoriteListView.setAdapter(null);
        favoriteListView = null;
        v = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mNetworkChecker = null;
        super.onDestroy();
    }

    private void getNewData() {
        page = 1;
        total = 0;
        currentCount = 0;
        favoriteBeans.clear();
        adapter.notifyDataSetChanged();
        getJsonData();
    }

    public void getJsonData() {
        if (favoriteListView.getVisibility() == View.GONE) {
            setHint(R.string.favorite_new_loading);
        }
        if (mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
            if (!mNetworkChecker.isConnected()) {
                setHint(R.string.favorite_hint_no_net);
                return;
            }
            String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
            mVolleySingleton.addToRequestQueue(buildGetMyFavoriteRequest(token, page, 10));
        } else {
            setHint(R.string.favorite_hint_no_login);
        }
    }

    private void setHint(int res) {
        mTvHint.setText(getString(res));
        mTvHint.setVisibility(View.VISIBLE);
        favoriteListView.setVisibility(View.GONE);
        mTvHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewData();
            }
        });
    }


    @Override
    protected boolean canChildScrollUp() {
        return favoriteListView.getVisibility() == View.VISIBLE &&
                favoriteListView.canScrollVertically(-1);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //显示的时候判断有没有走onCreated，没有判断是否有列表，没有则刷新列表。
        if (isVisibleToUser) {
            if (mIsCreated) {
                mIsCreated = false;
            } else {
                if (favoriteListView.getVisibility() == View.GONE) {
                    getNewData();
                }
            }
        }
    }

    //TODO add delete feathure on UI
    private void sendDeleteRequest(String id, final int index) {
        if (!mNetworkChecker.isConnected()) {
            Toast.makeText(getActivity(), R.string.favorite_delete_no_net, Toast.LENGTH_SHORT).show();
            return;
        }
        String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
        if (!mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
            Toast.makeText(getActivity(), R.string.favorite_delete_no_login, Toast.LENGTH_SHORT).show();
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

    /** 创建取消收藏的请求 */
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

            //TODO stop refreshing & change hint
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
            }
        }

        private static void doHandle(JSONObject response, FavoriteFragment owner)
                throws JSONException {
            FavoriteInfo newFavoriteInfo = convertToFavoriteInfo(response);

            //TODO add addFavoriteInfo method?
            owner.total = newFavoriteInfo.total;
            if (newFavoriteInfo.total != 0) {
                owner.currentCount += newFavoriteInfo.currentReadCountIncludingUnneeded;
                if (owner.currentCount == owner.total) {
                    owner.tvViewMore.setEnabled(false);
                    owner.tvViewMore.setText(owner.getString(R.string.favorite_view_more_last));
                } else {
                    if (BuildConfig.DEBUG && owner.currentCount >= owner.total) {
                        throw new AssertionError("owner.currentCount < owner.total");
                    }
                    owner.tvViewMore.setEnabled(true);
                    owner.tvViewMore.setText(owner.getString(R.string.favorite_view_more));
                    owner.page++;//TODO check newFavoriteInfo.currentPage?
                }

                owner.favoriteBeans.addAll(newFavoriteInfo.hasRead);
                owner.adapter.notifyDataSetChanged();
                owner.favoriteListView.setVisibility(View.VISIBLE);
                if (owner.mTvHint.getVisibility() == View.VISIBLE) {
                    owner.mTvHint.setVisibility(View.GONE);
                }
            } else {
                owner.setHint(R.string.favorite_no_fav);
            }

            owner.setRefreshing(false);
        }

        private static FavoriteInfo convertToFavoriteInfo(JSONObject jsonObject)
                throws JSONException {
            FavoriteInfo favoriteInfo = new FavoriteInfo();
            favoriteInfo.total = jsonObject.getInt("total");
            favoriteInfo.currentPage = jsonObject.getInt("page");
            JSONArray videoArray = jsonObject.getJSONArray("videos");
            favoriteInfo.currentReadCountIncludingUnneeded = videoArray.length();
            favoriteInfo.hasRead = convertNeededVideos(videoArray);
            return favoriteInfo;
        }

        private static List<FavoriteBean> convertNeededVideos(JSONArray videoArray)
                throws JSONException {
            List<FavoriteBean> result = new LinkedList<>();
            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject itemObject = videoArray.getJSONObject(i);
                if (itemObject.getString("category").equals(DEFAULT_CATEGORY_LABEL)) {
                    FavoriteBean bean = new FavoriteBean(itemObject);
                    result.add(bean);
                }
            }
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
                //TODO add remove video method?
                owner.favoriteBeans.remove(mVideoIndexInListView);
                owner.adapter.notifyDataSetChanged();
                if (owner.favoriteBeans.size() == 0) {
                    owner.setHint(R.string.favorite_no_fav);
                }
            }
            if (context != null) {
                final String message = context.getString(R.string.favorite_delete_success);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static class FavoriteInfo {

        int total;

        int currentPage;

        int currentReadCountIncludingUnneeded;

        List<FavoriteBean> hasRead;
    }

}
