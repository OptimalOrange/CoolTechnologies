package com.optimalorange.cooltechnologies.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by WANGZHENGZE on 2014/11/29.
 */
public class FavoriteBean implements Serializable {
    public String title = "";
    public String imageUrl = "";
    public String duration = "";
    public String link = "";
    public String videoId = "";

    public FavoriteBean(){}

    public FavoriteBean(Video video){
        title = video.getTitle();
        imageUrl = video.getThumbnail();
        duration = String.valueOf(video.getDuration());
        link = video.getLink();
        videoId = video.getId();
    }

    public FavoriteBean(JSONObject jsonObject) throws JSONException {
        title = jsonObject.getString("title");
        link = jsonObject.getString("link");
        imageUrl = jsonObject.getString("thumbnail");
        duration = jsonObject.getString("duration");
        videoId = jsonObject.getString("id");
    }

}
