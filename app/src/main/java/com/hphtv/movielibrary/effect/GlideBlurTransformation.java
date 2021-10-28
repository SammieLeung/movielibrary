package com.hphtv.movielibrary.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.renderscript.RSRuntimeException;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.hphtv.movielibrary.util.GaussianBlurUtil;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import jp.wasabeef.glide.transformations.internal.FastBlur;
import jp.wasabeef.glide.transformations.internal.RSBlur;


/**
 * author: Sam Leung
 * date:  2021/10/28
 */
public class GlideBlurTransformation extends BitmapTransformation {
    private static String STRING_CHARSET_NAME = "UTF-8";
    private static String ID = "com.hphtv.movielibrary.effect.GlideBlurTransformation";
    private static Charset CHARSET = Charset.forName(STRING_CHARSET_NAME);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);
    private Context mContext;
    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;
    private int mSampling;
    private int mRadius;
    private BitmapPool mBitmapPool;

    public GlideBlurTransformation(Context context) {
        this(context, Glide.get(context).getBitmapPool(), MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(Context context, BitmapPool pool) {
        this(context, pool, MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(Context context, BitmapPool pool, int radius) {
        this(context, pool, radius, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(Context context, int radius) {
        this(context, Glide.get(context).getBitmapPool(), radius, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(Context context, int radius, int sampling) {
        this(context, Glide.get(context).getBitmapPool(), radius, sampling);
    }

    public GlideBlurTransformation(Context context, BitmapPool pool, int radius, int sampling) {
        mContext = context.getApplicationContext();
        mBitmapPool = pool;
        mRadius = radius;
        mSampling = sampling;
    }

    @Override
    protected Bitmap transform(@NonNull @NotNull BitmapPool pool, @NonNull @NotNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap source = toTransform;

        int width = source.getWidth();
        int height = source.getHeight();
        int scaleWidth = width / mSampling;
        int scaleHeight = height / mSampling;

        Bitmap bitmap = mBitmapPool.get(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);
        bitmap = FastBlur.blur(bitmap,mRadius,true);
        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull @NotNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlideBlurTransformation;
    }

}
