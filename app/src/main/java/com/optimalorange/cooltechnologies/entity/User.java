package com.optimalorange.cooltechnologies.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;

public class User {

    /**
     * 用户ID
     */
    @NonNull
    private final String id;

    /**
     * 用户名
     */
    @NonNull
    private final String name;

    /**
     * 用户个人空间地址
     */
    @NonNull
    private final String link;

    public User(JSONObject user) throws JSONException {
        id = user.getString("id");
        name = user.getString("name");
        link = user.getString("link");
    }

    /**
     * @return 用户ID
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * @return 用户名
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * @return 用户个人空间地址
     */
    @NonNull
    public String getLink() {
        return link;
    }
}
