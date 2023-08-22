package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.content.Context
import com.hphtv.movielibrary.adapter.BaseScaleAdapter
import com.hphtv.movielibrary.databinding.MiniFileItemBinding

class FileTreeAdapter(context: Context, list: List<String>) :
    BaseScaleAdapter<MiniFileItemBinding, BaseScaleAdapter<*, *, *>.ViewHolder, String>(
        context,
        list
    ){


}