package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.MiniFileItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView;
import com.hphtv.movielibrary.ui.homepage.fragment.unknown.UnknownFileViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class UnknownRootItemListAdapter extends BaseScaleAdapter<MiniFileItemBinding, UnknownRootDataView> {

    public UnknownRootItemListAdapter(Context context, List<UnknownRootDataView> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        MiniFileItemBinding itemBinding = (MiniFileItemBinding) viewHolder.mBinding;
        itemBinding.getRoot().setId(View.generateViewId());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);
        UnknownRootDataView dataView = mList.get(position);
        MiniFileItemBinding binding = (MiniFileItemBinding) holder.mBinding;
        String text;
        String subText = null;
        if (dataView.type.equals(Constants.UnknownRootType.FILE)) {
            text = dataView.root.substring(dataView.root.lastIndexOf("/") + 1);
            binding.setType(UnknownFileViewModel.TYPE_FILE);
        } else if (dataView.type.equals(Constants.UnknownRootType.FOLDER)) {
            text = dataView.root;
            if (dataView.root.endsWith("/")) ;
            {
                text = text.substring(0, text.length() - 1);
            }
            text = text.substring(text.lastIndexOf("/") + 1);
            subText = mContext.getString(R.string.item_count_format, String.valueOf(dataView.count));
            binding.setType(UnknownFileViewModel.TYPE_FOLDER);

        } else {
            text = mContext.getString(R.string.goback);
            binding.setType(UnknownFileViewModel.TYPE_BACK);
        }
        binding.setText(text);
        binding.setSubText(subText);
    }


}


