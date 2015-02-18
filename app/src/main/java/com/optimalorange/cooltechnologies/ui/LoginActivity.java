package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.util.Const;
import com.umeng.analytics.MobclickAgent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by WANGZHENGZE on 2014/11/26.
 */
public class LoginActivity extends BaseActivity implements Handler.Callback {

    private static final String KEY_URL = LoginActivity.class.getName() + ".KEY_URL";

    private static final String MY_URL = "http://optimalorange.github.io/CoolTechnologies/";

    private static final int GET_TOKEN_OK = 0;

    private static final int GET_TOKEN_ERROR = 1;

    private static final String RESPONSE_TYPE = "token";

    private static final String BASE_AUTHORIZE_URL
            = "https://openapi.youku.com/v2/oauth2/authorize";

    private Handler mHandler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final WebView loginWebView = (WebView) findViewById(R.id.login_web_view);
        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(MY_URL)) {
                    if (url.contains("error")) {
                        Message msg = new Message();
                        msg.what = GET_TOKEN_ERROR;
                        handleMessage(msg);
                        return true;
                    }
                    loginWebView.setVisibility(View.GONE);
                    String newUrl = url.replace("#", "?");
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_URL, newUrl);
                    msg.setData(bundle);
                    msg.what = GET_TOKEN_OK;
                    handleMessage(msg);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        };

        loginWebView.setWebViewClient(client);
        loginWebView.loadUrl(buildUrl());

    }

    private String buildUrl() {
        final Uri.Builder urlBuilder = Uri.parse(BASE_AUTHORIZE_URL)
                .buildUpon();
        urlBuilder.appendQueryParameter("client_id", getString(R.string.youku_client_id));
        urlBuilder.appendQueryParameter("response_type", RESPONSE_TYPE);
        urlBuilder.appendQueryParameter("redirect_uri", MY_URL);
        return urlBuilder.build().toString();
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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case GET_TOKEN_OK:
                String newUrl = msg.getData().getString(KEY_URL);
                String access_token = Uri.parse(newUrl).getQueryParameter("access_token");
                String expires_in = Uri.parse(newUrl).getQueryParameter("expires_in");
                String token_type = Uri.parse(newUrl).getQueryParameter("token_type");
                String beginTime = String.valueOf(System.currentTimeMillis() / 1000);
                DefaultSharedPreferencesSingleton preferences = DefaultSharedPreferencesSingleton
                        .getInstance(LoginActivity.this);
                if (preferences.commitSaveString("access_token", access_token) && preferences
                        .commitSaveString("expires_in", expires_in) &&
                        preferences.commitSaveString("token_type", token_type) && preferences
                        .commitSaveString("beginTime", beginTime)) {

                    setResult(Const.RESULT_CODE_SUCCESS_LOGIN_ACTIVITY);
                    finish();
                    showToastInUIThread(R.string.action_login_success);
                } else {
                    setResult(Const.RESULT_CODE_FAIL_LOGIN_ACTIVITY);
                    finish();
                    showToastInUIThread(R.string.action_login_fail);
                }
                break;
            case GET_TOKEN_ERROR:
                setResult(Const.RESULT_CODE_FAIL_LOGIN_ACTIVITY);
                finish();
                showToastInUIThread(R.string.action_login_fail);
                break;
        }
        return false;
    }

    private void showToastInUIThread(final int res) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, getString(res), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
