package com.hphtv.movielibrary.ui.homepage.fragment.customtag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemWithMoreListAdapter;
import com.hphtv.movielibrary.databinding.FragmentHomepageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.filterpage.FilterPageActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver;
import com.hphtv.movielibrary.ui.homepage.fragment.unknow.UnknowFileFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.AddGenreDialogFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;
import com.hphtv.movielibrary.ui.pagination.PaginationActivity;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Sam Leung
 * date:  2022/10/26
 */
public class CustomTagFragment extends BaseAutofitHeightFragment<CustomTagViewModel, FragmentHomepageBinding>   implements IRefreshGenre, ILoadingState {
    public static final String TAG=CustomTagFragment.class.getSimpleName();

    private HistoryListAdapter mHistoryListAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private NewMovieItemWithMoreListAdapter mRecentlyAddListAdapter;
    private NewMovieItemWithMoreListAdapter mFavoriteListAdapter;
    private NewMovieItemListAdapter mRecommendListAdapter;

    private List<HistoryMovieDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();
    private List<MovieDataView> mFavoriteList = new ArrayList<>();
    private List<MovieDataView> mRecommandList = new ArrayList<>();

    public AtomicInteger atomicState = new AtomicInteger();

    //电影点击监听
    private BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView> mMovieDataViewEventListener = (view, postion, data) -> mViewModel.startDetailActivity((AppBaseActivity) getActivity(), data);
    //固定主题动作监听
    private GenreTagAdapter.GenreListener mGenreListener = new GenreTagAdapter.GenreListener() {
        @Override
        public void addGenre() {
            AddGenreDialogFragment fragment = AddGenreDialogFragment.newInstance();
            fragment.addAllIRefreshGenreList(getBaseActivity().getAllRefreshGenreList());
            fragment.show(getChildFragmentManager(), AddGenreDialogFragment.TAG);
        }

        @Override
        public void browseAll() {
            Intent intent = new Intent(getContext(), FilterPageActivity.class);
            startActivityForResult(intent);
        }
    };

    //动态主题动作监听
    private BaseAdapter2.OnRecyclerViewItemClickListener mGenreItemClickListener = (view, postion, data) -> {
        Intent intent = new Intent(getContext(), FilterPageActivity.class);
        intent.putExtra(FilterPageActivity.EXTRA_GENRE, data.toString());
        startActivityForResult(intent);
    };

    private BaseAdapter2.OnItemLongClickListener<MovieDataView> mPosterItemLongClickListener = (view, postion, data) -> {
        ActivityHelper.showPosterMenuDialog(getChildFragmentManager(), postion, data);
        return false;
    };

    private NewMovieItemWithMoreListAdapter.OnMoreItemClickListener mOnMoreItemClickListener = type -> {
        LogUtil.v("mOnMoreItemClickListener click " + type);
        Intent intent = new Intent(getBaseActivity(), PaginationActivity.class);
        intent.putExtra(PaginationActivity.EXTRA_PAGE_TYPE, type);
        startActivityForResult(intent);
    };

    //TvRecyclerView的按键处理(按键均被监听)
    private TvRecyclerView.OnBackPressListener mOnBackPressListener = () -> getActivity().finish();


    private CustomTagFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position);
    }

    public static CustomTagFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        CustomTagFragment fragment = new CustomTagFragment(autofitHeight, positon);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected CustomTagViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareHistoryData();
    }

    @Override
    public void startLoading() {

    }

    @Override
    public void finishLoading() {

    }

    @Override
    public void refreshGenreUI() {

    }

    private void initViews() {
        mBinding.btnQuickAddShortcut.setOnClickListener(getBaseActivity()::startShortcutManager);
        initRecentlyPlayedList();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mRecentlyPlayedList);
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
        mHistoryListAdapter.setOnItemClickListener((view, position, data) -> mViewModel.playingVideo(data.path, data.filename, list -> {
            mHistoryListAdapter.addAll(list);
        }));
    }

    /**
     * 读取历史记录数据
     */
    public void prepareHistoryData() {
        mViewModel.prepareHistory()
                .subscribe(new SimpleLoadingObserver<List<HistoryMovieDataView>>(this) {
                    @Override
                    public void onAction(List<HistoryMovieDataView> historyMovieDataViews) {
                        updateRecentlyPlayed(historyMovieDataViews);
//                        prepareRecommand();
                    }
                });
    }

    private void updateRecentlyPlayed(List<HistoryMovieDataView> historyList) {
        if (historyList.size() > 0) {
            mBinding.setHasHistory(true);
            mBinding.rvHistoryList.setVisibility(View.VISIBLE);
            mBinding.tvHistoryEmptyTips.setVisibility(View.GONE);
        } else {
            mBinding.setHasHistory(false);
            mBinding.tvHistoryEmptyTips.setVisibility(View.VISIBLE);
            mBinding.rvHistoryList.setVisibility(View.GONE);
        }
        mHistoryListAdapter.addAll(historyList);
    }
}
