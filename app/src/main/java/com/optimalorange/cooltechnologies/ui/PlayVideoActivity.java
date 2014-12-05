package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

import java.util.LinkedList;

import name.cpr.VideoEnabledWebChromeClient;
import name.cpr.VideoEnabledWebView;

//TODO 处理横竖屏切换等onConfigurationChange
public class PlayVideoActivity extends Activity {

    /**
     * 应当播放的Video的id<br/>
     * Type: String
     *
     * @see com.optimalorange.cooltechnologies.entity.Video#getId() Video.getId()
     */
    public static final String EXTRA_KEY_VIDEO_ID =
            PlayVideoActivity.class.getName() + ".extra.KEY_VIDEO_ID";

    /** {@link android.webkit.WebView WebView}要加载的网页的路径 */
    private static final String PATH_PLAY_VIDEO_HTML = "file:///android_asset/playvideo.html";

    /** {@link #PATH_PLAY_VIDEO_HTML}中用到的{@link WebAppInterface WebAppInterface}实例名 */
    private static final String JAVASCRIPT_INTERFACE_NAME = "webAppInterface";

    private VideoEnabledWebView mWebView;

    private VideoEnabledWebChromeClient mChromeClient;


    //--------------------------------------------------------------------------
    // 覆写Activity的生命周期方法
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String vid = getIntent().getStringExtra(EXTRA_KEY_VIDEO_ID);
        if (vid == null) {
            throw new IllegalStateException("Please do intent.putExtra(EXTRA_KEY_VIDEO_ID, vid)");
        }

        setContentView(R.layout.activity_play_video);

        mWebView = (VideoEnabledWebView) findViewById(R.id.webView);
        View nonVideoLayout = findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout);
        View loadingView =
                getLayoutInflater().inflate(R.layout.view_loading_video, videoLayout, false);

        // 设置WebView的lyaout_height
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int desirableHeight = (int) Math.ceil(widthPixels * 9.0 / 16);
        if (desirableHeight <= heightPixels) {
            mWebView.getLayoutParams().height = desirableHeight;
        } else {
            mWebView.getLayoutParams().height = heightPixels;
        }

        mChromeClient =
                new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, mWebView);
        mChromeClient.setOnToggledFullscreen(
                new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
                    @Override
                    public void toggledFullscreen(boolean fullscreen) {
                        // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                        if (fullscreen) {
                            WindowManager.LayoutParams attrs = getWindow().getAttributes();
                            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            getWindow().setAttributes(attrs);
                            //noinspection all
                            getWindow().getDecorView()
                                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                            if (getActionBar() != null) {
                                getActionBar().hide();
                            }
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        } else {
                            WindowManager.LayoutParams attrs = getWindow().getAttributes();
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            getWindow().setAttributes(attrs);
                            //noinspection all
                            getWindow().getDecorView()
                                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            if (getActionBar() != null) {
                                getActionBar().show();
                            }
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }

                    }
                });
        mWebView.setWebChromeClient(mChromeClient);

        WebAppInterface webAppInterface = new WebAppInterface.Builder()
                .setClientId(getString(R.string.youku_client_id))
                .setVid(vid)
                .build();
        //TODO 分析解决安全问题
        mWebView.addJavascriptInterface(webAppInterface, JAVASCRIPT_INTERFACE_NAME);
        mWebView.loadUrl(PATH_PLAY_VIDEO_HTML);
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mChromeClient.onBackPressed()) {
            return;
        }

        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }

        super.onBackPressed();
    }

    //--------------------------------------------------------------------------
    // 内部类
    //--------------------------------------------------------------------------

    /**
     * {@link android.webkit.WebView WebView}中网页可以访问的本地API，用于设置参数，如
     * {@link WebAppInterface#getYoukuPlayerArguments() YoukuPlayerArguments}
     *
     * @see WebAppInterface.Builder WebAppInterface.Builder
     */
    private static class WebAppInterface {

        private String mTitle;

        private JSONObject mYoukuPlayerArguments;

        @JavascriptInterface
        public String getTitle() {
            return mTitle;
        }

        @JavascriptInterface
        public String getYoukuPlayerArguments() {
            return mYoukuPlayerArguments.toString();
        }

        /**
         * 用于构建{@link WebAppInterface WebAppInterface}的Builder
         */
        public static class Builder {

            private static final String DEFAULT_TITLE = "";

            /** 控制条底色：明；主色板颜色：橘色 */
            private static final String DEFAULT_STYLE_ID = "8";

            private String mTitle = DEFAULT_TITLE;

            private String mStyleId = DEFAULT_STYLE_ID;

            private String mClientId;

            /** Video ID */
            private String mVid;

            private Boolean mAutoplay;

            private Boolean mShowRelated;

            public Builder setTitle(String title) {
                mTitle = title;
                return this;
            }

            public Builder setStyleId(String styleId) {
                mStyleId = styleId;
                return this;
            }

            public Builder setClientId(String clientId) {
                mClientId = clientId;
                return this;
            }

            public Builder setVid(String vid) {
                mVid = vid;
                return this;
            }

            public WebAppInterface build() {
                assertState("build()");
                WebAppInterface result = new WebAppInterface();
                result.mTitle = this.mTitle;
                JSONObject youkuPlayerArguments = new JSONObject();
                try {
                    youkuPlayerArguments
                            .put("styleid", mStyleId)
                            .put("client_id", mClientId)
                            .put("vid", mVid);
                    if (mAutoplay != null) {
                        youkuPlayerArguments.put("autoplay", mAutoplay.booleanValue());
                    }
                    if (mShowRelated != null) {
                        youkuPlayerArguments.put("show_related", mShowRelated.booleanValue());
                    }
                } catch (JSONException e) {
                    throw new Error("json format error", e);
                }
                result.mYoukuPlayerArguments = youkuPlayerArguments;
                return result;
            }

            private void assertState(String where) {
                LinkedList<String> nulls = new LinkedList<>();
                if (mClientId == null) {
                    nulls.add("clientId");
                }
                if (mVid == null) {
                    nulls.add("Vid");
                }
                if (nulls.size() > 0) {
                    throw new IllegalStateException("Please set " + nulls + " before " + where);
                }
            }
        }
    }

}
