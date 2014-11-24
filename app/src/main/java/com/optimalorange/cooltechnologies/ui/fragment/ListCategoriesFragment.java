package com.optimalorange.cooltechnologies.ui.fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Video;
import com.optimalorange.cooltechnologies.util.ItemsCountCalculater;
import com.optimalorange.cooltechnologies.util.VideosRequest;
import com.optimalorange.cooltechnologies.util.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
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

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 分类
 */
public class ListCategoriesFragment extends Fragment {

    /**
     * @see {@literal http://open.youku.com/docs/api_searches.html#schemas-video-category}
     */
    private static final String YOUKU_API_SCHEMAS_VIDEO_CATEGORY
            = "https://openapi.youku.com/v2/schemas/video/category.json";

    private static final String YOUKU_API_VIDEOS_BY_CATEGORY
            = "https://openapi.youku.com/v2/videos/by_category.json";

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

    private View v;

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
        return new VideosRequest.Builder()
                .setClient_id(mYoukuClientId)
                .setCategory(CATEGORY_LABEL_OF_TECH)
                .setGenre(genre)
                .setPeriod(VideosRequest.Builder.PERIOD.MONTH)
                .setResponseListener(new Response.Listener<List<Video>>() {
                    @Override
                    public void onResponse(List<Video> videos) {
                        mGenres.second.set(index, videos);
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
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(new String[]{"title1", "title2", "title3"});
        mRecyclerView.setAdapter(mAdapter);
    }

    //--------------------------------------------------------------------------
    // Adapter
    //--------------------------------------------------------------------------

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private String[] mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(String[] myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_categories, parent, false);
            //TODO set the view's size, margins, paddings and layout parameters
            return new ViewHolder(v, parent);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTitleTextView.setText(mDataset[position]);
            int i = 0;
            for (CardViewNative cardView : holder.mCardViews) {
                i++;
                Card card = new Card(holder.mItemView.getContext());
                CardHeader cardHeader = new CardHeader(holder.mItemView.getContext());
                cardHeader.setTitle("CardHeader:" + mDataset[position] + "@" + i);
                card.addCardHeader(cardHeader);
                card.setTitle("CardTitle:" + mDataset[position] + "@" + i);
                cardView.setCard(card);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {

            public View mItemView;

            public TextView mTitleTextView;

            public LinearLayout mVideosContainer;

            public List<CardViewNative> mCardViews;

            public ViewHolder(View itemView, View parent) {
                super(itemView);
                mItemView = itemView;
                mTitleTextView = (TextView) itemView.findViewById(R.id.title_textView);
                mVideosContainer = (LinearLayout) itemView.findViewById(R.id.videos_container);

                addCardViews(parent);
            }

            private void addCardViews(View parent) {
                Resources resources = mVideosContainer.getResources();
                int margin = resources.getDimensionPixelSize(R.dimen.card_margin_half);
                float cardWidthMin = resources.getDimension(R.dimen.card_width_min);
                float cardWidthMax = resources.getDimension(R.dimen.card_width_max);
                ItemsCountCalculater.Result itemsCountAndDimension = ItemsCountCalculater
                        .calculateItemsCountAndDimension(
                                cardWidthMin, cardWidthMax, margin * 2,
                                parent.getWidth() - margin * 2,
                                ItemsCountCalculater.Preference.SMALLER_OR_MORE_ITEMS);
                mCardViews = new ArrayList<>(itemsCountAndDimension.getCount());
                for (int i = itemsCountAndDimension.getCount(); i >= 1; i--) {
                    CardViewNative newCardView = new CardViewNative(mVideosContainer.getContext());

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            itemsCountAndDimension.getDimension(),
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(margin, margin, margin, margin);
                    mVideosContainer.addView(newCardView, layoutParams);
                    mCardViews.add(newCardView);
                }
            }
        }


    }


}
