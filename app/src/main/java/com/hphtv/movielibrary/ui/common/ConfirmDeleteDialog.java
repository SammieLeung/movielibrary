package com.hphtv.movielibrary.ui.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.detail.MovieDetailViewModel;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by tchip on 17-11-10.
 */

public class ConfirmDeleteDialog extends ConfirmDialog<ConfirmDeleteViewModel> {
    private ConfirmDeleteListener mConfirmDeleteListener;

    private String mMessage;
    private String movieId;
    private String mType;

    public static ConfirmDeleteDialog newInstance(String movie_id, Constants.SearchType searchType) {

        Bundle args = new Bundle();

        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        args.putString("movie_id",movie_id);
        args.putString("type",searchType.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.movieId=getArguments().getString("movie_id");
        this.mType=getArguments().getString("type");
    }

    @Override
    protected ConfirmDeleteViewModel createViewModel() {
        return mViewModel = new ViewModelProvider(getActivity()).get(ConfirmDeleteViewModel.class);
    }

    public ConfirmDeleteDialog setMessage(String title) {
        mMessage = title;
        return this;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setDialogTitle(mMessage);
    }

    @Override
    public void confirm(View v) {
        mViewModel.removeMovieWrapper(this.movieId,mType)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String movie_id) {
                        BroadcastHelper.sendBroadcastMovieRemoveSync(getContext(),movie_id);
                        if(mConfirmDeleteListener!=null)
                            mConfirmDeleteListener.confirmDelete(movie_id);
                        dismiss();
                    }
                });
    }

    @Override
    public void cancel(View v) {
        if(mConfirmDeleteListener!=null)
            mConfirmDeleteListener.onDismiss();
        dismiss();
    }

    public void setConfirmDeleteListener(ConfirmDeleteListener confirmDeleteListener) {
        mConfirmDeleteListener = confirmDeleteListener;
    }

    public interface ConfirmDeleteListener{
        void confirmDelete(String movie_id);
        void onDismiss();
    }
}
