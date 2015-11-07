package com.optimalorange.cooltechnologies.ui.viewholder;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.entity.Loading;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecyclerLoadingViewHolder extends RecyclerView.ViewHolder {

    public final TextView hint;

    public RecyclerLoadingViewHolder(View itemView) {
        super(itemView);
        hint = (TextView) itemView.findViewById(R.id.hint);
    }

    public static class Factory extends BaseVHFactory<Loading, RecyclerLoadingViewHolder> {

        @Override
        public Class<Loading> forClass() {
            return Loading.class;
        }

        @Override
        public RecyclerLoadingViewHolder createViewHolder(
                LayoutInflater inflater, ViewGroup parent) {
            return new RecyclerLoadingViewHolder(
                    inflater.inflate(R.layout.recycler_loading, parent, false));
        }

        @Override
        public void bindViewHolder(RecyclerLoadingViewHolder holder, Loading value) {
            holder.hint.setText(value.hint);
        }

    }

}
