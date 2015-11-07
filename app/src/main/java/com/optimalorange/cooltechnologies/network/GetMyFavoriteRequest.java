package com.optimalorange.cooltechnologies.network;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import android.net.Uri;

/**
 * see http://open.youku.com/docs?id=53
 */
public class GetMyFavoriteRequest extends JsonObjectRequest {


    public GetMyFavoriteRequest(
            String url,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public GetMyFavoriteRequest(
            int method,
            String url,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public GetMyFavoriteRequest(
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public GetMyFavoriteRequest(
            int method,
            String url,
            String requestBody,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    public GetMyFavoriteRequest(
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }


    public static class Builder {

        private static final int YOUKU_API_REQUEST_METHOD = Request.Method.GET;

        private static final String YOUKU_API_FAVORITE_ME =
                "https://openapi.youku.com/v2/videos/favorite/by_me.json";

        /**
         * Listener to receive the comments response
         */
        private Response.Listener<JSONObject> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        private Response.ErrorListener mErrorListener;

        private String client_id;

        private String access_token;

        private String orderby;

        private Integer page;

        private Integer count;

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
         * OAuth2授权<br/>
         * 类型：String<br/>
         * 必选
         */
        public Builder setAccess_token(String access_token) {
            this.access_token = access_token;
            return this;
        }

        /**
         * 排序 favorite-time: 收藏时间<br/>
         * 类型：String<br/>
         * 可选<br/>
         * 默认值：favorite-time<br/>
         * 示例：favorite-time
         */
        public Builder setOrderby(String orderby) {
            this.orderby = orderby;
            return this;
        }

        /**
         * 页数<br/>
         * 类型：int<br/>
         * 可选<br/>
         * 默认值：1<br/>
         * 示例：1
         */
        public Builder setPage(Integer page) {
            this.page = page;
            return this;
        }

        /**
         * 页大小<br/>
         * 类型：int<br/>
         * 可选<br/>
         * 默认值：20<br/>
         * 示例：20
         */
        public Builder setCount(Integer count) {
            this.count = count;
            return this;
        }

        public GetMyFavoriteRequest build() {
            return new GetMyFavoriteRequest(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_FAVORITE_ME).buildUpon();
            //必选参数
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            if (access_token == null) {
                throw new IllegalStateException("Please set access_token before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            urlBuilder.appendQueryParameter("access_token", access_token);
            //可选参数
            if (orderby != null) {
                urlBuilder.appendQueryParameter("orderby", orderby);
            }
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
