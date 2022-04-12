package com.hphtv.movielibrary.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * NestedScrollView/Srollview包裹Viewpager自适应高度+禁止滑动
 *
 * 使用方法有四点必须实现：
 * 1，先把AutofitHeightViewPager像Android原生的ViewPager一样写进xml布局。
 * 2，在上层Java代码的mViewPager添加的切换事件addOnPageChangeListener里面每次updateHeight
 * mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 *             @Override
 *             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 *
 *             }
 *
 *             @Override
 *             public void onPageSelected(int position) {
 *                 mViewPager.updateHeight(position);
 *             }
 *
 *             @Override
 *             public void onPageScrollStateChanged(int state) {
 *
 *             }
 *         });
 *
 * 3，构建mViewPager肯定要设置FragmentPagerAdapter，在把创建Fragment喂给FragmentPagerAdapter时候，给Fragment把当前Fragment位置position设置进去
 *         Fragment xxxFragment = new XXXFragment();
 *         Bundle xxxBunle = new Bundle();
 *         xxxBunle.putInt(AutofitHeightViewPager.POSITION, 1); //1为当前位置，这里的位置索引即为要传递的值，不同的Fragment按照先后添加顺序添加和传递索引
 *         xxxFragment.setArguments(xxxBunle);
 *         mPagerAdapter.addFragment(xxxFragment)
 * 4，然后转入到具体的XXXFragment里面，把当前XXXFragment创建的View高度传回给AutofitHeightViewPager,一般是在onCreateView：
 *  int pos = getArguments().getInt(AutofitHeightViewPager.POSITION);
 *         (强制转换为ViewPager所在的Activity) getActivity()).mViewPager.setViewPosition(view, pos);
 * ————————————————
 * 版权声明：本文为CSDN博主「zhangphil」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/zhangphil/article/details/80294038
 * @author lxp
 * @date 20-9-22
 */
public class NoScrollAutofitHeightViewPager extends ViewPager {
    public static final String POSITION = "position";

    private int mCurPosition;
    private int mHeight = 0;
    private HashMap<Integer, View> mChildrenViews = new LinkedHashMap<Integer, View>();

    private boolean isScorll = false;

    public NoScrollAutofitHeightViewPager(@NonNull Context context) {
        super(context);
    }

    public NoScrollAutofitHeightViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateHeight(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mChildrenViews.size() > mCurPosition) {
            View child = mChildrenViews.get(mCurPosition);
            if (child != null) {
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                mHeight = child.getMeasuredHeight();
            }
        }

        if (mHeight != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void updateHeight(int current) {
        this.mCurPosition = current;
        if (mChildrenViews.size() > current) {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeight);
            } else {
                layoutParams.height = mHeight;
            }

            setLayoutParams(layoutParams);
        }
    }

    public void setViewPosition(View view, int position) {
        mChildrenViews.put(position, view);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScorll)
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }


    public boolean isScorll() {
        return isScorll;
    }

    public void setScorll(boolean scorll) {
        isScorll = scorll;
    }


    @Override
    public boolean arrowScroll(int direction) {
        //禁止viewpager翻页qgq
        return false;
    }
}
