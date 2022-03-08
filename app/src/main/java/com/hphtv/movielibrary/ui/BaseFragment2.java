package com.hphtv.movielibrary.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;

import com.station.kit.view.mvvm.ViewDataBindingHelper;
import com.station.kit.view.mvvm.ViewModelHelper;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/8/31
 */
public abstract class BaseFragment2<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends Fragment {
    protected VDB mBinding;
    protected VM mViewModel;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel=createViewModel();
        if (mViewModel==null)
            mViewModel = ViewModelHelper.createAndroidViewModel(this, this.getClass());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = ViewDataBindingHelper.inflateVDB(getContext(), this.getClass());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setLifecycleOwner(this);
    }

    protected abstract VM createViewModel();

}
