package com.optimalorange.cooltechnologies.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import gq.baijie.classbasedviewadapter.android.adapter.ViewHolderFactory;

public abstract class BaseVHFactory<T, VH extends RecyclerView.ViewHolder>
        implements ViewHolderFactory<T, VH> {

    @Override
    final public VH createViewHolder(ViewGroup parent) {
        return createViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    public abstract VH createViewHolder(LayoutInflater inflater, ViewGroup parent);

}
