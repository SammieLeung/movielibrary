package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.UnknowFileItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.FragmentUnknowfileBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknownFileFragment extends BaseAutofitHeightFragment<UnknowFileViewModel, FragmentUnknowfileBinding> implements ILoadingState {
    private UnknowFileItemListAdapter mUnknowsFileItemListAdapter;
    public AtomicInteger atomicState = new AtomicInteger();

    public UnknownFileFragment(){
        super(UnknownFileFragment.class.getSimpleName());
    }

    public static UnknownFileFragment newInstance(NoScrollAutofitHeightViewPager viewPager, int position) {
        Bundle args = new Bundle();
        UnknownFileFragment fragment = new UnknownFileFragment();
        args.putInt(BaseAutofitHeightFragment.POSITION,position);
        fragment.setArguments(args);
        fragment.setAutoFitHeightViewPager(viewPager);
        return fragment;
    }

    @Override
    protected UnknowFileViewModel createViewModel() {
        return null;
    }

    @Override
    public void forceRefresh() {
        reloadUnknownFiles();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadUnknownFiles();
    }

    /**
     * 初始化UI
     */
    private void initViews() {
        mUnknowsFileItemListAdapter = new UnknowFileItemListAdapter(getContext(), mViewModel.getUnrecognizedFileDataViewList());
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(getContext(), 5, GridLayoutManager.VERTICAL, false);
        mBinding.rvUnknowsfile.setLayoutManager(gridLayoutManager);
        mBinding.rvUnknowsfile.setAdapter(mUnknowsFileItemListAdapter);
        mBinding.rvUnknowsfile.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                if (mViewModel != null)
                    mViewModel.loadMoreUnknowFiles()
                            .subscribe(new SimpleObserver<List<ConnectedFileDataView>>() {
                                @Override
                                public void onAction(List<ConnectedFileDataView> connectedFileDataViews) {
                                    mUnknowsFileItemListAdapter.appendAll(connectedFileDataViews);
                                }
                            });
            }
        });
        mBinding.rvUnknowsfile.addItemDecoration(new GridSpacingItemDecorationVertical(
                getResources().getDimensionPixelSize(R.dimen.poster_item_1_w),
                DensityUtil.dip2px(getContext(), 43),
                DensityUtil.dip2px(getContext(), 27),
                DensityUtil.dip2px(getContext(), 40),
                DensityUtil.dip2px(getContext(), 28),
                5)
        );
        mBinding.rvUnknowsfile.setOnBackPressListener(new TvRecyclerView.OnBackPressListener() {

            @Override
            public void onBackPress() {
                ActivityNewHomepageBinding binding = getBaseActivity().getBinding();
                int pos = binding.tabLayout.getSelectedTabPosition();
                binding.tabLayout.getTabAt(pos).view.requestFocus();
            }
        });
        mUnknowsFileItemListAdapter.setOnItemClickListener(mActionListener);
        mUnknowsFileItemListAdapter.setOnItemLongClickListener(this::showUnknownsFileMenuDialog);
    }

    /**
     * 显示菜单
     *
     * @param view
     * @param position
     * @param data
     * @return
     */
    private boolean showUnknownsFileMenuDialog(View view, int position, ConnectedFileDataView data) {
        ActivityHelper.showUnknownsFileMenuDialog(getChildFragmentManager(), position, data);
        return true;
    }

    private BaseAdapter2.OnRecyclerViewItemClickListener mActionListener = new BaseAdapter2.OnRecyclerViewItemClickListener<ConnectedFileDataView>() {
        @Override
        public void onItemClick(View view, int position, ConnectedFileDataView data) {
            mViewModel.playVideo(data.path, data.filename)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mViewModel.setPlayingVideo(true);
                            super.onSubscribe(d);
                            getBaseActivity().startLoading();
                        }

                        @Override
                        public void onAction(String s) {
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            getBaseActivity().stopLoading();
                        }
                    });
        }
    };

    private void reloadUnknownFiles() {
        if (mViewModel.isPlayingVideo()) {
            mViewModel.setPlayingVideo(false);
            return;
        }
        if (mViewModel != null) {
            mViewModel.reLoadUnknownFiles()
                    .subscribe(new SimpleLoadingObserver<List<ConnectedFileDataView>>(this) {
                        @Override
                        public void onAction(List<ConnectedFileDataView> connectedFileDataViews) {
                            if(connectedFileDataViews.size()>0) {
                                mBinding.setIsEmpty(false);
                                mUnknowsFileItemListAdapter.addAll(connectedFileDataViews);
                            }else{
                                mBinding.setIsEmpty(true);
                                mUnknowsFileItemListAdapter.addAll(connectedFileDataViews);
                            }
                        }
                    });
        }

    }

    @Override
    public void startLoading() {
        int i = atomicState.incrementAndGet();
        mBinding.setIsLoading(true);
    }

    @Override
    public void finishLoading() {
        int i = atomicState.decrementAndGet();
        if (i <= 0) {
            mBinding.setIsLoading(false);
            atomicState.set(0);
        }
    }
}
