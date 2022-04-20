package com.hphtv.movielibrary.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;

/**
 * author: Sam Leung
 * date:  2021/9/6
 */
public class GlideTools {

    private static GlideUrl buildGlideUrl(String photo) {
        GlideUrl path;
        //需要特殊处理
        if (photo.startsWith("http://img31.mtime.cn/")) {
            path = new GlideUrl(photo,
                    new LazyHeaders.Builder()
                            .addHeader("Accept-Encoding", "gzip, deflate")
                            .build());
        } else {
            path = new GlideUrl(photo,
                    new LazyHeaders.Builder()
                            .build());
        }
        return path;
    }

    public static RequestBuilder<Drawable> GlideWrapper(Context context, String path) {
        if (TextUtils.isEmpty(path) || !(path.startsWith("http") || path.startsWith("/")||path.lastIndexOf(".")==-1)||path.endsWith("/")) {
            return Glide.with(context)
                    .load(R.mipmap.default_poster);
        } else {
            return Glide.with(context)
                    .load(path)
//                    .load(buildGlideUrl(path))
//                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .signature(new ObjectKey(Constants.GLIDE_CACHE_VERSION))
                    .placeholder(R.mipmap.default_poster)
                    .fallback(R.mipmap.default_poster)
                    .error(R.mipmap.default_poster);
        }
    }

    public static RequestBuilder<Drawable> GlideWrapperWithCrossFade(Context context, String path) {
        DrawableCrossFadeFactory.Builder builder=new DrawableCrossFadeFactory.Builder(500);
        DrawableCrossFadeFactory fadeFactory=builder.setCrossFadeEnabled(false).build();
        if (TextUtils.isEmpty(path) || !(path.startsWith("http") || path.startsWith("/")||path.lastIndexOf(".")==-1)||path.endsWith("/")) {
            return Glide.with(context)
                    .load(R.mipmap.default_poster);
        } else {
            return Glide.with(context)
                    .load(path)
//                    .load(buildGlideUrl(path))
//                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.with(fadeFactory))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .signature(new ObjectKey(Constants.GLIDE_CACHE_VERSION))
                    .placeholder(R.mipmap.default_poster)
                    .fallback(R.mipmap.default_poster)
                    .error(R.mipmap.default_poster);
        }
    }


}
