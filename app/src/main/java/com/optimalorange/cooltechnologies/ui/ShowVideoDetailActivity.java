package com.optimalorange.cooltechnologies.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.network.CreateFavoriteRequest;
import com.optimalorange.cooltechnologies.network.DestroyFavoriteRequest;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.network.VideoDetailRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.storage.sqlite.DBManager;
import com.optimalorange.cooltechnologies.ui.entity.Video;
import com.optimalorange.cooltechnologies.ui.fragment.SimpleListCommentsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class ShowVideoDetailActivity extends LoginableBaseActivity {

    /**
     * 应当播放的Video的ID<br/>
     * Type: {@link String}
     */
    public static final String EXTRA_KEY_VIDEO_ID =
            ShowVideoDetailActivity.class.getName() + ".extra.KEY_VIDEO_ID";


    private VolleySingleton mVolleySingleton;

    private String mYoukuClientId;

    private Video mVideo;

    /**
     * 状态属性：已经收藏此视频
     */
    private boolean mHasBookmarked = false;

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
            mViews.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo();
                }
            });
            // load thumbnail
            final ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(
                    mViews.thumbnail, 0, 0
            );
            mVolleySingleton.getImageLoader().get(video.bigThumbnail, imageListener);
        }
    }

    public void playVideo() {
        if (mVideo == null) {
            throw new IllegalStateException("cannot play video before load it.");
        }

        final FavoriteBean favoriteBean = convertToFavoriteBean(mVideo);
        // 保存播放历史
        DBManager.getInstance(this).saveHistory(favoriteBean);
        // 跳转到PlayVideoActivity
        final Intent intent = new Intent(ShowVideoDetailActivity.this, PlayVideoActivity.class);
        intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO, favoriteBean);
        startActivity(intent);
    }

    private static FavoriteBean convertToFavoriteBean(Video video) {
        final FavoriteBean result = new FavoriteBean();
        result.videoId = video.id;
        result.title = video.title;
        result.duration = video.duration;
        result.imageUrl = video.bigThumbnail;
        result.link = video.link;
        return result;
    }

    /**
     * 有新添加的菜单项
     */
    private boolean hasDeclaredMenuItem() {
        return hasLoggedIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化属性
        final String videoIdExtra = getIntent().getStringExtra(EXTRA_KEY_VIDEO_ID);
        if (videoIdExtra == null) {
            throw new IllegalStateException("Please do intent.putExtra(EXTRA_KEY_VIDEO_ID, vid)");
        }
        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(this);

        //TODO 实现响应式UI
        if (!NetworkChecker.newInstance(this).isConnected()) {
            NetworkChecker.openNoConnectionDialog(getSupportFragmentManager());
        }

        initViews();
        addCommentsFragment(savedInstanceState, videoIdExtra);

        mVolleySingleton.addToRequestQueue(buildVideoDetailRequest(videoIdExtra));
    }

    private void initViews() {
        setContentView(R.layout.activity_show_video_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViews = new ViewHolder(this);

        setTitle(null);
    }

    // 评论
    private void addCommentsFragment(Bundle savedInstanceState, String videoId) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.comments_fragment_container,
                            SimpleListCommentsFragment.newInstance(videoId))
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        mViews = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean superResult = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_activity_show_video_detail, menu);
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean superResult = super.onPrepareOptionsMenu(menu);
        MenuItem bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
        boolean hasLoggedIn = hasLoggedIn();
        bookmarkMenuItem.setVisible(hasLoggedIn);
        bookmarkMenuItem.setEnabled(hasLoggedIn);
        if (mHasBookmarked) {
            bookmarkMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            bookmarkMenuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
        }
        return hasDeclaredMenuItem() || superResult;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bookmark:
                if (!getNetworkChecker().isConnected()) {
                    NetworkChecker.openNoConnectionDialog(getSupportFragmentManager());
                    return true;
                }
                DefaultSharedPreferencesSingleton defaultSharedPreferences
                        = getDefaultSharedPreferencesSingleton();
                if (defaultSharedPreferences.hasLoggedIn()) {
                    String token = defaultSharedPreferences.retrieveString("access_token", "");
                    if (mHasBookmarked) {
                        mVolleySingleton.addToRequestQueue(buildDestroyFavoriteRequest(token));
                    } else {
                        mVolleySingleton.addToRequestQueue(buildCreateFavoriteRequest(token));
                    }
                } else {
                    System.err.println("Shouldn't be there.@onOptionsItemSelected.action_bookmark");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    /** 创建添加收藏的请求 */
    private CreateFavoriteRequest buildCreateFavoriteRequest(String token) {
        return new CreateFavoriteRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(mVideo.id)
                .setAccess_token(token)
                .setResponseListener(new OnResponseListener(this, RequestType.CREATE_FAVORITE))
                .setErrorListener(new OnErrorResponseListener(this, RequestType.CREATE_FAVORITE))
                .build();
    }

    /** 创建取消收藏的请求 */
    private DestroyFavoriteRequest buildDestroyFavoriteRequest(String token) {
        return new DestroyFavoriteRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setVideo_id(mVideo.id)
                .setAccess_token(token)
                .setResponseListener(new OnResponseListener(this, RequestType.DESTROY_FAVORITE))
                .setErrorListener(new OnErrorResponseListener(this, RequestType.DESTROY_FAVORITE))
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

    private enum RequestType {CREATE_FAVORITE, DESTROY_FAVORITE}

    /**
     * 此类不会导致内存泄漏
     */
    private static class OnResponseListener implements Response.Listener<JSONObject> {

        private final WeakReference<ShowVideoDetailActivity> mActivityWeakReference;

        private final WeakReference<Context> mContextWeakReference;

        private final RequestType mRequestType;

        public OnResponseListener(ShowVideoDetailActivity activity, RequestType requestType) {
            mActivityWeakReference = new WeakReference<>(activity);
            mContextWeakReference = new WeakReference<>(activity.getApplicationContext());
            mRequestType = requestType;
        }

        @Override
        public void onResponse(JSONObject jsonObject) {
            final ShowVideoDetailActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                activity.mHasBookmarked = mRequestType == RequestType.CREATE_FAVORITE;
                activity.invalidateOptionsMenu();
            }
            final Context context = mContextWeakReference.get();
            if (context != null) {
                Toast.makeText(context, getToastTextResId(), Toast.LENGTH_SHORT).show();
            }
        }

        private int getToastTextResId() {
            switch (mRequestType) {
                case CREATE_FAVORITE:
                    return R.string.create_favorite_success;
                case DESTROY_FAVORITE:
                    return R.string.destroy_favorite_success;
                default:
                    throw new IllegalArgumentException("Unknown RequestType:" + mRequestType);
            }
        }
    }

    /**
     * 此类不会导致内存泄漏
     */
    private static class OnErrorResponseListener implements Response.ErrorListener {

        private final WeakReference<Context> mContextWeakReference;

        private final RequestType mRequestType;

        private OnErrorResponseListener(Context context, RequestType requestType) {
            mContextWeakReference = new WeakReference<>(context.getApplicationContext());
            mRequestType = requestType;
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            final Context context = mContextWeakReference.get();
            if (context != null) {
                Toast.makeText(context, getToastTextResId(), Toast.LENGTH_SHORT).show();
            }
        }

        private int getToastTextResId() {
            switch (mRequestType) {
                case CREATE_FAVORITE:
                    return R.string.create_favorite_failure;
                case DESTROY_FAVORITE:
                    return R.string.destroy_favorite_failure;
                default:
                    throw new IllegalArgumentException("Unknown RequestType:" + mRequestType);
            }
        }
    }


    private static class ViewHolder {

        TextView title;

        TextView description;

        ImageView thumbnail;

        View playButton;

        ViewHolder(Activity container) {
            title = (TextView) container.findViewById(R.id.title);
            description = (TextView) container.findViewById(R.id.description);
            thumbnail = (ImageView) container.findViewById(R.id.app_bar_image);
            playButton = container.findViewById(R.id.fab);
        }

    }


}
