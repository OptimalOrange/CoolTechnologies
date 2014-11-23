package com.optimalorange.cooltechnologies.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;

import java.util.LinkedList;

public class Video {

    /** 视频唯一ID */
    @NonNull
    private final String id;

    /** 视频标题 */
    @NonNull
    private final String title;

    /** 视频播放链接 */
    @NonNull
    private final String link;

    /** 视频截图 */
    @NonNull
    private final String thumbnail;

    /** 视频时长，单位：秒 */
    private final int duration;

    /** 视频分类 */
    @NonNull
    private final String category;

    /** 视频状态 */
    @NonNull
    private final State state;

    /** 总播放数 */
    private final int view_count;

    /** 总收藏数 */
    private final int favorite_count;

    /** 总评论数 */
    private final int comment_count;

    /** 总顶数 */
    private final int up_count;

    /** 总踩数 */
    private final int down_count;

    /** 发布时间 */
    @NonNull
    private final String published;

    /** 上传用户 */
    @NonNull
    private final User user;

    /** 操作限制。非空，但数组长度可能为0。 */
    @NonNull
    private final OperationLimit[] operation_limit;

    /** 视频格式。非空，但数组长度可能为0。 */
    @NonNull
    private final StreamType[] streamtypes;

    /** 收藏时间 */
    @NonNull
    private final String favorite_time;

    public Video(JSONObject video) throws JSONException {
        duration = video.getInt("duration");
        view_count = video.getInt("view_count");
        favorite_count = video.getInt("favorite_count");
        comment_count = video.getInt("comment_count");
        up_count = video.getInt("up_count");
        down_count = video.getInt("down_count");

        id = video.getString("id");
        title = video.getString("title");
        link = video.getString("link");
        thumbnail = video.getString("thumbnail");
        category = video.getString("category");
        published = video.getString("published");
        favorite_time = video.getString("favorite_time");

        state = State.fromStringIgnoreCase(video.getString("state"));

        user = new User(video.getJSONObject("user"));

        JSONArray jsonArray = video.optJSONArray("operation_limit");
        if (jsonArray == null) {
            operation_limit = new OperationLimit[0];
        } else {
            LinkedList<OperationLimit> limits = new LinkedList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                limits.add(OperationLimit.fromStringIgnoreCase(jsonArray.getString(i)));
            }
            operation_limit = limits.toArray(new OperationLimit[limits.size()]);
        }
        jsonArray = video.optJSONArray("streamtypes");
        if (jsonArray == null) {
            streamtypes = new StreamType[0];
        } else {
            LinkedList<StreamType> streamTypeses = new LinkedList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                streamTypeses.add(StreamType.fromStringIgnoreCase(jsonArray.getString(i)));
            }
            streamtypes = streamTypeses.toArray(new StreamType[streamTypeses.size()]);
        }

    }

    /**
     * @return 视频唯一ID
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * @return 视频标题
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * @return 视频播放链接
     */
    @NonNull
    public String getLink() {
        return link;
    }

    /**
     * @return 视频截图
     */
    @NonNull
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * @return 视频时长，单位：秒
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return 视频分类
     */
    @NonNull
    public String getCategory() {
        return category;
    }

    /**
     * @return 视频状态
     */
    @NonNull
    public State getState() {
        return state;
    }

    /**
     * @return 总播放数
     */
    public int getView_count() {
        return view_count;
    }

    /**
     * @return 总收藏数
     */
    public int getFavorite_count() {
        return favorite_count;
    }

    /**
     * @return 总评论数
     */
    public int getComment_count() {
        return comment_count;
    }

    /**
     * @return 总顶数
     */
    public int getUp_count() {
        return up_count;
    }

    /**
     * @return 总踩数
     */
    public int getDown_count() {
        return down_count;
    }

    /**
     * @return 发布时间
     */
    @NonNull
    public String getPublished() {
        return published;
    }

    /**
     * @return 上传用户
     */
    @NonNull
    public User getUser() {
        return user;
    }

    /**
     * 深拷贝
     *
     * @return 操作限制。非空，但数组长度可能为0。
     */
    @NonNull
    public OperationLimit[] getOperation_limit() {
        OperationLimit[] result = new OperationLimit[operation_limit.length];
        System.arraycopy(operation_limit, 0, result, 0, operation_limit.length);
        return result;
    }

    /**
     * 深拷贝
     *
     * @return 操作限制。非空，但数组长度可能为0
     */
    @NonNull
    public StreamType[] getStreamtypes() {
        StreamType[] result = new StreamType[streamtypes.length];
        System.arraycopy(streamtypes, 0, result, 0, streamtypes.length);
        return result;
    }

    /**
     * @return 收藏时间
     */
    @NonNull
    public String getFavorite_time() {
        return favorite_time;
    }

    /**
     * 视频状态
     */
    public static enum State {
        /**
         * 正常
         */
        NORMAL,
        /** 转码中 */
        ENCODING,
        /** 转码失败 */
        FAIL,
        /** 审核中 */
        IN_REVIEW,
        /** 已屏蔽 */
        BLOCKED;

        @NonNull
        public static State fromStringIgnoreCase(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * 操作限制
     */
    public static enum OperationLimit {
        /**
         * 禁评论
         */
        COMMENT_DISABLED,
        /**
         * 禁下载
         */
        DOWNLOAD_DISABLED;

        @NonNull
        public static OperationLimit fromStringIgnoreCase(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * 视频格式
     */
    public static enum StreamType {
        FLVHD,
        FLV,
        _3GPHD,
        _3GP,
        HD,
        HD2;

        @NonNull
        public static StreamType fromStringIgnoreCase(String value) {
            value = value.toUpperCase();
            switch (value) {
                case "3GP":
                case "3GPHD":
                    value = "_" + value;
                    break;
            }
            return valueOf(value);
        }
    }

}
