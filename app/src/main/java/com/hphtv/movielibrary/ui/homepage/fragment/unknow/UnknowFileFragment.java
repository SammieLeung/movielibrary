package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.os.Bundle;
import android.util.Log;
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
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.homepage.fragment.ILoadingState;
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknowFileFragment extends BaseAutofitHeightFragment<UnknowFileViewModel, FragmentUnknowfileBinding> implements ILoadingState {
    public static final String TAG = UnknowFileFragment.class.getSimpleName();
    private UnknowFileItemListAdapter mUnknowsFileItemListAdapter;

    public UnknowFileFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position);
    }

    public static UnknowFileFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        UnknowFileFragment fragment = new UnknowFileFragment(autofitHeight, positon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected UnknowFileViewModel createViewModel() {
        return null;
    }

    @Override
    public void forceRefresh() {
        Log.e(TAG, "forceRefresh");
        reloadUnknownFiles();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated");

        initViews();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
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
                if(mViewModel!=null)
                mViewModel.loadMoreUnknowFiles()
                        .subscribe(new  SimpleObserver<List<UnrecognizedFileDataView>>() {
                            @Override
                            public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViews) {
                                mUnknowsFileItemListAdapter.appendAll(unrecognizedFileDataViews);
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
        mBinding.rvUnknowsfile.setOnKeyPressListener(new TvRecyclerView.OnKeyPressListener() {
            @Override
            public void processKeyEvent(int keyCode) {

            }

            @Override
            public void onBackPress() {
                ActivityNewHomepageBinding binding = getBaseActivity().getBinding();
                int pos = binding.tabLayout.getSelectedTabPosition();
                binding.tabLayout.getTabAt(pos).view.requestFocus();
            }
        });
        mUnknowsFileItemListAdapter.setOnItemClickListener(mActionListener);
        mUnknowsFileItemListAdapter.setOnItemLongClickListener(this::showUnknowsFileMenuDialog);
    }

    /**
     * 显示菜单
     *
     * @param view
     * @param postion
     * @param data
     * @return
     */
    private boolean showUnknowsFileMenuDialog(View view, int postion, UnrecognizedFileDataView data) {
        ActivityHelper.showUnknowsFileMenuDialog(getChildFragmentManager(), postion, data);
        return true;
    }

    private BaseAdapter2.OnRecyclerViewItemActionListener mActionListener = new BaseAdapter2.OnRecyclerViewItemActionListener<UnrecognizedFileDataView>() {
        @Override
        public void onItemClick(View view, int postion, UnrecognizedFileDataView data) {
            mViewModel.playVideo(data.path, data.filename)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String s) {
                            getBaseActivity().refreshFragment(0);//主页刷新历史播放
                            getBaseActivity().refreshFragment(1);//主页刷新历史播放
                            getBaseActivity().refreshFragment(2);//主页刷新历史播放

                        }
                    });
        }

        @Override
        public void onItemFocus(View view, int postion, UnrecognizedFileDataView data) {

        }
    };

    private void reloadUnknownFiles() {
        if (mViewModel != null) {
            mViewModel.reLoadUnknownFiles()
                    .subscribe(new  SimpleObserver<List<UnrecognizedFileDataView>>() {
                        @Override
                        public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViews) {
                            mUnknowsFileItemListAdapter.addAll(unrecognizedFileDataViews);
                        }
                    });
        }

    }

    @Override
    public void startLoading() {
        int i=atomicState.incrementAndGet();
        LogUtil.v(TAG, "startLoading "+i);
        mBinding.setIsLoading(true);
    }

    @Override
    public void finishLoading() {
        if(atomicState.decrementAndGet()<=0) {
            LogUtil.v(TAG, "finishLoading ");
            mBinding.setIsLoading(false);
            atomicState.set(0);
        }
    }
}
