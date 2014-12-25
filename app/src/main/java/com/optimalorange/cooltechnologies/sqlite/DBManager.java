package com.optimalorange.cooltechnologies.sqlite;

import com.optimalorange.cooltechnologies.entity.FavoriteBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/12/24.
 */
public class DBManager {
    // database版本
    private final static int DB_VERSION = 1;
    // database名
    private final static String DB_NAME = "cool_technologies.db";

    private Context context;

    private static DBManager dbManage;

    private SQLiteDatabase db = null;

    private DataBaseHelper dbHelper = null;

    private DBManager(Context context) {
        this.context = context;
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        Context context;
        DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table history(_id integer primary key, _videoId text, _title text, _duration text, _imageUrl text, _link text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public static synchronized DBManager getInstance(Context context) {
        if (dbManage == null) {
            dbManage = new DBManager(context);
        }
        return dbManage;
    }

    public void open() throws SQLException {
        if (isOpen()) {
            return;
        }
        dbHelper = new DataBaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void quit() {
        try {
            if (isOpen() && dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOpen() {
        return db != null && db.isOpen();
    }

    public void saveHistory(FavoriteBean bean) {
        open();
        if (isInHistory(bean.videoId)) {
            deleteHistory(bean.videoId);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_videoId", bean.videoId);
        contentValues.put("_title", bean.title);
        contentValues.put("_duration", bean.duration);
        contentValues.put("_imageUrl", bean.imageUrl);
        contentValues.put("_link", bean.link);
        db.insert("history", null, contentValues);
    }

    public ArrayList<FavoriteBean> getAllHistory() {
        open();
        ArrayList<FavoriteBean> favoriteBeans = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from history order by _id desc", null);
        while (cursor.moveToNext()) {
            FavoriteBean favoriteBean = new FavoriteBean();
            favoriteBean.videoId = cursor.getString(1);
            favoriteBean.title = cursor.getString(2);
            favoriteBean.duration = cursor.getString(3);
            favoriteBean.imageUrl = cursor.getString(4);
            favoriteBean.link = cursor.getString(5);
            favoriteBeans.add(favoriteBean);
        }
        return favoriteBeans;
    }

    public boolean isInHistory(String videoId) {
        open();
        int count = 0;
        Cursor cursor = db.rawQuery("select count(_videoId) from history where _videoId = ?", new String[]{videoId});
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count != 0;
    }

    public void deleteHistory(String videoId) {
        open();
        db.execSQL("delete from history where _videoId = ?", new String[]{videoId});
    }

}
