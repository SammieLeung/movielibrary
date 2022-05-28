package com.hphtv.movielibrary.ui.view;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hphtv.movielibrary.R;
import com.station.kit.util.LogUtil;

/**
 * author: Sam Leung
 * date:  2022/4/28
 */
public class GifTextView extends LinearLayout {
    public static final String TAG=GifTextView.class.getSimpleName();

    private final static float sZoomRatio = 1.1f;
    private SimpleDraweeView mSimpleDraweeViewStart = null;
    private TextView mTextView = null;
    private String mTitle="";
    private Animatable mAnimatable;
    private boolean isSelected = false;

    public GifTextView(Context context) {
        this(context, null);
    }

    public GifTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setGravity(Gravity.CENTER);
        setOnFocusChangeListener(mOnFocusChangeListener);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GifTextView);
        addGifSrcStart(ta);
        addTextView(ta);
        ta.recycle();
    }

    private OnFocusChangeListener mOnFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            zoomIn();
            start();
        } else {
            stop();
            zoomOut();
        }
    };

    public interface ImageLoaderListener {
        void onImageSet(String id, ImageInfo info, Animatable anim);
    }

    private ImageLoaderListener mLoaderListener;

    public void setLoaderListener(ImageLoaderListener loaderListener) {
        mLoaderListener = loaderListener;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        isSelected = selected;
        if (selected)
            showGif();
        else
            hideGif();
    }

    public void setGifText(String text){
        mTitle=text;
        if(mTextView!=null)
            mTextView.setText(mTitle);
    }

    public void showGif() {
        LogUtil.v(TAG,"showGif()");
        if (mSimpleDraweeViewStart != null)
            mSimpleDraweeViewStart.setVisibility(View.VISIBLE);
    }

    public void hideGif() {
        LogUtil.v(TAG,"hideGif()");
        if (mSimpleDraweeViewStart != null)
            mSimpleDraweeViewStart.setVisibility(View.GONE);
    }

    public void start() {
        if (mAnimatable != null && mSimpleDraweeViewStart.getVisibility() == VISIBLE)
            mAnimatable.start();
    }

    public void stop() {
        if (mAnimatable != null && mSimpleDraweeViewStart.getVisibility() == VISIBLE)
            mAnimatable.stop();
    }

    private void zoomIn() {
        ViewCompat.animate(this).scaleX(sZoomRatio).scaleY(sZoomRatio).translationZ(1).setDuration(200).start();
    }

    private void zoomOut() {
        ViewCompat.animate(this).scaleX(1).scaleY(1).translationZ(0).setDuration(200).start();
    }

    public void addGifSrcStart(TypedArray ta) {
        if (mSimpleDraweeViewStart == null) {
            mSimpleDraweeViewStart = new SimpleDraweeView(getContext());
            addView(mSimpleDraweeViewStart);
            if (!isSelected())
                mSimpleDraweeViewStart.setVisibility(View.GONE);
        }
        int resId = -1;
        int gifWidth = LayoutParams.MATCH_PARENT;
        int gifHeight = LayoutParams.MATCH_PARENT;
        int gifPadding = 0;
        LayoutParams lp = new LayoutParams(gifWidth, gifHeight);
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int index = ta.getIndex(i);
            switch (index) {
                case R.styleable.GifTextView_gifDrawStart:
                    resId = ta.getResourceId(index, -1);
                    Uri uri = Uri.parse("res://" + getContext().getPackageName() + "/" + resId);
                    ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(
                                String id,
                                @Nullable ImageInfo imageInfo,
                                @Nullable Animatable anim) {
                            if (anim != null) {
                                // 其他控制逻辑
                                mAnimatable = anim;
                                if (mLoaderListener != null)
                                    mLoaderListener.onImageSet(id, imageInfo, anim);
                            }
                        }
                    };

                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setUri(uri)
                            .setControllerListener(controllerListener)
                            .setAutoPlayAnimations(true)
                            .build();

                    mSimpleDraweeViewStart.setController(controller);
                    break;
                case R.styleable.GifTextView_gifPlaceHolder:
                    int placeHolderResId = ta.getResourceId(index, -1);
                    GenericDraweeHierarchy hierarchy = mSimpleDraweeViewStart.getHierarchy();
                    if (hierarchy != null) {
                        hierarchy.setPlaceholderImage(placeHolderResId, ScalingUtils.ScaleType.CENTER_INSIDE);
                    } else {
                        builder.setPlaceholderImage(placeHolderResId)
                                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
                        mSimpleDraweeViewStart.setHierarchy(builder.build());
                    }
                    break;
                case R.styleable.GifTextView_gifDrawWidth:
                    gifWidth = ta.getDimensionPixelOffset(index, -1);
                    lp.width = gifWidth;
                    break;
                case R.styleable.GifTextView_gifDrawHeight:
                    gifHeight = ta.getDimensionPixelOffset(index, -1);
                    lp.height = gifHeight;
                    break;
                case R.styleable.GifTextView_gifDrawPadding:
                    gifPadding = ta.getDimensionPixelOffset(index, 0);
                    lp.rightMargin = gifPadding;
                    break;
            }
        }

        mSimpleDraweeViewStart.setLayoutParams(lp);

    }


    private void addTextView(TypedArray ta) {
        if (mTextView == null) {
            mTextView = new TextView(getContext());
            addView(mTextView);
        }
        CharSequence gifText = "";
        ColorStateList gifTextColorStateList;
        int gifTextStyle = 0;
        int gifTextSize = -1;
        int textUnit = -1;
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int index = ta.getIndex(i);
            switch (index) {
                case R.styleable.GifTextView_gifText:
                    gifText = ta.getText(index);
                    mTitle=gifText.toString();
                    mTextView.setText(gifText);
                    break;
                case R.styleable.GifTextView_gifTextColor:
                    gifTextColorStateList = ta.getColorStateList(index);
                    mTextView.setTextColor(gifTextColorStateList);
                    break;
                case R.styleable.GifTextView_gifTextStyle:
                    gifTextStyle = ta.getInt(index, gifTextStyle);
                    mTextView.setTypeface(null, gifTextStyle);
                    break;
                case R.styleable.GifTextView_gifTextSize:
                    gifTextSize = ta.getDimensionPixelSize(index, gifTextSize);//获取已经经过转换的px值
                    mTextView.setTextSize(COMPLEX_UNIT_PX, gifTextSize);//setTextSize默认设置的是SP的值
                    break;
            }
        }

    }

}
