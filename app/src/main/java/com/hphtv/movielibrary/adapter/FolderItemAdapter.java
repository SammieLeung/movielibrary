package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.activity.AppBaseActivity;
import com.hphtv.movielibrary.activity.bean.FolderItem;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FolderAddItemLayoutBinding;
import com.hphtv.movielibrary.databinding.FolderItemLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/8/25
 */
public class FolderItemAdapter extends RecyclerView.Adapter<CommonViewHolder> {
    public static final int TYPE_HEAD = 0;
    public static final int TYPE_FOLDER = 1;
    private List<FolderItem> mFolderItemList = new ArrayList<>();
    private List<CommonViewHolder> mViewHolderList = new ArrayList<>();
    private Context mContext;


    Comparator mComparator = (Comparator<FolderItem>) (o1, o2) -> {

        if (o1.type < Constants.DeviceType.DEVICE_TYPE_DLNA && o2.type >= Constants.DeviceType.DEVICE_TYPE_DLNA) {
            return -1;
        } else if (o1.type == Constants.DeviceType.DEVICE_TYPE_DLNA && o2.type == Constants.DeviceType.DEVICE_TYPE_SMB) {
            return -1;
        } else if (o2.type == Constants.DeviceType.DEVICE_TYPE_DLNA && o1.type == Constants.DeviceType.DEVICE_TYPE_SMB) {
            return 1;
        } else {
            return 0;
        }

    };

    public FolderItemAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @NotNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            FolderAddItemLayoutBinding binding = FolderAddItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            CommonViewHolder<FolderAddItemLayoutBinding> vh = new CommonViewHolder<>(binding);
            binding.setActivity((AppBaseActivity) mContext);
            return vh;
        } else {
            FolderItemLayoutBinding binding = FolderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            CommonViewHolder<FolderItemLayoutBinding> vh = new CommonViewHolder<>(binding);
            return vh;
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEAD;
        return TYPE_FOLDER;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOLDER) {
            FolderItem folderItem = mFolderItemList.get(position - 1);
            CommonViewHolder<FolderItemLayoutBinding> vh = holder;
            vh.mDataBinding.setName(folderItem.title);
            vh.mDataBinding.setUri(folderItem.uri);
            vh.itemView.setTag(folderItem);
        }
    }

    @Override
    public int getItemCount() {
        return 1 + mFolderItemList.size();
    }


    public void addAllShortcuts(List<Shortcut> shortcutList) {
        Iterator<FolderItem> iterator = mFolderItemList.listIterator();
        while (iterator.hasNext()) {
            FolderItem folderItem = iterator.next();
            if (folderItem.item instanceof Shortcut) {
                iterator.remove();
            }
        }
        for (Shortcut shortcut : shortcutList) {
            mFolderItemList.add(getFolderItem(shortcut));
        }
        mFolderItemList.sort(mComparator);
        notifyDataSetChanged();
    }


    public void add(Shortcut shortcut) {
        FolderItem folderItem = getFolderItem(shortcut);
        if (!mFolderItemList.contains(folderItem)) {
            mFolderItemList.add(folderItem);
            mFolderItemList.sort(mComparator);
            notifyItemInserted(mFolderItemList.size());
        }
    }


    public FolderItem getFolderItem(Shortcut shortcut) {
        FolderItem folderItem = new FolderItem();
        folderItem.title = shortcut.firendlyName;
        folderItem.uri = shortcut.uri;
        folderItem.item = shortcut;
        folderItem.type = shortcut.devcieType;
        return folderItem;
    }

}
