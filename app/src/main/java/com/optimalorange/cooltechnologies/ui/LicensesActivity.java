package com.optimalorange.cooltechnologies.ui;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;

public class LicensesActivity extends BaseActivity {

    /** {@link android.webkit.WebView WebView}要加载的网页的路径 */
    private static final String PATH_LICENSES_HTML = "file:///android_asset/licenses.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSettingsMenuItem(false);
        WebView webView = new WebView(this);
        setContentView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView.loadUrl(PATH_LICENSES_HTML);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onPause();
    }

}
