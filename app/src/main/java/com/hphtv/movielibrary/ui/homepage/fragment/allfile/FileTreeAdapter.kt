package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.hphtv.movielibrary.adapter.BaseScaleAdapter
import com.hphtv.movielibrary.databinding.MiniAllFileItemBinding

class FileTreeAdapter(context: Context, list: List<FolderItem>) :
    BaseScaleAdapter<MiniAllFileItemBinding, FolderItem>(
        context,
        list
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseScaleAdapter<MiniAllFileItemBinding, FolderItem>.ViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        val itemBinding = viewHolder.mBinding as MiniAllFileItemBinding
        itemBinding.root.id = View.generateViewId()
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseScaleAdapter<*, *>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val folderItem = mList[position]
        val itemBinding = holder.mBinding as MiniAllFileItemBinding
        itemBinding.text = folderItem.name
        itemBinding.viewIcon.setImageDrawable(getDrawable(mContext, folderItem.icon))
    }

    companion object {
        const val TYPE_BACK = 1
        const val TYPE_NORMAL = 0
    }
}