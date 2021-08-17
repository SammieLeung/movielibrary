package com.hphtv.movielibrary.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FolderAddItemLayoutBinding;
import com.hphtv.movielibrary.databinding.FolderItemLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.view.AnimateWrapper;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/8/25
 */
public class FolderItemAdapter extends RecyclerView.Adapter<CommonViewHolder> {
    public static final int TYPE_HEAD = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_HIDDEN_FOLDER = 2;
    private int mFolderType = TYPE_FOLDER;
    private List<ScanDirectory> mScanDirectoryList;
    private List<CommonViewHolder> mViewHolderList = new ArrayList<>();
    private Context mContext;

    public FolderItemAdapter(Context context, List<ScanDirectory> list) {
        mScanDirectoryList = list;
        mContext = context;
    }

    @NonNull
    @NotNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            FolderAddItemLayoutBinding binding = FolderAddItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            CommonViewHolder<FolderAddItemLayoutBinding> vh = new CommonViewHolder<>(binding);
            vh.mDataBinding.getRoot().setOnClickListener(v -> {
                if (mOnClickListener != null)
                    mOnClickListener.addClick();
            });
            return vh;
        } else {
            FolderItemLayoutBinding binding = FolderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            CommonViewHolder<FolderItemLayoutBinding> vh = new CommonViewHolder<>(binding);
            vh.mDataBinding.tvFolder.setOnClickListener(v -> {
                LogUtil.v("click");

                hideItemMenu(vh.mDataBinding);
                if (mOnClickListener != null)
                    mOnClickListener.onClick((ScanDirectory) v.getTag());
            });
            vh.mDataBinding.btnDelete.setOnClickListener(v -> {
                hideItemMenu(vh.mDataBinding);
                if (mOnClickListener != null)
                    mOnClickListener.delete(vh.mDataBinding.getFolder());
            });
            vh.mDataBinding.btnMove.setOnClickListener(v -> {
                hideItemMenu(vh.mDataBinding);
                if (mOnClickListener != null)
                    mOnClickListener.move(vh.mDataBinding.getFolder());
            });
            if (viewType == TYPE_FOLDER) {
                vh.mDataBinding.setBtnMoveText(mContext.getResources().getString(R.string.folder_item_add_to_hidden));
            } else {
                vh.mDataBinding.setBtnMoveText(mContext.getResources().getString(R.string.folder_item_move_out));
            }
            setEvent(vh.mDataBinding, vh.mDataBinding.tvFolder, vh.mDataBinding.btnDelete, vh.mDataBinding.btnMove);
            return vh;
        }
    }

    private void setEvent(FolderItemLayoutBinding binding, View... views) {
        for (View view : views) {
            view.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_MENU:
                        case KeyEvent.KEYCODE_BUTTON_START:
                            toggleItemMenu(binding);
                            return false;
                        case KeyEvent.KEYCODE_BACK:
                        case KeyEvent.KEYCODE_ESCAPE:
                            hideItemMenu(binding);
                            return true;
                    }
                }
                return false;
            });

            view.setOnLongClickListener(v -> {
                LogUtil.v("LongClick");
                showItemMenu(binding);
                return true;
            });
        }
    }

    /**
     * 显示选项动画
     *
     * @param binding
     */
    private void showItemMenu(FolderItemLayoutBinding binding) {
        int width = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_dialog_folderitem_button_w);
        AnimateWrapper btnDeleteWrapper = new AnimateWrapper(binding.btnDelete);
        AnimateWrapper btnMoveWrapper = new AnimateWrapper(binding.btnMove);
        int currentWidth = btnDeleteWrapper.getWidth();
        ObjectAnimator animDelete = ObjectAnimator.ofInt(btnDeleteWrapper, "width", currentWidth, width);
        ObjectAnimator animMove = ObjectAnimator.ofInt(btnMoveWrapper, "width", currentWidth, width);
        animDelete.setDuration(50);
        animMove.setDuration(50);
        animDelete.start();
        animMove.start();
        binding.setOption(true);
    }

    /**
     * 隐藏选项动画
     *
     * @param binding
     */
    private void hideItemMenu(FolderItemLayoutBinding binding) {
        AnimateWrapper btnDeleteWrapper = new AnimateWrapper(binding.btnDelete);
        AnimateWrapper btnMoveWrapper = new AnimateWrapper(binding.btnMove);
        int currentWidth = btnDeleteWrapper.getWidth();
        ObjectAnimator animDelete = ObjectAnimator.ofInt(btnDeleteWrapper, "width", currentWidth, 0);
        ObjectAnimator animMove = ObjectAnimator.ofInt(btnMoveWrapper, "width", currentWidth, 0);
        animDelete.setDuration(50);
        animMove.setDuration(50);
        animDelete.start();
        animMove.start();
        binding.setOption(false);
    }


    /**
     * 显示/隐藏选项动画
     *
     * @param binding
     */
    private void toggleItemMenu(FolderItemLayoutBinding binding) {
        boolean option = binding.getOption();
        if (!option) {
            showItemMenu(binding);
        } else {
            hideItemMenu(binding);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mFolderType == TYPE_FOLDER && position == 0)
            return TYPE_HEAD;
        return mFolderType;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOLDER) {
            ScanDirectory scanDirectory = mScanDirectoryList.get(position - 1);
            CommonViewHolder<FolderItemLayoutBinding> vh = holder;
            vh.mDataBinding.setFolder(scanDirectory.path);
            vh.itemView.setTag(scanDirectory);
            hideItemMenu(vh.mDataBinding);
        }else if( getItemViewType(position) == TYPE_HIDDEN_FOLDER){
            ScanDirectory scanDirectory = mScanDirectoryList.get(position);
            CommonViewHolder<FolderItemLayoutBinding> vh = holder;
            vh.mDataBinding.setFolder(scanDirectory.path);
            vh.itemView.setTag(scanDirectory);
            hideItemMenu(vh.mDataBinding);
        }
    }

    @Override
    public int getItemCount() {
        if (mFolderType == TYPE_FOLDER) {
            return mScanDirectoryList.size() + 1;
        } else {
            return mScanDirectoryList.size();
        }
    }

    public void addAll(List<ScanDirectory> list) {
        mScanDirectoryList.clear();
        mScanDirectoryList.addAll(list);
        notifyDataSetChanged();
    }

    public void setTypeFolder(int type) {
        mFolderType = type;
    }

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void addClick();

        void onClick(ScanDirectory scanDirectory);

        void delete(String path);

        void move(String path);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

}
