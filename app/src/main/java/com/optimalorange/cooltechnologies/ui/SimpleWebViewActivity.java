package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SimpleWebViewActivity extends Activity {

    /**
     * 应当显示的页面的url<br/>
     * Type: {@link String}
     */
    public static final String EXTRA_KEY_LINK =
            SimpleWebViewActivity.class.getName() + ".extra.KEY_VIDEO_LINK";

    /** 用于重置{@link WebView}的空网页 */
    private static final String URL_BLANK = "about:blank";

    private String mLink;

    private WebView mWebView;

    public static Intent buildIntent(Context context, String link) {
        final Intent result = new Intent(context, SimpleWebViewActivity.class);
        result.putExtra(EXTRA_KEY_LINK, link);
        return result;
    }

    public static void start(Context context, String link) {
        context.startActivity(buildIntent(context, link));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web_view);

        mLink = getIntent().getStringExtra(EXTRA_KEY_LINK);
        if (mLink == null) {
            throw new IllegalStateException("Please set url link with EXTRA_KEY_LINK");
        }

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWebView.loadUrl(mLink);
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

}
