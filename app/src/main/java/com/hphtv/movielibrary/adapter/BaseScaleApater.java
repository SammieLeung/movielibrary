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
public abstract class BaseScaleApater<VDB extends ViewDataBinding, VH extends BaseApater2.ViewHolder, T> extends BaseApater2<VDB,VH,T> implements View.OnFocusChangeListener , View.OnHoverListener {
    public static float ZOOM_RATIO = 1.05f;

    public BaseScaleApater(Context context, List<T> list) {
        super(context,list);
        mContext = context;
        mList = list;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate((View) v).scaleX(ZOOM_RATIO).scaleY(ZOOM_RATIO).translationZ(1).setDuration(200).start();
        } else {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(200).start();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            ViewCompat.animate((View) v).scaleX(ZOOM_RATIO).scaleY(ZOOM_RATIO).translationZ(1).setDuration(200).start();
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(200).start();
        }
        return false;
    }

    public class ViewHolder extends BaseApater2.ViewHolder {

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
            mBinding.getRoot().setOnFocusChangeListener(BaseScaleApater.this::onFocusChange);
            mBinding.getRoot().setOnHoverListener(BaseScaleApater.this::onHover);
        }

    }
}
