package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.station.kit.view.mvvm.ViewDataBindingHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class BaseApater2<VDB extends ViewDataBinding, VH extends BaseApater2.ViewHolder, T> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    protected Context mContext;
    protected List<T> mList;

    public BaseApater2(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        VDB binding = ViewDataBindingHelper.inflateVDB(mContext, this.getClass());
        VH viewHolder = null;
        try {
            Type superclass = getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = null;
            if (superclass instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) superclass;
                Type[] typeArray = parameterizedType.getActualTypeArguments();
                if (typeArray != null && typeArray.length > 0) {
                    Class Class_VH = (Class) typeArray[1];
                    Constructor[] constructors = (Constructor[]) Class_VH.getDeclaredConstructors();
                    viewHolder = (VH) constructors[0].newInstance(this, binding);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull VH holder, int position) {
        holder.mBinding.getRoot().setTag(position);
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            int postion = (int) v.getTag();
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, postion, mList.get(postion));
        }
    }

    public void addAll(List data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void appendAll(List data) {
        int oldSize = mList.size();
        mList.addAll(data);
        int newSize = mList.size();
        notifyItemRangeInserted(oldSize,newSize);
    }

    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void put(T item) {
        if (!mList.contains(item)) {
            mList.add(item);
            notifyItemInserted(mList.size() - 1);
        }
    }

    /**
     * 添加数据
     *
     * @param data
     * @param position
     */
    public void add(T data, int position) {
        mList.add(position, data);
        notifyItemInserted(position); // Attention!
    }

    public void add(T data) {
        mList.add(data);
        notifyItemInserted(mList.size() - 1);
    }


    protected OnRecyclerViewItemActionListener<T> mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemActionListener<T> listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemActionListener<T> {
        void onItemClick(View view, int postion, T data);
        void onItemFocus(View view,int postion,T data);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding mBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(BaseApater2.this::onClick);
        }
    }
}
