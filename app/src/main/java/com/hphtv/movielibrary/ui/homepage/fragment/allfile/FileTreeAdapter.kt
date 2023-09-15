package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DiffUtil
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

    fun setData(newList: List<FolderItem>){
        val diffResult= DiffUtil.calculateDiff(FileTreeDiffCallback(mList, newList))
        mList=newList
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val TYPE_BACK = 1
        const val TYPE_NORMAL = 0
    }
}

class FileTreeDiffCallback(
    private val oldList: List<FolderItem>,
    private val newList: List<FolderItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].type == newList[newItemPosition].type &&
                oldList[oldItemPosition].path == newList[newItemPosition].path &&
                oldList[oldItemPosition].name == newList[newItemPosition].name

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

}