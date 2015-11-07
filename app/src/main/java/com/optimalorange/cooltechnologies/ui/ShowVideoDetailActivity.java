package com.optimalorange.cooltechnologies.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VideoDetailRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.ui.entity.Video;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ShowVideoDetailActivity extends AppCompatActivity {

    /**
     * 应当播放的Video的ID<br/>
     * Type: {@link String}
     */
    public static final String EXTRA_KEY_VIDEO_ID =
            ShowVideoDetailActivity.class.getName() + ".extra.KEY_VIDEO_ID";


    private VolleySingleton mVolleySingleton;

    private NetworkChecker mNetworkChecker;

    private String mYoukuClientId;

    private String mVideoIdExtra;

    private Video mVideo;

    private ViewHolder mViews;

    public static Intent buildIntent(Context context, String videoId) {
        final Intent result = new Intent(context, ShowVideoDetailActivity.class);
        result.putExtra(EXTRA_KEY_VIDEO_ID, videoId);
        return result;
    }

    public static void start(Context context, String videoId) {
        context.startActivity(buildIntent(context, videoId));
    }

    public void showVideo(Video video) {
        if (mVideo == video) {
            return;
        }
        mVideo = video;
        if (mViews != null) {
            mViews.description.setText(video.description);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化属性
        mVideoIdExtra = getIntent().getStringExtra(EXTRA_KEY_VIDEO_ID);
        if (mVideoIdExtra == null) {
            throw new IllegalStateException("Please do intent.putExtra(EXTRA_KEY_VIDEO_ID, vid)");
        }
        mNetworkChecker = NetworkChecker.newInstance(this);
        mVolleySingleton = VolleySingleton.getInstance(this);
        mYoukuClientId = getString(R.string.youku_client_id);

        setContentView(R.layout.activity_show_video_detail);
        mViews = new ViewHolder(this);

        mVolleySingleton.addToRequestQueue(buildVideoDetailRequest(mVideoIdExtra));
    }

    @Override
    protected void onDestroy() {
        mViews = null;
        mNetworkChecker = null;
        super.onDestroy();
    }

    private VideoDetailRequest buildVideoDetailRequest(String videoId) {
        final VideoDetailRequestHandler handler =
                new VideoDetailRequestHandler(new WeakReference<>(this));
        return new VideoDetailRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(videoId)
                .setResponseListener(handler)
                .setErrorListener(handler)
                .build();
    }

    private static class VideoDetailRequestHandler
            implements Response.Listener<JSONObject>, Response.ErrorListener {

        private final WeakReference<ShowVideoDetailActivity> mOwner;

        private VideoDetailRequestHandler(WeakReference<ShowVideoDetailActivity> owner) {
            mOwner = owner;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            new RuntimeException(error).printStackTrace();
        }

        @Override
        public void onResponse(JSONObject response) {
            final ShowVideoDetailActivity owner = mOwner.get();
            if (owner != null) {
                try {
                    owner.showVideo(convertToVideo(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private static Video convertToVideo(JSONObject jsonObject) throws JSONException {
            Video result = new Video();
            result.id = jsonObject.getString("id");
            result.title = jsonObject.getString("title");
            result.link = jsonObject.getString("link");
            result.thumbnail = jsonObject.getString("thumbnail");
            result.bigThumbnail = jsonObject.getString("bigThumbnail");
            result.duration = jsonObject.getString("duration");
            result.description = jsonObject.getString("description");
            return result;
        }
    }

    private static class ViewHolder {

        TextView description;

        ViewHolder(Activity container) {
            description = (TextView) container.findViewById(R.id.description);
        }
    }


}
