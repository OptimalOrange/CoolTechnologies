package com.optimalorange.cooltechnologies.ui.view;

import com.android.volley.toolbox.NetworkImageView;
import com.optimalorange.cooltechnologies.R;

import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

public class VideoCardViewBuilder {

    /**
     * 创建的VideoCardView中图片的默认长宽比：16:9
     */
    public static final double DEFAULT_ASPECT_RATIO = 16.0 / 9.0;

    private LayoutInflater mInflater;

    private ViewGroup mParent;

    private boolean mAttachToParent = false;

    private Integer mWidth;

    private Integer mImageHeight;


    public VideoCardViewBuilder setInflater(LayoutInflater inflater) {
        mInflater = inflater;
        return this;
    }

    /**
     * 创建的VideoCard所在的{@link ViewGroup}
     *
     * @param parent 父View
     */
    public VideoCardViewBuilder setParent(ViewGroup parent) {
        mParent = parent;
        return this;
    }

    /**
     * {@link #build()}时，是否自动把新创建的VideoCard附到{@link #setParent(ViewGroup) parent}中
     *
     * @param attachToParent ture：自动把Card添加到parent中；false：只创建不添加
     */
    public VideoCardViewBuilder setAttachToParent(boolean attachToParent) {
        mAttachToParent = attachToParent;
        return this;
    }

    /**
     * Card（同时也是图片）宽度。
     *
     * @param width 宽度值，单位px
     */
    public VideoCardViewBuilder setWidth(Integer width) {
        mWidth = width;
        return this;
    }

    /**
     * Card中图片的高度。如果不设置会根据{@link #DEFAULT_ASPECT_RATIO 默认长宽比}设置高度。
     *
     * @param imageHeight 高度值，单位px
     */
    public VideoCardViewBuilder setImageHeight(Integer imageHeight) {
        mImageHeight = imageHeight;
        return this;
    }


    public VideoCardViewHolder build() {
        assertState("build()");
        int imageHeight;
        if (mImageHeight != null) {
            imageHeight = mImageHeight;
        } else {
            imageHeight = (int) (mWidth / DEFAULT_ASPECT_RATIO);
        }
        CardView newCardView = (CardView) mInflater.inflate(
                R.layout.view_video_card, mParent, false);
        newCardView.getLayoutParams().width = mWidth;
        VideoCardViewHolder cardViewHolder = new VideoCardViewHolder(newCardView);
        cardViewHolder.mImageView.getLayoutParams().height = imageHeight;
        if (mAttachToParent) {
            mParent.addView(newCardView);
        }
        return cardViewHolder;
    }

    private void assertState(String where) {
        LinkedList<String> nulls = new LinkedList<>();
        if (mInflater == null) {
            nulls.add("inflater");
        }
        if (mParent == null) {
            nulls.add("parent");
        }
        if (mWidth == null) {
            nulls.add("width");
        }
        if (nulls.size() > 0) {
            throw new IllegalStateException("Please set " + nulls + " before " + where);
        }
    }


    public static class VideoCardViewHolder {

        public CardView mRootCardView;

        public NetworkImageView mImageView;

        public TextView mViewCountView;

        public TextView mdurationView;

        public TextView mTextView;

        public VideoCardViewHolder(CardView rootCardView) {
            mRootCardView = rootCardView;
            mImageView =
                    (NetworkImageView) rootCardView.findViewById(R.id.card_thumbnail_image);
            mViewCountView = (TextView) rootCardView.findViewById(R.id.view_count);
            mdurationView = (TextView) rootCardView.findViewById(R.id.duration);
            mTextView = (TextView) rootCardView.findViewById(R.id.card_simple_title);
        }
    }
}
