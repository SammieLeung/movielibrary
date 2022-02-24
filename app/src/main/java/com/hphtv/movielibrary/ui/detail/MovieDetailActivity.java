package com.hphtv.movielibrary.ui.detail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.adapter.BaseScaleApater;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.LayoutNewDetailBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.videoselect.VideoSelectDialog;
import com.hphtv.movielibrary.util.BroadcastHelper;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MovieDetailActivity extends AppBaseActivity<MovieDetailViewModel, LayoutNewDetailBinding> {
    public static final int REMOVE=1;
    public static final String TAG = MovieDetailActivity.class.getSimpleName();
    private MovieWrapper mCurWrapper;
    private NewMovieItemListAdapter mRecommandMovieAdapter;
    private Handler mUIHandler =new Handler(Looper.getMainLooper()){
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what)
            {
                case REMOVE:
                    refreshParent();
                    stopLoading();
                    finish();
                    Toast.makeText(MovieDetailActivity.this, getResources().getString(R.string.toast_del_success), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private  BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_FAVORITE_MOVIE_CHANGE)) {
                String movie_id = intent.getStringExtra("movie_id");
                String curMovieId = mCurWrapper != null ? mCurWrapper.movie.movieId : "";
                if (movie_id != null && curMovieId != null && curMovieId.equals(movie_id)) {
                    boolean is_favorite = intent.getBooleanExtra("is_favorite", false);
                    mBinding.btnFavorite.setSelected(is_favorite);
                    refreshParent();
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
                        startLoading();
                        String path = mBinding.getWrapper().videoFiles.get(0).path;
                        String name = mBinding.getWrapper().videoFiles.get(0).filename;
                        playVideo(path, name);
                    } else if (mBinding.getWrapper().videoFiles.size() > 1) {
                        showRadioDialog();
                    }
                }
                break;
            case R.id.btn_remove:
                ConfirmDeleteDialog confirmDialogFragment = ConfirmDeleteDialog.newInstance(mUIHandler);
                confirmDialogFragment.setMessage(getResources().getString(R.string.remove_confirm)).show(getSupportFragmentManager(),TAG);
                break;
            case R.id.btn_favorite:
                toggleFavroite();
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
        mBinding.btnEdit.setOnClickListener(mClickListener);
        mBinding.btnPlay.setOnClickListener(mClickListener);
        mBinding.btnRemove.setOnClickListener(mClickListener);
        mBinding.btnFavorite.setOnClickListener(mClickListener);
        mBinding.btnExpand.setOnClickListener(mClickListener);
        mBinding.tvMore.setOnClickListener(mClickListener);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false);
        mRecommandMovieAdapter=new NewMovieItemListAdapter(this,new ArrayList<>());
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(this,72),DensityUtil.dip2px(this,15),DensityUtil.dip2px(this,30)));
        mBinding.rvRecommand.setLayoutManager(linearLayoutManager);
        mBinding.rvRecommand.setAdapter(mRecommandMovieAdapter);
        mRecommandMovieAdapter.setOnItemClickListener(new BaseApater2.OnRecyclerViewItemActionListener<MovieDataView>() {
            @Override
            public void onItemClick(View view, int postion, MovieDataView data) {
                prepareMovieWrapper(data.id);
            }

            @Override
            public void onItemFocus(View view, int postion, MovieDataView data) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            int currentMode = intent.getIntExtra(Constants.Extras.MODE, -1);
            mViewModel.setCurrentMode(currentMode);
            switch (currentMode) {
                case Constants.MovieDetailMode.MODE_WRAPPER:
                    mBinding.setUnmatch(false);
                    long movieId = intent.getLongExtra(Constants.Extras.MOVIE_ID, -1);
                    prepareMovieWrapper(movieId);//1.加载电影数据
                    break;
                case Constants.MovieDetailMode.MODE_UNRECOGNIZEDFILE:
                    String unrecognizedFileKeyword = intent.getStringExtra(Constants.Extras.UNRECOGNIZE_FILE_KEYWORD);
                    mBinding.setUnmatch(true);
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
        mViewModel.loadMovieWrapper(movieId)
                .subscribe(new SimpleObserver<MovieWrapper>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        startLoading();
                    }

                    @Override
                    public void onAction(MovieWrapper wrapper) {
                        if (wrapper != null) {
                            mCurWrapper = wrapper;
                            mBinding.setWrapper(mCurWrapper);
                            String stagePhoto = "";
                            if (wrapper.stagePhotos != null && wrapper.stagePhotos.size() > 0) {
                                Random random = new Random();
                                int index = random.nextInt(wrapper.stagePhotos.size());
                                stagePhoto = wrapper.stagePhotos.get(index).imgUrl;
                            }
//                GlideTools.GlideWrapper(this, wrapper.movie.poster).error(R.mipmap.ic_poster_default)
//                        .into(mBinding.ivCover);
//                GlideTools.GlideWrapper(this, stagePhoto)
//                        .apply(RequestOptions.bitmapTransform(new GlideBlurTransformation(MovieDetailActivity.this, 25, 3)))
//                        .error(R.mipmap.ic_poster_default)
//                        .into(mBinding.ivStage);
                            mBinding.btnFavorite.setSelected(wrapper.movie.isFavorite);
                            if(!TextUtils.isEmpty(stagePhoto)){
                                GlideTools.GlideWrapper(MovieDetailActivity.this,stagePhoto).into(mBinding.ivStagephoto);
                            }else{
                                GlideTools.GlideWrapper(MovieDetailActivity.this,wrapper.movie.poster).into(mBinding.ivStagephoto);
                            }
                            mViewModel.loadTags().subscribe(new SimpleObserver<List<String>>() {
                                @Override
                                public void onAction(List<String> tagList) {
                                    Context context=MovieDetailActivity.this;
                                    int paddingTop=DensityUtil.dip2px(context,5);
                                    int paddingLeft=DensityUtil.dip2px(context,11);
                                    mBinding.viewTags.removeAllViews();
                                    for(String tag:tagList){
                                        TextView textView=new TextView(context);
                                        textView.setText(tag);
                                        textView.setTextSize(20);
                                        textView.setTextColor(Color.parseColor("#FFCCCCCC"));
                                        textView.setBackground(getDrawable(R.drawable.detail_tag));
                                        textView.setPadding(paddingLeft,paddingTop,paddingLeft,paddingTop);
                                        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                        layoutParams.rightMargin=DensityUtil.dip2px(context,23);
                                        mBinding.viewTags.addView(textView,layoutParams);
                                    }
                                }
                            });
                            mViewModel.loadRecommand(mCurWrapper.genres).subscribe(new SimpleObserver<List<MovieDataView>>() {
                                @Override
                                public void onAction(List<MovieDataView> movieDataViewList) {
                                    if(movieDataViewList!=null&&movieDataViewList.size()>0) {
                                        mBinding.setRecommand(true);
                                        mRecommandMovieAdapter.addAll(movieDataViewList);
                                        mRecommandMovieAdapter.notifyDataSetChanged();
                                    }else{
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

    /**
     * 组合未识别文件数据
     *
     * @param keyword
     */
    private void prepareUnrecogizedFile(String keyword) {
        mViewModel.loadUnrecogizedFile(keyword, unrecognizedFileDataViewList -> {
            if (unrecognizedFileDataViewList != null && unrecognizedFileDataViewList.size() > 0) {
                Glide.with(this).load(R.mipmap.default_poster).error(R.mipmap.default_poster).into(mBinding.ivStagephoto);
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
     * 编辑封面信息
     */
    private void editVideoInfo() {
        String keyword = mBinding.getWrapper().videoFiles.get(0).keyword;
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((movie_id,source,type) -> {
            startLoading();
            boolean is_favoirte = false;//默认收藏状态为false
            if (mCurWrapper != null && mCurWrapper.movie != null) {
                String last_movie_id = mCurWrapper.movie.movieId;
                is_favoirte = mCurWrapper.movie.isFavorite;//获取当前收藏状态
                //TODO 更正发送信息
                BroadcastHelper.sendBroadcastMovieUpdateSync(this, last_movie_id, movie_id, is_favoirte ? 1 : 0);//向手机助手发送电影更改的广播
            } else {
                BroadcastHelper.sendBroadcastMovieAddSync(this, movie_id);//向手机助手发送添加电影的广播
            }
            //选择新电影逻辑
            mViewModel.selectMovie( movie_id,source,type, is_favoirte, movieWrapper -> {
                prepareMovieWrapper(movieWrapper.movie.id);
                refreshParent();
                stopLoading();
            });
        });
        movieSearchFragment.show(getSupportFragmentManager(), "");
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
                mBinding.btnFavorite.setSelected(isFavorite);
                refreshParent();
            }
        });
    }

    public void showRadioDialog() {
        VideoSelectDialog dialogFragment = VideoSelectDialog.newInstance(mBinding.getWrapper());
        dialogFragment.setPlayingVideo(new VideoSelectDialog.PlayVideoListener() {
            @Override
            public void playVideo(Observable<String> observable) {
                observable.subscribe(new SimpleObserver<String>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        startLoading();
                    }

                    @Override
                    public void onAction(String s) {
                        refreshParent();
                    }
                });
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "detail");
    }

    public void showViewMoreDialog(){
        MovieDetialPlotDialog dialog=MovieDetialPlotDialog.newInstance(mBinding.nestScrollview);
        dialog.show(getSupportFragmentManager(),"");
    }

    private void playVideo(String path, String name) {
        startLoading();
        mViewModel.playingVideo(path, name);
        refreshParent();
    }

    /**
     * 返回时刷新主页
     */
    private void refreshParent() {
        setResult(RESULT_OK);
    }
}
