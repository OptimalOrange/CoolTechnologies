package com.optimalorange.cooltechnologies.ui.viewholder;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.entity.Empty;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecyclerEmptyViewHolder extends RecyclerView.ViewHolder {

    public final TextView hint;

    public RecyclerEmptyViewHolder(View itemView) {
        super(itemView);
        hint = (TextView) itemView.findViewById(R.id.hint);
    }

    public static class Factory extends BaseVHFactory<Empty, RecyclerEmptyViewHolder> {

        @Override
        public Class<Empty> forClass() {
            return Empty.class;
        }

        @Override
        public RecyclerEmptyViewHolder createViewHolder(
                LayoutInflater inflater, ViewGroup parent) {
            return new RecyclerEmptyViewHolder(
                    inflater.inflate(R.layout.recycler_empty, parent, false));
        }

        @Override
        public void bindViewHolder(RecyclerEmptyViewHolder holder, Empty value) {
            holder.hint.setText(value.hint);
        }

    }

}
