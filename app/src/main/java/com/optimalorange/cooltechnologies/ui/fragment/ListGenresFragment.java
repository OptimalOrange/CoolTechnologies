package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.ui.ListVideosActivity;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.ui.view.VideoCardViewBuilder;
import com.optimalorange.cooltechnologies.util.ItemsCountCalculater;
import com.optimalorange.cooltechnologies.util.Utils;
import com.optimalorange.cooltechnologies.network.VideosRequest;
import com.optimalorange.cooltechnologies.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 分类
 */
//TODO 执行网络操作前，检查网络可用性
//TODO 刷新
//TODO reCreat问题（横竖屏转换、接完电话回来时）
//TODO 点击时的视觉表现（动画等）
public class ListGenresFragment extends Fragment {

    /**
     * @see {@literal http://open.youku.com/docs/api_searches.html#schemas-video-category}
     */
    private static final String YOUKU_API_SCHEMAS_VIDEO_CATEGORY
            = "https://openapi.youku.com/v2/schemas/video/category.json";

    private static final String CATEGORY_LABEL_OF_TECH = "科技";

    private String mYoukuClientId;

    private VolleySingleton mVolleySingleton;

    /**
     * 类型列表及每种类型对应的视频
     */
    private Pair<ArrayList<String>, ArrayList<List<Video>>> mGenres;


    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private ItemsCountCalculater.Result mItemsCountAndDimension;

    //--------------------------------------------------------------------------
    // Network Request to Youku
    //--------------------------------------------------------------------------

    private final JsonObjectRequest mVideoCategorySchemaRequest = new JsonObjectRequest(
            Request.Method.GET,
            YOUKU_API_SCHEMAS_VIDEO_CATEGORY,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    JSONObject category = null;
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("categories");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject currentObject = jsonArray.optJSONObject(i);
                            if (currentObject == null) {
                                continue;
                            }
                            if (CATEGORY_LABEL_OF_TECH.equals(currentObject.optString("label"))) {
                                category = currentObject;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (category != null) {
                        JSONArray genresJson = category.optJSONArray("genres");
                        final int length = genresJson != null ? genresJson.length() : 0;
                        ArrayList<String> genres = new ArrayList<>(length);
                        ArrayList<List<Video>> videos = new ArrayList<>(length);
                        for (int i = 0; i < length; i++) {
                            try {
                                String genre = genresJson.getJSONObject(i).getString("label");
                                genres.add(genre);
                                videos.add(null);
                                mVolleySingleton
                                        .addToRequestQueue(buildQueryVideosRequest(genre, i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mGenres = new Pair<>(genres, videos);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                }
            }
    );

    /**
     * @param genre 类型
     * @param index 在{@link #mGenres}中的索引
     * @see #mGenres
     */
    private VideosRequest buildQueryVideosRequest(final String genre, final int index) {
        if (mItemsCountAndDimension == null) {
            throw new IllegalStateException("calculateItemsCount before query");
        }
        return new VideosRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setGenre(genre)
                .setPeriod(VideosRequest.Builder.PERIOD.MONTH)
                .setCount(mItemsCountAndDimension.getCount())
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        mGenres.second.set(index, videos);
                        mAdapter.notifyItemChanged(index);
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

    //--------------------------------------------------------------------------
    // Override Methods of Fragment
    //--------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mYoukuClientId = getString(R.string.youku_client_id);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());
        mVolleySingleton.addToRequestQueue(mVideoCategorySchemaRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_categories, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        // use RecyclerView.setHasFixedSize(true) to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mItemsCountAndDimension = calculateItemsCount(mRecyclerView);
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    //--------------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------------

    private static ItemsCountCalculater.Result calculateItemsCount(View parent) {
        Resources resources = parent.getResources();
        float margin = resources.getDimension(R.dimen.card_margin_half);
        float cardWidthMin = resources.getDimension(R.dimen.card_width_min);
        float cardWidthMax = resources.getDimension(R.dimen.card_width_max);
        return ItemsCountCalculater
                .calculateItemsCountAndDimension(
                        cardWidthMin, cardWidthMax, margin * 2,
                        resources.getDisplayMetrics().widthPixels - margin * 2,
                        ItemsCountCalculater.Preference.SMALLER_OR_MORE_ITEMS);
    }

    //--------------------------------------------------------------------------
    // Adapter
    //--------------------------------------------------------------------------

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.list_item_categories, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            addCardViews(inflater, viewHolder);
            return viewHolder;
        }

        private void addCardViews(LayoutInflater inflater, ViewHolder viewHolder) {
            ArrayList<VideoCardViewBuilder.VideoCardViewHolder> cardViews =
                    new ArrayList<>(mItemsCountAndDimension.getCount());
            for (int i = mItemsCountAndDimension.getCount(); i >= 1; i--) {
                VideoCardViewBuilder.VideoCardViewHolder cardViewHolder =
                        new VideoCardViewBuilder()
                                .setInflater(inflater)
                                .setParent(viewHolder.mVideosContainer)
                                .setAttachToParent(true)
                                .setWidth(mItemsCountAndDimension.getDimension())
                                .build();

                cardViewHolder.mImageView.setDefaultImageResId(R.drawable.ic_launcher);
                cardViewHolder.mImageView.setErrorImageResId(R.drawable.ic_launcher);

                cardViews.add(cardViewHolder);
            }
            viewHolder.mCardViews = cardViews;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final String title = mGenres.first.get(position);
            holder.mTitleTextView.setText(title);
            holder.mTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ListVideosActivity.class);
                    intent.putExtra(ListVideosActivity.EXTRA_KEY_GENRE, title);
                    startActivity(intent);
                }
            });
            List<Video> videos = mGenres.second.get(position);
            int cardViewsIndex = 0;
            if (videos != null) {
                for (final Video video : videos) {
                    VideoCardViewBuilder.VideoCardViewHolder currentCardView =
                            holder.mCardViews.get(cardViewsIndex);
                    currentCardView.mImageView.setImageUrl(
                            video.getThumbnail_v2(), mVolleySingleton.getImageLoader());
                    currentCardView.mViewCountView.setText(Utils.formatViewCount(
                            video.getView_count(), currentCardView.mViewCountView.getContext()));
                    currentCardView.mdurationView.setText(
                            Utils.getDurationString(video.getDuration()));
                    currentCardView.mTextView.setText(video.getTitle());
                    currentCardView.mRootCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), PlayVideoActivity.class);
                            FavoriteBean favoriteBean = new FavoriteBean(video);
                            intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO_ID, favoriteBean);
                            startActivity(intent);
                        }
                    });
                    currentCardView.mRootCardView.setVisibility(CardView.VISIBLE);
                    cardViewsIndex++;
                }
            }
            for (; cardViewsIndex < holder.mCardViews.size(); cardViewsIndex++) {
                holder.mCardViews.get(cardViewsIndex).mRootCardView.setVisibility(CardView.GONE);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mGenres != null ? mGenres.first.size() : 0;
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mItemView;

        public TextView mTitleTextView;

        public LinearLayout mVideosContainer;

        public ArrayList<VideoCardViewBuilder.VideoCardViewHolder> mCardViews;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mTitleTextView = (TextView) itemView.findViewById(R.id.title_textView);
            mVideosContainer = (LinearLayout) itemView.findViewById(R.id.videos_container);
        }
    }


}
