package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.StaggeredGridView;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.util.Utils;
import com.optimalorange.cooltechnologies.util.VideosRequest;
import com.optimalorange.cooltechnologies.util.VolleySingleton;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 热门
 */
public class ListVideosFragment extends Fragment {

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    private StaggeredGridView mGridView;

    private ItemsAdapter mItemsAdapter;

    private LinkedList<Video> mListVideos = new LinkedList<Video>();

    /**
     * 获取Video（见entity包中Video）
     *
     * @return VideoRequest
     */
    private VideosRequest buildQueryVideosRequest() {

        return new VideosRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setPeriod(VideosRequest.Builder.PERIOD.WEEK)
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        for (Video mVideo : videos) {
                            mListVideos.add(mVideo);
                            mItemsAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                })
                .build();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mVolleySingleton.addToRequestQueue(buildQueryVideosRequest());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list_videos, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridView = (StaggeredGridView) view.findViewById(R.id.grid_view);
        mItemsAdapter = new ItemsAdapter(mListVideos, mVolleySingleton.getImageLoader());
        mGridView.setAdapter(mItemsAdapter);
    }

    /**
     * 热门视频的图片墙的适配器
     *
     * @author Zhou Peican
     */
    public class ItemsAdapter extends BaseAdapter {

        private LinkedList<Video> mVideos;

        private ImageLoader mImageLoader;

        public ItemsAdapter(LinkedList<Video> mVideos, ImageLoader mImageLoader) {
            super();
            this.mVideos = mVideos;
            this.mImageLoader = mImageLoader;
        }

        @Override
        public int getCount() {
            return mVideos.size();
        }

        @Override
        public Object getItem(int position) {
            return mVideos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_videos, parent, false);
                vh = new ViewHolder();
                vh.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                vh.duration = (TextView) convertView.findViewById(R.id.duration);
                vh.title = (TextView) convertView.findViewById(R.id.title);
                vh.viewCount = (TextView) convertView.findViewById(R.id.view_count);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            //加载图片
            mImageLoader.get(mVideos.get(position).getThumbnail_v2(),
                    ImageLoader.getImageListener(vh.thumbnail,
                            R.drawable.ic_launcher, R.drawable.ic_launcher));
            //显示播放时长
            vh.duration.setText(Utils.getDurationString(mVideos.get(position).getDuration()));
            //显示视频标题
            vh.title.setText(mVideos.get(position).getTitle());
            //显示播放次数（这里使用字符串资源格式化）
            vh.viewCount.setText(Utils.formatViewCount(mVideos.get(position).getView_count(),
                    parent.getContext()));

            return convertView;
        }

        private class ViewHolder {

            ImageView thumbnail;

            TextView duration;

            TextView title;

            TextView viewCount;
        }

    }

}
