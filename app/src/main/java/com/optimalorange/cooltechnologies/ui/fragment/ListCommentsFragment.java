package com.optimalorange.cooltechnologies.ui.fragment;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Comment;
import com.optimalorange.cooltechnologies.network.CommentsRequest;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.RequestsManager;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.ui.LoginActivity;
import com.optimalorange.cooltechnologies.ui.view.CommentView;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
    private RequestsManager mRequestsManager;

    /**
     * 状态属性：网络联通性。true表示已连接网络；false表示网络已断开。
     */
    private boolean mIsConnected = false;

    private BroadcastReceiver mNetworkReceiver;

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
            //为了降低PlayVideoActivity crash崩溃BUG的触发率，暂时不在启动时显示加载中动画。
            //TODO 找到BUG的真正原因并修复。目前在播放时，点刷新，有可能触发类似BUG。
            //日志：A/libc﹕ Fatal signal 11 (SIGSEGV) at 0x0000658a (code=0), thread 25994 (ooltechnologies)
            //setRefreshing(true);
            startLoad();
        }

        mHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = mDefaultSharedPreferencesSingleton.retrieveString("access_token", "");
                if (!mDefaultSharedPreferencesSingleton.hasLoggedIn()) {
                    if (!mNetworkChecker.isConnected()) {
                        Toast.makeText(getActivity(), R.string.comment_no_connection,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ToLoginDialogFragment mDialog = ToLoginDialogFragment
                            .newInstance(getString(R.string.comment_to_login_message));
                    mDialog.show(getChildFragmentManager(), null);
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
                            .show(getChildFragmentManager(), CreateCommentFragment.class.getName());
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

    //TODO 为何禁止super.onCreateOptionsMenu运行?
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    /**
     * “提示需要登陆才能评论”对话框
     */
    public static class ToLoginDialogFragment extends DialogFragment {

        private static final String ARGMENT_KEY_MESSAGE = ToLoginDialogFragment.class.getName()
                + ".KEY_MESSAGE";

        private String mMessage;

        public static ToLoginDialogFragment newInstance(String message) {
            ToLoginDialogFragment frag = new ToLoginDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ARGMENT_KEY_MESSAGE, message);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mMessage = getArguments().getString(ARGMENT_KEY_MESSAGE);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.not_login_title)
                    .setMessage(mMessage)
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
            CommentView.Holder commentViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_comment, parent, false);
                commentViewHolder = new CommentView.Holder(convertView);
                convertView.setTag(commentViewHolder);
            } else {
                commentViewHolder = (CommentView.Holder) convertView.getTag();
            }
            commentViewHolder.updateData(mComments.get(position));

            //当滑到末尾的位置时加载更多Video
            if (position == mListComments.size() - 1) {
                mRequestsManager.addRequest(buildQueryCommentsRequest());
            }

            return convertView;
        }

    }
}
