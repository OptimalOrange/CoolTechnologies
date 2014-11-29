package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.util.NetworkChecker;
import com.optimalorange.cooltechnologies.util.Utils;
import com.optimalorange.cooltechnologies.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    private TextView mTvHint;
    private boolean mIsCreated = false;

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
        favoriteListView = (ListView) view.findViewById(R.id.favorite_list);
        mTvHint = (TextView) v.findViewById(R.id.favorite_hint);
        Log.e("wzz fav", "is onCreated!!!");
        getJsonData();
        mIsCreated = true;
    }

    public void getJsonData() {
        String token = Utils.getString(getActivity(), "user_token", "");
        if (!token.isEmpty()) {
            if (!NetworkChecker.isConnected(getActivity())) {
                setHint(R.string.favorite_hint_no_net);
            }
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            String url = FAVORITE_BASE_URL + "?client_id=" + getString(R.string.youku_client_id) + "&access_token=" + token;
            Log.e("wzz json", "url=" + url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        ArrayList<FavoriteBean> favoriteBeans = new ArrayList<>();
                        int total = jsonObject.getInt("total");
                        if (total != 0) {
                            JSONArray videoArray = jsonObject.getJSONArray("videos");
                            for (int i = 0; i < videoArray.length(); i++) {
                                JSONObject itemObject = videoArray.getJSONObject(i);
                                FavoriteBean bean = new FavoriteBean(itemObject);
                                favoriteBeans.add(bean);
                            }
                        }
                        FavoriteAdapter adapter = new FavoriteAdapter(getActivity(), favoriteBeans, mVolleySingleton.getImageLoader());
                        favoriteListView.setAdapter(adapter);
                        favoriteListView.setVisibility(View.VISIBLE);
                        if (mTvHint.getVisibility() == View.VISIBLE) {
                            mTvHint.setVisibility(View.GONE);
                        }
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
    }


    private class FavoriteAdapter extends BaseAdapter {

        private class ViewHolder {
            public TextView tvTitle;
            public TextView tvDuration;
            public ImageView ivImage;
        }

        private Context mContext;
        private ArrayList<FavoriteBean> mFavoriteBeans;
        private ImageLoader mImageLoader;

        private FavoriteAdapter(Context context, ArrayList<FavoriteBean> favoriteBeans, ImageLoader imageLoader) {
            mContext = context;
            mFavoriteBeans = favoriteBeans;
            mImageLoader = imageLoader;
        }

        @Override
        public int getCount() {
            return mFavoriteBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return mFavoriteBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_favorite, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.thumbnail);
                viewHolder.tvDuration = (TextView) convertView.findViewById(R.id.duration);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            mImageLoader.get(mFavoriteBeans.get(position).imageUrl, ImageLoader.getImageListener(viewHolder.ivImage, R.drawable.ic_launcher, R.drawable.ic_launcher));
            viewHolder.tvTitle.setText(mFavoriteBeans.get(position).title);
            viewHolder.tvDuration.setText(Utils.getDurationString((int)Float.parseFloat(mFavoriteBeans.get(position).duration)));
            return convertView;
        }
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
                    getJsonData();
                }
            }
        }
    }
}
