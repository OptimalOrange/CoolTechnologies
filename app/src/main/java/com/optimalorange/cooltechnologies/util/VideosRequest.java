package com.optimalorange.cooltechnologies.util;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.optimalorange.cooltechnologies.entity.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @see {@literal http://open.youku.com/docs/api_videos.html#videos-by-category}
 */
public class VideosRequest extends JsonRequest<List<Video>> {

    private static final String YOUKU_API_JSON_VIDEOS_ATTRIBUTE_NAME = "videos";

    public VideosRequest(int method, String url, JSONObject jsonRequest,
            Response.Listener<List<Video>> listener,
            Response.ErrorListener errorListener) {

        super(method, url, jsonRequest == null ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    @Override
    protected Response<List<Video>> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray videosJson = jsonObject.getJSONArray(YOUKU_API_JSON_VIDEOS_ATTRIBUTE_NAME);
            int length = videosJson.length();
            List<Video> videos = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                videos.add(new Video(videosJson.getJSONObject(i)));
            }
            return Response.success(videos,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    public static class Builder {

        public static final String DEFAULT_CATEGORY_LABEL = "科技";

        private static final int YOUKU_API_REQUEST_METHOD = Request.Method.GET;

        private static final String YOUKU_API_VIDEOS_BY_CATEGORY
                = "https://openapi.youku.com/v2/videos/by_category.json";

        /**
         * A {@link JSONObject} to post with the request. Null is allowed and
         * indicates no parameters will be posted along with request.
         */
        @Nullable
        private JSONObject mJsonRequest;

        /**
         * Listener to receive the videos response
         */
        private Response.Listener<List<Video>> mResponseListener;

        /**
         * Error listener, or null to ignore errors.
         */
        @Nullable
        private Response.ErrorListener mErrorListener;

        /** 应用Key（必设） */
        private String client_id;

        /**
         * 分类<br />
         * 示例：资讯
         * 默认值：{@link #DEFAULT_CATEGORY_LABEL}
         */
        @Nullable
        private String category = DEFAULT_CATEGORY_LABEL;

        /**
         * 类型<br />
         * 示例：社会资讯
         */
        @Nullable
        private String genre;

        /** 时间范围 */
        @Nullable
        private PERIOD period;

        /** 排序 */
        @Nullable
        private ORDER_BY orderby;

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
         * 分类<br />
         * 示例：资讯
         * 默认值：{@link #DEFAULT_CATEGORY_LABEL}
         */
        public Builder setCategory(@Nullable String category) {
            this.category = category;
            return this;
        }

        /**
         * 类型<br />
         * 示例：社会资讯
         */
        public Builder setGenre(@Nullable String genre) {
            this.genre = genre;
            return this;
        }

        /** 时间范围 */
        public Builder setPeriod(@Nullable PERIOD period) {
            this.period = period;
            return this;
        }

        /** 排序 */
        public Builder setOrderby(@Nullable ORDER_BY orderby) {
            this.orderby = orderby;
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
         * Listener to receive the videos response
         */
        public Builder setResponseListener(Response.Listener<List<Video>> responseListener) {
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

        public VideosRequest build() {
            return new VideosRequest(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mJsonRequest,
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_VIDEOS_BY_CATEGORY).buildUpon();
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            if (category != null) {
                urlBuilder.appendQueryParameter("category", category);
            }
            if (genre != null) {
                urlBuilder.appendQueryParameter("genre", genre);
            }
            if (period != null) {
                urlBuilder.appendQueryParameter("period", period.toString());
            }
            if (orderby != null) {
                urlBuilder.appendQueryParameter("orderby", orderby.toString());
            }
            if (page != null) {
                urlBuilder.appendQueryParameter("page", page.toString());
            }
            if (count != null) {
                urlBuilder.appendQueryParameter("count", count.toString());
            }
            return urlBuilder.build().toString();
        }

        /** 时间范围 */
        public static enum PERIOD {
            /** 今日 */
            TODAY,
            /** 本周 */
            WEEK,
            /** 本月 */
            MONTH,
            /** 历史 */
            HISTORY;

            public String toString() {
                return name().toLowerCase();
            }
        }

        /** 排序 */
        public static enum ORDER_BY {
            /** 发布时间 */
            PUBLISHED,
            /** 总播放数 */
            VIEW_COUNT,
            /** 总评论数 */
            COMMENT_COUNT,
            /** 总引用 */
            REFERENCE_COUNT,
            /** 收藏时间 */
            FAVORITE_TIME,
            /** 总收藏数 */
            FAVORITE_COUNT;

            public String toString() {
                return name().toLowerCase().replace('-', '_');
            }
        }
    }
}
