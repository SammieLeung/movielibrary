package com.hphtv.movielibrary.ui.homepage.genretag;


import android.content.Context;

import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseScaleApater;
import com.hphtv.movielibrary.databinding.GenreEditDialogfragmentRecyclerviewItemLayoutBinding;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreListApter extends BaseScaleApater<GenreEditDialogfragmentRecyclerviewItemLayoutBinding, GenreListApter.ViewHolder,GenreTagItem> {


    public GenreListApter(Context context, List<GenreTagItem> list) {
        super(context, list);
    }

    public class ViewHolder extends BaseScaleApater.ViewHolder{

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
        }
    }
}
