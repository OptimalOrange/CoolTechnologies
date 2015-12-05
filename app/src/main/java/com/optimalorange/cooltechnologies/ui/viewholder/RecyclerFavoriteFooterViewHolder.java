package com.optimalorange.cooltechnologies.ui.viewholder;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.entity.FavoriteFooter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecyclerFavoriteFooterViewHolder extends RecyclerView.ViewHolder {

    public final TextView hint;

    public RecyclerFavoriteFooterViewHolder(View itemView) {
        super(itemView);
        hint = (TextView) itemView.findViewById(R.id.hint);
    }

    public static class Factory
            extends BaseVHFactory<FavoriteFooter, RecyclerFavoriteFooterViewHolder> {

        @Override
        public Class<FavoriteFooter> forClass() {
            return FavoriteFooter.class;
        }

        @Override
        public RecyclerFavoriteFooterViewHolder createViewHolder(
                LayoutInflater inflater, ViewGroup parent) {
            return new RecyclerFavoriteFooterViewHolder(
                    inflater.inflate(R.layout.recycler_favorite_footer, parent, false));
        }

        @Override
        public void bindViewHolder(
                RecyclerFavoriteFooterViewHolder holder, FavoriteFooter value, int position) {
            holder.hint.setText(value.hint);
            holder.hint.setOnClickListener(value.listener);
        }

    }
}
