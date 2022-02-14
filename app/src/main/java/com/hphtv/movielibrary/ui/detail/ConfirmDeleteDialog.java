package com.hphtv.movielibrary.ui.detail;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.common.ConfirmDialog;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by tchip on 17-11-10.
 */

public class ConfirmDeleteDialog extends ConfirmDialog<MovieDetailViewModel> {

    private String mMessage;
    private WeakReference<Handler> mWeakReference;

    public static ConfirmDeleteDialog newInstance(Handler handler) {

        Bundle args = new Bundle();

        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        fragment.setArguments(args);
        fragment.mWeakReference=new WeakReference<>(handler);
        return fragment;
    }


    @Override
    protected MovieDetailViewModel createViewModel() {
        return mViewModel = new ViewModelProvider(getActivity()).get(MovieDetailViewModel.class);
    }

    public ConfirmDeleteDialog setMessage(String title) {
        mMessage = title;
        return this;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setDialogTitle(getResources().getString(R.string.remove_confirm));
    }

    @Override
    public void confirm(View v) {
        mViewModel.removeMovieWrapper()
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                    }

                    @Override
                    public void onAction(String s) {
                        mWeakReference.get().sendEmptyMessage(MovieDetailActivity.REMOVE);
                        dismiss();
                    }
                });
    }

    @Override
    public void cancel(View v) {
        dismiss();
    }

}
