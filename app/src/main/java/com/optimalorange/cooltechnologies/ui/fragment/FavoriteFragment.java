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
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.ui.view.PullRefreshLayout;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 收藏
 */
public class FavoriteFragment extends Fragment {

    private View v;
    private static final String FAVORITE_BASE_URL = "https://openapi.youku.com/v2/videos/favorite/by_me.json";
    private ListView favoriteListView;
    private VolleySingleton mVolleySingleton;
    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;
    private NetworkChecker mNetworkChecker;
    private TextView mTvHint;
    private boolean mIsCreated = false;
    private PullRefreshLayout refreshLayout;

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams windowParams = null;
    private View deleteView = null;
    private ArrayList<FavoriteBean> favoriteBeans;
    private FavoriteAdapter adapter;

    private static final String BASE_FAVORITE_DELETE_URL = "https://openapi.youku.com/v2/videos/favorite/destroy.json";
    private static final int DELETE_FAVORITE_OK = 0;
    private static final int DELETE_FAVORITE_ERROR = 1;
    private int page = 1;
    private int total = 0;
    private int currentCount = 0;
    private TextView tvViewMore;
    private View footer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELETE_FAVORITE_OK:
                    favoriteBeans.remove(msg.arg1);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            removeWindowView();
                            Toast.makeText(getActivity(), getString(R.string.favorite_delete_success), Toast.LENGTH_SHORT).show();
                            if (favoriteBeans.size() == 0) {
                                setHint(R.string.favorite_no_fav);
                            }
                        }
                    });
                    break;
                case DELETE_FAVORITE_ERROR:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.favorite_delete_fail), Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }
    };

    private boolean mIsDelButtonCreate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkChecker = NetworkChecker.newInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_favorite, container, false);
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mDefaultSharedPreferencesSingleton =
                DefaultSharedPreferencesSingleton.getInstance(getActivity());
        favoriteListView = (ListView) view.findViewById(R.id.favorite_list);
        favoriteListView.setVisibility(View.GONE);
        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO_ID, favoriteBeans.get(position));
                startActivity(intent);
            }
        });
        mTvHint = (TextView) v.findViewById(R.id.favorite_hint);
        favoriteBeans = new ArrayList<>();
        adapter = new FavoriteAdapter(getActivity(), favoriteBeans, mVolleySingleton.getImageLoader());
        if (footer != null) {
            favoriteListView.removeFooterView(footer);
        }
        footer = LayoutInflater.from(getActivity()).inflate(R.layout.ll_favorite_footer, null);
        tvViewMore = (TextView) footer.findViewById(R.id.tv_more);
        tvViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)v).setText(getString(R.string.favorite_view_more_loading));
                getJsonData();
            }
        });
        favoriteListView.addFooterView(footer);
        favoriteListView.setAdapter(adapter);
        Log.e("wzz fav", "is onCreated!!!");
        getNewData();
        mIsCreated = true;
        refreshLayout = (PullRefreshLayout) view.findViewById(R.id.pull_refresh_layout);
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewData();
            }
        });
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

                if (event.getAction() == MotionEvent.ACTION_UP && deleteView != null && !mIsDelButtonCreate) {
                    removeWindowView();
                    return true;
                }
                return false;
            }
        });
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
        getJsonData();
    }

    public void getJsonData() {
        if (favoriteListView.getVisibility() == View.GONE) {
            setHint(R.string.favorite_new_loading);
        }
        String token = mDefaultSharedPreferencesSingleton.retrieveString("user_token", "");
        if (!token.isEmpty()) {
            if (!mNetworkChecker.isConnected()) {
                setHint(R.string.favorite_hint_no_net);
                return;
            }
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            String url = FAVORITE_BASE_URL + "?client_id=" + getString(R.string.youku_client_id) + "&access_token=" + token + "&page=" + page + "&count=10";
            Log.e("wzz json", "url=" + url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                                FavoriteBean bean = new FavoriteBean(itemObject);
                                favoriteBeans.add(bean);
                            }
                            adapter.notifyDataSetChanged();
                            favoriteListView.setVisibility(View.VISIBLE);
                            if (mTvHint.getVisibility() == View.VISIBLE) {
                                mTvHint.setVisibility(View.GONE);
                            }
                        } else {
                            setHint(R.string.favorite_no_fav);
                        }

                        refreshLayout.setRefreshing(false);
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
        String token = mDefaultSharedPreferencesSingleton.retrieveString("user_token", "");
        if (token.isEmpty()) {
            Toast.makeText(getActivity(), R.string.favorite_delete_no_login, Toast.LENGTH_SHORT).show();
            return;
        }
        final HttpPost request = new HttpPost(BASE_FAVORITE_DELETE_URL);
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        BasicNameValuePair param;
        param = new BasicNameValuePair("client_id", getString(R.string.youku_client_id));
        paramList.add(param);
        param = new BasicNameValuePair("access_token", token);
        paramList.add(param);
        param = new BasicNameValuePair("video_id", id);
        paramList.add(param);
        try {
            request.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpResponse response = client.execute(request);
                    Message msg = new Message();
                    if (response.getStatusLine().getStatusCode() == 200) {
                        msg.what = DELETE_FAVORITE_OK;
                        msg.arg1 = index;
                        msg.obj = EntityUtils.toString(response.getEntity());
                        mHandler.handleMessage(msg);
                    } else {
                        msg.what = DELETE_FAVORITE_ERROR;
                        mHandler.handleMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
