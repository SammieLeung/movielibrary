package com.hphtv.movielibrary.ui.detail;

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
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutUnionsearchBinding;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MovieSearchDialog extends DialogFragment {
    public static final String TAG = MovieSearchDialog.class.getSimpleName();
    private FLayoutUnionsearchBinding mBinding;
    private MovieSearchDialogViewModel mViewModel;
    private MovieSearchAdapter mAdapter;

    public static MovieSearchDialog newInstance(String keyword) {
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        MovieSearchDialog fragment = new MovieSearchDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(MovieSearchDialogViewModel.class);
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
            Constants.SearchType type=movie.type;
            if(mOnSelectPosterListener!=null){
                mOnSelectPosterListener.OnSelect(movie_id,source,type);
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
        void OnSelect( String movie_id,String source,Constants.SearchType type);
    }

    public void setOnSelectPosterListener(OnSelectPosterListener onSelectPosterListener) {
        mOnSelectPosterListener = onSelectPosterListener;
    }
}
