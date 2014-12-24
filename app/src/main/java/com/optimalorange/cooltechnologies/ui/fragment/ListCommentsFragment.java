package com.optimalorange.cooltechnologies.ui.fragment;


import com.optimalorange.cooltechnologies.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    /**
     * 应当显示的评论所属的视频的ID。
     *
     * @see #ARGUMENT_KEY_VIDEO_ID
     */
    private String mVideoID;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //TODO 删除临时sample代码
        ((TextView) view.findViewById(R.id.sample_text)).setText("mVideoId: " + mVideoID);
    }
}
