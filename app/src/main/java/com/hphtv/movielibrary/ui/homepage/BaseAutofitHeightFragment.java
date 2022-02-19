package com.hphtv.movielibrary.ui.homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public abstract class BaseAutofitHeightFragment<VM extends AndroidViewModel, VDB extends ViewDataBinding>  extends BaseFragment2<VM, VDB> implements IAutofitHeight{
    private IAutofitHeight mIAutofitHeight;
    private int mPostion;
    public BaseAutofitHeightFragment(IAutofitHeight autofitHeight,int position){
        mIAutofitHeight=autofitHeight;
        mPostion=position;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        int pos = mPostion;
        getAutofitHeightViewPager().setViewPosition(view, pos);
        return view;
    }

    @Override
    public NoScrollAutofitHeightViewPager getAutofitHeightViewPager() {
        return mIAutofitHeight.getAutofitHeightViewPager();
    }
}
