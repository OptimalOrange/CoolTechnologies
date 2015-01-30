package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.network.NetworkChecker;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.storage.sqlite.DBManager;
import com.optimalorange.cooltechnologies.ui.fragment.ListCommentsFragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import name.cpr.VideoEnabledWebChromeClient;
import name.cpr.VideoEnabledWebView;

//TODO 处理横竖屏切换等onConfigurationChange
public class PlayVideoActivity extends LoginableBaseActivity {

    /**
     * 应当播放的Video<br/>
     * Type: {@link FavoriteBean}
     */
    public static final String EXTRA_KEY_VIDEO =
            PlayVideoActivity.class.getName() + ".extra.KEY_VIDEO";

    /** {@link android.webkit.WebView WebView}要加载的网页的路径 */
    private static final String PATH_PLAY_VIDEO_HTML = "file:///android_asset/playvideo.html";

    private static final String URL_BLANK = "about:blank";

    /** {@link #PATH_PLAY_VIDEO_HTML}中用到的{@link WebAppInterface WebAppInterface}实例名 */
    private static final String JAVASCRIPT_INTERFACE_GENERIC = "webAppInterface";

    /** {@link #PATH_PLAY_VIDEO_HTML}中用到的{@link OnPlayStartListener OnPlayStartListener}实例名 */
    private static final String JAVASCRIPT_INTERFACE_ON_PLAY_START_LISTENER =
            "webAppOnPlayStartListener";

    /**
     * {@link #PATH_PLAY_VIDEO_HTML}中用到的
     * {@link WebAppFullscreenToggleSwitch WebAppFullscreenToggleSwitch}实例名
     */
    private static final String JAVASCRIPT_INTERFACE_FULLSCREEN_TOGGLE_SWITCH =
            "webAppFullscreenToggleSwitch";

    private FavoriteBean mFavoriteBean;

    private DefaultSharedPreferencesSingleton mDefaultSharedPreferencesSingleton;

    private NetworkChecker mNetworkChecker;

    private BroadcastReceiver mNetworkReceiver;

    /**
     * 状态属性：已经加载播放器
     */
    private boolean mPlayerIsLoaded = false;

    private LinearLayout mNonVideoLayout;

    private VideoEnabledWebView mWebView;

    private CustomWebViewClient mWebViewClient;

    private VideoEnabledWebChromeClient mChromeClient;

    //--------------------------------------------------------------------------
    // 初始化属性
    //--------------------------------------------------------------------------

    {
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initializeWebView();
            }
        };
    }

    //--------------------------------------------------------------------------
    // 覆写Activity的生命周期方法
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化属性
        mFavoriteBean = (FavoriteBean) getIntent().getSerializableExtra(EXTRA_KEY_VIDEO);
        if (mFavoriteBean == null) {
            throw new IllegalStateException("Please do intent.putExtra(EXTRA_KEY_VIDEO, vid)");
        }
        mDefaultSharedPreferencesSingleton = DefaultSharedPreferencesSingleton.getInstance(this);
        mNetworkChecker = NetworkChecker.newInstance(this);

        setContentView(R.layout.activity_play_video);

        mWebView = new VideoEnabledWebView(this);
        mNonVideoLayout = (LinearLayout) findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout);
        View loadingView =
                getLayoutInflater().inflate(R.layout.view_loading_video, videoLayout, false);

        addWebViewToNonVideoLayout();
        addOthersToNonVideoLayout(savedInstanceState);

        // setWebViewClient
        mWebViewClient = new CustomWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);
        // setWebChromeClient
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mChromeClient = new VideoEnabledWebChromeClient(
                    mNonVideoLayout, videoLayout, loadingView, mWebView);
        } else {
            final WebAppFullscreenToggleSwitch fullscreenToggleSwitch =
                    new WebAppFullscreenToggleSwitch();
            //TODO 分析解决安全问题
            mWebView.addJavascriptInterface(
                    fullscreenToggleSwitch, JAVASCRIPT_INTERFACE_FULLSCREEN_TOGGLE_SWITCH);
            mChromeClient = new VideoEnabledWebChromeClient(
                    mNonVideoLayout, videoLayout, loadingView, mWebView) {
                // API level >= 19 时，super.onHideCustomView不会触发callback.onCustomViewHidden，
                // 需要覆写以触发此事件方法。
                @Override
                public void onHideCustomView() {
                    super.onHideCustomView();
                    if (fullscreenToggleSwitch.mVideoViewContainer != null) {
                        fullscreenToggleSwitch.callback.onCustomViewHidden();
                    }
                }
            };
        }
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
        // end setWebChromeClient

        // addJavascriptInterface
        WebAppInterface webAppInterface = new WebAppInterface()
                .setClientId(getString(R.string.youku_client_id))
                .setVid(getVideoId());
        //TODO 分析解决安全问题
        mWebView.addJavascriptInterface(webAppInterface, JAVASCRIPT_INTERFACE_GENERIC);
        mWebView.addJavascriptInterface(
                new OnPlayStartListener(DBManager.getInstance(this), mFavoriteBean),
                JAVASCRIPT_INTERFACE_ON_PLAY_START_LISTENER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(
                mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        initializeWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mNetworkReceiver);
        finalizeWebView();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mChromeClient = null;
        mWebViewClient = null;
        mNonVideoLayout = null;
        if (mWebView != null) {
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }
            mWebView.destroy();
            mWebView = null;
        }
        mNetworkChecker = null;
        mDefaultSharedPreferencesSingleton = null;
        mFavoriteBean = null;
        super.onDestroy();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // readd webview to update its height
        if (mNonVideoLayout.indexOfChild(mWebView) >= 0) { // if mWebView is in mNonVideoLayout
            mNonVideoLayout.removeView(mWebView);
            addWebViewToNonVideoLayout();
        }
    }

    //--------------------------------------------------------------------------
    // 新声明方法
    //--------------------------------------------------------------------------

    private String getVideoId() {
        return mFavoriteBean.videoId;
    }

    private void addWebViewToNonVideoLayout() {
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int desirableHeight = (int) Math.ceil(widthPixels * 9.0 / 16);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                desirableHeight <= heightPixels ? desirableHeight : heightPixels,
                0 //WebView的layout_weight设为0，评论等其他View的weight非0，让其他View充满剩余空间
        );
        mNonVideoLayout.addView(mWebView, 0, layoutParams);
    }

    private void addOthersToNonVideoLayout(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.others_container, ListCommentsFragment.newInstance(getVideoId()))
                    .commit();
        }
    }

    private void loadUrlAndClearHistory(String url) {
        mWebViewClient.shouldClearHistory(true);
        mWebView.loadUrl(url);
    }

    /**
     * 如果条件允许的话，加载播放器；否则，加载{@link #URL_BLANK}
     *
     * @return {@link #mPlayerIsLoaded}
     */
    private boolean initializeWebView() {
        boolean loadPlayer = canPlayNow();
        if (loadPlayer) {
            if (!mPlayerIsLoaded) {
                loadUrlAndClearHistory(PATH_PLAY_VIDEO_HTML);
            }
        } else {
            if (mPlayerIsLoaded) {
                loadUrlAndClearHistory(URL_BLANK);
            }
        }
        mPlayerIsLoaded = loadPlayer;
        return loadPlayer;
    }

    private void finalizeWebView() {
        loadUrlAndClearHistory(URL_BLANK);
        mPlayerIsLoaded = false;
    }

    private boolean canPlayNow() {
        if (mDefaultSharedPreferencesSingleton.onlyPlayVideoWhenUseWlan()) {
            return mNetworkChecker.isWifiConnected();
        } else {
            return mNetworkChecker.isConnected();
        }
    }

    //--------------------------------------------------------------------------
    // 嵌套类
    //--------------------------------------------------------------------------

    //-------------------------------------
    // WebViewClient
    //-------------------------------------

    private static class CustomWebViewClient extends WebViewClient {

        // 参考自 http://stackoverflow.com/questions/8103532/how-to-clear-webview-history-in-android#20657089
        private boolean mShouldClearHistory = false;

        public void shouldClearHistory(boolean shouldClearHistory) {
            mShouldClearHistory = shouldClearHistory;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mShouldClearHistory) {
                view.clearHistory();
                mShouldClearHistory = false;
            }
        }
    }

    //-------------------------------------
    // JavascriptInterface
    //-------------------------------------

    /**
     * {@link android.webkit.WebView WebView}中网页可以访问的本地API，用于设置参数，如
     * {@link WebAppInterface#getVid() vid}<br/>
     * 本类是线程安全的
     */
    private static class WebAppInterface {

        /** 控制条底色：明；主色板颜色：橘色 */
        private static final String DEFAULT_STYLE_ID = "8";

        // important! must be **volatile**
        private volatile String mTitle = "";

        private volatile String mStyleId = DEFAULT_STYLE_ID;

        private volatile String mClientId;

        /** Video ID */
        private volatile String mVid;

        private volatile boolean mAutoplay = true;

        private volatile boolean mShowRelated = false;

        public WebAppInterface setTitle(String title) {
            mTitle = title;
            return this;
        }

        public WebAppInterface setStyleId(String styleId) {
            mStyleId = styleId;
            return this;
        }

        public WebAppInterface setClientId(String clientId) {
            mClientId = clientId;
            return this;
        }

        public WebAppInterface setVid(String vid) {
            mVid = vid;
            return this;
        }

        public WebAppInterface setAutoplay(boolean autoplay) {
            mAutoplay = autoplay;
            return this;
        }

        public WebAppInterface setShowRelated(boolean showRelated) {
            mShowRelated = showRelated;
            return this;
        }

        @JavascriptInterface
        public String getTitle() {
            return mTitle;
        }

        @JavascriptInterface
        public String getStyleId() {
            return mStyleId;
        }

        @JavascriptInterface
        public String getClientId() {
            return mClientId;
        }

        @JavascriptInterface
        public String getVid() {
            return mVid;
        }

        @JavascriptInterface
        public boolean isAutoplay() {
            return mAutoplay;
        }

        @JavascriptInterface
        public boolean isShowRelated() {
            return mShowRelated;
        }
    }

    /**
     * {@link WebView}中的播放器开始播放时被调用。
     * <p>本类是线程安全的。</p>
     */
    private static class OnPlayStartListener {

        private final DBManager mDBManager;

        private final FavoriteBean mFavoriteBean;

        private OnPlayStartListener(DBManager dbManager, FavoriteBean favoriteBean) {
            mDBManager = dbManager;
            mFavoriteBean = favoriteBean;
        }

        @JavascriptInterface
        public void onPlayStart() {
            mDBManager.saveHistory(mFavoriteBean);
        }
    }

    /**
     * 在{@link Build.VERSION_CODES#KITKAT}以上版本，点击{@link #PATH_PLAY_VIDEO_HTML}中视屏全屏按钮时，
     * 触发本类的实例的{@link PlayVideoActivity.WebAppFullscreenToggleSwitch#toggleFullscreen()}方法。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private class WebAppFullscreenToggleSwitch {

        private FrameLayout mVideoViewContainer;

        private final WebChromeClient.CustomViewCallback callback =
                new WebChromeClient.CustomViewCallback() {
                    @Override
                    public void onCustomViewHidden() {
                        if (mVideoViewContainer != null) {
                            mVideoViewContainer.removeView(mWebView);
                            addWebViewToNonVideoLayout();
                            // release memory
                            mVideoViewContainer = null;
                        }
                    }
                };

        /**
         * 触发全屏与非全屏状态间的切换。
         */
        @JavascriptInterface
        public void toggleFullscreen() {
            PlayVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mChromeClient.isVideoFullscreen()) {
                        assert mVideoViewContainer == null;
                        mNonVideoLayout.removeView(mWebView);
                        mVideoViewContainer = new FrameLayout(PlayVideoActivity.this);
                        mVideoViewContainer.addView(
                                mWebView,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        );
                        mChromeClient.onShowCustomView(mVideoViewContainer, callback);
                    } else {
                        mChromeClient.onHideCustomView();
                        // see also WebAppFullscreenToggleSwitch.callback
                    }
                }
            });
        }
    }

}
