package com.optimalorange.cooltechnologies.network;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;

/**
 * Created by peican on 2015/2/1.
 */
public class CreateComment extends JsonRequest<String> {

    private static final String YOUKU_API_JSON_CREATE_COMMENT_ID = "id";

    public CreateComment(int method, String url, JSONObject jsonRequest,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener) {

        super(method, url, jsonRequest == null ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = new JSONObject(jsonString);
            String id = jsonObject.getString(YOUKU_API_JSON_CREATE_COMMENT_ID);
            return Response.success(id,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    public static class Builder {

        private static final int YOUKU_API_REQUEST_METHOD = Method.GET;

        private static final String YOUKU_API_CREATE_COMMENT
                = "https://openapi.youku.com/v2/comments/create.json";

        /**
         * A {@link JSONObject} to post with the request. Null is allowed and
         * indicates no parameters will be posted along with request.
         */
        @Nullable
        private JSONObject mJsonRequest;

        /**
         * Listener to receive the comments response
         */
        private Response.Listener<String> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        @Nullable
        private Response.ErrorListener mErrorListener;

        /** 应用Key（必设） */
        private String client_id;

        /** OAuth2授权 */
        private String access_token;

        /** 视频ID */
        private String video_id;

        /** 评论内容 */
        private String content;

        /** 回复ID */
        @Nullable
        private String reply_id;

        /** 验证码key */
        @Nullable
        private String captcha_key;

        /** 验证码内容 */
        @Nullable
        private String captcha_text;

        /**
         * 应用Key（必设参数）
         */
        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
            return this;
        }

        /**
         * OAuth2授权
         */
        public Builder setAccess_token(String access_token) {
            this.access_token = access_token;
            return this;
        }

        /**
         * 关键字 多个关键字用空格分隔
         */
        public Builder setVideo_id(String video_id) {
            this.video_id = video_id;
            return this;
        }

        /**
         * 评论内容
         */
        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * 回复ID
         */
        public Builder setReply_id(String reply_id) {
            this.reply_id = reply_id;
            return this;
        }

        /**
         * 验证码key
         */
        public Builder setCaptcha_key(String captcha_key) {
            this.captcha_key = captcha_key;
            return this;
        }

        /**
         * 验证码内容
         */
        public Builder setCaptcha_text(String captcha_text) {
            this.captcha_text = captcha_text;
            return this;
        }

        /**
         * A {@link JSONObject} to post with the request. Null is allowed and
         * indicates no parameters will be posted along with request.
         */
        public Builder setJsonRequest(@Nullable JSONObject jsonRequest) {
            mJsonRequest = jsonRequest;
            return this;
        }

        /**
         * Listener to receive the id response
         */
        public Builder setResponseListener(Response.Listener<String> responseListener) {
            mResponseListener = responseListener;
            return this;
        }

        /**
         * Error listener, or null to ignore errors.
         */
        public Builder setErrorListener(@Nullable Response.ErrorListener errorListener) {
            mErrorListener = errorListener;
            return this;
        }

        public CreateComment build() {
            return new CreateComment(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mJsonRequest,
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_CREATE_COMMENT)
                    .buildUpon();
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            if (access_token == null) {
                throw new IllegalStateException("Please set access_token before build");
            }
            urlBuilder.appendQueryParameter("access_token", access_token);
            if (video_id == null) {
                throw new IllegalStateException("Please set video_id before build");
            }
            urlBuilder.appendQueryParameter("video_id", video_id);
            if (content == null) {
                throw new IllegalStateException("please set content before build");
            }
            urlBuilder.appendQueryParameter("content", content);
            if (reply_id != null) {
                urlBuilder.appendQueryParameter("reply_id", reply_id);
            }
            if (captcha_key != null) {
                urlBuilder.appendQueryParameter("captcha_key", captcha_key);
            }
            if (captcha_text != null) {
                urlBuilder.appendQueryParameter("captcha_text", captcha_text);
            }
            return urlBuilder.build().toString();
        }
    }

}
