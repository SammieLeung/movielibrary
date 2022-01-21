package com.hphtv.movielibrary.ui.homepage.genretag;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.databinding.GenreEditDialogfragmentLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreEditDialogFragment extends BaseDialogFragment2<GenreEditDialogViewModel,GenreEditDialogfragmentLayoutBinding> {
    private GenreListApter mGenreListApter;
    private List<GenreTagItem> mGenreTagItemList=new ArrayList<>();
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    protected GenreEditDialogViewModel createViewModel() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareGenreList();
    }

    private void initView(){
        LinearLayoutManager manager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        mBinding.rvCategoryList.setLayoutManager(manager);
        mGenreListApter=new GenreListApter(getContext(),mGenreTagItemList);
        mBinding.rvCategoryList.setAdapter(mGenreListApter);

    }

    private void prepareGenreList(){
        mViewModel.prepareGenreList(genreTagItemList -> {
            mGenreListApter.addAll(genreTagItemList);
        });
    }

}
