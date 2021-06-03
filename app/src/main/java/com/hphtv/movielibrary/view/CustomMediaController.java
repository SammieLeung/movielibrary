package com.hphtv.movielibrary.view;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;

import com.hphtv.movielibrary.R;

/**
 * Created by tchip on 17-9-29.
 */

public class CustomMediaController extends MediaController {
    private Context context;
    private boolean isFullScreen = false;

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMediaController(Context context) {
        super(context);
        this.context = context;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
        try {
            //新的seekbar
            SeekBar newSeekBar = (SeekBar) LayoutInflater.from(getContext()).inflate(
                    R.layout.my_seekbar, null);

            //MediaController对象
            Field mRoot = android.widget.MediaController.class
                    .getDeclaredField("mRoot");
            mRoot.setAccessible(true);

            ViewGroup mRootVg = (ViewGroup) mRoot.get(this);
            ViewGroup vg = findSeekBarParent(mRootVg);//递归查找包含seekbar的ViewGroup

            int seekbar_index = 1;
            for (int i = 0; i < vg.getChildCount(); i++) {//2.获取子控件在容器里的位置 （唯一控件）

                if (vg.getChildAt(i) instanceof SeekBar) {
                    seekbar_index = i;
                    break;
                }
            }

            vg.removeViewAt(seekbar_index);//删除原来的seekbar
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            newSeekBar.setPadding(DensityUtil.dip2px(context, 15), 0, DensityUtil.dip2px(context, 15), 0);
            vg.addView(newSeekBar, seekbar_index, params);//添加自定义seekbar


            //获取播放时间textView
            Field mCurrentTime = android.widget.MediaController.class.getDeclaredField("mCurrentTime");
            mCurrentTime.setAccessible(true);
            TextView tv_currenttime = (TextView) mCurrentTime.get(this);
            LinearLayout.LayoutParams tv_currenttime_lp = (LinearLayout.LayoutParams) tv_currenttime.getLayoutParams();
            int tv_currenttime_index = vg.indexOfChild(tv_currenttime);//1.获取子控件在容器里的位置 （具体的控件）
            //播放时间textview控件放在seekbar后
            vg.removeViewAt(tv_currenttime_index);
            vg.addView(tv_currenttime, seekbar_index, tv_currenttime_lp);

            //播放时长和视频时长之间添加一个分隔符‘/’
            TextView split = new TextView(context);
            split.setText("/");
            split.getPaint().setFakeBoldText(true);
            split.setPadding(DensityUtil.dip2px(context, 4), DensityUtil.dip2px(context, 4), DensityUtil.dip2px(context, 4), 0);
            vg.addView(split, seekbar_index + 1, tv_currenttime_lp);

            //播放/暂停按钮
            Field mPauseButton = android.widget.MediaController.class.getDeclaredField("mPauseButton");
            mPauseButton.setAccessible(true);
            final ImageButton ib_pause_button = (ImageButton) mPauseButton.get(this);
            ib_pause_button.setBackgroundColor(context.getResources().getColor(R.color.colornull));
            ib_pause_button.setFocusable(false);
            newSeekBar.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                            ib_pause_button.performClick();
                            return true;
                        }
                    }
                    return false;
                }
            });
            LinearLayout ll_btn_parents = ((LinearLayout) ib_pause_button.getParent());
            ll_btn_parents.removeView(ib_pause_button);//删除pause按钮使其可以添加到其他容器里
            ll_btn_parents.setVisibility(LinearLayout.GONE);//隐藏容器
            vg.addView(ib_pause_button, 0);

            //将控件的高度统一为imagebutton的高度
            int pHeight = ib_pause_button.getLayoutParams().height;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                LinearLayout.LayoutParams cLP = (LinearLayout.LayoutParams) child.getLayoutParams();
                cLP.height = pHeight;
                child.setLayoutParams(cLP);
                if (child instanceof TextView) {
                    //修改TextView控件内容的排列方式
                    ((TextView) child).setGravity(Gravity.CENTER);
                }
            }

//            //添加全屏按钮以及注册全屏接口
//            final ImageButton ibFullScreen= (ImageButton) LayoutInflater.from(context).inflate(R.layout.my_imagebutton,null);
//            LinearLayout.LayoutParams ibFullScreenParams = new LinearLayout.LayoutParams(
//                    pHeight,pHeight);
//            vg.addView(ibFullScreen,ibFullScreenParams);
//            ibFullScreen.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(isFullScreen){
//                        ibFullScreen.setImageDrawable(context.getResources().getDrawable(R.mipmap.fullscreen));
//                    }else{
//                        ibFullScreen.setImageDrawable(context.getResources().getDrawable(R.mipmap.smallscreen));
//                    }
//                    mFullScreenListener.OnFullScreen(isFullScreen);
//                    MyMediaController.this.show();
//                    isFullScreen=!isFullScreen;
//                }
//            });

            //重新注册progressbar事件
            Field mProgress = android.widget.MediaController.class
                    .getDeclaredField("mProgress");
            mProgress.setAccessible(true);
            mProgress.set(this, newSeekBar);//将该字段对象设置为指定的对象

            Field mSeekListener = android.widget.MediaController.class
                    .getDeclaredField("mSeekListener");
            mSeekListener.setAccessible(true);//获取OnSeekBarChangeListener
            //与源码同样的设置
            newSeekBar.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) mSeekListener
                    .get(this));
            newSeekBar.setMax(1000);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ViewGroup findSeekBarParent(ViewGroup vg) {
        ViewGroup viewGroup = null;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View view = vg.getChildAt(i);
            if (view instanceof SeekBar) {
                viewGroup = (ViewGroup) view.getParent();
                break;
            } else if (view instanceof ViewGroup) {
                viewGroup = findSeekBarParent((ViewGroup) view);
            } else {
                continue;
            }
        }
        return viewGroup;
    }

    private OnFullScreenListener mFullScreenListener;


    public interface OnFullScreenListener {
        public void OnFullScreen(Boolean isFullScreen);
    }

    public void setOnFullScreenListener(OnFullScreenListener listener) {
        this.mFullScreenListener = listener;
    }

    public void changeFullScreenState() {
        if (mFullScreenListener != null)
            mFullScreenListener.OnFullScreen(isFullScreen);
    }
}
