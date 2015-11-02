package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.adapter.FavoriteAdapter;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.storage.sqlite.DBManager;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 历史
 */
public class HistoryFragment extends Fragment {

    private ArrayList<FavoriteBean> favoriteBeans;
    private FavoriteAdapter adapter;

    private View v;
    private ListView favoriteListView;
    private TextView mTvHint;

    private boolean mIsCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoriteBeans = new ArrayList<>(20);
        adapter = new FavoriteAdapter(
                getActivity(), favoriteBeans,
                VolleySingleton.getInstance(getActivity()).getImageLoader());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_history, container, false);
        favoriteListView = (ListView) v.findViewById(R.id.favorite_list);
        mTvHint = (TextView) v.findViewById(R.id.favorite_hint);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO, favoriteBeans.get(position));
                startActivity(intent);
            }
        });

        favoriteListView.setAdapter(adapter);
        favoriteListView.setVisibility(View.VISIBLE);
        mIsCreated = true;
        isNoHistory();
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
        mTvHint = null;
        favoriteListView.setAdapter(null);
        favoriteListView.setOnTouchListener(null);
        favoriteListView.setOnItemLongClickListener(null);
        favoriteListView.setOnItemClickListener(null);
        favoriteListView = null;
        v = null;
        super.onDestroyView();
    }

    public void refreshData() {
        if (favoriteBeans != null) {
            favoriteBeans.clear();
            favoriteBeans.addAll(DBManager.getInstance(getActivity()).getAllHistory());
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                isNoHistory();
            }
        }
    }

    private void isNoHistory() {
        if (favoriteBeans.size() == 0) {
            setHint(R.string.history_no_history);
        } else {
            mTvHint.setVisibility(View.GONE);
            favoriteListView.setVisibility(View.VISIBLE);
        }
    }

    private void setHint(int res) {
        mTvHint.setText(getString(res));
        mTvHint.setVisibility(View.VISIBLE);
        favoriteListView.setVisibility(View.GONE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //显示的时候判断有没有走onCreated，没有判断是否有列表，没有则刷新列表。
        if (isVisibleToUser) {
            if (mIsCreated) {
                mIsCreated = false;
            } else {
                refreshData();
            }
        }
    }
}
