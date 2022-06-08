package com.hphtv.movielibrary.ui.filterpage;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.LayoutMovieClassifyFilterBoxBinding;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/12/6
 */
public class FilterBoxDialogFragment extends BaseDialogFragment2<FilterBoxViewModel, LayoutMovieClassifyFilterBoxBinding> implements OnFilterBoxItemClickListener, TvRecyclerView.OnKeyPressListener, TvRecyclerView.OnForceFocusListener {
    private static final String TAG = FilterBoxDialogFragment.class.getSimpleName();

    private FilterBoxAdapter mGenreAdapter, mYearsApdater;
    private FilterBoxVideoTagAdapter mVideoTagAdapter;
    private FilterBoxDeviceAdapter mDeviceAdapter;
    private FilterBoxOrderAdapter mOrderAdapter;
    private FilterPageViewModel mFilterPageViewModel;


    public static FilterBoxDialogFragment newInstance() {
        Bundle args = new Bundle();
        FilterBoxDialogFragment fragment = new FilterBoxDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    protected FilterBoxViewModel createViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(getActivity()).get(FilterBoxViewModel.class);
        }
        if (mFilterPageViewModel == null) {
            mFilterPageViewModel = new ViewModelProvider(getActivity()).get(FilterPageViewModel.class);
            String genre = mFilterPageViewModel.getGenre();
            VideoTag videoTag=mFilterPageViewModel.getVideoTag();
            mViewModel.setPresetGenreName(genre);
            mViewModel.setPresetVideoTag(videoTag);
        }
        return mViewModel;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareDevices();
        prepareGenres();
        prepareTypes();
        prepareYears();
        prepareOrders();
    }

    private void init() {
        mDeviceAdapter = new FilterBoxDeviceAdapter(getContext(), mViewModel.getDevicePos());
        mGenreAdapter = new FilterBoxAdapter(getContext(), mViewModel.getGenresPos());
        mVideoTagAdapter = new FilterBoxVideoTagAdapter(getContext(), mViewModel.getVideoTypePos());
        mYearsApdater = new FilterBoxAdapter(getContext(), mViewModel.getYearPos());
        mOrderAdapter = new FilterBoxOrderAdapter(getContext(), mViewModel.getFilterOrderPos(), mViewModel.getDesFlag());
        mDeviceAdapter.setOnFilterBoxItemClickListener(this);
        mGenreAdapter.setOnFilterBoxItemClickListener(this);
        mYearsApdater.setOnFilterBoxItemClickListener(this);
        mOrderAdapter.setOnFilterBoxItemClickListener(this);
        mVideoTagAdapter.setOnFilterBoxItemClickListener(this);


        prepareRecyclerView(mBinding.viewSortbyDevice, mDeviceAdapter);
        prepareRecyclerView(mBinding.viewSortbyGenres, mGenreAdapter);
        prepareRecyclerView(mBinding.viewSortbyYear, mYearsApdater);
        prepareRecyclerView(mBinding.viewOrder, mOrderAdapter);
        prepareRecyclerView(mBinding.viewSortbyType, mVideoTagAdapter);
    }

    private void prepareRecyclerView(TvRecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnKeyPressListener(this);
        recyclerView.setOnForceFocusListener(this);
    }

    private void prepareDevices() {
        mViewModel.prepareDevices(mDeviceAdapter);
    }

    private void prepareGenres() {
        mViewModel.prepareGenres(mGenreAdapter);
    }

    private void prepareYears() {
        mViewModel.prepareYears(mYearsApdater);
    }

    private void prepareOrders() {
        mViewModel.prepareOrders(mOrderAdapter);
    }

    private void prepareTypes() {
        mViewModel.prepareTypes(mVideoTagAdapter);
    }


    @Override
    public void OnFilterChange() {
        Shortcut shortcut = (Shortcut) mViewModel.getRealShortCut();
        VideoTag videoTag = mViewModel.getRealVideoTag();
        String genre = mViewModel.getRealGenre();
        String year = mViewModel.getRealYear();
        mFilterPageViewModel.onFilterChange(shortcut, videoTag, genre, year);
        mFilterPageViewModel.reloadMoiveDataViews();
    }

    @Override
    public void OnOrderChange() {
        int order = mViewModel.getFilterOrderPos().get();
        boolean isDesc = mViewModel.getDesFlag().get();
        mFilterPageViewModel.onSortByChange(order, isDesc);
        mFilterPageViewModel.reOrderMovieDataViews();
    }

    @Override
    public void processKeyEvent(int keyCode) {
    }

    @Override
    public void onBackPress() {
        dismiss();
    }

    private void requestNextRightFocus(View currentFocusView) {
        int id = ((View) currentFocusView.getParent()).getId();
        switch (id) {
            case R.id.view_sortby_device:
                requestChildFocus(mBinding.viewSortbyType);
                break;
            case R.id.view_sortby_type:
                requestChildFocus(mBinding.viewSortbyGenres);
                break;
            case R.id.view_sortby_genres:
                requestChildFocus(mBinding.viewSortbyYear);
                break;
            case R.id.view_sortby_year:
                requestChildFocus(mBinding.viewOrder);
                break;
        }
    }

    private void requestNextLeftFocus(View currentFocusView) {
        int id = ((View) currentFocusView.getParent()).getId();
        switch (id) {
            case R.id.view_sortby_type:
                requestChildFocus(mBinding.viewSortbyDevice);
                break;
            case R.id.view_sortby_genres:
                requestChildFocus(mBinding.viewSortbyType);
                break;
            case R.id.view_sortby_year:
                requestChildFocus(mBinding.viewSortbyGenres);
                break;
            case R.id.view_order:
                requestChildFocus(mBinding.viewSortbyYear);
                break;
        }
    }

    private void requestChildFocus(TvRecyclerView recyclerView) {
        int viewChildCount = recyclerView.getChildCount();
        for (int i = 0; i < viewChildCount; i++) {
            View childView = recyclerView.getChildAt(i);
            if (childView.isSelected()) {
                childView.requestFocus();
                return;
            }
        }
        int pos= (int) Math.floor(viewChildCount/2);
        recyclerView.getChildAt(pos).requestFocus();
    }


    @Override
    public boolean forceFocusLeft(View currentFocus) {
        requestNextLeftFocus(currentFocus);
        return true;
    }

    @Override
    public boolean forceFocusRight(View currentFocus) {
        requestNextRightFocus(currentFocus);
        return true;
    }

    @Override
    public boolean forceFocusUp(View currentFocus) {
        return false;
    }

    @Override
    public boolean forceFocusDown(View currentFocus) {
        return false;
    }
}
