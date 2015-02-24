package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.network.CreateComment;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VolleySingleton;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 创建评论
 */
public class CreateCommentFragment extends DialogFragment {

    private static final String ARGUMENT_KEY_VIDEO_ID =
            CreateCommentFragment.class.getName() + ".argument.KEY_VIDEO_ID";

    private static final String ARGUMENT_KEU_ACCESS_TOKEN = CreateCommentFragment.class.getName()
            + ".argument.KEY_ACCESS_TOKEN";

    private static final String ARGUMENT_KEY_CONTENT = CreateCommentFragment.class.getName()
            + ".KEY_CONTENT";

    private String mVideoId;

    private String mYoukuClientId;

    private String mAccessToken;

    private NetworkChecker mNetworkChecker;

    private VolleySingleton mVolleySingleton;

    private EditText mContentView;

    private ImageView mSendView;

    private String mInitContent, mSendContent;

    private OnSaveContentListener mOnSaveContentListener = null;

    private OnCreateCommentListener mCreateCommentListener = null;

    private CreateComment buildCreateComment() {
        CreateComment.Builder builder = new CreateComment.Builder()
                .setClient_id(mYoukuClientId)
                .setAccess_token(mAccessToken)
                .setVideo_id(mVideoId)
                .setContent(mSendContent)
                .setResponseListener(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String id) {
                        mCreateCommentListener.onCreateComment(true);
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mCreateCommentListener.onCreateComment(false);
                    }
                });

        return builder.build();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment CreateCommentFragment.
     */
    public static CreateCommentFragment newInstance(String videoId, String accessToken,
            String content) {
        CreateCommentFragment fragment = new CreateCommentFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_KEY_VIDEO_ID, videoId);
        args.putString(ARGUMENT_KEU_ACCESS_TOKEN, accessToken);
        args.putString(ARGUMENT_KEY_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateCommentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mInitContent = getArguments().getString(ARGUMENT_KEY_CONTENT);
            mAccessToken = getArguments().getString(ARGUMENT_KEU_ACCESS_TOKEN);
            mVideoId = getArguments().getString(ARGUMENT_KEY_VIDEO_ID);
        }

        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        //检测网络是否连接
        mNetworkChecker = NetworkChecker.newInstance(getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_create_comment, null);
        mContentView = (EditText) rootView.findViewById(R.id.content);
        mSendView = (ImageView) rootView.findViewById(R.id.send);
        builder.setView(rootView);
        if (mInitContent != null) {
            mContentView.setText(mInitContent);
        }
        mSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNetworkChecker.isConnected()) {
                    Toast.makeText(getActivity(), R.string.comment_no_connection,
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mSendContent = mContentView.getText().toString().trim();
                    if (!mSendContent.isEmpty()) {
                        mVolleySingleton.addToRequestQueue(buildCreateComment());
                        dismiss();
                    }
                }
            }
        });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        if (mOnSaveContentListener != null) {
            mOnSaveContentListener.onSaveContent(mContentView.getText().toString().trim());
        }
        super.onDestroyView();
    }

    public void setOnSaveContentListener(OnSaveContentListener listener) {
        mOnSaveContentListener = listener;
    }

    public void setOnCreateCommentListener(OnCreateCommentListener listener) {
        mCreateCommentListener = listener;
    }

    interface OnSaveContentListener {

        void onSaveContent(String content);
    }

    interface OnCreateCommentListener {

        void onCreateComment(boolean isSuccess);
    }

}
