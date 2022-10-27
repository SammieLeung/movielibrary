package com.hphtv.movielibrary.ui.detail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.hphtv.movielibrary.adapter.ActorPosterItemListApdater;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.LayoutNewDetailViewmoreBinding;
import com.hphtv.movielibrary.effect.GlideBlurTransformation;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/1/19
 */
public class MovieDetailPlotDialog extends BaseDialogFragment2<MovieDetailViewModel, LayoutNewDetailViewmoreBinding> {
    WeakReference<View> mViewWeakReference;
    private ActorPosterItemListApdater mActorPosterItemListApdater;
    private Bitmap mScreenBitmap;
    private Disposable mGetBitmapDisposable;

    public static MovieDetailPlotDialog newInstance(View view) {
        Bundle args = new Bundle();
        MovieDetailPlotDialog fragment = new MovieDetailPlotDialog();
        fragment.mViewWeakReference = new WeakReference<>(view);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected MovieDetailViewModel createViewModel() {
        return mViewModel = new ViewModelProvider(getActivity()).get(MovieDetailViewModel.class);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        MovieWrapper movieWrapper= mViewModel.getMovieWrapper();
        mActorPosterItemListApdater = new ActorPosterItemListApdater(getContext(), movieWrapper.actors);
        mBinding.rvActorList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mBinding.rvActorList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 62), DensityUtil.dip2px(getContext(), 22), DensityUtil.dip2px(getContext(), 22)));
        mBinding.rvActorList.setAdapter(mActorPosterItemListApdater);
        String plot=movieWrapper.movie.plot;
        if(movieWrapper.movie.type.equals(Constants.VideoType.tv)&&movieWrapper.season!=null&&!TextUtils.isEmpty(movieWrapper.season.plot))
            plot=movieWrapper.season.plot;
        mBinding.setPlot(plot);
        mBinding.btnFold.setOnClickListener(v -> dismiss());
        mViewModel.loadFileList().subscribe(new SimpleObserver<String>() {
            @Override
            public void onAction(String s) {
                mBinding.setFilelist(s);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            View bgView = mViewWeakReference.get();
            int count = 0;
            //需要目标view先获取到高度
            while (mBinding.ivFastblur.getWidth() <= 0 && mBinding.ivFastblur.getHeight() <= 0) {
                if (count >= 200) {
                    emitter.onComplete();
                    return;
                }
                Thread.sleep(1);
                count++;
            }
            mScreenBitmap = Bitmap.createBitmap(bgView.getWidth(), bgView.getHeight(), Bitmap.Config.ARGB_8888);//创建空白bitmap
            HandlerThread handlerThread = new HandlerThread("getBitmap");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            //通过pixelCopy获取全屏截图，大约50-90ms
            PixelCopy.request(getActivity().getWindow(), mScreenBitmap, copyResult -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    Bitmap bitmap = getPartBitmap(mBinding.ivFastblur, bgView, mScreenBitmap);//截取需要的部分
                    emitter.onNext(bitmap);
                }
                emitter.onComplete();
            }, handler);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mGetBitmapDisposable=d;
                    }

                    @Override
                    public void onAction(Bitmap bitmap) {
                        DrawableCrossFadeFactory.Builder builder = new DrawableCrossFadeFactory.Builder(100);//淡入动画100ms
                        DrawableCrossFadeFactory fadeFactory = builder.setCrossFadeEnabled(false).build();
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.optionalTransform(new GlideBlurTransformation(getContext()));//Glide模糊处理
                        Glide.with(getContext())
                                .load(bitmap)
                                .transition(DrawableTransitionOptions.with(fadeFactory))
                                .apply(requestOptions)
                                .into(mBinding.ivFastblur);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        mGetBitmapDisposable=null;
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mGetBitmapDisposable=null;
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGetBitmapDisposable!=null){
            mGetBitmapDisposable.dispose();
            mGetBitmapDisposable=null;
        }
    }


    private Bitmap getPartBitmap(ImageView imageView, View tagetView, Bitmap bitmap) {
        //获取屏幕整张图片
        if (bitmap != null) {
            //需要截取的长和宽
            int outWidth = imageView.getWidth();
            int outHeight = imageView.getHeight();

            //获取需要截图部分的在屏幕上的坐标(view的左上角坐标）
            int[] viewLocationArray = new int[2];


            imageView.getLocationOnScreen(viewLocationArray);

            int[] listLocationArray = new int[2];
            tagetView.getLocationOnScreen(listLocationArray);

            //从屏幕整张图片中截取指定区域
            bitmap = Bitmap.createBitmap(bitmap, viewLocationArray[0] - listLocationArray[0], viewLocationArray[1] - listLocationArray[1], outWidth, outHeight);
        }
        return bitmap;
    }

}
