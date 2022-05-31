package com.hphtv.movielibrary.ui.detail;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.EpisodeItemListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.LayoutTvDetailBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.common.ConfirmDeleteDialog;
import com.hphtv.movielibrary.ui.common.MovieSearchDialog;
import com.hphtv.movielibrary.ui.videoselect.VideoSelectDialog;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;
import com.station.kit.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.disposables.Disposable;

public class MovieDetailActivity extends AppBaseActivity<MovieDetailViewModel, LayoutTvDetailBinding> {
    public static final int REMOVE = 1;
    public static final String TAG = MovieDetailActivity.class.getSimpleName();
    private NewMovieItemListAdapter mRecommandMovieAdapter;
    private EpisodeItemListAdapter mEpisodeItemListAdapter;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;
    private int mSeason = 0;


    //TODO 同步系统状态需要一个后台服务绑定
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_FAVORITE_MOVIE_CHANGE)) {
                String movie_id = intent.getStringExtra("movie_id");
                String curMovieId = mViewModel.getMovieWrapper() != null ? mViewModel.getMovieWrapper().movie.movieId : "";
                if (movie_id != null && curMovieId != null && curMovieId.equals(movie_id)) {
                    boolean is_favorite = intent.getBooleanExtra("is_favorite", false);
                    mViewModel.setLike(is_favorite)
                            .subscribe(new SimpleObserver<Boolean>() {
                                @Override
                                public void onAction(Boolean isFavorite) {
                                    MovieWrapper wrapper = mViewModel.getMovieWrapper();
                                    BroadcastHelper.sendBroadcastMovieUpdateSync(getBaseContext(), wrapper.movie.movieId, wrapper.movie.movieId, wrapper.movie.isFavorite ? 1 : 0);//向手机助手发送电影更改的广播
                                    mBinding.btnFavorite.setSelected(isFavorite);
                                    refreshParent();
                                }
                            });
                }
            }
        }
    };


    public OnClickListener mClickListener = view -> {
        switch (view.getId()) {
            case R.id.btn_edit:
                editVideoInfo();
                break;
            case R.id.btn_play:
                if (mBinding.getWrapper() != null) {
                    if (mBinding.getWrapper().videoFiles.size() == 1) {
                        String path = mBinding.getWrapper().videoFiles.get(0).path;
                        String name = mBinding.getWrapper().videoFiles.get(0).filename;
                        playVideo(path, name);
                    } else if (mBinding.getWrapper().videoFiles.size() > 1) {
                        showVideoSelectDialog();
                    }
                }
                break;
            case R.id.btn_play_episode:
                if (mViewModel.getLastPlayVideoFile() != null) {
                    playingEpisodeVideo(mViewModel.getLastPlayVideoFile());
                } else {
                    playingEpisodeVideo(mViewModel.getFirstEnableEpisodeVideoFile());
                }
                break;
            case R.id.btn_remove:
                ConfirmDeleteDialog confirmDialogFragment = ConfirmDeleteDialog.newInstance(mViewModel.getMovieWrapper().movie.movieId);
                confirmDialogFragment.setConfirmDeleteListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
                    @Override
                    public void confirmDelete(String movie_id) {
                        refreshParent();
                        stopLoading();
                        finish();
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_del_success), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                confirmDialogFragment.setMessage(getResources().getString(R.string.remove_confirm)).show(getSupportFragmentManager(), TAG);
                break;
            case R.id.btn_favorite:
                toggleLike();
                break;
            case R.id.btn_expand:
                mBinding.setExpand(!mBinding.getExpand());
                break;
            case R.id.tv_more:
                showViewMoreDialog();
                break;
        }
    };


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        this.onNewIntent(getIntent());
    }


    public void initView() {
        mBinding.setExpand(false);
        mBinding.setPlayEpisodeBtnText(mViewModel.getEpisodePlayBtnText());
        mBinding.btnEdit.setOnClickListener(mClickListener);
        mBinding.btnPlay.setOnClickListener(mClickListener);
        mBinding.btnPlayEpisode.setOnClickListener(mClickListener);
        mBinding.btnRemove.setOnClickListener(mClickListener);
        mBinding.btnFavorite.setOnClickListener(mClickListener);
        mBinding.btnExpand.setOnClickListener(mClickListener);
        mBinding.tvMore.setOnClickListener(mClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRecommandMovieAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(this, 72), DensityUtil.dip2px(this, 15), DensityUtil.dip2px(this, 30)));
        mBinding.rvRecommand.setLayoutManager(linearLayoutManager);
        mBinding.rvRecommand.setAdapter(mRecommandMovieAdapter);
        mRecommandMovieAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<MovieDataView>() {
            @Override
            public void onItemClick(View view, int position, MovieDataView data) {
                prepareMovieWrapper(data.id, data.season);
            }

            @Override
            public void onItemFocus(View view, int position, MovieDataView data) {

            }
        });

        mEpisodeItemListAdapter = new EpisodeItemListAdapter(this, new ArrayList<>());
        mEpisodeItemListAdapter.setLastPlayEpisodePos(mViewModel.getLastPlayEpisodePos());
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mBinding.rvEpisodeList.setLayoutManager(linearLayoutManager);
        mBinding.rvEpisodeList.setAdapter(mEpisodeItemListAdapter);
        mBinding.rvEpisodeList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(this, 72), DensityUtil.dip2px(this, 7), DensityUtil.dip2px(this, 23)));
        mEpisodeItemListAdapter.setOnItemClickListener(new EpisodeItemListAdapter.OnRecyclerViewItemActionListener() {
            @Override
            public void onItemClick(View view, int position, List<VideoFile> data) {
                LogUtil.v("position " + position);
                if (data.size() == 1) {
                    startLoading();
                    VideoFile videoFile = data.get(0);
                    playingEpisodeVideo(videoFile);
                } else if (data.size() > 1) {
                    showTVEpisodeSelectDialog(data);
                }
            }

            @Override
            public void onItemFocus(View view, int position, List<VideoFile> data) {

            }
        });
        mBinding.nestScrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> startBottomMaskAnimate());
        LinearLayout linearLayout = (LinearLayout) mBinding.tabEpisodeSet.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setDividerDrawable(getDrawable(R.drawable.divider_vertical));
        linearLayout.setDividerPadding(DensityUtil.dip2px(getBaseContext(), 13));
        mBinding.tabEpisodeSet.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                List<String> keySetList = new ArrayList<>(mViewModel.getTabLayoutPaginationMap().keySet());
                String key = keySetList.get(pos);
                mEpisodeItemListAdapter.setPart(pos);
                mEpisodeItemListAdapter.addAll(mViewModel.getTabLayoutPaginationMap().get(key));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            long movieId = intent.getLongExtra(Constants.Extras.MOVIE_ID, -1);
            int season = intent.getIntExtra(Constants.Extras.SEASON, 0);
            prepareMovieWrapper(movieId, season);//1.加载电影数据
        }
    }

    /**
     * 加载电影数据
     *
     * @param movieId
     */
    private void prepareMovieWrapper(long movieId, int season) {

        mViewModel.loadMovieWrapper(movieId, season)
                .subscribe(new SimpleObserver<MovieWrapper>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        startLoading();
                    }

                    @Override
                    public void onAction(MovieWrapper wrapper) {
                        if (wrapper != null) {
                            if (Constants.SearchType.tv.equals(wrapper.movie.type)) {
                                mBinding.setEpisodesTitle(getString(R.string.detail_episodes_list_title, wrapper.season.episodeCount));
                            }

                            mBinding.setWrapper(wrapper);
                            TabLayout tabLayout = mBinding.tabEpisodeSet;
                            tabLayout.removeAllTabs();

                            for (String name : mViewModel.getTabLayoutPaginationMap().keySet()) {
                                tabLayout.addTab(tabLayout.newTab().setText(name));
                            }
                            tabLayout.selectTab(tabLayout.getTabAt(0));

                            String stagePhoto = "";
                            if (wrapper.stagePhotos != null && wrapper.stagePhotos.size() > 0) {
                                Random random = new Random();
                                int index = random.nextInt(wrapper.stagePhotos.size());
                                stagePhoto = wrapper.stagePhotos.get(index).imgUrl;
                            }
                            mBinding.btnFavorite.setSelected(wrapper.movie.isFavorite);

                            if (!TextUtils.isEmpty(stagePhoto)) {
                                GlideTools.GlideWrapperWithCrossFade(getBaseContext(), stagePhoto).into(mBinding.ivStagephoto);
                            } else if (!TextUtils.isEmpty(wrapper.movie.poster)) {
                                GlideTools.GlideWrapperWithCrossFade(getBaseContext(), wrapper.movie.poster).into(mBinding.ivStagephoto);
                            } else {
                                Glide.with(getBaseContext()).load(R.mipmap.default_poster).into(mBinding.ivStagephoto);
                            }

                            mViewModel.loadTags().subscribe(new SimpleObserver<List<String>>() {
                                @Override
                                public void onAction(List<String> tagList) {
                                    Context context = MovieDetailActivity.this;
                                    int paddingTop = DensityUtil.dip2px(context, 5);
                                    int paddingLeft = DensityUtil.dip2px(context, 11);
                                    mBinding.viewTags.removeAllViews();
                                    for (String tag : tagList) {
                                        TextView textView = new TextView(context);
                                        textView.setText(tag);
                                        textView.setTextSize(20);
                                        textView.setTextColor(Color.parseColor("#FFCCCCCC"));
                                        textView.setBackground(getDrawable(R.drawable.detail_tag));
                                        textView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        layoutParams.rightMargin = DensityUtil.dip2px(context, 23);
                                        mBinding.viewTags.addView(textView, layoutParams);
                                    }
                                }
                            });

                            mViewModel.loadRecommend()
                                    .subscribe(new SimpleObserver<List<MovieDataView>>() {
                                        @Override
                                        public void onAction(List<MovieDataView> movieDataViewList) {
                                            if (movieDataViewList != null && movieDataViewList.size() > 0) {
                                                mBinding.setRecommand(true);
                                                mRecommandMovieAdapter.addAll(movieDataViewList);
                                                mRecommandMovieAdapter.notifyDataSetChanged();
                                            } else {
                                                mBinding.setRecommand(false);
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        stopLoading();
                    }
                });
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopLoading();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 蒙版隱藏動畫
     */
    private void startBottomMaskAnimate() {
        mHandler.removeCallbacks(mBottomMaskFadeInTask);
        if (mBinding.bottomMask.getAlpha() > 0) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 0).setDuration(200);
            objectAnimator.start();
        }
        mBottomMaskFadeInTask = () -> {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 1).setDuration(500);
            objectAnimator.start();
        };
        mHandler.postDelayed(mBottomMaskFadeInTask, 800);
    }

    /**
     * 编辑封面信息
     */
    private void editVideoInfo() {
        String keyword = mBinding.getWrapper().videoFiles.get(0).keyword;
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            startLoading();
            //选择新电影逻辑
            mViewModel.selectMovie(wrapper)
                    .subscribe(new SimpleObserver<MovieWrapper>() {

                        @Override
                        public void onAction(MovieWrapper movieWrapper) {
                            if (movieWrapper.movie != null) {
                                prepareMovieWrapper(movieWrapper.movie.id, mSeason);
                                refreshParent();
                            } else {
                                ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.toast_selectmovie_faild));
                            }
                            stopLoading();
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            e.printStackTrace();
                            ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.toast_selectmovie_faild));
                            stopLoading();
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            stopLoading();
                        }
                    });
        });
        movieSearchFragment.show(getSupportFragmentManager(), "");
    }


    /**
     * 切换收藏状态
     */
    private void toggleLike() {
        mViewModel.toggleLike()
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onAction(Boolean isFavorite) {
                        MovieWrapper wrapper = mViewModel.getMovieWrapper();
                        BroadcastHelper.sendBroadcastMovieUpdateSync(getBaseContext(), wrapper.movie.movieId, wrapper.movie.movieId, wrapper.movie.isFavorite ? 1 : 0);//向手机助手发送电影更改的广播
                        mBinding.btnFavorite.setSelected(isFavorite);
                        refreshParent();
                    }
                });
    }

    public void showVideoSelectDialog() {
        VideoSelectDialog dialogFragment = VideoSelectDialog.newInstance(mViewModel.getMovieWrapper().videoFiles);
        dialogFragment.setPlayingVideo((videoFile, position) -> {
            playVideo(videoFile.path,videoFile.filename);
        });
        dialogFragment.show(getSupportFragmentManager(), "detail");
    }

    public void showTVEpisodeSelectDialog(List<VideoFile> videoFileList) {
        VideoSelectDialog dialogFragment = VideoSelectDialog.newInstance(videoFileList);
        dialogFragment.setPlayingVideo((videoFile, position) -> playingEpisodeVideo(videoFile));
        dialogFragment.show(getSupportFragmentManager(), "detail");
    }

    public void showViewMoreDialog() {
        MovieDetailPlotDialog dialog = MovieDetailPlotDialog.newInstance(mBinding.nestScrollview);
        dialog.show(getSupportFragmentManager(), "");
    }

    private void playVideo(String path, String name) {
        startLoading();
        mViewModel.playingVideo(path, name);
        refreshParent();
    }

    private void playingEpisodeVideo(VideoFile videoFile) {
        startLoading();
        mViewModel.playingEpisodeVideo(videoFile);
        refreshParent();
    }
    /**
     * 返回时刷新主页
     */
    private void refreshParent() {
        setResult(RESULT_OK);
    }
}
