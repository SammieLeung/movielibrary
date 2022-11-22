package com.hphtv.movielibrary.ui.moviesearch.online;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MovieSearchDialog extends BaseDialogFragment2<MovieSearchDialogViewModel, FLayoutUnionsearchBinding> {
    public static final String TAG = MovieSearchDialog.class.getSimpleName();
    private MovieSearchAdapter mAdapter;
    private ArrayAdapter<String> mSpinnerAdapter;

    private TvRecyclerView.OnNoNextFocusListener mOnNoNextFocusListener = new TvRecyclerView.OnNoNextFocusListener() {
        @Override
        public boolean forceFocusLeft(View currentFocus) {
            return false;
        }

        @Override
        public boolean forceFocusRight(View currentFocus) {
            return false;
        }

        @Override
        public boolean forceFocusUp(View currentFocus) {
            mBinding.etBoxName.requestFocus();
            return true;
        }

        @Override
        public boolean forceFocusDown(View currentFocus) {
            return false;
        }
    };

    private TvRecyclerView.OnBackPressListener mOnBackPressListener= () -> {
        mBinding.etBoxName.selectAll();
        mBinding.etBoxName.requestFocus();
    };

    public static MovieSearchDialog newInstance(String keyword) {
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        MovieSearchDialog fragment = new MovieSearchDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private MovieSearchDialog() {
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String keyword = getArguments().getString("keyword");
        mViewModel.setCurrentKeyword(keyword);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        //recyclerview
        mAdapter = new MovieSearchAdapter(getContext(), mViewModel.getSelectPos());
        mAdapter.setOnItemClickListener(movie -> {

            String movie_id = movie.movieId;
            String source = movie.source;
            Constants.VideoType type = movie.type;
            mViewModel.selectMovie(movie_id, source, type)
                    .subscribe(new SimpleObserver<MovieWrapper>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            super.onSubscribe(d);
                            if(getActivity() instanceof AppBaseActivity)
                                ((AppBaseActivity)getActivity()).startLoading();
                        }

                        @Override
                        public void onAction(MovieWrapper movieWrapper) {
                            if (mOnSelectPosterListener != null) {
                                mOnSelectPosterListener.OnSelect(movieWrapper);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            ToastUtil.newInstance(getContext()).toast(getString(R.string.toast_selectmovie_getdetial_faild));
                            if(getActivity() instanceof AppBaseActivity)
                                ((AppBaseActivity)getActivity()).stopLoading();
                        }
                    });
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
        mBinding.recyclerviewSearchResult.setOnNoNextFocusListener(mOnNoNextFocusListener);
        mBinding.recyclerviewSearchResult.setOnBackPressListener(mOnBackPressListener);
        mBinding.etBoxName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                reSearch();
                return true;
            }
            return false;
        });
        //搜索按钮
        mBinding.btnSearch.setOnClickListener(v -> {
            reSearch();
        });
        mBinding.btnClose.setOnClickListener(v -> dismiss());
        if (mViewModel.getCurrentKeyword() != null) {
            mBinding.etBoxName.setText(mViewModel.getCurrentKeyword());
            mBinding.etBoxName.selectAll();
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
    protected MovieSearchDialogViewModel createViewModel() {
        return new ViewModelProvider(getActivity()).get(MovieSearchDialogViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void reSearch() {
        mAdapter.clearAll();
        String keyword = mBinding.etBoxName.getText().toString();
        mViewModel.refresh(keyword, mAdapter);
    }


    private OnSelectPosterListener mOnSelectPosterListener;

    public interface OnSelectPosterListener {
        void OnSelect(MovieWrapper movieWrapper);
    }

    public void setOnSelectPosterListener(OnSelectPosterListener onSelectPosterListener) {
        mOnSelectPosterListener = onSelectPosterListener;
    }

}
