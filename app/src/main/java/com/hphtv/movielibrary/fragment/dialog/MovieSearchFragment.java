package com.hphtv.movielibrary.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleCoroutineScope;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.LoadStates;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.adapter.UnionSearchMovieAdapter;
import com.hphtv.movielibrary.databinding.FLayoutUnionsearchBinding;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.viewmodel.fragment.MovieSearchFragmentViewModel;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.FlowCollector;

/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MovieSearchFragment extends DialogFragment {
    public static final String TAG = MovieSearchFragment.class.getSimpleName();
    private FLayoutUnionsearchBinding mFLayoutUnionsearchBinding;
    private MovieSearchFragmentViewModel mViewModel;
    private UnionSearchMovieAdapter mAdapter;
    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MovieSearchFragmentViewModel.class);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mFLayoutUnionsearchBinding = FLayoutUnionsearchBinding.inflate(inflater, container, false);
        return mFLayoutUnionsearchBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        search("test");
    }

    private void initRecyclerView() {
        mAdapter = new UnionSearchMovieAdapter(getContext());
        mFLayoutUnionsearchBinding.rvUnionsearchResult.setAdapter(mAdapter);
    }


    private void search(String keyword) {
        if(mDisposable!=null)
        mDisposable.dispose();
        mDisposable = mViewModel.unionSearchMovie(keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((PagingData<Movie> moviePagingData) -> {
                    mAdapter.submitData(getLifecycle(),moviePagingData);
                });
    }

}
