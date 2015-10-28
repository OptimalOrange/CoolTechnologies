package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.adapter.FavoriteAdapter;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.network.DestroyFavoriteRequest;
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
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 收藏
 */
public class FavoriteFragment extends SwipeRefreshFragment {

    private static final String DEFAULT_CATEGORY_LABEL= "科技";

    private String mYoukuClientId;

    private View v;
    private static final String FAVORITE_BASE_URL = "https://openapi.youku.com/v2/videos/favorite/by_me.json";
    private ListView favoriteListView;
    private VolleySingleton mVolleySingleton;
    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;
    private NetworkChecker mNetworkChecker;
    private TextView mTvHint;
    private boolean mIsCreated = false;

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams windowParams = null;
    private View deleteView = null;
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

    private boolean mIsDelButtonCreate;

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
        favoriteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                int[] locationInWindow = new int[2];
                view.getLocationInWindow(locationInWindow);
                windowParams = new WindowManager.LayoutParams();
                windowParams.gravity = Gravity.TOP | Gravity.LEFT;
                windowParams.x = locationInWindow[0] + view.getWidth() / 2;
                windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                //添加属性FLAG_WATCH_OUTSIDE_TOUCH，用于监听窗口外Touch事件
                windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                windowParams.format = PixelFormat.TRANSLUCENT;
                windowParams.windowAnimations = 0;
                windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                if (deleteView != null) {
                    windowManager.removeViewImmediate(deleteView);
                }
                deleteView = LayoutInflater.from(getActivity()).inflate(R.layout.ll_favorite_delete, null);
                deleteView.measure(0, 0);
                int deleteViewHeight = deleteView.getMeasuredHeight();
                windowParams.y = locationInWindow[1] - deleteViewHeight;
                deleteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendDeleteRequest(favoriteBeans.get(position).videoId, position);
                    }
                });
                windowManager.addView(deleteView, windowParams);
                return true;
            }
        });

        favoriteListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deleteView == null) {
                        mIsDelButtonCreate = true;
                    } else {
                        mIsDelButtonCreate = false;
                        return true;
                    }
                }

                if ((event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_MOVE) && deleteView != null
                        && !mIsDelButtonCreate) {
                    removeWindowView();
                    return true;
                }
                return false;
            }
        });
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

    private void removeWindowView() {
        if (windowManager != null && deleteView != null) {
            windowManager.removeViewImmediate(deleteView);
            deleteView = null;
        }
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
        String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
        if (mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
            if (!mNetworkChecker.isConnected()) {
                setHint(R.string.favorite_hint_no_net);
                return;
            }
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            String url = FAVORITE_BASE_URL + "?client_id=" + getString(R.string.youku_client_id) + "&access_token=" + token + "&page=" + page + "&count=10";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        total = jsonObject.getInt("total");
                        if (total != 0) {
                            JSONArray videoArray = jsonObject.getJSONArray("videos");
                            int pageCount = videoArray.length();
                            currentCount += pageCount;
                            if (currentCount == total) {
                                tvViewMore.setEnabled(false);
                                tvViewMore.setText(getString(R.string.favorite_view_more_last));
                            } else {
                                tvViewMore.setEnabled(true);
                                tvViewMore.setText(getString(R.string.favorite_view_more));
                                page++;
                            }
                            for (int i = 0; i < videoArray.length(); i++) {
                                JSONObject itemObject = videoArray.getJSONObject(i);
                                if(itemObject.getString("category").equals(DEFAULT_CATEGORY_LABEL)){
                                    FavoriteBean bean = new FavoriteBean(itemObject);
                                    favoriteBeans.add(bean);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            favoriteListView.setVisibility(View.VISIBLE);
                            if (mTvHint.getVisibility() == View.VISIBLE) {
                                mTvHint.setVisibility(View.GONE);
                            }
                        } else {
                            setHint(R.string.favorite_no_fav);
                        }

                        setRefreshing(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                }
            });
            requestQueue.add(jsonObjectRequest);
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
                owner.removeWindowView();
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

}
