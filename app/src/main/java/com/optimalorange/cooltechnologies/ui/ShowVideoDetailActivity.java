package com.optimalorange.cooltechnologies.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
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
            mViews.title.setText(video.title);
            mViews.description.setText(video.description);
            final ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(
                    mViews.thumbnail, 0, 0
            );
            VolleySingleton.getInstance(mViews.thumbnail.getContext()).getImageLoader()
                    .get(video.bigThumbnail, imageListener);
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

        initViews();

        mVolleySingleton.addToRequestQueue(buildVideoDetailRequest(mVideoIdExtra));
    }

    private void initViews() {
        setContentView(R.layout.activity_show_video_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mViews = new ViewHolder(this);

        setTitle(null);
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

        TextView title;

        TextView description;

        ImageView thumbnail;

        ViewHolder(Activity container) {
            title = (TextView) container.findViewById(R.id.title);
            description = (TextView) container.findViewById(R.id.description);
            thumbnail = (ImageView) container.findViewById(R.id.app_bar_image);
        }
    }


}
