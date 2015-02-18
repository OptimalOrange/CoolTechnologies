package com.optimalorange.cooltechnologies.adapter;

/**
 * Created by WANGZHENGZE on 2014/12/24.
 */

import com.android.volley.toolbox.ImageLoader;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.util.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FavoriteAdapter extends BaseAdapter {

private class ViewHolder {
    public TextView tvTitle;
    public TextView tvDuration;
    public ImageView ivImage;
}

private Context mContext;
private ArrayList<FavoriteBean> mFavoriteBeans;
private ImageLoader mImageLoader;

    public FavoriteAdapter(Context context, ArrayList<FavoriteBean> favoriteBeans, ImageLoader imageLoader) {
        mContext = context;
        mFavoriteBeans = favoriteBeans;
        mImageLoader = imageLoader;
    }

    @Override
    public int getCount() {
        return mFavoriteBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mFavoriteBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_favorite, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.thumbnail);
            viewHolder.tvDuration = (TextView) convertView.findViewById(R.id.duration);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        mImageLoader.get(mFavoriteBeans.get(position).imageUrl, ImageLoader.getImageListener(viewHolder.ivImage, R.drawable.ic_image_view_placeholder, R.drawable.ic_image_view_placeholder));
        viewHolder.tvTitle.setText(mFavoriteBeans.get(position).title);
        viewHolder.tvDuration.setText(Utils.getDurationString((int) Float.parseFloat(mFavoriteBeans.get(position).duration)));
        return convertView;
    }
}
