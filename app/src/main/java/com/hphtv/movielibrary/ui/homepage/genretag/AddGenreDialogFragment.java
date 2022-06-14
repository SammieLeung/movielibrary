package com.hphtv.movielibrary.ui.homepage.genretag;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.databinding.DialogCustomGenreTagLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.view.recyclerview.ItemDragCallback;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/3/7
 */
public class AddGenreDialogFragment extends BaseDialogFragment2<AddGenreDialogViewModel, DialogCustomGenreTagLayoutBinding> implements View.OnClickListener {
    public static final String TAG = AddGenreDialogFragment.class.getName();
    private GenreListApter mGenreListApter;
    private GenreListApter mGenreSortListApter;
    private List<IRefreshGenre> mIRefreshGenreList = new ArrayList<>();


    private View.OnGenericMotionListener mOnGenericMotionListener = (v, event) -> {
        if (event.getSource() == InputDevice.SOURCE_MOUSE) {
            mViewModel.getSortModeTips().set(getString(R.string.genre_tag_sortmode_tips_mouse));
            mBinding.rvThemeSort.exitSortMode();
        }
        return false;
    };

    private View.OnFocusChangeListener mOnFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus)
            refreshSortTips();
    };

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
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        prepare();

    }

    public void initView() {
        mBinding.getRoot().setOnGenericMotionListener(mOnGenericMotionListener);

        mBinding.setCheckPos(mViewModel.getCheckPos());
        mBinding.setBtnSortEnable(mViewModel.getSortEnable());
        mBinding.setSortTips(mViewModel.getSortModeTips());

        mBinding.cbtvGenre.setOnClickListener(this);
        mBinding.cbtvSort.setOnClickListener(this);
        mBinding.cbtvGenre.setOnFocusChangeListener(mOnFocusChangeListener);
        mBinding.cbtvSort.setOnFocusChangeListener(mOnFocusChangeListener);

        mBinding.rvTheme.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mGenreListApter = new GenreListApter(getContext(), new ArrayList<>(), GenreListApter.TYPE_EDIT);
        mBinding.rvTheme.setAdapter(mGenreListApter);
        mGenreListApter.setOnItemClickListener((view, position, data) -> {
            boolean isChecked = data.isChecked().get();
            if (!isChecked) {
                int count = mGenreSortListApter.getDatas().size();
                if (count >= 9) {
                    ToastUtil.newInstance(getContext()).toast(getString(R.string.added_genretags_reached_upper_limit));
                    return;
                }
            }
            data.setChecked(!isChecked);
            if (!isChecked) {
                mGenreSortListApter.getDatas().add(data);
                mGenreSortListApter.notifyDataSetChanged();
                mViewModel.getSortEnable().set(true);
            } else {
                mGenreSortListApter.getDatas().remove(data);
                mGenreSortListApter.notifyDataSetChanged();
                if (mGenreSortListApter.getItemCount() == 0) {
                    mViewModel.getSortEnable().set(false);
                }
            }
        });

        mBinding.rvThemeSort.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mGenreSortListApter = new GenreListApter(getContext(), mViewModel.getGenreTagItemSortList(), GenreListApter.TYPE_SORT);
        mGenreSortListApter.setSortPos(mBinding.rvThemeSort.getSelectPos());
        mGenreSortListApter.setOnItemFocusListener((view, position, data) -> {
            refreshSortTips();
        });
        mBinding.rvThemeSort.setAdapter(mGenreSortListApter);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemDragCallback());
        helper.attachToRecyclerView(mBinding.rvThemeSort);
        mBinding.rvThemeSort.setOnDraggableCallback(isDraggable -> {
            if (isDraggable) {
                mViewModel.getSortModeTips().set(getString(R.string.genre_tag_exit_sortmode_tips_dpad));
            } else {
                mViewModel.getSortModeTips().set(getString(R.string.genre_tag_enter_sortmode_tips_dpad));
            }
        });
    }

    public void prepare() {
        mViewModel.prepareGenreList()
                .subscribe(genreTagItems -> {
                    mGenreListApter.addAll(genreTagItems);
                    mGenreSortListApter.notifyDataSetChanged();
                });
    }

    public void addAllIRefreshGenreList(List<IRefreshGenre> iRefreshGenreList) {
        mIRefreshGenreList.clear();
        mIRefreshGenreList.addAll(iRefreshGenreList);
    }

    public void refreshSortTips() {
        if (mBinding.rvThemeSort.isDraggable()) {
            mViewModel.getSortModeTips().set(getString(R.string.genre_tag_exit_sortmode_tips_dpad));
        } else {
            mViewModel.getSortModeTips().set(getString(R.string.genre_tag_enter_sortmode_tips_dpad));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbtv_genre:
                mViewModel.getCheckPos().set(0);
                break;
            case R.id.cbtv_sort:
                mViewModel.getCheckPos().set(1);
                break;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.saveGenreTagList()
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        for (IRefreshGenre refreshGenre : mIRefreshGenreList) {
                            refreshGenre.refreshGenreUI();
                        }
                    }
                });
    }

}
