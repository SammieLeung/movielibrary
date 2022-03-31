package com.hphtv.movielibrary.ui.detail;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.ui.common.ConfirmDialog;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by tchip on 17-11-10.
 */

public class ConfirmDeleteDialog extends ConfirmDialog<MovieDetailViewModel> {

    private String mMessage;

    public static ConfirmDeleteDialog newInstance() {

        Bundle args = new Bundle();

        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        fragment.setArguments(args);
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
        mBinding.setDialogTitle(mMessage);
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
                    public void onAction(String movie_id) {
                        BroadcastHelper.sendBroadcastMovieRemoveSync(getContext(),movie_id);
                        dismiss();
                    }
                });
    }

    @Override
    public void cancel(View v) {
        dismiss();
    }

}
