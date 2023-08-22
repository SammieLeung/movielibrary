package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.IRemoteRefresh;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public abstract class BaseAutofitHeightFragment<VM extends ViewModel, VDB extends ViewDataBinding> extends BaseFragment2<VM, VDB> implements IActivityResult, IRemoteRefresh {
    public static String TAG;
    public static final String AUTO_FIT_HEIGHT = "arg_autofit";
    public static final String POSITION = "pos";
    private int mPosition;
    protected WeakReference<NoScrollAutofitHeightViewPager> mViewPagerWeakReference;


    public BaseAutofitHeightFragment() {

    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        int pos = mPosition;
        if (mViewPagerWeakReference != null) {
            mViewPagerWeakReference.get().setViewPosition(view, pos);
            mViewPagerWeakReference = null;
            Logger.d("onCreateView:  getWeakRefence");
        } else {
            Logger.d("onCreateView:  getBaseActivity.viewpager");
            getBaseActivity().getBinding().viewpager.setViewPosition(view, pos);
        }
        return view;
    }

    @Override
    public void startActivityForResult(Intent data) {
        if (getActivity() instanceof AppBaseActivity)
            ((AppBaseActivity) getActivity()).startActivityForResult(data);
    }

    @Override
    public void forceRefresh() {

    }

    @Override
    public void remoteUpdateMovieNotify(long o_id, long n_id) {

    }

    @Override
    public void remoteRemoveMovieNotify(String movie_id, String type) {

    }

    public void setAutoFitHeightViewPager(NoScrollAutofitHeightViewPager viewPager) {
        mViewPagerWeakReference = new WeakReference<>(viewPager);
    }

    public HomePageActivity getBaseActivity() {
        return (HomePageActivity) getActivity();
    }

}
