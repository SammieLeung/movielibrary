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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public abstract class BaseAdapter2<VDB extends ViewDataBinding, VH extends BaseAdapter2.ViewHolder, T> extends RecyclerView.Adapter<VH> implements View.OnClickListener, View.OnFocusChangeListener , View.OnHoverListener {
    public static final float ZOOM_RATIO = 1.05f;
    protected static int POSTION = 1;
    protected Context mContext;
    protected List<T> mList;

    public BaseAdapter2(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        VDB binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), getBaseItemLayoutId(), parent, false);
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


    protected abstract int getBaseItemLayoutId();

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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemFocus(v, hasFocus);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            ViewCompat.animate((View) v).scaleX(ZOOM_RATIO).scaleY(ZOOM_RATIO).translationZ(1).start();
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).start();
        }
        return false;
    }

    public void addAll(List data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void removeAll() {
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
    public void addItem(T data, int position) {
        mList.add(position, data);
        notifyItemInserted(position); // Attention!
    }


    private OnRecyclerViewItemActionListener<T> mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemActionListener<T> listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemActionListener<T> {
        void onItemClick(View view, int postion, T data);

        void onItemFocus(View view, boolean hasFocus);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding mBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(BaseAdapter2.this::onClick);
            mBinding.getRoot().setOnFocusChangeListener(BaseAdapter2.this::onFocusChange);
            mBinding.getRoot().setOnHoverListener(BaseAdapter2.this::onHover);
        }

    }
}
