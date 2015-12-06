package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SimpleWebViewActivity extends Activity {

    /**
     * 应当显示的页面的url<br/>
     * Type: {@link String}
     */
    public static final String EXTRA_KEY_VIDEO_ID =
            SimpleWebViewActivity.class.getName() + ".extra.KEY_VIDEO_ID";

    /** 用于重置{@link WebView}的空网页 */
    private static final String URL_BLANK = "about:blank";

    /** {@link #URL_PLAY_VIDEO}中用到的{@link WebAppInterface WebAppInterface}实例名 */
    private static final String JAVASCRIPT_INTERFACE_GENERIC = "webAppInterface";

    /** 正常情况下，{@link android.webkit.WebView WebView}要加载的网页的路径 */
    private static final String URL_PLAY_VIDEO = "file:///android_asset/playvideo.html";

    private WebView mWebView;

    public static Intent buildIntent(Context context, String videoId) {
        final Intent result = new Intent(context, SimpleWebViewActivity.class);
        result.putExtra(EXTRA_KEY_VIDEO_ID, videoId);
        return result;
    }

    public static void start(Context context, String link) {
        context.startActivity(buildIntent(context, link));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web_view);

        final String videoId = getIntent().getStringExtra(EXTRA_KEY_VIDEO_ID);
        if (videoId == null) {
            throw new IllegalStateException("Please set url link with EXTRA_KEY_VIDEO_ID");
        }

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        // addJavascriptInterface
        WebAppInterface webAppInterface = new WebAppInterface()
                .setClientId(getString(R.string.youku_client_id))
                .setVid(videoId);
        //TODO 分析解决安全问题
        mWebView.addJavascriptInterface(webAppInterface, JAVASCRIPT_INTERFACE_GENERIC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWebView.loadUrl(URL_PLAY_VIDEO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // call it because this didn't extend BaseActivity
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(getClass().getSimpleName());

        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        mWebView.onPause();

        MobclickAgent.onPageEnd(getClass().getSimpleName());
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mWebView.loadUrl(URL_BLANK);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        final ViewParent parent = mWebView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(mWebView);
            mWebView.destroy();
        }
        mWebView = null;
        super.onDestroy();
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

        private volatile boolean mAutoplay = false;

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

}
