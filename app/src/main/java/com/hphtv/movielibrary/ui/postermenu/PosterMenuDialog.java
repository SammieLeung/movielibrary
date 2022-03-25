package com.hphtv.movielibrary.ui.postermenu;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.databinding.DialogPosterItemMenuBinding;
import com.hphtv.movielibrary.databinding.PostermenuMoreItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuDialog extends BaseDialogFragment2<PosterMenuViewModel, DialogPosterItemMenuBinding> {

    public static PosterMenuDialog newInstance(MovieDataView movieDataView) {
        Bundle args = new Bundle();
        PosterMenuDialog fragment = new PosterMenuDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected PosterMenuViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
