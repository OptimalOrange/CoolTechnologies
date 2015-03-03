package com.optimalorange.cooltechnologies.ui.fragment;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.adapter.FavoriteAdapter;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;
import com.optimalorange.cooltechnologies.storage.sqlite.DBManager;
import com.optimalorange.cooltechnologies.ui.BaseActivity;
import com.optimalorange.cooltechnologies.ui.PlayVideoActivity;
import com.optimalorange.cooltechnologies.network.VolleySingleton;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by WANGZHENGZE on 2014/11/20.
 * 历史
 */
public class HistoryFragment extends Fragment {

    private View v;
    private ListView favoriteListView;
    private ArrayList<FavoriteBean> favoriteBeans;
    private FavoriteAdapter adapter;
    private VolleySingleton mVolleySingleton;
    private TextView mTvHint;

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams windowParams = null;
    private View deleteView = null;

    private boolean mIsDelButtonCreate;
    private boolean mIsCreated;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_history, container, false);
        favoriteListView = (ListView) v.findViewById(R.id.favorite_list);
        mTvHint = (TextView) v.findViewById(R.id.favorite_hint);
        favoriteBeans = DBManager.getInstance(getActivity()).getAllHistory();
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVolleySingleton = VolleySingleton.getInstance(getActivity());

        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                intent.putExtra(PlayVideoActivity.EXTRA_KEY_VIDEO, favoriteBeans.get(position));
                startActivity(intent);
            }
        });

        favoriteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                int[] locationInWindow = new int[2];
                view.getLocationInWindow(locationInWindow);
                windowParams = new WindowManager.LayoutParams();
                windowParams.gravity = Gravity.TOP | Gravity.LEFT;
                windowParams.x = locationInWindow[0] + view.getWidth() / 2;
                windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                //添加属性FLAG_WATCH_OUTSIDE_TOUCH，用于监听窗口外Touch事件
                windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                windowParams.format = PixelFormat.TRANSLUCENT;
                windowParams.windowAnimations = 0;
                windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                if (deleteView != null) {
                    windowManager.removeViewImmediate(deleteView);
                }
                deleteView = LayoutInflater.from(getActivity()).inflate(R.layout.ll_favorite_delete, null);
                deleteView.measure(0, 0);
                int deleteViewHeight = deleteView.getMeasuredHeight();
                windowParams.y = locationInWindow[1] - deleteViewHeight;
                deleteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBManager.getInstance(getActivity()).deleteHistory(favoriteBeans.get(position).videoId);
                        favoriteBeans.remove(position);
                        adapter.notifyDataSetChanged();
                        removeWindowView();
                        Toast.makeText(getActivity(), getString(R.string.history_delete_success), Toast.LENGTH_SHORT).show();
                        isNoHistory();
                    }
                });
                windowManager.addView(deleteView, windowParams);
                return true;
            }
        });

        favoriteListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deleteView == null) {
                        mIsDelButtonCreate = true;
                    } else {
                        mIsDelButtonCreate = false;
                        return true;
                    }
                }

                if ((event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_MOVE) && deleteView != null
                        && !mIsDelButtonCreate) {
                    removeWindowView();
                    return true;
                }
                return false;
            }
        });

        adapter = new FavoriteAdapter(getActivity(), favoriteBeans, mVolleySingleton.getImageLoader());
        favoriteListView.setAdapter(adapter);

        if (getActivity() instanceof BaseActivity) {
            favoriteListView.setOnScrollListener(
                    ((BaseActivity) getActivity()).getOnScrollListenerForAbsListView());
        }

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
        favoriteListView.setOnScrollListener(null);
        favoriteListView.setAdapter(null);
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

    private void removeWindowView() {
        if (windowManager != null && deleteView != null) {
            windowManager.removeViewImmediate(deleteView);
            deleteView = null;
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
