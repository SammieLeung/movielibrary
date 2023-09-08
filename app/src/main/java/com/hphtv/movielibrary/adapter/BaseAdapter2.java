package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.util.ViewHolderCreator;
import com.station.kit.view.mvvm.ViewDataBindingHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class BaseAdapter2<VDB extends ViewDataBinding, VH extends BaseAdapter2.ViewHolder, T> extends RecyclerView.Adapter<VH> implements View.OnClickListener, View.OnKeyListener, View.OnLongClickListener {
    protected Context mContext;
    protected List<T> mList;

    public BaseAdapter2(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        VDB binding = ViewDataBindingHelper.inflateVDB(mContext, parent, this.getClass());
        VH viewHolder = null;
        try {
            Constructor<VH>[] constructors = ViewHolderCreator.getViewHolderConstructors(this.getClass());
            viewHolder = (VH) constructors[0].newInstance(this, binding);
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
        if (mOnItemClickListener != null && v.getTag() != null) {
            int position = (int) v.getTag();
            T data = mList.get(position);
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, position, data);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BUTTON_START) {
                if (v.getTag() != null) {
                    int pos = (int) v.getTag();
                    if (mOnItemLongClickListener != null)
                        mOnItemLongClickListener.onItemLongClick(v, pos, mList.get(pos));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null && v.getTag() != null) {
            int position = (int) v.getTag();
            T data = mList.get(position);
            return mOnItemLongClickListener.onItemLongClick(v, position, data);
        }
        return false;
    }

    public void addAll(@NonNull @NotNull List<? extends T> data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void remove(T data, int position) {
        T _data = mList.get(position);
        if (data.equals(_data)) {
            mList.remove(data);
            notifyItemRemoved(position);
        }
    }

    public void insert(T data, int position) {
        mList.add(position, data);
        notifyItemInserted(position);
    }

    public void replace(T data, int position) {
        mList.set(position, data);
        notifyItemChanged(position);
    }

    public void appendAll(List data) {
        int oldSize = mList.size();
        mList.addAll(data);
        int newSize = mList.size();
        notifyItemRangeInserted(oldSize, newSize);
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

    public List<T> getDatas() {
        return mList;
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


    protected OnRecyclerViewItemClickListener<T> mOnItemClickListener = null;
    protected OnRecyclerViewItemFocusListener<T> mOnItemFocusListener = null;
    protected OnItemLongClickListener<T> mOnItemLongClickListener;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<T> listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemFocusListener(OnRecyclerViewItemFocusListener<T> listener) {
        this.mOnItemFocusListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public interface OnRecyclerViewItemFocusListener<T> {
        void onItemFocus(View view, int position, T data);
    }

    public interface OnRecyclerViewItemClickListener<T> {
        void onItemClick(View view, int position, T data);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(View view, int postion, T data);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding mBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(BaseAdapter2.this);
            mBinding.getRoot().setOnLongClickListener(BaseAdapter2.this);
            mBinding.getRoot().setOnKeyListener(BaseAdapter2.this);
        }
    }
}
