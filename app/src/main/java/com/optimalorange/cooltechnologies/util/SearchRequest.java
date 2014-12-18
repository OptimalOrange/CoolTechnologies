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
import java.util.Locale;

/**
 * Created by Zhou Peican on 2014/12/9.
 */
public class SearchRequest extends JsonRequest<List<Video>> {

    private static final String YOUKU_API_JSON_VIDEOS_ATTRIBUTE_NAME = "videos";

    public SearchRequest(int method, String url, JSONObject jsonRequest,
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

        private static final int DEFAULT_TIMELESS = 0;

        private static final int DEFAULT_TIMEMORE = 0;

        private static final int YOUKU_API_REQUEST_METHOD = Request.Method.GET;

        private static final String YOUKU_API_SEARCHES_VIDEOS_BY_KEYWORD
                = "https://openapi.youku.com/v2/searches/video/by_keyword.json";

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

        /** 关键字 多个关键字用空格分隔(必设） */
        private String keyword;

        /**
         * 分类<br />
         * 示例：资讯
         * 默认值：{@link #DEFAULT_CATEGORY_LABEL}
         */
        @Nullable
        private String category = DEFAULT_CATEGORY_LABEL;

        /** 时间范围 */
        @Nullable
        private PERIOD period;

        /** 排序 */
        @Nullable
        private ORDER_BY orderby;

        /** 公开类型 all */
        @Nullable
        private PUBLIC_TYPE public_type;

        /** 付费状态 付费 1 免费 0 */
        @Nullable
        private PAID paid;

        /**
         * 视频时长筛选：定义时长小 于几分钟（不包含），默认 0表示不筛选
         */
        @Nullable
        private int timeless = DEFAULT_TIMELESS;

        /**
         * 视频时长筛选：定义时长大 于等于几分钟（不包含）
         */
        @Nullable
        private int timemore = DEFAULT_TIMEMORE;

        /**
         * 格式筛选(逗号分隔，表示‘或’关系)
         * | 1:mp4（高清） | 2:3gp | 1,2,3 3:H263FLV |
         * 4:3GPHD | 5:H264FLV（FLV升级版h264） |
         * 6:Hd2 720P（超清) |
         */
        @Nullable
        private STREAMTYPES streamtypes;

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
        public Builder setKeyword(String keyword) {
            this.keyword = keyword;
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

        /** 公开类型 */
        public Builder setPublic_type(@Nullable PUBLIC_TYPE public_type) {
            this.public_type = public_type;
            return this;
        }

        /** 付费状态 */
        public Builder setPaid(@Nullable PAID paid) {
            this.paid = paid;
            return this;
        }

        /** 视频时长筛选：定义时长小 于几分钟（不包含） */
        public Builder setTimeless(@Nullable int timeLess) {
            this.timeless = timeless;
            return this;
        }

        /** 视频时长筛选：定义时长大 于等于几分钟（不包含） */
        public Builder setTimemore(@Nullable int timemore) {
            this.timemore = timemore;
            return this;
        }

        /** 格式筛选 */
        public Builder setStreamtypes(@Nullable STREAMTYPES streamtypes) {
            this.streamtypes = streamtypes;
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

        public SearchRequest build() {
            return new SearchRequest(
                    YOUKU_API_REQUEST_METHOD,
                    buildUrl(),
                    mJsonRequest,
                    mResponseListener,
                    mErrorListener
            );
        }

        private String buildUrl() {
            final Uri.Builder urlBuilder = Uri.parse(YOUKU_API_SEARCHES_VIDEOS_BY_KEYWORD)
                    .buildUpon();
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            urlBuilder.appendQueryParameter("client_id", client_id);
            if (keyword == null) {
                throw new IllegalStateException("Please input keyword before build");
            }
            urlBuilder.appendQueryParameter("keyword", keyword);
            if (category != null) {
                urlBuilder.appendQueryParameter("category", category);
            }
            if (period != null) {
                urlBuilder.appendQueryParameter("period", period.toString());
            }
            if (orderby != null) {
                urlBuilder.appendQueryParameter("orderby", orderby.toString());
            }
            if (public_type != null) {
                urlBuilder.appendQueryParameter("public_type", public_type.toString());
            }
            if (paid != null) {
                urlBuilder.appendQueryParameter("paid", PAID.fromPaidType(paid));
            }
            if (timeless != 0) {
                urlBuilder.appendQueryParameter("timeless", String.valueOf(timeless));
            }
            if (timemore != 0) {
                urlBuilder.appendQueryParameter("timemore", String.valueOf(timemore));
            }
            if (streamtypes != null) {
                urlBuilder.appendQueryParameter("streamtypes",
                        STREAMTYPES.fromStreamTypes(streamtypes));
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
                return name().toLowerCase(Locale.US);
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
                return name().toLowerCase(Locale.US).replace('-', '_');
            }
        }

        /** 公开类型 */
        public static enum PUBLIC_TYPE {
            /** 公开 */
            ALL,
            /** 仅好友观看 */
            FRIEND,
            /** 输入密码观看 */
            PASSWORD;

            @Override
            public String toString() {
                return name().toLowerCase(Locale.US);
            }
        }

        /** 付费状态 */
        public static enum PAID {
            /** 免费 0 */
            FREE,
            /** 付费 1 */
            PAY;

            public static String fromPaidType(PAID paid) {
                return String.valueOf(paid.ordinal());
            }
        }

        /** 格式筛选 */
        public static enum STREAMTYPES {
            /** 1:mp4（高清） */
            MP4,
            /** 2:3gp */
            _3GP,
            /** 3:H263FLV */
            H263FLV,
            /** 4:3GPHD */
            _3GPHD,
            /** 5:H264FLV */
            H264FLV,
            /** 6:Hd2 */
            Hd2;

            public static String fromStreamTypes(STREAMTYPES streamtypes) {
                return String.valueOf(streamtypes.ordinal() + 1);
            }
        }
    }

}
