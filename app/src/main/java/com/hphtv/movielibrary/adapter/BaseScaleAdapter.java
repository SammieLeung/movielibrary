package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.data.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public abstract class BaseScaleAdapter<VDB extends ViewDataBinding, T> extends BaseAdapter2<VDB, BaseScaleAdapter.ViewHolder, T> implements View.OnFocusChangeListener,View.OnHoverListener {
    protected float mZoomRatio = 1.15f;

    public BaseScaleAdapter(Context context, List<T> list) {
        super(context, list);
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public BaseScaleAdapter<VDB, T>.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
            if (mOnItemFocusListener != null) {
                int pos = (int) v.getTag();
                if (getItemCount() == 0)
                    return;
                if (pos < 0 || pos >= getItemCount())
                    pos = 0;
                T data = mList.get(pos);
                mOnItemFocusListener.onItemFocus(v, pos, data);
            }
        } else {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            if (mOnItemFocusListener != null) {
                int pos = (int) v.getTag();
                if (getItemCount() == 0)
                    return false;
                if (pos < 0 || pos >= getItemCount())
                    pos = 0;
                T data = mList.get(pos);
                mOnItemFocusListener.onItemFocus(v, pos, data);
            }
        }
        return false;
    }

    public class ViewHolder extends BaseAdapter2.ViewHolder {

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
            mBinding.getRoot().setOnFocusChangeListener(BaseScaleAdapter.this);
            mBinding.getRoot().setOnHoverListener(BaseScaleAdapter.this);
        }
    }

    public void setZoomRatio(float zoomRatio) {
        mZoomRatio = zoomRatio;
    }
}
