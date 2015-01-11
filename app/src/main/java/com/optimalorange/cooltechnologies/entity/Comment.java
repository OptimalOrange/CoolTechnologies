package com.optimalorange.cooltechnologies.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;

/**
 * Created by Zhou Peican on 2014/12/28.
 */
public class Comment {

    public static final String YOUKU_API_COMMENTS = "https://openapi.youku.com/v2/comments/by_video.json";

    /** 评论ID */
    @NonNull
    private final String id;

    /** 评论内容 */
    @NonNull
    private final String content;

    /** 发布时间 */
    @NonNull
    private final String published;

    /** 发布用户 */
    @NonNull
    private final User user;

    public Comment(JSONObject comment) throws JSONException {
        id = comment.getString("id");
        content = comment.getString("content");
        published = comment.getString("published");

        user = new User(comment.getJSONObject("user"));
    }

    /**
     * @return 评论ID
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * @return 评论内容
     */
    @NonNull
    public String getContent() {
        return content;
    }

    /**
     * @return 发布时间
     */
    @NonNull
    public String getPublished() {
        return published;
    }

    /**
     * @return 发布用户
     */
    @NonNull
    public User getUser() {
        return user;
    }

}
