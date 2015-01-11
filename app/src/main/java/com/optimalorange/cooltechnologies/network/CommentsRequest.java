package com.optimalorange.cooltechnologies.network;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.optimalorange.cooltechnologies.entity.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhou Peican on 2014/12/29.
 */
public class CommentsRequest extends JsonRequest<List<Comment>> {

    private static final String YOUKU_API_JSON_COMMENTS_ATTRIBUTE_NAME = "comments";

    public CommentsRequest(int method, String url, JSONObject jsonRequest,
            Response.Listener<List<Comment>> listener,
            Response.ErrorListener errorListener) {

        super(method, url, jsonRequest == null ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    @Override
    protected Response<List<Comment>> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray commentsJson = jsonObject
                    .getJSONArray(YOUKU_API_JSON_COMMENTS_ATTRIBUTE_NAME);
            int length = commentsJson.length();
            List<Comment> comments = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                comments.add(new Comment(commentsJson.getJSONObject(i)));
            }
            return Response.success(comments,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    public static class Builder {

        private static final int YOUKU_API_REQUEST_METHOD = Request.Method.GET;

        private static final String YOUKU_API_COMMENTS_BY_VIDEO
                = "https://openapi.youku.com/v2/comments/by_video.json";

        /**
         * A {@link JSONObject} to post with the request. Null is allowed and
         * indicates no parameters will be posted along with request.
         */
        @Nullable
        private JSONObject mJsonRequest;

        /**
         * Listener to receive the comments response
         */
        private Response.Listener<List<Comment>> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        @Nullable
        private Response.ErrorListener mErrorListener;

        /** 应用Key（必设） */
        private String client_id;

        /** 视频ID */
        @Nullable
        private String video_id;

        /**
         * 页数<br />
         * 示例：1
         */
        @Nullable
        private Integer page;

        /**
         * 页大小<br />
         * 示例：20
         */
        @Nullable
        private Integer count;

        /**
         * 应用Key（必设参数）
         */
        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
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
         * 页数<br />
         * 示例：1
         */
        public Builder setPage(@Nullable Integer page) {
            this.page = page;
            return this;
        }

        /**
         * 页大小<br />
         * 示例：20
         */
        public Builder setCount(@Nullable Integer count) {
            this.count = count;
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
         * Listener to receive the comments response
         */
        public Builder setResponseListener(Response.Listener<List<Comment>> responseListener) {
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

        public CommentsRequest build() {
            return new CommentsRequest(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mJsonRequest,
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_COMMENTS_BY_VIDEO)
                    .buildUpon();
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            if (video_id == null) {
                throw new IllegalStateException("Please set video_id before build");
            }
            urlBuilder.appendQueryParameter("video_id", video_id);
            if (page != null) {
                urlBuilder.appendQueryParameter("page", page.toString());
            }
            if (count != null) {
                urlBuilder.appendQueryParameter("count", count.toString());
            }
            return urlBuilder.build().toString();
        }
    }

}
