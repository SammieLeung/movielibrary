package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerEventHandler;
import com.hphtv.movielibrary.ui.shortcutmanager.bean.FolderItem;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FolderItemLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.StringTools;

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
    private ShortcutManagerEventHandler mEventHandler;
    private OnClickListener mOnClickListener;

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
        mEventHandler = new ShortcutManagerEventHandler((AppBaseActivity) mContext);
    }

    @NonNull
    @NotNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
//        if (viewType == TYPE_HEAD) {
//            FolderAddItemLayoutBinding binding = FolderAddItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
//            CommonViewHolder<FolderAddItemLayoutBinding> vh = new CommonViewHolder<>(binding);
//            binding.setPosterHandler(mEventHandler);
//            return vh;
//        } else {
        FolderItemLayoutBinding binding = FolderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        CommonViewHolder<FolderItemLayoutBinding> vh = new CommonViewHolder<>(binding);
        binding.getRoot().setOnClickListener(v -> {
            if (mOnClickListener != null)
                mOnClickListener.onClick((FolderItem) v.getTag());
        });
        return vh;
//        }
    }

    @Override
    public int getItemViewType(int position) {
//        if (position == 0)
//            return TYPE_HEAD;
        return TYPE_FOLDER;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOLDER) {
            FolderItem folderItem = mFolderItemList.get(position);
            CommonViewHolder<FolderItemLayoutBinding> vh = holder;
            switch (folderItem.type) {
                case Constants.DeviceType.DEVICE_TYPE_DLNA:
                    Glide.with(mContext).load(R.mipmap.icon_new_dlna_folder).into(vh.mDataBinding.imgIcon);
                    break;
                case Constants.DeviceType.DEVICE_TYPE_SMB:
                    Glide.with(mContext).load(R.mipmap.icon_new_smb_folder).into(vh.mDataBinding.imgIcon);
                    break;
                default:
                    Glide.with(mContext).load(R.mipmap.icon_new_folder).into(vh.mDataBinding.imgIcon);
                    break;
            }
            vh.mDataBinding.setItem(folderItem);
            vh.mDataBinding.getRoot().setTag(folderItem);
        }
    }

    @Override
    public int getItemCount() {
        return mFolderItemList.size();
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
            mFolderItemList.add(buildFolderItem(shortcut));
        }
        mFolderItemList.sort(mComparator);
        notifyDataSetChanged();
    }

    public void add(Shortcut shortcut) {
        FolderItem folderItem = buildFolderItem(shortcut);
        if (!mFolderItemList.contains(folderItem)) {
            mFolderItemList.add(0,folderItem);
            notifyItemInserted(0);
        }
    }

    public FolderItem buildFolderItem(Shortcut shortcut) {
        FolderItem folderItem = new FolderItem();
        folderItem.uri = shortcut.uri;
        folderItem.title = shortcut.friendlyName;
        folderItem.item = shortcut;
        folderItem.type = shortcut.deviceType;
        folderItem.file_count = shortcut.fileCount;
        folderItem.poster_count = shortcut.posterCount;
        folderItem.state = shortcut.isScanned == 2 ? FolderItem.State.SCANNING : shortcut.isScanned == 1 ? FolderItem.State.SCANNED : FolderItem.State.UNSCANNED;
        switch (folderItem.type) {
            case Constants.DeviceType.DEVICE_TYPE_SMB:
                String smbUri = shortcut.uri;
                folderItem.sub_title = StringTools.hideSmbAuthInfo(smbUri);
                break;
            case Constants.DeviceType.DEVICE_TYPE_DLNA:
                String dlnaUri = shortcut.uri;
                String[] splits = dlnaUri.split(":");
                if (splits.length >= 3) {
                    dlnaUri = splits[2];
                }
                folderItem.sub_title = dlnaUri;
                break;
            default:
                folderItem.sub_title = shortcut.uri;

        }
        return folderItem;
    }

    public void updateShortcut(Shortcut shortcut) {
        for (int i = 0; i < mFolderItemList.size(); i++) {
            FolderItem folderItem = mFolderItemList.get(i);
            String uri = shortcut.uri;
            if (folderItem.uri.equals(uri)) {
                folderItem = buildFolderItem(shortcut);
                mFolderItemList.set(i, folderItem);
                notifyItemChanged(i);
            }
        }
    }

    public void removeShortcut(Shortcut shortcut) {
        for (int i = 0; i < mFolderItemList.size(); i++) {
            FolderItem folderItem = mFolderItemList.get(i);
            String uri = shortcut.uri;
            if (folderItem.uri.equals(uri)) {
                mFolderItemList.remove(folderItem);
                notifyItemRemoved(i);
            }
        }
    }

    public void pickerClose() {
        if (mEventHandler != null)
            mEventHandler.pickerClose();
    }

    public interface OnClickListener {
        void onClick(FolderItem item);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
}
