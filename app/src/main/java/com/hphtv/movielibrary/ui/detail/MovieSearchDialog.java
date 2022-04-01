package com.hphtv.movielibrary.ui.detail;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutUnionsearchBinding;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MovieSearchDialog extends DialogFragment {
    public static final String TAG = MovieSearchDialog.class.getSimpleName();
    private FLayoutUnionsearchBinding mBinding;
    private MovieSearchDialogViewModel mViewModel;
    private MovieSearchAdapter mAdapter;
    private ArrayAdapter<String> mSpinnerAdapter;

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
            if(getActivity() instanceof AppBaseActivity)
                ((AppBaseActivity)getActivity()).startLoading();
            String movie_id = movie.movieId;
            String source = movie.source;
            Constants.SearchType type = movie.type;
            mViewModel.selectMovie(movie_id, source, type)
                    .subscribe(new SimpleObserver<MovieWrapper>() {
                        @Override
                        public void onAction(MovieWrapper movieWrapper) {
                            if (mOnSelectPosterListener != null) {
                                mOnSelectPosterListener.OnSelect(movieWrapper);
                            }
                        }
                    });
            dismiss();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mBinding.recyclerviewSearchResult.setLayoutManager(layoutManager);
        mBinding.recyclerviewSearchResult.setAdapter(mAdapter);
        mBinding.recyclerviewSearchResult.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                mViewModel.loading(mAdapter);
            }
        });
        //搜索按钮
        mBinding.btnSearch.setOnClickListener(v -> {
            mAdapter.clearAll();
            String keyword = mBinding.etBoxName.getText().toString();
            mViewModel.refresh(keyword, mAdapter);
        });
        mBinding.btnClose.setOnClickListener(v -> dismiss());
        if (mViewModel.getCurrentKeyword() != null) {
            mBinding.etBoxName.setText(mViewModel.getCurrentKeyword());
            mBinding.etBoxName.setSelection(0, mViewModel.getCurrentKeyword().length());
        }
        mSpinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout, Arrays.asList(getResources().getStringArray(R.array.search_type)));
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropitem_layout);
        mBinding.spinner.setAdapter(mSpinnerAdapter);
        mBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.setSearchMode(position, mAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private OnSelectPosterListener mOnSelectPosterListener;

    public interface OnSelectPosterListener {
        void OnSelect(MovieWrapper movieWrapper);
    }

    public void setOnSelectPosterListener(OnSelectPosterListener onSelectPosterListener) {
        mOnSelectPosterListener = onSelectPosterListener;
    }
}
