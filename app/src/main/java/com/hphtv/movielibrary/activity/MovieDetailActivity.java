package com.hphtv.movielibrary.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieTrailerAdapter;
import com.hphtv.movielibrary.adapter.MovieTrailerAdapter.OnRecyclerViewItemClickListener;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.LayoutDetailBinding;
import com.hphtv.movielibrary.effect.GlideBlurTransformation;
import com.hphtv.movielibrary.fragment.dialog.MovieSearchFragment;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.fragment.dialog.ConfirmDialogFragment;
import com.hphtv.movielibrary.fragment.dialog.CustomRadioDialogFragment;
import com.hphtv.movielibrary.viewmodel.MovieDetailViewModel;
import com.station.kit.util.LogUtil;
import com.station.kit.util.ToastUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import me.khrystal.library.widget.ItemViewMode;
import me.khrystal.library.widget.ScaleXCenterViewMode;

public class MovieDetailActivity extends AppBaseActivity<MovieDetailViewModel, LayoutDetailBinding> {
    public static final String TAG = "MovieDetailActivity";

    private ItemViewMode mItemViewMode;
    private LinearLayoutManager mLayoutManager;
    private MovieTrailerAdapter mMovieTrailerAdapter;
    private MovieWrapper mCurWrapper;

    @Override
    protected int getContentViewId() {
        return R.layout.layout_detail;
    }

    @Override
    protected void processLogic() {
        // 初始化
        initView();
        this.onNewIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            int currentMode = intent.getIntExtra(Constants.IntentKey.KEY_MODE, -1);
            mViewModel.setCurrentMode(currentMode);
            switch (currentMode) {
                case Constants.MovieDetailMode.MODE_WRAPPER:
                    long movieId = intent.getLongExtra(Constants.IntentKey.KEY_MOVIE_ID, -1);
                    prepareMovieWrapper(movieId);//1.加载电影数据
                    break;
                case Constants.MovieDetailMode.MODE_UNRECOGNIZEDFILE:
                    String unrecognizedFileKeyword = intent.getStringExtra(Constants.IntentKey.KEY_UNRECOGNIZE_FILE_KEYWORD);
                    prepareUnrecogizedFile(unrecognizedFileKeyword);//2.组合未识别文件数据
                    break;
            }
        }
    }

    /**
     * 加载电影数据
     *
     * @param movieId
     */
    private void prepareMovieWrapper(long movieId) {
        mBinding.btnFavorite.setVisibility(View.VISIBLE);
        mBinding.btnTrailer.setVisibility(View.VISIBLE);
        mBinding.btnRemove.setVisibility(View.VISIBLE);
        mViewModel.loadMovieWrapper(movieId, wrapper -> {
            if (wrapper != null) {
                mCurWrapper = wrapper;
                mBinding.setWrapper(mCurWrapper);
                String stagePhoto = "";
                if (wrapper.stagePhotos != null && wrapper.stagePhotos.size() > 0) {
                    Random random = new Random();
                    int index = random.nextInt(wrapper.stagePhotos.size());
                    stagePhoto = wrapper.stagePhotos.get(index).imgUrl;
                }
                GlideTools.GlideWrapper(this, wrapper.movie.poster).error(R.mipmap.ic_poster_default)
                        .into(mBinding.ivCover);
                GlideTools.GlideWrapper(this, stagePhoto)
                        .apply(RequestOptions.bitmapTransform(new GlideBlurTransformation(MovieDetailActivity.this, 25, 3)))
                        .error(R.mipmap.ic_poster_default)
                        .into(mBinding.ivStage);
                mBinding.btnFavorite.setFavoriteState(wrapper.movie.isFavorite);
                mMovieTrailerAdapter.addItems(wrapper.trailers);
            }
        });
    }

    /**
     * 组合未识别文件数据
     *
     * @param keyword
     */
    private void prepareUnrecogizedFile(String keyword) {
        mBinding.btnFavorite.setVisibility(View.GONE);
        mBinding.btnTrailer.setVisibility(View.GONE);
        mBinding.btnRemove.setVisibility(View.GONE);
        mViewModel.loadUnrecogizedFile(keyword, unrecognizedFileDataViewList -> {
            if (unrecognizedFileDataViewList != null && unrecognizedFileDataViewList.size() > 0) {
                Glide.with(this).load(R.mipmap.ic_poster_default).error(R.mipmap.ic_poster_default).into(mBinding.ivCover);
                MovieWrapper movieWrapper = new MovieWrapper();
                movieWrapper.videoFiles = new ArrayList<>();
                StringBuffer stringBuffer = new StringBuffer();
                unrecognizedFileDataViewList.sort((o1, o2) -> o1.filename.compareTo(o2.filename));
                int i = 1;
                for (UnrecognizedFileDataView dataView : unrecognizedFileDataViewList) {
                    stringBuffer.append(i++ + ". " + dataView.filename + "\n");
                    VideoFile videoFile = new VideoFile();
                    videoFile.path = dataView.path;
                    videoFile.filename = dataView.filename;
                    videoFile.keyword = dataView.keyword;
                    movieWrapper.videoFiles.add(videoFile);
                }
                stringBuffer.substring(0, stringBuffer.length() - 1);
                movieWrapper.movie = new Movie();
                movieWrapper.movie.title = keyword;
                movieWrapper.movie.plot = stringBuffer.toString();
                movieWrapper.movie.poster = "";

                mBinding.setWrapper(movieWrapper);
            }
        });
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        super.onResume();
        this.onNewIntent(getIntent());
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

    public void initView() {
        mBinding.btnEdit.setOnClickListener(mClickListener);
        mBinding.btnTrailer.setOnClickListener(mClickListener);
        mBinding.btnPlay.setOnClickListener(mClickListener);
        mBinding.btnRemove.setOnClickListener(mClickListener);
        mBinding.btnFavorite.setOnClickListener(mClickListener);
        mBinding.btnExit.setOnClickListener(mClickListener);

        mBinding.rvTrailer.setOnCenterItemFocusListener((v, isViewOnCenter) -> {
            if (isViewOnCenter) {
                v.findViewById(R.id.text_group_trailer).setVisibility(View.VISIBLE);
            } else {
                v.findViewById(R.id.text_group_trailer).setVisibility(View.GONE);
            }
        });

        mBinding.svDetail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                mBinding.btnPlay.requestFocus();
            }
        });
        initMovieTrailerList();
    }

    /**
     * 预告片组件初始化。
     */
    private void initMovieTrailerList() {

        // 创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);

        mBinding.rvTrailer.setLayoutManager(mLayoutManager);

        mItemViewMode = new ScaleXCenterViewMode();
        mBinding.rvTrailer.setViewMode(mItemViewMode);
        mBinding.rvTrailer.setNeedCenterForce(true);
        mBinding.rvTrailer.setNeedLoop(true);
        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mBinding.rvTrailer.setHasFixedSize(true);
        // 创建并设置Adapter
        mMovieTrailerAdapter = new MovieTrailerAdapter(MovieDetailActivity.this,
                new ArrayList<>());
        mBinding.rvTrailer.setAdapter(mMovieTrailerAdapter);
        mMovieTrailerAdapter
                .setOnItemClickListener(mCenterItemClickListener);
        mBinding.viewTrailer.setOnClickListener(v -> hideMovieTrailer());
    }

    /**
     * 编辑封面信息
     */
    private void editVideoInfo() {
        String keyword = mBinding.getWrapper().videoFiles.get(0).keyword;
        MovieSearchFragment movieSearchFragment = MovieSearchFragment.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((source, movie_id) -> {
            startLoading();
            boolean is_favoirte = false;//默认收藏状态为false
            if (mCurWrapper != null && mCurWrapper.movie != null) {
                String last_movie_id = mCurWrapper.movie.movieId;
                is_favoirte = mCurWrapper.movie.isFavorite;//获取当前收藏状态
                BroadcastHelper.sendBroadcastMovieUpdateSync(this, last_movie_id, movie_id, is_favoirte ? 1 : 0);//向手机助手发送电影更改的广播
            } else {
                BroadcastHelper.sendBroadcastMovieAddSync(this, movie_id);//向手机助手发送添加电影的广播
            }
            //选择新电影逻辑
            mViewModel.selectMovie(source, movie_id, is_favoirte, movieWrapper -> {
                prepareMovieWrapper(movieWrapper.movie.id);
                setActivityResult();
                stopLoading();
            });
        });
        movieSearchFragment.show(getSupportFragmentManager(), "");
    }


    /**
     * 删除电影和电影文件信息
     */
    private void removeMovie() {
        startLoading();
        mViewModel.removeMovieWrapper(args -> {
            if (args != null && args.length > 0) {
                String movie_id = (String) args[0];
                BroadcastHelper.sendBroadcastMovieRemoveSync(this, movie_id);//向手机助手发送电影移除的广播
            }
            setActivityResult();
            stopLoading();
            finish();
            Toast.makeText(MovieDetailActivity.this, getResources().getString(R.string.toast_del_success), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavroite() {
        mViewModel.setFavorite(mBinding.getWrapper(), args -> {
            if (args[0] != null && mCurWrapper != null && mCurWrapper.movie != null) {
                boolean isFavorite = (boolean) args[0];
                String movie_id = mCurWrapper.movie.movieId;
                int is_favorite = isFavorite == true ? 1 : 0;
                BroadcastHelper.sendBroadcastMovieUpdateSync(this, movie_id, movie_id, is_favorite);//向手机助手发送电影更改的广播
                mCurWrapper.movie.isFavorite = isFavorite;
                mBinding.btnFavorite.setFavoriteState(isFavorite);
                setActivityResult();
            }
        });
    }

    /**
     * 显示电影预告片
     */
    private void showMovieTrailer() {
        if (mMovieTrailerAdapter.getRealItemCount() > 0) {
            mBinding.viewTrailer.setVisibility(View.VISIBLE);
            if (mBinding.rvTrailer.getChildAt(0) != null)
                mBinding.rvTrailer.getChildAt(0).requestFocus();
        } else {
            ToastUtil.newInstance(this).toast(getResources().getString(R.string.toastmsg_no_trailer_found));
        }
    }

    /**
     * 隐藏电影预告片
     */
    private void hideMovieTrailer() {
        mBinding.viewTrailer.setVisibility(View.GONE);
    }

    /**
     * 判断预告片弹出框是否显示
     *
     * @return
     */
    private boolean isMovieTrailerShowing() {
        return mBinding.viewTrailer.getVisibility() == RelativeLayout.VISIBLE;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isMovieTrailerShowing())
                    if (mBinding.rvTrailer.findFocus() == null) {
                        mBinding.rvTrailer.getChildAt(0).requestFocus();
                        return true;
                    } else {
                        if (mBinding.rvTrailer.getChildAt(0).isFocused()) {
                            return false;
                        }
                    }

            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (isMovieTrailerShowing()) {
                    int postion = mBinding.rvTrailer.getChildCount() - 1;
                    if (mBinding.rvTrailer.findFocus() == null) {
                        mBinding.rvTrailer.getChildAt(postion).requestFocus();
                        return true;
                    } else {

                        if (mBinding.rvTrailer.getChildAt(postion).isFocused()) {
                            return false;
                        }
                    }
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (isMovieTrailerShowing()) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void showRadioDialog() {
        CustomRadioDialogFragment dialogFragment = CustomRadioDialogFragment.newInstance(mBinding.getWrapper());
        dialogFragment.setOnClickListener(new CustomRadioDialogFragment.OnClickListener() {
            @Override
            public void doPositiveClick(MovieWrapper movieWrapper, VideoFile videoFile) {
                String path = videoFile.path;
                String name = videoFile.filename;
                playVideo(movieWrapper, path, name);
            }

            @Override
            public void doItemSelect(MovieWrapper movieWrapper, VideoFile videoFile) {
                doPositiveClick(movieWrapper, videoFile);
            }

        });
        dialogFragment.show(getSupportFragmentManager(), "detail");
    }

    private void playVideo(MovieWrapper wrapper, String path, String name) {
        startLoading();
        mViewModel.playingVideo(wrapper, path, name);
        setActivityResult();
    }

    /**
     * 返回时刷新主页
     */
    private void setActivityResult() {
        setResult(1);
    }

    @Override
    public void onBackPressed() {
        if (isMovieTrailerShowing())
            hideMovieTrailer();
        else {
            finish();
        }
    }


    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_FAVORITE_MOVIE_CHANGE)) {
                String movie_id = intent.getStringExtra("movie_id");
                String curMovieId = mCurWrapper != null ? mCurWrapper.movie.movieId : "";
                if (movie_id != null && curMovieId != null && curMovieId.equals(movie_id)) {
                    boolean is_favorite = intent.getBooleanExtra("is_favorite", false);
                    mBinding.btnFavorite.setFavoriteState(is_favorite);
                    setActivityResult();
                }
            }
        }
    };


    public OnClickListener mClickListener = view -> {

        switch (view.getId()) {
            case R.id.btn_edit:
                editVideoInfo();
                break;
            case R.id.btn_trailer:
                showMovieTrailer();
                break;
            case R.id.btn_play:
                if (mBinding.getWrapper() != null) {
                    if (mBinding.getWrapper().videoFiles.size() == 1) {
                        startLoading();
                        String path = mBinding.getWrapper().videoFiles.get(0).path;
                        String name = mBinding.getWrapper().videoFiles.get(0).filename;
                        playVideo(mBinding.getWrapper(), path, name);
                    } else if (mBinding.getWrapper().videoFiles.size() > 1) {
                        showRadioDialog();
                    }
                }
                break;
            case R.id.btn_remove:
                ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance();
                confirmDialogFragment.setMessage(getResources().getString(R.string.remove_confirm))
                        .setOnClickListener(new ConfirmDialogFragment.OnClickListener() {
                            @Override
                            public void OK() {
                                removeMovie();
                            }

                            @Override
                            public void Cancel() {

                            }
                        });
                confirmDialogFragment.show(getSupportFragmentManager(), TAG);

                break;
            case R.id.btn_favorite:
                toggleFavroite();
                break;
            case R.id.btn_exit:
                finish();
                break;
        }
    };

    OnRecyclerViewItemClickListener mCenterItemClickListener = (view, trailer) -> {
        if (trailer != null) {
            startLoading();
            VideoPlayTools.play(getApplicationContext(), Uri.parse(trailer.url));
            Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> stopLoading());
        }
    };
}
