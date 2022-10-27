package com.hphtv.movielibrary.ui.homepage.fragment.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
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
import com.hphtv.movielibrary.ui.filterpage.FilterPageActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomePageViewModel;
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver;
import com.hphtv.movielibrary.ui.homepage.genretag.AddGenreDialogFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;
import com.hphtv.movielibrary.ui.pagination.PaginationActivity;
import com.hphtv.movielibrary.ui.pagination.PaginationViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class HomePageFragment extends BaseHomeFragment<HomeFragmentViewModel> {
    public static final String TAG=HomePageFragment.class.getSimpleName();
    private HomePageFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position,TAG);
    }

    public static HomePageFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        HomePageFragment fragment = new HomePageFragment(autofitHeight, positon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getVideoTagName() {
        return null;
    }
}
