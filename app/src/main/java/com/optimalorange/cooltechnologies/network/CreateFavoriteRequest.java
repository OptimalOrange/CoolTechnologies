package com.optimalorange.cooltechnologies.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CreateFavoriteRequest extends SimpleRequest<JSONObject> {

    private static final int YOUKU_API_REQUEST_METHOD = Method.POST;

    private static final String YOUKU_API_CREATE_FAVORITE
            = "https://openapi.youku.com/v2/videos/favorite/create.json";

    private final Map<String, String> mParams;

    public CreateFavoriteRequest(Map<String, String> params,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(YOUKU_API_REQUEST_METHOD, YOUKU_API_CREATE_FAVORITE, listener, errorListener);
        mParams = params;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    public static class Builder {
        /**
         * Listener to receive the response
         */
        private Response.Listener<JSONObject> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        @Nullable
        private Response.ErrorListener mErrorListener;

        /** 应用Key（必设） */
        private String client_id;

        /** OAuth2授权（必设） */
        private String access_token;

        /** 视频ID（必设） */
        private String video_id;

        /**
         * 应用Key（必设参数）
         */
        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
            return this;
        }

        /**
         * OAuth2授权（必设参数）
         */
        public Builder setAccess_token(String access_token) {
            this.access_token = access_token;
            return this;
        }

        /**
         * 视频ID（必设参数）
         */
        public Builder setVideo_id(String video_id) {
            this.video_id = video_id;
            return this;
        }

        /**
         * Listener to receive the response
         */
        public Builder setResponseListener(Response.Listener<JSONObject> responseListener) {
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

        public CreateFavoriteRequest build() {
            return new CreateFavoriteRequest(buildParams(), mResponseListener, mErrorListener);
        }

        private Map<String, String> buildParams() {
            Map<String, String> params = new HashMap<>();
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            params.put("client_id", client_id);
            if (access_token == null) {
                throw new IllegalStateException("Please set access_token before build");
            }
            params.put("access_token", access_token);
            if (video_id == null) {
                throw new IllegalStateException("Please set video_id before build");
            }
            params.put("video_id", video_id);
            return params;
        }
    }

}
