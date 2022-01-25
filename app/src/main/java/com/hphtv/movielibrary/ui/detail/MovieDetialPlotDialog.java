package com.hphtv.movielibrary.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.ActorPosterItemListApdater;
import com.hphtv.movielibrary.databinding.LayoutNewDetailViewmoreBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * author: Sam Leung
 * date:  2022/1/19
 */
public class MovieDetialPlotDialog extends BaseDialogFragment2<MovieDetailViewModel, LayoutNewDetailViewmoreBinding> {
    WeakReference<View> mViewWeakReference;
    Handler mHandler=new Handler(Looper.getMainLooper());
    private ActorPosterItemListApdater mActorPosterItemListApdater;
    int count=0;
    public static MovieDetialPlotDialog newInstance(View view) {
        Bundle args = new Bundle();
        MovieDetialPlotDialog fragment = new MovieDetialPlotDialog();
        fragment.mViewWeakReference=new WeakReference<>(view);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected MovieDetailViewModel createViewModel() {
        return mViewModel=new ViewModelProvider(getActivity()).get(MovieDetailViewModel.class);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, Bundle savedInstanceState) {
        View view= super.onCreateView(inflater, container, savedInstanceState);
        mActorPosterItemListApdater=new ActorPosterItemListApdater(getContext(),mViewModel.getMovieWrapper().actors);
        mBinding.rvActorList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));
        mBinding.rvActorList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(),62),DensityUtil.dip2px(getContext(),22)));
        mBinding.rvActorList.setAdapter(mActorPosterItemListApdater);
        mBinding.setPlot(mViewModel.getMovieWrapper().movie.plot);
        mBinding.btnFold.setOnClickListener(v->dismiss());
        mViewModel.loadFileList().subscribe(new SimpleObserver<String>() {
            @Override
            public void onAction(String s) {
                mBinding.setFilelist(s);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable,0);
    }
    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            mBinding.ivFastblur.refreshBG(mViewWeakReference.get());
            if(count++>=5)
            mHandler.postDelayed(mRunnable,10);
        }
    };

}
