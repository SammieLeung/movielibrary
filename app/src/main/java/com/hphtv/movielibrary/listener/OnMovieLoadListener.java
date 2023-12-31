package com.hphtv.movielibrary.listener;

import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * author: Sam Leung
 * date:  2021/8/16
 */
public abstract class OnMovieLoadListener extends RecyclerView.OnScrollListener {
    private int countItem;
    private int lastItem;
    private int firstItem;
    private boolean isScolled = false;//是否可以滑动

    /**
     * 加载回调方法
     *
     * @param countItem 总数量
     * @param lastItem  最后显示的position
     */
    protected abstract void onLoadingNext(int countItem, int lastItem);

    protected  void onLoadingPre(int count, int firstItem){
    }

    protected void onScrollStart() {
    }


    protected void onScrolledEnd() {
    }


    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        /* 测试这三个参数的作用
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            Log.d("test", "SCROLL_STATE_IDLE,空闲");
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            Log.d("test", "SCROLL_STATE_DRAGGING,拖拽");
        } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
            Log.d("test", "SCROLL_STATE_SETTLING,固定");
        } else {
            Log.d("test", "其它");
        }//*/
        //拖拽或者惯性滑动时isScolled设置为true
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
            isScolled = true;
            onScrollStart();
        } else {
            isScolled = false;
            onScrolledEnd();
        }

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            countItem = layoutManager.getItemCount();
            lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
            firstItem = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (layoutManager instanceof VisibleItemListener)
                ((VisibleItemListener) layoutManager).getFirstVisibleItem(layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition()));
        }
        if (isScolled && countItem != lastItem && lastItem == countItem - 1) {
            onLoadingNext(countItem, lastItem);
        }
        if (isScolled &&  firstItem == 0) {
            onLoadingPre(countItem, firstItem);
        }
    }
}
