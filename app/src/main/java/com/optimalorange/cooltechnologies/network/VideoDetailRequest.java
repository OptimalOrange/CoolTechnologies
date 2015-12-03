package com.optimalorange.cooltechnologies.network;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import android.net.Uri;

/**
 * see http://open.youku.com/docs?id=46
 */
public class VideoDetailRequest extends JsonObjectRequest {

    public VideoDetailRequest(int method, String url, JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public VideoDetailRequest(int method, String url,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public VideoDetailRequest(int method, String url, String requestBody,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    public VideoDetailRequest(String url, JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    public VideoDetailRequest(String url,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public static class Builder {

        private static final int YOUKU_API_REQUEST_METHOD = Request.Method.GET;

        private static final String YOUKU_API_SHOW_VIDEO_DETAIL =
                "https://openapi.youku.com/v2/videos/show.json";

        /**
         * Listener to receive the comments response
         */
        private Response.Listener<JSONObject> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        private Response.ErrorListener mErrorListener;

        private String client_id;

        private String video_id;

        private String ext;

        public Builder setResponseListener(
                Response.Listener<JSONObject> responseListener) {
            mResponseListener = responseListener;
            return this;
        }

        public Builder setErrorListener(Response.ErrorListener errorListener) {
            mErrorListener = errorListener;
            return this;
        }

        /**
         * 应用Key<br/>
         * 类型：String<br/>
         * 必选
         */
        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
            return this;
        }

        /**
         * 视频ID<br/>
         * 类型：String<br/>
         * 必选
         */
        public Builder setVideo_id(String video_id) {
            this.video_id = video_id;
            return this;
        }

        /**
         * 视频扩展信息返回， 多个用逗号分隔<br/>
         * 类型：String<br/>
         * 可选<br/>
         * 示例：thumbnails,show, dvd,reference
         */
        public Builder setExt(String ext) {
            this.ext = ext;
            return this;
        }

        public VideoDetailRequest build() {
            return new VideoDetailRequest(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_SHOW_VIDEO_DETAIL).buildUpon();
            //必选参数
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            if (video_id == null) {
                throw new IllegalStateException("Please set video_id before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            urlBuilder.appendQueryParameter("video_id", video_id);
            //可选参数
            if (ext != null) {
                urlBuilder.appendQueryParameter("ext", ext);
            }
            return urlBuilder.build().toString();
        }

    }

}
