package com.hphtv.movielibrary.ui.homepage.filterbox;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.homepage.HomePageActivity;
import com.hphtv.movielibrary.databinding.LayoutMovieClassifyFilterBoxBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment;
import com.hphtv.movielibrary.ui.homepage.HomePageFragementViewModel;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/6
 */
public class FilterBoxDialogFragment extends BaseDialogFragment<FilterBoxViewModel, LayoutMovieClassifyFilterBoxBinding> implements OnFilterBoxItemClickListener {
    private static final String TAG = FilterBoxDialogFragment.class.getSimpleName();
    public static final String KEY_DEVICE_POS = "device_pos";
    public static final String KEY_YEAR_POS = "year_pos";
    public static final String KEY_GENRES_POS = "genre_pos";
    public static final String KEY_FILTER_ORDER_POS = "filter_order_pos";

    private FilterBoxAdapter mGenreAdapter, mYearsApdater;
    private FilterBoxDeviceAdapter mDeviceAdapter;
    private FilterBoxOrderAdapter mOrderAdapter;
    private HomePageFragementViewModel mHomePageFragementViewModel;


    public static FilterBoxDialogFragment newInstance() {
        Bundle args = new Bundle();
        FilterBoxDialogFragment fragment = new FilterBoxDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void createAndroidViewModel() {
        if(mViewModel==null){
            mViewModel=new ViewModelProvider(getActivity()).get(FilterBoxViewModel.class);
        }
        if(mHomePageFragementViewModel==null){
            mHomePageFragementViewModel=new ViewModelProvider(getActivity()).get(HomePageFragementViewModel.class);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_movie_classify_filter_box;
    }

    @Override
    protected void onViewCreated() {
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareDevices();
        prepareGenres();
        prepareYears();
        prepareOrders();

    }

    private void init() {
        mDeviceAdapter = new FilterBoxDeviceAdapter(getContext(),mViewModel.getDevicePos());
        mGenreAdapter = new FilterBoxAdapter(getContext(),mViewModel.getGenresPos());
        mYearsApdater = new FilterBoxAdapter(getContext(),mViewModel.getYearPos());
        mOrderAdapter=new   FilterBoxOrderAdapter(getContext(),mViewModel.getFilterOrderPos(),mViewModel.getDesFlag());
        mDeviceAdapter.setOnFilterBoxItemClickListener(this);
        mGenreAdapter.setOnFilterBoxItemClickListener(this);
        mYearsApdater.setOnFilterBoxItemClickListener(this);
        mOrderAdapter.setOnFilterBoxItemClickListener(this);
        prepareRecyclerView(mBinding.viewSortbyDevice, mDeviceAdapter);
        prepareRecyclerView(mBinding.viewSortbyGenres, mGenreAdapter);
        prepareRecyclerView(mBinding.viewSortbyYear, mYearsApdater);
        prepareRecyclerView(mBinding.viewOrder,mOrderAdapter);
    }

    private void prepareRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void prepareDevices() {
        mViewModel.prepareDevices(args -> {
            mDeviceAdapter.addAll((List<Object>) args[0]);
        });
    }

    private void prepareGenres() {
        mViewModel.prepareGenres(args -> {
            mGenreAdapter.addAll((List<String>) args[0]);
        });
    }

    private void prepareYears() {
        mViewModel.prepareYears(args -> {
            mYearsApdater.addAll((List<String>) args[0]);
        });
    }

    private void prepareOrders(){
        mViewModel.prepareOrders(args -> {
            mOrderAdapter.addAll((List<String>) args[0]);
        });
    }

    @Override
    public void OnFilterChange() {
        if(mHomePageFragementViewModel!=null){
//            mHomePageFragementViewModel.prepareMovies(mViewModel.getRealShortCut(), mViewModel.getRealYear(), mViewModel.getRealGenre(), mViewModel.getFilterOrderPos().get(), mViewModel.getDesFlag().get(), new HomePageFragementViewModel.Callback() {
//                @Override
//                public void runOnUIThread(Object... args) {
//
//                }
//            });
        }
    }

    @Override
    public void OnOrderChange() {
        if(getActivity() instanceof HomePageActivity){
            Log.e(TAG, "OnOrderChange: OnOrderChange");
        }
    }
}
