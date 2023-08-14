package com.hphtv.movielibrary.ui.detail;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.EpisodeItemListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.bean.PlayList;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.LayoutTvDetailBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.common.ConfirmDeleteDialog;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment;
import com.hphtv.movielibrary.ui.moviesearch.online.MovieSearchDialog;
import com.hphtv.movielibrary.ui.moviesearch.online.SeasonSelectDialog;
import com.hphtv.movielibrary.ui.videoselect.VideoSelectDialog;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;
import com.station.kit.util.PackageTools;
import com.station.kit.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieDetailActivity extends AppBaseActivity<MovieDetailViewModel, LayoutTvDetailBinding> {
    public static final int REMOVE = 1;
    public static final String TAG = MovieDetailActivity.class.getSimpleName();
    private NewMovieItemListAdapter mRecommandMovieAdapter;
    private EpisodeItemListAdapter mEpisodeItemListAdapter;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;
    private PlayVideoReceiver mPlayVideoReceiver;

    public OnClickListener mClickListener = view -> {
        switch (view.getId()) {
            case R.id.btn_edit:
                showMovieSearchDialog();
                break;
            case R.id.btn_play:
                if (mViewModel.getMovieWrapper() != null) {
                    if (mViewModel.getVideoFileList().size() == 0) {
                        ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.errtips_no_playback_source));
                    } else if (mViewModel.getVideoFileList().size() == 1) {
                        String path = mViewModel.getVideoFileList().get(0).path;
                        String name = mViewModel.getVideoFileList().get(0).filename;
                        playVideo(path, name);
                    } else if (mViewModel.getVideoFileList().size() > 1) {
                        showVideoSelectDialog(mViewModel.getVideoFileList());
                    }
                }
                break;
            case R.id.btn_play_episode:
                if (mViewModel.getMovieWrapper().season != null) {
                    if (mViewModel.getLastPlayEpisodeVideoFile() != null) {
                        playingEpisodeVideo(mViewModel.getLastPlayEpisodeVideoFile());
                    } else {
                        VideoFile videoFile = mViewModel.getFirstEnableEpisodeVideoFile();
                        if (videoFile != null)
                            playingEpisodeVideo(mViewModel.getFirstEnableEpisodeVideoFile());
                        else if (mViewModel.getVideoFileList().size() > 1)
                            showVideoSelectDialog(mViewModel.getVideoFileList());
                    }
                } else {
                    if (mViewModel.getVideoFileList().size() == 1) {
                        String path = mViewModel.getVideoFileList().get(0).path;
                        String name = mViewModel.getVideoFileList().get(0).filename;
                        playVideo(path, name);
                    } else if (mViewModel.getVideoFileList().size() > 1) {
                        showVideoSelectDialog(mViewModel.getVideoFileList());
                    }
                }
                break;
            case R.id.btn_remove:
                ConfirmDeleteDialog confirmDialogFragment = ConfirmDeleteDialog.newInstance(mViewModel.getMovieWrapper().movie.movieId, mViewModel.getMovieWrapper().movie.type);
                confirmDialogFragment.setConfirmDeleteListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
                    @Override
                    public void confirmDelete(String movie_id, String type) {
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

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        startBottomMaskAnimate();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchTouchEvent(ev);
    }

    public void initView() {
        mBinding.setExpand(false);
        mBinding.setPlayEpisodeBtnText(mViewModel.getEpisodePlayBtnText());
        mBinding.btnEdit.setOnClickListener(mClickListener);
        mBinding.btnPlay.setOnClickListener(mClickListener);
        mBinding.btnPlayEpisode.setOnClickListener(mClickListener);
        mBinding.btnRemove.setOnClickListener(mClickListener);
        mBinding.btnExpand.setOnClickListener(mClickListener);
        mBinding.tvMore.setOnClickListener(mClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRecommandMovieAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(this, 72), DensityUtil.dip2px(this, 15), DensityUtil.dip2px(this, 30)));
        mBinding.rvRecommand.setLayoutManager(linearLayoutManager);
        mBinding.rvRecommand.setAdapter(mRecommandMovieAdapter);
        mRecommandMovieAdapter.setOnItemClickListener((view, position, data) -> prepareMovieWrapper(data.id, data.season));

        mEpisodeItemListAdapter = new EpisodeItemListAdapter(this, new ArrayList<>());
        mEpisodeItemListAdapter.setLastPlayEpisodePos(mViewModel.getLastPlayEpisodePos());
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mBinding.rvEpisodeList.setLayoutManager(linearLayoutManager);
        mBinding.rvEpisodeList.setOnBackPressListener(() -> onBackPressed());
        mBinding.rvEpisodeList.setAdapter(mEpisodeItemListAdapter);
        mBinding.rvEpisodeList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(this, 72), DensityUtil.dip2px(this, 7), DensityUtil.dip2px(this, 23)));
        mEpisodeItemListAdapter.setOnItemClickListener(new EpisodeItemListAdapter.OnRecyclerViewItemActionListener() {
            @Override
            public void onEpisodeClick(View view, int position, List<VideoFile> data) {
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
            public void onUnknownClick(VideoFile videoFile) {
                playingOtherEpisodeVideo(videoFile);
            }


        });
        mBinding.nestScrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> startBottomMaskAnimate());
        LinearLayout linearLayout = (LinearLayout) mBinding.tabEpisodeSet.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setDividerDrawable(AppCompatResources.getDrawable(getBaseContext(), R.drawable.divider_vertical));
        linearLayout.setDividerPadding(DensityUtil.dip2px(getBaseContext(), 13));
        mBinding.tabEpisodeSet.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                mEpisodeItemListAdapter.setSelectTabPos(pos);
                if (pos < mViewModel.getTabLayoutPaginationMap().size()) {
                    List<String> keySetList = new ArrayList<>(mViewModel.getTabLayoutPaginationMap().keySet());
                    String key = keySetList.get(pos);
                    mEpisodeItemListAdapter.addAll(mViewModel.getTabLayoutPaginationMap().get(key));
                } else {
                    mEpisodeItemListAdapter.addOthers(mViewModel.getUnknownEpisodeList());
                }
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

        mViewModel.loadMovieWrapper(movieId, season).subscribe(new SimpleObserver<MovieWrapper>() {
            @Override
            public void onSubscribe(Disposable d) {
                super.onSubscribe(d);
                startLoading();
            }

            @Override
            public void onAction(MovieWrapper wrapper) {
                if (wrapper != null) {
                    if(wrapper.videoFiles.size()>0){
                        mBinding.setHasNoVideos(false);
                    }else{
                        mBinding.setHasNoVideos(true);
                    }
                    if (Constants.VideoType.tv.equals(wrapper.movie.type) && wrapper.season != null) {
                        if (wrapper.containVideoTags(Constants.VideoType.variety_show)) {
                            mEpisodeItemListAdapter.setVarietyShow(true);
                            mBinding.setEpisodesTitle(getString(R.string.detail_varietyshow_list_title, wrapper.season.episodeCount));
                            mEpisodeItemListAdapter.addAll(mViewModel.getEpisodeList());
                        } else {
                            mEpisodeItemListAdapter.setVarietyShow(false);
                            mEpisodeItemListAdapter.setEpisodeCount(mViewModel.getEpisodeList().size());//设置总集数
                            mBinding.setEpisodesTitle(getString(R.string.detail_episodes_list_title, wrapper.season.episodeCount));
                            TabLayout tabLayout = mBinding.tabEpisodeSet;
                            tabLayout.removeAllTabs();

                            for (String name : mViewModel.getTabLayoutPaginationMap().keySet()) {
                                tabLayout.addTab(newTabView(tabLayout, name));
                            }

                            //如有未分类剧集，设置“其他” tabview
                            if (mViewModel.getUnknownEpisodeList().size() > 0) {
                                tabLayout.addTab(newTabView(tabLayout, getString(R.string.episode_pagination_others_title)));
                            }
                            tabLayout.selectTab(tabLayout.getTabAt(0));
                        }
                    }

                    mBinding.setWrapper(wrapper);
                    String stagePhoto = "";
                    if (wrapper.stagePhotos != null && wrapper.stagePhotos.size() > 0) {
                        Random random = new Random();
                        int index = random.nextInt(wrapper.stagePhotos.size());
                        stagePhoto = wrapper.stagePhotos.get(index).imgUrl;
                    }

                    if (!TextUtils.isEmpty(stagePhoto)) {
                        GlideTools.GlideWrapperWithCrossFade(getBaseContext(), stagePhoto).into(mBinding.ivStagephoto);
                    } else if (!TextUtils.isEmpty(wrapper.movie.poster)) {
                        GlideTools.GlideWrapperWithCrossFade(getBaseContext(), wrapper.movie.poster).into(mBinding.ivStagephoto);
                    } else {
                        Glide.with(getBaseContext()).load(R.drawable.default_poster).into(mBinding.ivStagephoto);
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
                                textView.setBackground(AppCompatResources.getDrawable(context, R.drawable.detail_tag));
                                textView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.rightMargin = DensityUtil.dip2px(context, 23);
                                mBinding.viewTags.addView(textView, layoutParams);
                            }
                        }
                    });

                    mViewModel.loadRecommend().subscribe(new SimpleObserver<List<MovieDataView>>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onAction(List<MovieDataView> movieDataViewList) {
                            if (movieDataViewList != null && movieDataViewList.size() > 0) {
                                mBinding.setRecommand(true);
                                mRecommandMovieAdapter.addAll(movieDataViewList);
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
        super.onResume();
    }

    @Override
    protected void onPause() {
        forceStopLoading();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void remoteUpdateMovieNotify(long o_id, long n_id) {
        long cur_id = mViewModel.getMovieWrapper() != null ? mViewModel.getMovieWrapper().movie.id : -1;
        if (o_id != -1 && o_id == cur_id) {
            prepareMovieWrapper(n_id, mViewModel.getSeason());
            ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.remote_movie_sync_tips));
        }
    }

    @Override
    public void remoteRemoveMovieNotify(String movie_id, String type) {
        refreshParent();
        stopLoading();
        finish();
        ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.remote_remove_movie_sync_tips));
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
    private void showMovieSearchDialog() {
        String keyword = mViewModel.getVideoFileList().get(0).keyword;
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            if (wrapper.movie != null && wrapper.movie.type.equals(Constants.VideoType.tv) && wrapper.seasons != null) {
                showSeasonDialog(wrapper, (wrapper1, season) -> {
                    startLoading();
                    mViewModel.saveSeries(wrapper1, season)
                            .subscribe(new SimpleObserver<MovieWrapper>() {

                                @Override
                                public void onAction(MovieWrapper movieWrapper) {
                                    if (movieWrapper.movie != null) {
                                        prepareMovieWrapper(movieWrapper.movie.id, season.seasonNumber);
                                        refreshParent();
                                    } else {
                                        ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.toast_selectmovie_faild));
                                    }
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
                                    movieSearchFragment.dismiss();
                                }
                            });
                });
            } else {
                //选择新电影逻辑
                mViewModel.saveMovie(wrapper).subscribe(new SimpleObserver<MovieWrapper>() {

                    @Override
                    public void onAction(MovieWrapper movieWrapper) {
                        if (movieWrapper.movie != null) {
                            prepareMovieWrapper(movieWrapper.movie.id, mViewModel.getSeason());
                            refreshParent();
                        } else {
                            ToastUtil.newInstance(getBaseContext()).toast(getString(R.string.toast_selectmovie_faild));
                        }
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
                        movieSearchFragment.dismiss();
                    }
                });
            }
        });
        movieSearchFragment.show(getSupportFragmentManager(), "");
    }

    private void showSeasonDialog(MovieWrapper wrapper, SeasonSelectDialog.OnClickListener listener) {
        SeasonSelectDialog seasonSelectDialog = SeasonSelectDialog.newInstance(wrapper);
        seasonSelectDialog.setOnClickListener(listener);
        seasonSelectDialog.show(getSupportFragmentManager(), "");
    }


    /**
     * 切换收藏状态
     */
    private void toggleLike() {
        mViewModel.toggleLike().subscribe(new SimpleObserver<Boolean>() {
            @Override
            public void onAction(Boolean isFavorite) {
                MovieWrapper wrapper = mViewModel.getMovieWrapper();
                //向手机助手发送电影更改的广播
                BroadcastHelper.sendBroadcastMovieUpdateSync(getBaseContext(), wrapper.movie.movieId, wrapper.movie.movieId, wrapper.movie.isFavorite ? 1 : 0);
                refreshParent();
            }
        });
    }

    public void showVideoSelectDialog(List<VideoFile> videoFileList) {
        VideoSelectDialog dialogFragment = VideoSelectDialog.newInstance(videoFileList);
        dialogFragment.setPlayingVideo((videoFile, position) -> {
            playVideo(videoFile.path, videoFile.filename);
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

    /**
     * 播放电影
     *
     * @param path
     * @param name
     */
    private void playVideo(String path, String name) {
        startLoading();
        if (PackageTools.getPackageVersionCode(getApplication(), Config.SYSTEM_PLAYER_PACKAGE) >= 40) {
            registerPlayReceiver();
            mViewModel.playingVideo(path, name)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String s) {
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            stopLoading();
                        }
                    });
        } else {
            mViewModel.playingVideo(path, name)
                    .flatMap((Function<String, ObservableSource<String>>) MovieHelper::updateHistory)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String path) {
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            stopLoading();
                        }
                    });
        }

    }

    /**
     * 播放电视剧
     *
     * @param videoFile
     */
    private void playingEpisodeVideo(VideoFile videoFile) {
        startLoading();
        if (PackageTools.getPackageVersionCode(getBaseContext(), Config.SYSTEM_PLAYER_PACKAGE) >= 40) {
            registerPlayReceiver();
            mViewModel.playingEpisodeVideo(videoFile).subscribe(new SimpleObserver<PlayList>() {

                @Override
                public void onComplete() {
                    super.onComplete();
                    stopLoading();
                }

                @Override
                public void onAction(PlayList playList) {

                }
            });
        } else {
            mViewModel.playingEpisodeVideo(videoFile)
                    .flatMap((Function<PlayList, ObservableSource<String>>) playList -> MovieHelper.updateHistory(videoFile.path))
                    .map(path -> {
                        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getVideoFileDao();
                        VideoFile videoFile1 = videoFileDao.queryByPath(path);
                        return videoFile1;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<VideoFile>() {
                        @Override
                        public void onAction(VideoFile videoFile) {
                            if (Constants.VideoType.tv.equals(mViewModel.getVideoType())) {
                                mViewModel.updatePlayEpisode(videoFile);
                            }
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            stopLoading();
                        }
                    });
        }
    }

    //播放其他剧集
    private void playingOtherEpisodeVideo(VideoFile videoFile) {
        startLoading();
        if (PackageTools.getPackageVersionCode(getBaseContext(), Config.SYSTEM_PLAYER_PACKAGE) >= 40) {
            registerPlayReceiver();
            mViewModel.playingOtherEpisodeVideo(videoFile).subscribe(new SimpleObserver<PlayList>() {
                @Override
                public void onComplete() {
                    super.onComplete();
                    stopLoading();
                }

                @Override
                public void onAction(PlayList playList) {

                }
            });
        } else {
            mViewModel.playingOtherEpisodeVideo(videoFile)
                    .flatMap((Function<PlayList, ObservableSource<String>>) playList -> MovieHelper.updateHistory(videoFile.path))
                    .map(path -> {
                        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getVideoFileDao();
                        VideoFile videoFile1 = videoFileDao.queryByPath(path);
                        return videoFile1;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<VideoFile>() {
                        @Override
                        public void onAction(VideoFile videoFile) {
                            if (Constants.VideoType.tv.equals(mViewModel.getVideoType())) {
                                mViewModel.updatePlayEpisode(videoFile);
                            }
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            stopLoading();
                        }
                    });
        }

    }

    /**
     * 返回时刷新主页
     */
    private void refreshParent() {
        Intent intent = new Intent();
        intent.putExtra(Constants.Extras.REQUEST_CODE,getIntent().getIntExtra(Constants.Extras.REQUEST_CODE,0));
        setResult(RESULT_OK,intent);
    }

    @SuppressLint("RestrictedApi")
    private TabLayout.Tab newTabView(TabLayout parent, String name) {

        TabLayout.Tab tab = parent.newTab().setText(name);
        TabLayout.TabView tabView = tab.view;
        ColorStateList tabRippleColorStateList = parent.getTabRippleColor();
        boolean unboundedRipple = parent.hasUnboundedRipple();

        Drawable background;
        Drawable contentDrawable = new GradientDrawable();
        ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);

        if (tabRippleColorStateList != null) {
            GradientDrawable maskDrawable = new GradientDrawable();
            maskDrawable.setCornerRadius(10F);
            maskDrawable.setColor(Color.WHITE);

            ColorStateList rippleColor = RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                background = new RippleDrawable(rippleColor, unboundedRipple ? null : contentDrawable, unboundedRipple ? null : maskDrawable);
            } else {
                Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
                DrawableCompat.setTintList(rippleDrawable, rippleColor);
                background = new LayerDrawable(new Drawable[]{contentDrawable, rippleDrawable});
            }
        } else {
            background = contentDrawable;
        }
        ViewCompat.setBackground(tabView, background);
        parent.invalidate();
        return tab;
    }

    private void registerPlayReceiver() {
        try {
            unregisterPlayReceiver();
            if (mPlayVideoReceiver == null)
                mPlayVideoReceiver = new PlayVideoReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.firefly.video.player");
            registerReceiver(mPlayVideoReceiver, intentFilter);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void unregisterPlayReceiver() {
        try {
            if (mPlayVideoReceiver != null) {
                unregisterReceiver(mPlayVideoReceiver);
                mPlayVideoReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlayVideoReceiver extends BroadcastReceiver {
        public static final String ACTION_PLAYER_CALLBACK = "com.firefly.video.player";
        public static final String EXTRA_PATH = "video_address";
        public static final String EXTRA_POSITION = "video_position";
        public static final String EXTRA_DURATION = "video_duration";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_PLAYER_CALLBACK.equals(action)) {
                String path = null;
                long position = 0;
                long duration = 0;
                if (intent.hasExtra(EXTRA_PATH)) {
                    path = intent.getStringExtra(EXTRA_PATH);
                }
                if (intent.hasExtra(EXTRA_POSITION)) {
                    position = intent.getLongExtra(EXTRA_POSITION, 0);
                }
                if (intent.hasExtra(EXTRA_DURATION)) {
                    duration = intent.getLongExtra(EXTRA_DURATION, 0);
                }

                Log.w(TAG, "onReceive: " + path + " " + position + "/" + duration);
                MovieHelper.updateHistory(path, position, duration)
                        .observeOn(Schedulers.newThread())
                        .map(new Function<String, VideoFile>() {
                            @Override
                            public VideoFile apply(String path) throws Throwable {
                                VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getVideoFileDao();
                                VideoFile videoFile = videoFileDao.queryByPath(path);
                                return videoFile;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<VideoFile>() {
                            @Override
                            public void onAction(VideoFile videoFile) {
                                if (Constants.VideoType.tv.equals(mViewModel.getVideoType())) {
                                    mViewModel.updatePlayEpisode(videoFile);
                                }
                            }
                        });
            }
            unregisterPlayReceiver();

        }
    }
}

