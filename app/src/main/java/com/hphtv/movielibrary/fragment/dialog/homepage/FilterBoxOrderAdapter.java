package com.hphtv.movielibrary.fragment.dialog.homepage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FilterBoxItemLayoutBinding;
import com.hphtv.movielibrary.databinding.FilterBoxOrderItemLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * author: Sam Leung
 * date:  2021/12/7
 */
public class FilterBoxOrderAdapter extends RecyclerView.Adapter<FilterBoxOrderAdapter.CommonFilterBoxViewHolder> {
    public static final String TAG = FilterBoxOrderAdapter.class.getSimpleName();
    private Context mContext;
    private List<String> mDataList = new ArrayList<>();
    private ObservableInt mCheckPos;
    private ObservableBoolean mDescFlag;
    private OnFilterBoxItemClickListener mOnFilterBoxItemClickListener;

    public FilterBoxOrderAdapter(Context context, ObservableInt checkPos, ObservableBoolean descFlag) {
        mContext = context;
        mCheckPos = checkPos;
        mDescFlag = descFlag;
    }

    @NonNull
    @NotNull
    @Override
    public CommonFilterBoxViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        FilterBoxOrderItemLayoutBinding itemLayoutBinding = FilterBoxOrderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        itemLayoutBinding.setCheckPos(mCheckPos);
        itemLayoutBinding.setIsDesc(mDescFlag);
        CommonFilterBoxViewHolder commonFilterBoxViewHolder = new CommonFilterBoxViewHolder(itemLayoutBinding);
        return commonFilterBoxViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonFilterBoxViewHolder holder, int position) {
        String name = mDataList.get(position);
        holder.mViewDataBinding.setName(name);
        holder.mViewDataBinding.setPos(position);
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(List<String> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void setCurrentPos(int pos) {
        if (mCheckPos != null)
            mCheckPos.set(pos);
    }

    public void setIsDesc(boolean isDesc) {
        if (mDescFlag != null)
            mDescFlag.set(isDesc);
    }

    public void setOnFilterBoxItemClickListener(OnFilterBoxItemClickListener onFilterBoxItemClickListener) {
        mOnFilterBoxItemClickListener = onFilterBoxItemClickListener;
    }

    public class CommonFilterBoxViewHolder extends RecyclerView.ViewHolder {
        public FilterBoxOrderItemLayoutBinding mViewDataBinding;

        public CommonFilterBoxViewHolder(@NonNull @NotNull FilterBoxOrderItemLayoutBinding filterBoxItemLayoutBinding) {
            super(filterBoxItemLayoutBinding.getRoot());
            mViewDataBinding = filterBoxItemLayoutBinding;
            mViewDataBinding.cbtvTitle.setOnClickListener(v -> {
                if (mCheckPos.get() == mViewDataBinding.getPos()) {
                    setIsDesc(!mDescFlag.get());
                }
                setCurrentPos(mViewDataBinding.getPos());
                if (mOnFilterBoxItemClickListener != null)
                    mOnFilterBoxItemClickListener.OnOrderChange();
            });
        }
    }
}
