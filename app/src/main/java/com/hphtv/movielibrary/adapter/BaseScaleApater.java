package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.data.Constants;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public abstract class BaseScaleApater<VDB extends ViewDataBinding, VH extends BaseAdapter2.ViewHolder, T> extends BaseAdapter2<VDB,VH,T> implements View.OnFocusChangeListener , View.OnHoverListener {
    protected float mZoomRatio = 1.15f;

    public BaseScaleApater(Context context, List<T> list) {
        super(context,list);
        mContext = context;
        mList = list;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
            if(mOnItemClickListener!=null) {
                int pos= (int) v.getTag();
                T data= mList.get(pos);
                mOnItemClickListener.onItemFocus(v, pos,data);
            }
        } else {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
            if(mOnItemClickListener!=null) {
                int pos= (int) v.getTag();
                T data= mList.get(pos);
                mOnItemClickListener.onItemFocus(v, pos,data);
            }
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
        return false;
    }

    public class ViewHolder extends BaseAdapter2.ViewHolder {

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
            mBinding.getRoot().setOnFocusChangeListener(BaseScaleApater.this);
            mBinding.getRoot().setOnHoverListener(BaseScaleApater.this);
        }
    }

    public void setZoomRatio(float zoomRatio){
        mZoomRatio = zoomRatio;
    }
}
