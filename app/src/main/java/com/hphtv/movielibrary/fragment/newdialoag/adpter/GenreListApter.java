package com.hphtv.movielibrary.fragment.newdialoag.adpter;


import android.content.Context;

import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.databinding.GenreEditDialogfragmentRecyclerviewItemLayoutBinding;
import com.hphtv.movielibrary.fragment.newdialoag.entity.GenreTagItem;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreListApter extends BaseAdapter2<GenreEditDialogfragmentRecyclerviewItemLayoutBinding, GenreListApter.ViewHolder,GenreTagItem> {


    public GenreListApter(Context context, List<GenreTagItem> list) {
        super(context, list);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.genre_edit_dialogfragment_recyclerview_item_layout;
    }

    public class ViewHolder extends BaseAdapter2.ViewHolder{

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
        }
    }
}
