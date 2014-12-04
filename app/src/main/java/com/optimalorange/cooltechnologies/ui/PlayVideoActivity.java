package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import name.cpr.VideoEnabledWebChromeClient;
import name.cpr.VideoEnabledWebView;

//TODO set vid
public class PlayVideoActivity extends Activity {

    private VideoEnabledWebView mWebView;

    private VideoEnabledWebChromeClient mChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mWebView.loadUrl("file:///android_asset/playvideo.html");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
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
}
