package com.hphtv.movielibrary.fragment.dialog.homepage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FilterBoxHeaditemLayoutBinding;
import com.hphtv.movielibrary.databinding.FilterBoxItemLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/7
 */
public class FilterBoxDeviceAdapter extends RecyclerView.Adapter<FilterBoxDeviceAdapter.CommonFilterBoxViewHolder> {
    public static final int VIEWTYPE_HEADITEM = 1;
    public static final int VIEWTYPE_DEVICE = 2;
    public static final int VIEWTYPE_ALL = 3;

    private Context mContext;
    private List<Object> mDataList = new ArrayList<>();
    private ObservableInt mCheckPos;
    private OnFilterBoxItemClickListener mOnFilterBoxItemClickListener;

    public FilterBoxDeviceAdapter(Context context, ObservableInt checkPos) {
        mContext = context;
        mCheckPos=checkPos;
    }


    @NonNull
    @NotNull
    @Override
    public CommonFilterBoxViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_DEVICE || viewType == VIEWTYPE_ALL) {
            FilterBoxItemLayoutBinding itemLayoutBinding = FilterBoxItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            itemLayoutBinding.setCheckPos(mCheckPos);
            CommonFilterBoxViewHolder commonFilterBoxViewHolder = new CommonFilterBoxViewHolder(itemLayoutBinding);
            return commonFilterBoxViewHolder;
        } else {
            FilterBoxHeaditemLayoutBinding headitemLayoutBinding = FilterBoxHeaditemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            CommonFilterBoxViewHolder commonFilterBoxViewHolder = new CommonFilterBoxViewHolder(headitemLayoutBinding);
            return commonFilterBoxViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonFilterBoxViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        int realPos=position-1;
        if (viewType == VIEWTYPE_ALL) {
            holder.mViewDataBinding.setName(mContext.getResources().getString(R.string.tx_all));
            holder.mViewDataBinding.setPos(position);
        } else if (viewType == VIEWTYPE_DEVICE) {
            Object o = mDataList.get(realPos);
            if (o instanceof ScanDirectory) {
                ScanDirectory scanDirectory = (ScanDirectory) o;
                holder.mViewDataBinding.setName(scanDirectory.friendlyName);
            } else if (o instanceof Shortcut) {
                Shortcut shortcut = (Shortcut) o;
                holder.mViewDataBinding.setName(shortcut.name);
            }
            holder.mViewDataBinding.setPos(position);
        } else {
            String title = (String) mDataList.get(realPos);
            holder.mHeaditemLayoutBinding.setName(title);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEWTYPE_ALL;
        int realPos=position-1;
        Object data = mDataList.get(realPos);
        if (data instanceof String) {
            return VIEWTYPE_HEADITEM;
        } else {
            return VIEWTYPE_DEVICE;
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size() + 1;
    }

    public void setCurrentPos(int pos) {
        if (mCheckPos != null)
            mCheckPos.set(pos);
    }

    public void setOnFilterBoxItemClickListener(OnFilterBoxItemClickListener onFilterBoxItemClickListener) {
        mOnFilterBoxItemClickListener = onFilterBoxItemClickListener;
    }

    public void addAll(List<Object> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public class CommonFilterBoxViewHolder extends RecyclerView.ViewHolder {
        public FilterBoxItemLayoutBinding mViewDataBinding;
        public FilterBoxHeaditemLayoutBinding mHeaditemLayoutBinding;

        public CommonFilterBoxViewHolder(@NonNull @NotNull FilterBoxItemLayoutBinding filterBoxItemLayoutBinding) {
            super(filterBoxItemLayoutBinding.getRoot());
            mViewDataBinding = filterBoxItemLayoutBinding;
            mViewDataBinding.cbtvTitle.setOnClickListener(v -> {
                setCurrentPos(mViewDataBinding.getPos());
                if(mOnFilterBoxItemClickListener!=null)
                    mOnFilterBoxItemClickListener.OnFilterChange();
            });
        }

        public CommonFilterBoxViewHolder(FilterBoxHeaditemLayoutBinding headitemLayoutBinding) {
            super(headitemLayoutBinding.getRoot());
            mHeaditemLayoutBinding = headitemLayoutBinding;
        }
    }
}
