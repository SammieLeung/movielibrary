package com.hphtv.movielibrary.ui.filterpage;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FilterBoxItemLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/7
 */
public class FilterBoxAdapter extends RecyclerView.Adapter<FilterBoxAdapter.CommonFilterBoxViewHolder> {
    public static final int VIEWTYPE_ALL = 1;
    public static final int VIEWTYPE_NORMAL = 2;
    private Context mContext;
    private List<String> mDataList = new ArrayList<>();
    private ObservableInt mCheckPos;
    private OnFilterBoxItemClickListener mOnFilterBoxItemClickListener;

    public FilterBoxAdapter(Context context, ObservableInt checkPos) {
        mContext = context;
        mCheckPos = checkPos;
    }


    @NonNull
    @NotNull
    @Override
    public CommonFilterBoxViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        FilterBoxItemLayoutBinding itemLayoutBinding = FilterBoxItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        itemLayoutBinding.setCheckPos(mCheckPos);
        CommonFilterBoxViewHolder commonFilterBoxViewHolder = new CommonFilterBoxViewHolder(itemLayoutBinding);
        return commonFilterBoxViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonFilterBoxViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEWTYPE_ALL) {
            holder.mViewDataBinding.setName(mContext.getResources().getString(R.string.all));
            holder.mViewDataBinding.setPos(position);
        } else {
            int realPos = position - 1;
            String name = mDataList.get(realPos);
            holder.mViewDataBinding.setName(name);
            holder.mViewDataBinding.setPos(position);
        }
    }


    @Override
    public int getItemCount() {
        return mDataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEWTYPE_ALL;
        return VIEWTYPE_NORMAL;
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

    public void setCheckValue(String value){
        if(!TextUtils.isEmpty(value)){
            int pos=mDataList.indexOf(value)+1;
            mCheckPos.set(pos);
        }
    }

    public void setOnFilterBoxItemClickListener(OnFilterBoxItemClickListener onFilterBoxItemClickListener) {
        mOnFilterBoxItemClickListener = onFilterBoxItemClickListener;
    }

    public class CommonFilterBoxViewHolder extends RecyclerView.ViewHolder {
        public FilterBoxItemLayoutBinding mViewDataBinding;

        public CommonFilterBoxViewHolder(@NonNull @NotNull FilterBoxItemLayoutBinding filterBoxItemLayoutBinding) {
            super(filterBoxItemLayoutBinding.getRoot());
            mViewDataBinding = filterBoxItemLayoutBinding;
            mViewDataBinding.viewGroup.setOnClickListener(v -> {
                setCurrentPos(mViewDataBinding.getPos());
                if (mOnFilterBoxItemClickListener != null)
                    mOnFilterBoxItemClickListener.OnFilterChange();
            });
        }
    }
}
