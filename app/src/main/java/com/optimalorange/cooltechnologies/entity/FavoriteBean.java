package com.optimalorange.cooltechnologies.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by WANGZHENGZE on 2014/11/29.
 */
public class FavoriteBean {
    public String title = "";
    public String imageUrl = "";
    public String duration = "";
    public String link = "";
    public String videoId = "";

    public FavoriteBean(JSONObject jsonObject) throws JSONException {
        title = jsonObject.getString("title");
        link = jsonObject.getString("link");
        imageUrl = jsonObject.getString("thumbnail");
        duration = jsonObject.getString("duration");
        videoId = jsonObject.getString("id");
    }

}
