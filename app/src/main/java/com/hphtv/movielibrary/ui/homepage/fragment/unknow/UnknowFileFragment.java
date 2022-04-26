package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.FragmentUnknowfileBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.postermenu.UnknownsFileMenuDialog;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknowFileFragment extends BaseAutofitHeightFragment<UnknowFileViewModel, FragmentUnknowfileBinding> {
    public static final String TAG=UnknowFileFragment.class.getSimpleName();
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
        mViewModel.reLoadUnknowFiles();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        mViewModel.reLoadUnknowFiles();
    }

    /**
     * 初始化UI
     */
    private void initViews() {
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(getContext(), 5, GridLayoutManager.VERTICAL, false);
        mBinding.rvUnknowsfile.setLayoutManager(gridLayoutManager);
        mBinding.rvUnknowsfile.setAdapter(mViewModel.getUnknownsFileItemListAdapter());
        mBinding.rvUnknowsfile.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                mViewModel.loadMoreUnknowFiles();
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
                int pos = binding.tablayout.getSelectedTabPosition();
                binding.tablayout.getTabAt(pos).view.requestFocus();
            }
        });
        mViewModel.getUnknownsFileItemListAdapter().setOnItemClickListener(mActionListener);
        mViewModel.getUnknownsFileItemListAdapter().setOnItemLongClickListener(this::showUnknowsFileMenuDialog);
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

    private BaseApater2.OnRecyclerViewItemActionListener mActionListener = new BaseApater2.OnRecyclerViewItemActionListener<UnrecognizedFileDataView>() {
        @Override
        public void onItemClick(View view, int postion, UnrecognizedFileDataView data) {
            mViewModel.playVideo(data.path, data.filename)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String s) {
                            getBaseActivity().refreshFragment(0);//主页刷新历史播放
                        }
                    });
        }

        @Override
        public void onItemFocus(View view, int postion, UnrecognizedFileDataView data) {

        }
    };

}
