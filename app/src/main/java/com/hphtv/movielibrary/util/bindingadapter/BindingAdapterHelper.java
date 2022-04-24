package com.hphtv.movielibrary.util.bindingadapter;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;

import java.util.Arrays;
import java.util.List;


/**
 * author: Sam Leung
 * date:  2022/2/17
 */
public class BindingAdapterHelper {
    @BindingAdapter("bindingBackground")
    public static void bindingBackground(ImageView v, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equals(v.getContext().getResources().getString(R.string.genre_add))) {
                Glide.with(v.getContext()).load(R.mipmap.bg_add).into(v);
            } else if (tag.equals(v.getContext().getResources().getString(R.string.genre_all))) {
                Glide.with(v.getContext()).load(R.mipmap.bg_documentary).into(v);
            } else {
                String[] tagArr = v.getContext().getResources().getStringArray(R.array.genre_tags);
                TypedArray ta = v.getContext().getResources().obtainTypedArray(R.array.genre_bg);

                List<String> tagList = Arrays.asList(tagArr);
                int pos = tagList.indexOf(tag);
                if (pos < 0)
                    pos = 0;
                Glide.with(v.getContext())
                        .load(ta.getResourceId(pos, 0))
                        .into(v);
                ta.recycle();
            }
        }
    }

    @BindingAdapter("filterSelect")
    public static void selectBackground(TextView textView, boolean selected) {
        if (selected == true) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, textView.getContext().getDrawable(R.drawable.ic_icon_filter_check), null);
            textView.getPaint().setFakeBoldText(true);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            textView.getPaint().setFakeBoldText(false);
        }
    }


    @BindingAdapter(value = {"orderSelect", "orderDesc"}, requireAll = false)
    public static void setOrderSelect(TextView textView, boolean selected, ObservableBoolean isDesc) {
        if (selected == true) {
            Drawable drawable = textView.getContext().getDrawable(isDesc.get() ? R.drawable.desc_order : R.drawable.asc_order);
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
            textView.getPaint().setFakeBoldText(true);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            textView.getPaint().setFakeBoldText(false);
        }
    }

    @BindingAdapter(value = "textViewSelect")
    public static void setTextViewSelected(TextView view, boolean selected) {
        view.setSelected(selected);
        view.getPaint().setFakeBoldText(selected);
    }

    @BindingAdapter(value = "viewSelect")
    public static void setViewSelected(View view, boolean selected) {
        view.setSelected(selected);
    }


}
