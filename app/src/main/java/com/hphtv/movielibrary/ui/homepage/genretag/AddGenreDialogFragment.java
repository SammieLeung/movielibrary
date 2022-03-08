package com.hphtv.movielibrary.ui.homepage.genretag;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.databinding.DialogCustomGenreTagLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.homepage.NewHomePageViewModel;
import com.hphtv.movielibrary.ui.homepage.NewPageFragment;
import com.hphtv.movielibrary.ui.homepage.NewPageFragmentViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * author: Sam Leung
 * date:  2022/3/7
 */
public class AddGenreDialogFragment extends BaseDialogFragment2<AddGenreDialogViewModel, DialogCustomGenreTagLayoutBinding> implements View.OnClickListener {
    private GenreListApter mGenreListApter;
    private NewPageFragmentViewModel mNewPageFragmentViewModel;

    public static AddGenreDialogFragment newInstance() {

        Bundle args = new Bundle();

        AddGenreDialogFragment fragment = new AddGenreDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected AddGenreDialogViewModel createViewModel() {
        return null;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewPageFragmentViewModel=new ViewModelProvider(getParentFragment()).get(NewPageFragmentViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setCheckPos(mViewModel.mCheckPos);
        initView();
        prepare();

    }

    public void initView(){
        mBinding.cbtvGenre.setOnClickListener(this);
        mBinding.cbtvSort.setOnClickListener(this);
        mBinding.rvTheme.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mGenreListApter=new GenreListApter(getContext(),new ArrayList<>());
        mBinding.rvTheme.setAdapter(mGenreListApter);
        mGenreListApter.setOnItemClickListener(new BaseApater2.OnRecyclerViewItemActionListener<GenreTagItem>() {
            @Override
            public void onItemClick(View view, int postion, GenreTagItem data) {
                boolean isChecked=data.isChecked().get();
                data.setChecked(!isChecked);
                if(!isChecked){
                    mViewModel.getGenreTagItemList().add(data);
                }else{
                    mViewModel.getGenreTagItemList().remove(data);
                }
            }

            @Override
            public void onItemFocus(View view, int postion, GenreTagItem data) {

            }
        });
    }

    public void prepare(){
        mViewModel.prepareGenreList()
                .subscribe(genreTagItems -> {
                    mGenreListApter.addAll(genreTagItems);
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cbtv_genre:
                mViewModel.mCheckPos.set(0);
                break;
            case R.id.cbtv_sort:
                mViewModel.mCheckPos.set(1);
                break;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mNewPageFragmentViewModel.updateGenreTagList(mViewModel.toGenreTagList());
    }
}
