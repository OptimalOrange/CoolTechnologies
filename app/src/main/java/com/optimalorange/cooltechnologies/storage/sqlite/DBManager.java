package com.optimalorange.cooltechnologies.storage.sqlite;

import com.optimalorange.cooltechnologies.ui.entity.Video;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

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
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        context = context.getApplicationContext();

        this.context = context;
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
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

    public void saveHistory(Video bean) {
        open();
        final ContentValues contentValues = convertToContentValues(bean);
        db.beginTransaction();
        try {
            if (isInHistory(bean.id)) {
                deleteHistory(bean.id);
            }
            db.insert("history", null, contentValues);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Video> getAllHistory() {
        open();
        List<Video> result = new LinkedList<>();
        Cursor cursor = db.rawQuery("select * from history order by _id desc", null);
        while (cursor.moveToNext()) {
            result.add(convertToVideo(cursor));
        }
        cursor.close();
        return result;
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

    public int deleteHistory(String videoId) {
        open();
        return db.delete("history", "_videoId = ?", new String[]{videoId});
    }

    private static ContentValues convertToContentValues(Video video) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put("_videoId", video.id);
        contentValues.put("_title", video.title);
        contentValues.put("_duration", video.duration);
        contentValues.put("_imageUrl", video.thumbnail);
        contentValues.put("_link", video.link);
        return contentValues;
    }

    /**
     * convert current row of {@link Cursor} to {@link Video}
     */
    private static Video convertToVideo(Cursor cursor) {
        final Video result = new Video();
        result.id = cursor.getString(1);
        result.title = cursor.getString(2);
        result.duration = cursor.getString(3);
        result.thumbnail = cursor.getString(4);
        result.link = cursor.getString(5);
        return result;
    }

}
