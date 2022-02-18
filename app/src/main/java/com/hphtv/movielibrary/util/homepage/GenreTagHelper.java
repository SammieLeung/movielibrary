package com.hphtv.movielibrary.util.homepage;

import android.content.res.TypedArray;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;

import java.util.Arrays;
import java.util.List;


/**
 * author: Sam Leung
 * date:  2022/2/17
 */
public class GenreTagHelper {
    public static GenreTagHelper sGenreTagHelper;

    public static GenreTagHelper getInstance(){
        if(sGenreTagHelper==null){
            synchronized (GenreTagHelper.class){
                if(sGenreTagHelper==null)
                    sGenreTagHelper=new GenreTagHelper();
            }
        }
        return sGenreTagHelper;
    }

    @BindingAdapter("bindingBackground")
    public static void bindingBackground(ImageView v,String tag){
        if(!TextUtils.isEmpty(tag)) {
            if (tag.equals(v.getContext().getResources().getString(R.string.genre_add))) {
                Glide.with(v.getContext()).load(R.drawable.bg_add).into(v);
            } else {
                String[] tagArr = v.getContext().getResources().getStringArray(R.array.genre_tags);
                TypedArray ta = v.getContext().getResources().obtainTypedArray(R.array.genre_bg);

                List<String> tagList = Arrays.asList(tagArr);
                int pos = tagList.indexOf(tag);
                Glide.with(v.getContext())
                        .load(ta.getResourceId(pos, 0))
                        .into(v);
                ta.recycle();
            }
        }
    }
}
