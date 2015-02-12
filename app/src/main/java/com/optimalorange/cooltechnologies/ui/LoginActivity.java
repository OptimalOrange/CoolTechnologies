package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.storage.DefaultSharedPreferencesSingleton;
import com.optimalorange.cooltechnologies.util.Const;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/11/26.
 */
public class LoginActivity extends BaseActivity implements Handler.Callback {

    private static final String MY_URL = "http://59.67.152.71/wangzhengze/";

    private static final String BASE_TOKEN_URL = "https://openapi.youku.com/v2/oauth2/token";

    private static final int GET_TOKEN_OK = 0;

    private static final int GET_TOKEN_ERROR = 1;

    private static final String GRANT_TYPE = "authorization_code";

    private static final String RESPONSE_TYPE = "code";

    private static final String BASE_AUTHORIZE_URL
            = "https://openapi.youku.com/v2/oauth2/authorize?";

    private Handler mHandler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final WebView loginWebView = (WebView) findViewById(R.id.login_web_view);
        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("wzz login", "url=" + url);
                if (url.startsWith(MY_URL)) {
                    if (url.contains("error")) {
                        Message msg = new Message();
                        msg.what = GET_TOKEN_ERROR;
                        handleMessage(msg);
                        return true;
                    }
                    loginWebView.setVisibility(View.GONE);
                    String params[] = url.split("\\?");
                    String code = params[1].substring(5, 37);
                    Log.e("wzz login", "code=" + code);
                    requestToken(code);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        };

        loginWebView.getSettings().setJavaScriptEnabled(true);
        loginWebView.setWebViewClient(client);
        loginWebView.loadUrl(BASE_AUTHORIZE_URL + "client_id=" + getString(R.string.youku_client_id)
                + "&response_type=" + RESPONSE_TYPE + "&redirect_uri=" + MY_URL);

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

    private void requestToken(String code) {
        final HttpPost request = createPostRequest(code);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpResponse response = client.execute(request);
                    Message msg = new Message();
                    if (response.getStatusLine().getStatusCode() == 200) {
                        msg.what = GET_TOKEN_OK;
                        msg.obj = EntityUtils.toString(response.getEntity());
                        handleMessage(msg);
                    } else {
                        msg.what = GET_TOKEN_ERROR;
                        handleMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private HttpPost createPostRequest(String code) {
        HttpPost request = new HttpPost(BASE_TOKEN_URL);
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        BasicNameValuePair param;
        param = new BasicNameValuePair("client_id", getString(R.string.youku_client_id));
        paramList.add(param);
        param = new BasicNameValuePair("client_secret", getString(R.string.youku_client_secret));
        paramList.add(param);
        param = new BasicNameValuePair("grant_type", GRANT_TYPE);
        paramList.add(param);
        param = new BasicNameValuePair("code", code);
        paramList.add(param);
        param = new BasicNameValuePair("redirect_uri", MY_URL);
        paramList.add(param);
        try {
            request.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return request;
    }

    private String getTokenString(String result) {
        String token = "";
        try {
            JSONObject tokenObject = new JSONObject(result);
            token = tokenObject.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case GET_TOKEN_OK:
                if (DefaultSharedPreferencesSingleton.getInstance(LoginActivity.this)
                        .commitSaveString("user_token", getTokenString(msg.obj.toString()))) {
                    showToastInUIThread(R.string.action_login_success);
                    setResult(Const.RESULT_CODE_SUCCESS_LOGIN_ACTIVITY);
                    Log.e("wzz login", "result=" + msg.obj);
                    finish();
                    return true;
                } // else goto case GET_TOKEN_ERROR
            case GET_TOKEN_ERROR:
                setResult(Const.RESULT_CODE_FAIL_LOGIN_ACTIVITY);
                finish();
                showToastInUIThread(R.string.action_login_fail);
                return true;
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
