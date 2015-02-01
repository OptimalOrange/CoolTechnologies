package com.optimalorange.cooltechnologies.ui.fragment;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Comment;
import com.optimalorange.cooltechnologies.network.CommentsRequest;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * 评论{@link Fragment}<br/>
 * Tip: 可以使用{@link ListCommentsFragment#newInstance}工厂方法创建{@link ListCommentsFragment}实例。
 */
public class ListCommentsFragment extends SwipeRefreshFragment {

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

    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;

    private NetworkChecker mNetworkChecker;

    /** 网络请求管理器 */
    private final RequestsManager mRequestsManager = new RequestsManager();

    /**
     * 状态属性：网络联通性。true表示已连接网络；false表示网络已断开。
     */
    private boolean mIsConnected = false;

    private BroadcastReceiver mNetworkReceiver;

    private VolleySingleton mVolleySingleton;

    private int mPage = 1;

    private View mHeader;

    private TextView mCommentsCount;

    private ListView mListView;

    private ItemsAdapter mItemsAdapter;

    private LinkedList<Comment> mListComments = new LinkedList<Comment>();

    private String mContent;

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
                                mRequestsManager.addRequestRespondeds();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRequestsManager.addRequestErrors();
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

        mDefaultSharedPreferencesSingleton =
                DefaultSharedPreferencesSingleton.getInstance(getActivity());
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
    }

    @Override
    public View onCreateChildView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_comments, container, false);
        mCommentsCount = (TextView) rootView.findViewById(R.id.comments_count);
        mListView = (ListView) rootView.findViewById(R.id.comments_list);
        mHeader = LayoutInflater.from(getActivity()).inflate(R.layout.list_comments_header, null);
        mListView.addHeaderView(mHeader);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mItemsAdapter = new ItemsAdapter(mListComments);
        mListView.setAdapter(mItemsAdapter);

        applyIsConnected();
        if (mIsConnected) {
            setRefreshing(true);
            startLoad();
        }

        mHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = mDefaultSharedPreferencesSingleton.retrieveString("user_token", "");
                if (token.isEmpty()) {
                    if (!mNetworkChecker.isConnected()) {
                        Toast.makeText(getActivity(), R.string.comment_no_connection,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ToLoginDialogFragment mDialog = new ToLoginDialogFragment();
                    mDialog.show(getFragmentManager(), null);
                } else {
                    CreateCommentFragment mCreateCommentFragment = CreateCommentFragment
                            .newInstance(mVideoID, token, mContent);
                    mCreateCommentFragment.setOnSaveContentListener(
                            new CreateCommentFragment.OnSaveContentListener() {
                                @Override
                                public void onSaveContent(String content) {
                                    mContent = content;
                                }
                            });
                    mCreateCommentFragment.setOnCreateCommentListener(
                            new CreateCommentFragment.OnCreateCommentListener() {
                                @Override
                                public void onCreateComment(boolean isSuccess) {
                                    if (isSuccess) {
                                        Toast.makeText(getActivity(),
                                                R.string.create_comment_success,
                                                Toast.LENGTH_SHORT).show();
                                        //发表成功后就清空评论输入框中的内容
                                        mContent = null;
                                        //发表成功后就刷新评论
                                        restartLoad();
                                    } else {
                                        Toast.makeText(getActivity(),
                                                R.string.create_comment_failure,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    mCreateCommentFragment
                            .show(getFragmentManager(), CreateCommentFragment.class.getName());
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        mCommentsCount = null;
        mListView.setAdapter(null);
        mListView = null;
        mHeader = null;
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
        //每次刷新时去除所有comments
        mListComments.clear();
        //重新请求第一页的内容
        mPage = 1;
        mItemsAdapter.notifyDataSetChanged();
        restartLoad();
    }

    @Override
    protected boolean canChildScrollUp() {
        return mListView.getVisibility() == View.VISIBLE &&
                mListView.canScrollVertically(-1);
    }

    private void startLoad() {
        if (mIsConnected) {
            mRequestsManager.addRequest(buildQueryCommentsRequest());
            mRequestsManager.addRequest(buildQueryTotalRequest());
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
        setRefreshable(mIsConnected);
    }

    private void applyVideos() {
        if (mItemsAdapter != null) {
            mItemsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {}

    /**
     * “提示需要登陆才能评论”对话框
     */
    public static class ToLoginDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.comment_to_login_title)
                    .setMessage(R.string.comment_to_login_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
            return builder.create();
        }
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

            TextView mUserName;

            TextView mContent;

            TextView mDate;
        }

    }
}
