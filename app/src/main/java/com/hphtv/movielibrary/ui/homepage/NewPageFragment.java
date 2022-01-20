package com.hphtv.movielibrary.ui.homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.CircleItemAdapter;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityNewpageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class NewPageFragment extends BaseAutofitHeightFragment<NewpageViewModel, ActivityNewpageBinding> {
    private HistoryListAdapter mHistoryListAdapter;
    private CircleItemAdapter mCircleItemAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private NewMovieItemListAdapter mRecentlyAddListAdapter;
    private List<HistoryMovieDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();

    public NewPageFragment(NewHomePageActivity activity) {
        super(activity);
    }

    public static NewPageFragment newInstance(NewHomePageActivity activity, int positon) {

        Bundle args = new Bundle();
        args.putInt(NoScrollAutofitHeightViewPager.POSITION, positon);
        NewPageFragment fragment = new NewPageFragment(activity);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        int pos = getArguments().getInt(NoScrollAutofitHeightViewPager.POSITION);
        getViewPager().setViewPosition(view, pos);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    protected boolean createViewModel() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareHistoryData();
        prepareMovieGenreTagData();
        prepareRecentlyAddedMovie();
    }

    private void initViews() {
        initRecentlyPlayedList();
//        initCategoryList();
        initGenreList();
        initRecentlyAddedList();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(),75),DensityUtil.dip2px(getContext(),15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mRecentlyPlayedList);
//        mHistoryListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<UnrecognizedFileDataView>() {
//
//            @Override
//            public void onItemClick(View view, int postion, UnrecognizedFileDataView data) {
//                mViewModel.playingVideo(data.path, data.filename, list -> {
//                    updateRecentlyPlayed((List<UnrecognizedFileDataView>) list);
////                notifyStopLoading();
//                });
//            }
//
//            @Override
//            public void onItemFocus(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    mBinding.scrollView.smoothScrollTo(0, 0);
//                }
//            }
//        });
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
    }


    /**
     * 初始化分类列表
     */
//    private void initCategoryList() {
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
//        mBinding.rvCategoryList.setLayoutManager(mLayoutManager);
//        List<String> categoryList = Arrays.asList(getResources().getStringArray(R.array.category_title_array).clone());
//        mCircleItemAdapter = new CircleItemAdapter(getContext(), categoryList);
//        mCircleItemAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<String>() {
//            @Override
//            public void onItemClick(View view, int postion, String data) {
//
//            }
//
//            @Override
//            public void onItemFocus(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    mBinding.scrollView.smoothScrollTo(0, 200);
//                }
//            }
//        });
//        mBinding.rvCategoryList.setAdapter(mCircleItemAdapter);
//    }


    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mGenreTagAdapter = new GenreTagAdapter(getContext(), mGenreTagList);
//        mGenreTagAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<String>() {
//            @Override
//            public void onItemClick(View view, int postion, String data) {
//                if(postion==mGenreTagAdapter.getItemCount()-1){
//
//                }
//            }
//
//            @Override
//            public void onItemFocus(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    mBinding.scrollView.smoothScrollTo(0, 400);
//                }
//            }
//        });

        mBinding.rvGenreList.setAdapter(mGenreTagAdapter);

    }

    /**
     * 初始化最新添加列表
     */
    private void initRecentlyAddedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecentlyAdded.setLayoutManager(mLayoutManager);
        mRecentlyAddListAdapter = new NewMovieItemListAdapter(getContext(), mRecentlyAddedList);
//        mRecentlyAddListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<MovieDataView>() {
//
//            @Override
//            public void onItemClick(View view, int postion, MovieDataView data) {
//
//            }
//
//            @Override
//            public void onItemFocus(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    mBinding.scrollView.smoothScrollTo(0, 600);
//                }
//            }
//        });
        mBinding.rvRecentlyAdded.setAdapter(mRecentlyAddListAdapter);

    }


    /**
     * 读取历史记录数据
     */
    public void prepareHistoryData() {
        mViewModel.prepareHistory(list -> updateRecentlyPlayed((List<HistoryMovieDataView>) list));
    }

    private void updateRecentlyPlayed(List<HistoryMovieDataView> historyList) {
        if (historyList.size() > 0) {
            mBinding.rvHistoryList.setVisibility(View.VISIBLE);
            mBinding.tvHistoryEmptyTips.setVisibility(View.GONE);
        } else {
            mBinding.tvHistoryEmptyTips.setVisibility(View.VISIBLE);
            mBinding.rvHistoryList.setVisibility(View.GONE);
        }
        mHistoryListAdapter.addAll(historyList);
    }

    private void prepareMovieGenreTagData() {
        mViewModel.prepareGenreList(list -> mGenreTagAdapter.addAll(list));
    }

    private void prepareRecentlyAddedMovie() {
        mViewModel.prepareRecentlyAddedMovie(list -> {
            mRecentlyAddListAdapter.addAll(list);
        });
    }
}
