package com.optimalorange.cooltechnologies.ui.fragment;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Comment;
import com.optimalorange.cooltechnologies.network.CommentsRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * 评论{@link Fragment}<br/>
 * Tip: 可以使用{@link ListCommentsFragment#newInstance}工厂方法创建{@link ListCommentsFragment}实例。
 */
public class ListCommentsFragment extends Fragment {

    // Fragment初始化参数

    /**
     * 应当显示的评论所属的视频的ID。<br/>
     * Type: String
     *
     * @see #newInstance(String videoId)
     */
    private static final String ARGUMENT_KEY_VIDEO_ID =
            ListCommentsFragment.class.getName() + ".argument.KEY_VIDEO_ID";

    private static final String YOUKU_API_COMMENTS_BY_VIDEO
            = "https://openapi.youku.com/v2/comments/by_video.json";

    /**
     * 应当显示的评论所属的视频的ID。
     *
     * @see #ARGUMENT_KEY_VIDEO_ID
     */
    private String mVideoID;

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    private int mPage = 1;

    private View mHeader;

    private TextView mCommentsCount;

    private ListView mListView;

    private ItemsAdapter mItemsAdapter;

    private LinkedList<Comment> mListComments = new LinkedList<Comment>();

    /**
     * 获取Comment（见entity包中Comment）
     *
     * @return CommentsRequest
     */
    private CommentsRequest buildQueryCommentsRequest() {
        CommentsRequest.Builder builder = new CommentsRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(mVideoID)
                .setPage(mPage)
                .setResponseListener(new Response.Listener<List<Comment>>() {
                    @Override
                    public void onResponse(List<Comment> comments) {
                        for (Comment mComment : comments) {
                            mListComments.add(mComment);
                            if (mItemsAdapter != null) {
                                mItemsAdapter.notifyDataSetChanged();
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

        //为下一次请求获取Comment翻页
        mPage++;

        return builder.build();
    }

    /** 建立请求评论的URL */
    private String buildUrl() {
        final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_COMMENTS_BY_VIDEO)
                .buildUpon();
        if (mYoukuClientId == null) {
            throw new IllegalStateException("Please set mYoukuClientId before build");
        }
        urlBuilder.appendQueryParameter("client_id", mYoukuClientId);
        if (mVideoID == null) {
            throw new IllegalStateException("Please set mVideoID before build");
        }
        urlBuilder.appendQueryParameter("video_id", mVideoID);
        return urlBuilder.build().toString();
    }

    /** 请求评论，是用来获取评论的total */
    private JsonObjectRequest buildQueryTotalRequest() {
        JsonObjectRequest totalRequest = new JsonObjectRequest
                (Request.Method.GET, buildUrl(), null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (!response.getString("total").isEmpty()) {
                                        mCommentsCount.setText(
                                                String.format(getString(R.string.comments_count),
                                                        response.getString("total")));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        return totalRequest;
    }

    /**
     * 用于 创建设置有指定参数的新{@link ListCommentsFragment}实例的 工厂方法
     *
     * @param videoId 应当显示的评论所属的视频的ID
     * @return 设置有指定参数的新实例
     * @see #ARGUMENT_KEY_VIDEO_ID
     */
    public static ListCommentsFragment newInstance(String videoId) {
        ListCommentsFragment fragment = new ListCommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_KEY_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVideoID = getArguments().getString(ARGUMENT_KEY_VIDEO_ID);
        }

        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mVolleySingleton.addToRequestQueue(buildQueryCommentsRequest());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCommentsCount = (TextView) view.findViewById(R.id.comments_count);
        mListView = (ListView) view.findViewById(R.id.comments_list);
        mHeader = LayoutInflater.from(getActivity()).inflate(R.layout.list_comments_header, null);
        mListView.addHeaderView(mHeader);
        mItemsAdapter = new ItemsAdapter(mListComments);
        mListView.setAdapter(mItemsAdapter);

        mVolleySingleton.addToRequestQueue(buildQueryTotalRequest());

    }

    /**
     *
     */
    private class ItemsAdapter extends BaseAdapter {

        private LinkedList<Comment> mComments;

        public ItemsAdapter(LinkedList<Comment> mComments) {
            super();
            this.mComments = mComments;
        }

        @Override
        public int getCount() {
            return mComments.size();
        }

        @Override
        public Object getItem(int position) {
            return mComments.get(position);
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
                        .inflate(R.layout.list_item_comment, parent, false);
                vh = new ViewHolder();
                vh.mImageView = (ImageView) convertView.findViewById(R.id.image_view);
                vh.mUserName = (TextView) convertView.findViewById(R.id.user_name);
                vh.mContent = (TextView) convertView.findViewById(R.id.content);
                vh.mDate = (TextView) convertView.findViewById(R.id.date);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.mUserName.setText(mComments.get(position).getUser().getName());
            vh.mContent.setText(mComments.get(position).getContent());
            vh.mDate.setText(mComments.get(position).getPublished());

            //当滑到末尾的位置时加载更多Video
            if (position == mListComments.size() - 1) {
                mVolleySingleton.addToRequestQueue(buildQueryCommentsRequest());
            }

            return convertView;
        }

        private class ViewHolder {

            ImageView mImageView;

            TextView mUserName;

            TextView mContent;

            TextView mDate;
        }

    }
}
