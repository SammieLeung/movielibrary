package com.hphtv.movielibrary.ui.postermenu;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.DialogUnknowfileItemMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.common.MovieSearchDialog;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/4/6
 */
public class UnknownsFileMenuDialog extends BaseDialogFragment2<UnknownsFileMenuViewModel, DialogUnknowfileItemMenuBinding> {

    public static UnknownsFileMenuDialog newInstance(int pos, UnrecognizedFileDataView unrecognizedFileDataView) {

        Bundle args = new Bundle();
        args.putInt("pos",pos);
        args.putSerializable("unknows",unrecognizedFileDataView);
        UnknownsFileMenuDialog fragment = new UnknownsFileMenuDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected UnknownsFileMenuViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        bindDatas();
    }

    private void initView(){
        mBinding.includePlaymovie.view.setOnClickListener(this::playVideo);
        mBinding.includeSelectPoster.view.setOnClickListener(this::showSelectPoster);
    }


    private void bindDatas(){
        UnrecognizedFileDataView dataView= (UnrecognizedFileDataView) getArguments().getSerializable("unknows");
        int pos=getArguments().getInt("pos");
        mViewModel.setItemPosition(pos);
        mViewModel.setUnrecognizedFileDataView(dataView);
        mBinding.setFilename(mViewModel.getUnrecognizedFileDataView().filename);
    }

    private void playVideo(View view)
    {
        UnrecognizedFileDataView unrecognizedFileDataView= mViewModel.getUnrecognizedFileDataView();
        MovieHelper.playingMovie(unrecognizedFileDataView.path,unrecognizedFileDataView.filename)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {

                    }
                });
    }

    private void showSelectPoster(View v){
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(mViewModel.getUnrecognizedFileDataView().keyword);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            mViewModel.reMatchMovie(wrapper)
                    .subscribe(new SimpleObserver<MovieDataView>() {
                        @Override
                        public void onAction(MovieDataView movieDataView) {
                            if (getActivity() instanceof OnMovieChangeListener) {
                                OnMovieChangeListener listener = (OnMovieChangeListener) getActivity();
                                listener.OnRematchPoster(movieDataView, getArguments().getInt("position"));
                            }
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            if(getActivity() instanceof AppBaseActivity)
                                ((AppBaseActivity)getActivity()).stopLoading();
                            getDialog().dismiss();

                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            if(getActivity() instanceof AppBaseActivity)
                                ((AppBaseActivity)getActivity()).stopLoading();
                            ToastUtil.newInstance(getContext()).toast(getString(R.string.toast_selectmovie_faild));
                        }
                    });

        });
        movieSearchFragment.show(getChildFragmentManager(), "");
    }
}
