package com.optimalorange.cooltechnologies.network;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * 创建收藏
 */
public class CreateFavorite {

    public static class Builder {

        private static final String YOUKU_API_CREATE_FAVORITE
                = "https://openapi.youku.com/v2/videos/favorite/create.json";

        /** 创建收藏请求结果的监听 */
        private OnCreateFavoriteListener mListener;

        /** 应用Key（必设） */
        private String client_id;

        /** OAuth2授权 */
        private String access_token;

        /** 视频ID */
        private String video_id;

        /**
         * 应用Key（必设参数）
         */
        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
            return this;
        }

        /**
         * OAuth2授权
         */
        public Builder setAccess_token(String access_token) {
            this.access_token = access_token;
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
         * 创建收藏请求结果的监听
         */
        public Builder setOnCreateFavoriteListener(OnCreateFavoriteListener listener) {
            this.mListener = listener;
            return this;
        }

        /**
         * 建立post请求
         */
        private HttpPost buildRequest() {
            final HttpPost request = new HttpPost(YOUKU_API_CREATE_FAVORITE);
            ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
            BasicNameValuePair param;
            if (client_id == null) {
                throw new IllegalStateException("Please set client_id before build");
            }
            param = new BasicNameValuePair("client_id", client_id);
            paramList.add(param);
            if (access_token == null) {
                throw new IllegalStateException("Please set access_token before build");
            }
            param = new BasicNameValuePair("access_token", access_token);
            paramList.add(param);
            if (video_id == null) {
                throw new IllegalStateException("Please set video_id before build");
            }
            param = new BasicNameValuePair("video_id", video_id);
            paramList.add(param);
            try {
                request.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return request;
        }

        /**
         * 执行请求
         */
        public void build() {
            new AsyncTask<Void, Void, HttpResponse>() {
                @Override
                protected HttpResponse doInBackground(Void... voids) {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response = null;
                    try {
                        response = client.execute(buildRequest());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(HttpResponse response) {
                    super.onPostExecute(response);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        mListener.onCreateFavorite(true);
                    } else {
                        mListener.onCreateFavorite(false);
                    }
                }
            }.execute();
        }

    }

    /** 创建收藏监听器 */
    public interface OnCreateFavoriteListener {

        void onCreateFavorite(boolean isCreated);
    }
}
