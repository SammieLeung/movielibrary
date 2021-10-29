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
public abstract class BaseAdapter<VDB extends ViewDataBinding,VH extends BaseAdapter.ViewHolder,T> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    public static final float SCALE_SIZE = 1.2f;
    protected Context mContext;
    protected List<T> mList;

    public BaseAdapter(Context context,List<T> list) {
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
                    viewHolder= (VH) constructors[0].newInstance(this,binding);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
            //获取焦点时变化
            if (hasFocus) {
                ViewCompat.animate((View) v).scaleX(SCALE_SIZE).scaleY(SCALE_SIZE).translationZ(1).start();
            } else {
                ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).start();
            }
        });
        binding.getRoot().setOnHoverListener((v, event) -> {
            //获取焦点时变化
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                ViewCompat.animate((View) v).scaleX(SCALE_SIZE).scaleY(SCALE_SIZE).translationZ(1).start();
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).start();
            }
            return false;
        });
        binding.getRoot().setOnClickListener(this);
        return viewHolder;
    }

    protected abstract int getBaseItemLayoutId();

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (T) v.getTag());
        }
    }

    public void addAll(List data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }
    public void removeAll(){
        mList.clear();
        notifyDataSetChanged();
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


    private OnRecyclerViewItemClickListener<T> mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<T> listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemClickListener<T> {
        void onItemClick(View view, T data);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public VDB mBinding;

        public ViewHolder(VDB binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
