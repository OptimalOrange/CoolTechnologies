package com.optimalorange.cooltechnologies.ui.adapter;

import com.optimalorange.cooltechnologies.BuildConfig;
import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.FavoriteBean;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private final Dataset mDataset = new DatasetImpl(this);


    @NonNull
    public Dataset getDataset() {
        return mDataset;
    }

    @Override
    public int getItemCount() {
        return mDataset.getDateCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.getDateType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case R.id.type_main_content:
                return new MainCotentViewHolder(
                        inflater.inflate(R.layout.recycler_favorite, parent, false));
            case R.id.type_footer:
                return new FooterViewHolder(
                        inflater.inflate(R.layout.recycler_favorite_footer, parent, false));
            case R.id.type_empty:
            case R.id.type_uninitialized:
                return new SimpleHintViewHolder(
                        inflater.inflate(R.layout.recycler_loading, parent, false));
            default:
                throw new UnsupportedOperationException("Unknown viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Object data = mDataset.getData(position);
        switch (holder.getItemViewType()) {
            case R.id.type_main_content:
                onBindMainCotentViewHolder((MainCotentViewHolder) holder, (FavoriteBean) data);
                break;
            case R.id.type_footer:
                onBindFooterViewHolder((FooterViewHolder) holder, (Integer) data);
                break;
            case R.id.type_empty:
                onBindEmptyiewHolder((SimpleHintViewHolder) holder, (Integer) data);
                break;
            case R.id.type_uninitialized:
                onBindUninitializedViewHolder((SimpleHintViewHolder) holder, (Integer) data);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unknown viewType: " + holder.getItemViewType());
        }
    }

    private void onBindUninitializedViewHolder(
            SimpleHintViewHolder holder, @StringRes int stringRes) {
        holder.hint.setText(stringRes);
        //TODO set click listener
    }

    private void onBindEmptyiewHolder(SimpleHintViewHolder holder, @StringRes int stringRes) {
        holder.hint.setText(stringRes);
        //TODO set click listener
    }

    private void onBindFooterViewHolder(FooterViewHolder holder, @StringRes int stringRes) {
        holder.footerHint.setText(stringRes);
        //TODO set click listener
    }

    private void onBindMainCotentViewHolder(MainCotentViewHolder holder, FavoriteBean favorite) {
        holder.title.setText(favorite.title);
        holder.duration.setText(favorite.duration);
        //TODO image
    }

    //--------------------------------------------------------------------------
    // 内部类
    //--------------------------------------------------------------------------

    //-------------------------------------
    // ViewHolders
    //-------------------------------------

    public static class SimpleHintViewHolder extends RecyclerView.ViewHolder {

        public final TextView hint;

        public SimpleHintViewHolder(View itemView) {
            super(itemView);
            hint = (TextView) itemView.findViewById(R.id.hint);
        }

    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public final TextView footerHint;

        public FooterViewHolder(View itemView) {
            super(itemView);
            footerHint = (TextView) itemView.findViewById(R.id.hint);
        }

    }

    public static class MainCotentViewHolder extends RecyclerView.ViewHolder {

        public final TextView title;

        public final TextView duration;

        public final ImageView thumbnail;

        public MainCotentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            duration = (TextView) itemView.findViewById(R.id.duration);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        }

    }

    //-------------------------------------
    // Entities
    //-------------------------------------

    public interface Dataset {

        enum State {
            UNINITIALIZED,
            EMPTY,
            HAVE_DATA
        }

        State getState();

        int getDateCount();

        int getDateType(int position);

        Object getData(int position);

        int getCurrentPage() throws IllegalStateException;

        void reset();

        void add(Favorites added);

        void remove(int position);
    }

    public static class DatasetImpl implements Dataset {

        @NonNull
        final FavoritesAdapter mAdapter;

        @Nullable
        private Favorites mFavorites;


        public DatasetImpl(@NonNull FavoritesAdapter adapter) {
            mAdapter = adapter;
        }


        private void ensureInit() {
            if (mFavorites == null) {
                mFavorites = new Favorites();
                mFavorites.setInterestingFavorites(new ArrayList<FavoriteBean>());
            }
        }

        public boolean haveInitialized() {
            return mFavorites != null;
        }

        @Override
        public State getState() {
            if (haveInitialized()) {
                assert mFavorites != null;
                // ------------------------- test this trick -----------------------
                return mFavorites.isEmpty() ? State.EMPTY : State.HAVE_DATA;
            } else {
                return State.UNINITIALIZED;
            }
        }

        @Override
        public int getDateCount() {
            switch (getState()) {
                case UNINITIALIZED:
                case EMPTY:
                    return 1;
                case HAVE_DATA:
                    assert mFavorites != null;
                    return mFavorites.getInterestingFavorites().size() + 1; // + footer
                default:
                    throw new UnsupportedOperationException("Unknown State: " + getState());
            }
        }

        @Override
        public int getDateType(int position) {
            switch (getState()) {
                case UNINITIALIZED:
                    return R.id.type_uninitialized;
                case EMPTY:
                    return R.id.type_empty;
                case HAVE_DATA:
                    assert mFavorites != null;
                    final int favoritesSize = mFavorites.getInterestingFavorites().size();
                    return position < favoritesSize ? R.id.type_main_content : R.id.type_footer;
                default:
                    throw new UnsupportedOperationException("Unknown State: " + getState());
            }
        }

        @Override
        public Object getData(int position) {
            switch (getState()) {
                case UNINITIALIZED:
                    return R.string.favorite_new_loading;//TODO
                case EMPTY:
                    return R.string.favorite_no_fav;
                case HAVE_DATA:
                    assert mFavorites != null;
                    List<FavoriteBean> favorites = mFavorites.getInterestingFavorites();
                    if (position < favorites.size()) {
                        return favorites.get(position);
                    } else { // footer
                        if (mFavorites.allRead()) {
                            return R.string.favorite_view_more_last;
                        } else {
                            return R.string.favorite_view_more;
                        }
                    }
                default:
                    throw new UnsupportedOperationException("Unknown State: " + getState());
            }
        }

        @Override
        public int getCurrentPage() throws IllegalStateException {
            if (getState() == State.UNINITIALIZED) {
                throw new IllegalStateException();
            }
            assert mFavorites != null;
            return mFavorites.getCurrentPage();
        }

        @Override
        public void reset() {
            mFavorites = null;
            mAdapter.notifyDataSetChanged();
        }

        public void add(final Favorites added) {
            final State beforeAddState = getState();
            ensureInit();
            assert mFavorites != null;
            final int beforeAddFavoritesSize = mFavorites.getInterestingFavorites().size();

            mFavorites.add(added);

            if (getState() != beforeAddState) {
                mAdapter.notifyDataSetChanged();
            } else {
                final int addedCount = added.getInterestingFavorites().size();
                mAdapter.notifyItemRangeInserted(beforeAddFavoritesSize, addedCount);
                // invalid footer
                mAdapter.notifyItemChanged(mFavorites.getInterestingFavorites().size());
            }
        }

        public void remove(final int position) {
            final State beforeAddState = getState();
            ensureInit();
            assert mFavorites != null;

            mFavorites.remove(position);

            if (getState() != beforeAddState) {
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.notifyItemRemoved(position);
                // invalid footer
                mAdapter.notifyItemChanged(mFavorites.getInterestingFavorites().size());
            }
        }

    }

    public static class Favorites {

        private int total;

        private int currentPage;

        private int currentReadCountIncludingUnneeded;

        @Nullable
        private List<FavoriteBean> interestingFavorites;


        public boolean allRead() {
            return currentReadCountIncludingUnneeded == total;
        }

        public boolean isEmpty() {
            if (total == 0) {
                return true;
            } else {
                if (BuildConfig.DEBUG) {
                    if (total < 0) {
                        throw new AssertionError("total < 0");
                    }
                }
                // ------------------------- test this trick -----------------------
                return allRead() && getInterestingFavorites().isEmpty();
            }
        }

        private void ensureInited() {
            if (interestingFavorites == null) {
                throw new IllegalStateException("haven't set interestingFavorites");
            }
        }

        @NonNull
        public List<FavoriteBean> getInterestingFavorites() {
            ensureInited();
            assert interestingFavorites != null;
            return Collections.unmodifiableList(interestingFavorites);
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public void setCurrentReadCountIncludingUnneeded(int currentReadCountIncludingUnneeded) {
            this.currentReadCountIncludingUnneeded = currentReadCountIncludingUnneeded;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public void setInterestingFavorites(@NonNull List<FavoriteBean> interestingFavorites) {
            this.interestingFavorites = interestingFavorites;
        }

        //TODO check currentPage + 1 != added.currentPage?
        //TODO check total != added.total?
        //TODO check illegal argument
        //TODO check state
        public void add(Favorites added) {
            ensureInited();
            assert interestingFavorites != null;

            total = added.total;
            currentPage = added.currentPage;
            currentReadCountIncludingUnneeded += added.currentReadCountIncludingUnneeded;
            interestingFavorites.addAll(added.getInterestingFavorites());
        }

        //TODO check illegal argument
        //TODO check state
        public void remove(int index) {
            ensureInited();
            assert interestingFavorites != null;

            total--;
            currentReadCountIncludingUnneeded--;
            interestingFavorites.remove(index);
        }

    }


}
