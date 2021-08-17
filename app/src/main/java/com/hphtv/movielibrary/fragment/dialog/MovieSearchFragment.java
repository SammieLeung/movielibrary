package com.hphtv.movielibrary.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.adapter.MovieSearchAdapter;
import com.hphtv.movielibrary.databinding.FLayoutUnionsearchBinding;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.viewmodel.fragment.MovieSearchFragmentViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MovieSearchFragment extends DialogFragment {
    public static final String TAG = MovieSearchFragment.class.getSimpleName();
    private FLayoutUnionsearchBinding mBinding;
    private MovieSearchFragmentViewModel mViewModel;
    private MovieSearchAdapter mAdapter;

    public static MovieSearchFragment newInstance(String keyword) {
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        MovieSearchFragment fragment = new MovieSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MovieSearchFragmentViewModel.class);
        String keyword = getArguments().getString("keyword");
        mViewModel.setCurrentKeyword(keyword);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mBinding = FLayoutUnionsearchBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        //recyclerview
        mAdapter = new MovieSearchAdapter(getContext());
        mAdapter.setOnItemClickListener(movie -> {
            String movie_id = movie.movieId;
            String source = movie.source;
            if(mOnSelectPosterListener!=null){
                mOnSelectPosterListener.OnSelect(source,movie_id);
            }
            dismiss();
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mBinding.recyclerviewSearchResult.setLayoutManager(gridLayoutManager);
        mBinding.recyclerviewSearchResult.setAdapter(mAdapter);
        mBinding.recyclerviewSearchResult.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                mViewModel.loading(mAdapter);
            }
        });
        mBinding.btnSearch.setOnClickListener(v -> {
            mAdapter.clearAll();
            String keyword = mBinding.etBoxName.getText().toString();
            search(keyword);
        });
        if (mViewModel.getCurrentKeyword() != null) {
            mBinding.etBoxName.setText(mViewModel.getCurrentKeyword());
            mBinding.etBoxName.setSelection(0, mViewModel.getCurrentKeyword().length());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        search(mViewModel.getCurrentKeyword());
    }

    private void search(String keyword) {
        mViewModel.refresh(keyword, mAdapter);
    }

    private OnSelectPosterListener mOnSelectPosterListener;
    public interface OnSelectPosterListener {
        void OnSelect(String source, String movie_id);
    }

    public void setOnSelectPosterListener(OnSelectPosterListener onSelectPosterListener) {
        mOnSelectPosterListener = onSelectPosterListener;
    }
}
