package com.optimalorange.cooltechnologies.ui.fragment;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Comment;
import com.optimalorange.cooltechnologies.network.CommentsRequest;
import com.optimalorange.cooltechnologies.network.RequestsManager;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.ui.ListCommentsActivity;
import com.optimalorange.cooltechnologies.ui.viewholder.CommentView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SimpleListCommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimpleListCommentsFragment extends Fragment {

    /**
     * 应当显示的评论所属的视频的ID。<br/>
     * Type: String
     *
     * @see #newInstance(String videoId)
     */
    private static final String ARGUMENT_KEY_VIDEO_ID =
            SimpleListCommentsFragment.class.getName() + ".argument.KEY_VIDEO_ID";


    private final Comments mComments;

    private final ViewAdapter mViewAdapter;

    private String mVideoID;

    private String mYoukuClientId;

    private RequestsManager mRequestsManager;

    //--------------------------------------------------------------------------
    // 初始化属性
    //--------------------------------------------------------------------------

    {
        mComments = new Comments();
        mViewAdapter = new ViewAdapter();
        mComments.addPropertyChangeListener(mViewAdapter);
    }

    /**
     * 用于 创建设置有指定参数的新{@link ListCommentsFragment}实例的 工厂方法
     *
     * @param videoId 应当显示的评论所属的视频的ID
     * @return 设置有指定参数的新实例
     * @see #ARGUMENT_KEY_VIDEO_ID
     */
    public static SimpleListCommentsFragment newInstance(String videoId) {
        SimpleListCommentsFragment fragment = new SimpleListCommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_KEY_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    //--------------------------------------------------------------------------
    // 覆写Fragment的生命周期方法
    //--------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVideoID = getArguments().getString(ARGUMENT_KEY_VIDEO_ID);
        }
        if (mVideoID == null) {
            throw new IllegalStateException("Please set ARGUMENT_KEY_VIDEO_ID Argument");
        }
        mYoukuClientId = getString(R.string.youku_client_id);
        mRequestsManager = new RequestsManager(VolleySingleton.getInstance(getActivity()));
        mRequestsManager.addRequest(buildQueryCommentsRequest());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_simple_list_comments, container, false);
        mViewAdapter.setViewHolder(new ViewHolder(view));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ListCommentsActivity.buildIntent(getActivity(), mVideoID));
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        mViewAdapter.setViewHolder(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mRequestsManager.cancelAllRequestSilently();
        super.onDestroy();
    }

    //--------------------------------------------------------------------------
    // 新声明方法
    //--------------------------------------------------------------------------

    /**
     * 获取Comment（见entity包中Comment）
     *
     * @return CommentsRequest
     */
    private CommentsRequest buildQueryCommentsRequest() {
        return new CommentsRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(mVideoID)
                .setPage(1)
                .setCount(3)
                .setResponseListener(new Response.Listener<List<Comment>>() {
                    @Override
                    public void onResponse(List<Comment> comments) {
                        int counter = 0;
                        for (Comment comment : comments) {
                            mComments.setComments(counter++, comment);
                        }
                        for (; counter <= 2; counter++) {
                            mComments.setComments(counter, null);
                        }
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
                .build();
    }

    //--------------------------------------------------------------------------
    // 嵌套类
    //--------------------------------------------------------------------------

    private static class Comments {

        private final Comment[] mComments = new Comment[3];

        private int mTotalCommentsNumber;

        private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

        public Comment[] getComments() {
            return mComments;
        }

        public Comment getComments(int index) {
            return mComments[index];
        }

        public Comments setComments(int index, Comment comment) {
            Comment oldValue = mComments[index];
            mComments[index] = comment;
            mPropertyChangeSupport.fireIndexedPropertyChange("comments", index, oldValue, comment);
            return this;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            mPropertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            mPropertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    private static class ViewHolder {

        View mRootView;

        final CommentView.Holder[] mComments = new CommentView.Holder[3];

        public ViewHolder(final View rootView) {
            mRootView = rootView;
            mComments[0] = new CommentView.Holder(rootView.findViewById(R.id.comment0));
            mComments[1] = new CommentView.Holder(rootView.findViewById(R.id.comment1));
            mComments[2] = new CommentView.Holder(rootView.findViewById(R.id.comment2));
        }
    }

    private static class ViewAdapter implements PropertyChangeListener {

        private ViewHolder mViewHolder;

        public ViewHolder getViewHolder() {
            return mViewHolder;
        }

        public ViewAdapter setViewHolder(ViewHolder viewHolder) {
            mViewHolder = viewHolder;
            return this;
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (mViewHolder == null) {
                return;
            }
            switch (event.getPropertyName()) {
                case "comments":
                    if (!(event instanceof IndexedPropertyChangeEvent)) {
                        throw new UnsupportedOperationException();
                    }
                    IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent) event;
                    CommentView.Holder commentVH = mViewHolder.mComments[indexedEvent.getIndex()];
                    Object newValue = indexedEvent.getNewValue();
                    commentVH.updateData((Comment) newValue);
                    commentVH.mRootView.setVisibility(newValue != null ? View.VISIBLE : View.GONE);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknow PropertyName:" + event.getPropertyName());
            }
        }
    }

}
