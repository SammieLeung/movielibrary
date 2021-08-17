package com.hphtv.movielibrary.adapter;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class CommonViewHolder<VDB extends  ViewDataBinding> extends RecyclerView.ViewHolder {
    public VDB mDataBinding;

    public CommonViewHolder(VDB binding) {
        super(binding.getRoot());
        mDataBinding = binding;
    }
}
