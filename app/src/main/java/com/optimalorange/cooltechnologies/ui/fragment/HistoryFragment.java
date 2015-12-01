package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.storage.sqlite.DBManager;
import com.optimalorange.cooltechnologies.ui.ShowVideoDetailActivity;
import com.optimalorange.cooltechnologies.ui.entity.Video;
import com.optimalorange.cooltechnologies.ui.viewholder.RecyclerFavoriteViewHolder;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gq.baijie.classbasedviewadapter.android.adapter.ClassBasedRecyclerViewAdapter;
import gq.baijie.classbasedviewadapter.android.adapter.DataSet;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 历史
 */
public class HistoryFragment extends Fragment {

    private final HistoryDataSet mHistoryDataSet = new HistoryDataSet();

    private final ClassBasedRecyclerViewAdapter mAdapter = new ClassBasedRecyclerViewAdapter();

    {
        mAdapter.getRegister().registerViewHolderFactory(new RecyclerFavoriteViewHolder.Factory() {
            @Override
            public void bindViewHolder(
                    RecyclerFavoriteViewHolder holder, final Video value, int position) {
                super.bindViewHolder(holder, value, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowVideoDetailActivity.start(v.getContext(), value.id);
                    }
                });
            }
        });
        mAdapter.setDataSet(mHistoryDataSet);
    }

    private ViewHolder mViewHolder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_history, container, false);
        mViewHolder = new ViewHolder(v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewHolder.histories.setLayoutManager(
                new LinearLayoutManager(mViewHolder.histories.getContext()));
        mViewHolder.histories.setAdapter(mAdapter);
        mViewHolder.histories.setVisibility(View.VISIBLE);
        checkIsNoHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
        refreshData();
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mViewHolder.histories.setAdapter(null);
        mViewHolder = null;
        super.onDestroyView();
    }

    public void refreshData() {
        mHistoryDataSet.setVideos(DBManager.getInstance(getActivity()).getAllHistory());
        mAdapter.notifyDataSetChanged();
        checkIsNoHistory();
    }

    private void checkIsNoHistory() {
        if (mHistoryDataSet.size() == 0) {
            setHint(R.string.history_no_history);
        } else {
            setHint(0);
        }
    }

    private void setHint(int res) {
        if (mViewHolder != null) {
            if (res != 0) {
                mViewHolder.mainHint.setText(getString(res));
                mViewHolder.mainHint.setVisibility(View.VISIBLE);
                mViewHolder.histories.setVisibility(View.GONE);
            } else {
                mViewHolder.mainHint.setVisibility(View.GONE);
                mViewHolder.histories.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class HistoryDataSet implements DataSet {

        final private List<Video> mVideos = new ArrayList<>();

        public void setVideos(List<Video> videos) {
            mVideos.clear();
            mVideos.addAll(videos);
        }

        @Override
        public int size() {
            return mVideos.size();
        }

        @Override
        public Video get(int position) {
            return mVideos.get(position);
        }

    }

    private static class ViewHolder {

        final RecyclerView histories;

        final TextView mainHint;

        public ViewHolder(View root) {
            mainHint = (TextView) root.findViewById(R.id.main_hint);
            histories = (RecyclerView) root.findViewById(R.id.histories);
        }
    }

}
