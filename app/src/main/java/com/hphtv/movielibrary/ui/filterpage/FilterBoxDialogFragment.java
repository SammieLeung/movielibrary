package com.hphtv.movielibrary.ui.filterpage;

import android.os.Bundle;
import android.view.Gravity;
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
public class FilterBoxDialogFragment extends BaseDialogFragment2<FilterBoxViewModel, LayoutMovieClassifyFilterBoxBinding> implements OnFilterBoxItemClickListener, TvRecyclerView.OnBackPressListener, TvRecyclerView.OnNoNextFocusListener {
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
            VideoTag videoTag = mFilterPageViewModel.getVideoTag();
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
        recyclerView.setOnBackPressListener(this);
        recyclerView.setOnNoNextFocusListener(this);
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
        mFilterPageViewModel.reloadMovieDataViews();
    }

    @Override
    public void OnOrderChange() {
        int order = mViewModel.getFilterOrderPos().get();
        boolean isDesc = mViewModel.getDesFlag().get();
        mFilterPageViewModel.onSortByChange(order, isDesc);
        mFilterPageViewModel.reOrderMovieDataViews();
    }

    @Override
    public void onBackPress() {
        dismiss();
    }

    /**
     * 让当前获取焦点的View所在列的右列中适合获取焦点的view获取到焦点
     *
     * @param currentFocusView
     */
    private boolean requestNextRightFocus(View currentFocusView) {
        int id = ((View) currentFocusView.getParent()).getId();
        switch (id) {
            case R.id.view_sortby_device:
                return requestChildFocus(mBinding.viewSortbyType);
            case R.id.view_sortby_type:
                return requestChildFocus(mBinding.viewSortbyGenres);
            case R.id.view_sortby_genres:
                return requestChildFocus(mBinding.viewSortbyYear);
            case R.id.view_sortby_year:
                return requestChildFocus(mBinding.viewOrder);
        }
        return false;
    }

    /**
     * 让当前获取焦点的View所在列的左列中适合获取焦点的view获取到焦点
     *
     * @param currentFocusView
     */
    private boolean requestNextLeftFocus(View currentFocusView) {
        int id = ((View) currentFocusView.getParent()).getId();
        switch (id) {
            case R.id.view_sortby_type:
                return requestChildFocus(mBinding.viewSortbyDevice);
            case R.id.view_sortby_genres:
                return requestChildFocus(mBinding.viewSortbyType);
            case R.id.view_sortby_year:
                return requestChildFocus(mBinding.viewSortbyGenres);
            case R.id.view_order:
                return requestChildFocus(mBinding.viewSortbyYear);
        }
        return false;
    }

    private boolean requestChildFocus(TvRecyclerView recyclerView) {
        int viewChildCount = recyclerView.getChildCount();
        for (int i = 0; i < viewChildCount; i++) {
            View childView = recyclerView.getChildAt(i);
            if (childView.isSelected()) {
                childView.requestFocus();
                return true;
            }
        }
        int pos = (int) Math.floor(viewChildCount / 2);
        View view = recyclerView.getChildAt(pos);
        if (view.isFocusable()) {
            view.requestFocus();
            return true;
        }
        return false;
    }


    @Override
    public boolean forceFocusLeft(View currentFocus) {
       return requestNextLeftFocus(currentFocus);
    }

    @Override
    public boolean forceFocusRight(View currentFocus) {
        return requestNextRightFocus(currentFocus);
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
