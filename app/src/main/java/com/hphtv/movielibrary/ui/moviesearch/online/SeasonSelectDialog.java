package com.hphtv.movielibrary.ui.moviesearch.online;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.CommonViewHolder;
import com.hphtv.movielibrary.databinding.FLayoutSeasonSelectBinding;
import com.hphtv.movielibrary.databinding.SeasonItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/8/24
 */
public class SeasonSelectDialog extends BaseDialogFragment2<MovieSearchDialogViewModel, FLayoutSeasonSelectBinding> {
    private List<Season> mSeasons;
    private SeasonAdapter mSeasonAdapter;
    private MovieWrapper mMovieWrapper;

    private OnClickListener mOnClickListener;

    public static SeasonSelectDialog newInstance(MovieWrapper movieWrapper) {
        Bundle args = new Bundle();
        SeasonSelectDialog fragment = new SeasonSelectDialog();
        args.putSerializable("wrapper", movieWrapper);
        fragment.setArguments(args);
        return fragment;
    }

    private SeasonSelectDialog() {
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovieWrapper = (MovieWrapper) getArguments().getSerializable("wrapper");
        mSeasons = mMovieWrapper.seasons;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mBinding.rvSeasonList.setLayoutManager(linearLayoutManager);
        mSeasonAdapter = new SeasonAdapter();
        mBinding.rvSeasonList.setAdapter(mSeasonAdapter);
        mSeasonAdapter.setSeasons(mSeasons);
    }

    @Override
    protected MovieSearchDialogViewModel createViewModel() {
        return new ViewModelProvider(getActivity()).get(MovieSearchDialogViewModel.class);
    }


    public interface OnClickListener {
        void onClick(MovieWrapper wrapper, Season season);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.ViewHolder> {

        private List<Season> mSeasons = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SeasonItemBinding binding = SeasonItemBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            ViewHolder vh = new ViewHolder(binding);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Season season = mSeasons.get(position);
            holder.mDataBinding.setSeason(season);
            holder.mDataBinding.setTitle(mMovieWrapper.movie.title
                    + " "
                    + ((season.name == null)
                    ? getString(R.string.season_name_for_unknow, season.seasonNumber)
                    : season.name));
        }

        @Override
        public int getItemCount() {
            return mSeasons.size();
        }

        public void setSeasons(List<Season> seasons) {
            mSeasons.clear();
            mSeasons.addAll(seasons);
            notifyDataSetChanged();
        }

        public class ViewHolder extends CommonViewHolder<SeasonItemBinding> {

            public ViewHolder(SeasonItemBinding binding) {
                super(binding);
                binding.getRoot().setOnClickListener(v -> {
                            if (mOnClickListener != null)
                                mOnClickListener.onClick(mMovieWrapper, binding.getSeason());
                            dismiss();
                        }
                );
            }
        }
    }
}
