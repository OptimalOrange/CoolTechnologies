package com.optimalorange.cooltechnologies.ui.viewholder;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.entity.Favorite;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerFavoriteViewHolder extends RecyclerView.ViewHolder {

    public final TextView title;

    public final TextView duration;

    public final ImageView thumbnail;

    public RecyclerFavoriteViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        duration = (TextView) itemView.findViewById(R.id.duration);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    }

    public static class Factory extends BaseVHFactory<Favorite, RecyclerFavoriteViewHolder> {

        @Override
        public Class<Favorite> forClass() {
            return Favorite.class;
        }

        @Override
        public RecyclerFavoriteViewHolder createViewHolder(LayoutInflater inflater,
                ViewGroup parent) {
            return new RecyclerFavoriteViewHolder(
                    inflater.inflate(R.layout.recycler_favorite, parent, false));
        }

        @Override
        public void bindViewHolder(RecyclerFavoriteViewHolder holder, Favorite value) {
            holder.title.setText(value.title);
            holder.duration.setText(value.duration);
            //TODO image
        }

    }

}
