package com.hphtv.movielibrary.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.ui.BaseFragment2;

/**
 * author: Sam Leung
 * date:  2022/2/19
 */
public class TvRecyclerView extends RecyclerView {
    private static final String TAG = "TvRecyclerView";
    private int mPosition;
    private BaseFragment2 mBindFragment;
    private View mHolderFocusView;

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TvRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        initView();
        //initAttr(context, attrs);
    }

    public void setBindFragment(BaseFragment2 fragment) {
        this.mBindFragment = fragment;
    }

    private void initView() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);//设置RecyclerView和他的后代的
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);

        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        /**
         防止RecyclerView刷新时焦点不错乱bug的步骤如下:
         (1)adapter执行setHasStableIds(true)方法
         (2)重写getItemId()方法,让每个view都有各自的id
         (3)RecyclerView的动画必须去掉
         */
        setItemAnimator(null);
    }

    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean hasFocus() {
        return super.hasFocus();
    }


    @Override
    public boolean isInTouchMode() {
        // 解决4.4版本抢焦点的问题
        if (Build.VERSION.SDK_INT == 19) {
            return !(hasFocus() && !super.isInTouchMode());
        } else {
            return super.isInTouchMode();
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();

        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;

        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int offScreenTop = Math.min(0, childTop - parentTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);


        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }

        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            postInvalidate();
            return true;
        }
        return false;
    }


    @Override
    public int getBaseline() {
        return -1;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }

    public void setHolderFocusView(View mHolderFocusView) {
        this.mHolderFocusView = mHolderFocusView;
    }

    /**
     * 判断是垂直，还是横向.
     */
    private boolean isVertical() {
        LayoutManager manager = getLayoutManager();
        if (manager != null) {
            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            return layout.getOrientation() == LinearLayoutManager.VERTICAL;

        }
        return false;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if (null != view) {

            mPosition = getChildAdapterPosition(view) - getFirstVisiblePosition();
            if (mPosition < 0) {
                return i;
            } else {
                if (i == childCount - 1) {
                    if (mPosition > i) {
                        mPosition = i;
                    }
                    return mPosition;
                }
                if (i == mPosition) {
                    return childCount - 1;
                }
            }
        }
        return i;
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildAdapterPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if (childCount == 0)
            return 0;
        else
            return getChildAdapterPosition(getChildAt(childCount - 1));
    }

    /**
     * 设置为0，这样可以防止View获取焦点的时候，ScrollView自动滚动到焦点View的位置
     */

    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }

    private float mVerticalScrollFactor = 20.f;

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {// 鼠标滑轮滑动事件
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (event.getAction() == MotionEvent.ACTION_SCROLL
                    && getScrollState() == SCROLL_STATE_IDLE) {// 鼠标滑轮事件执行&&RecyclerView不是真正滑动
                final float vscroll = event
                        .getAxisValue(MotionEvent.AXIS_VSCROLL);// 获取轴线距离
                if (vscroll != 0) {
                    final int delta = -1
                            * (int) (vscroll * mVerticalScrollFactor);
//                    Log.v(TAG, "onGenericMotionEvent()==>");

                    if (canScrollVertically(delta > 0 ? 1 : -1)) {
                        smoothScrollBy(0, delta);
                        Log.v(TAG, "canScrollVertically");
                        return true;

                    } else if (canScrollHorizontally(delta > 0 ? 1 : -1)) {
                        Log.v(TAG, "canScrollHorizontally");
                        smoothScrollBy(delta * 2, 0);
                        return true;
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        View focusView = this.getFocusedChild();
        if (focusView == null) {
            return result;
        } else {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                return true;
            } else {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        View rightView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_RIGHT);
                        if (rightView != null) {
                            rightView.requestFocus();
                            return handleSmoothScrollToCenterActionForDpadRight(rightView);
                        } else {
                            boolean focusResult = false;
                            if (mOnNoNextFocusListener != null)
                                focusResult = mOnNoNextFocusListener.enforceHandleFocusRight(focusView);
                            if (focusResult) {
                                return true;
                            } else {
                                //横向 最右一项，不响应
                                return !isVertical();
                            }
                        }
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        View leftView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_LEFT);
                        if (leftView != null) {
                            leftView.requestFocus();
                            return handleSmoothScrollToCenterActionForDpadLeft(leftView);
                        } else {
                            boolean focusResult = false;
                            if (mOnNoNextFocusListener != null)
                                focusResult = mOnNoNextFocusListener.enforceHandleFocusLeft(focusView);
                            if (focusResult) {
                                return true;
                            } else {
                                //横向 最左一项，不响应
                                return !isVertical();
                            }
                        }
                    case KeyEvent.KEYCODE_DPAD_DOWN:

                        View downView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_DOWN);
                        if (downView != null) {
                            downView.requestFocus();
                            return handleSmoothScrollToCenterActionForDpadDown(downView);
                        } else {
                            boolean focusResult = false;
                            if (mOnNoNextFocusListener != null)
                                focusResult = mOnNoNextFocusListener.enforceHandleFocusDown(focusView);
                            if (focusResult) {
                                return true;
                            } else {
                                return isVertical();//垂直最下一项 ，不响应
                            }
                        }
                    case KeyEvent.KEYCODE_DPAD_UP:
                        View upView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_UP);
                        if (upView != null) {
                            upView.requestFocus();
                            return handleSmoothScrollToCenterActionForDpadUp(upView);
                        } else {
                            boolean focusResult = false;
                            if (mOnNoNextFocusListener != null)
                                focusResult = mOnNoNextFocusListener.enforceHandleFocusUp(focusView);
                            if (focusResult) {
                                return true;
                            } else {
                                if (isVertical()) {
                                    return !(getLayoutManager() instanceof GridLayoutManager);
                                } else
                                    return false;
                            }
                        }
                    case KeyEvent.KEYCODE_BACK:
                        if (mOnBackPressListener != null) {
                            mOnBackPressListener.onBackPress();
                            return true;
                        }
                        return false;
                }
            }
        }
        return result;
    }

    private boolean handleSmoothScrollToCenterActionForDpadLeft(View nextView) {
        int leftOffset = nextView.getWidth() / 2 + getWidth() / 2 - nextView.getRight();
        this.customSmoothScrollBy(-leftOffset, 0);
        return true;
    }

    private boolean handleSmoothScrollToCenterActionForDpadRight(View nextView) {
        int rightOffset = nextView.getLeft() - getWidth() / 2 + nextView.getWidth() / 2;
        this.customSmoothScrollBy(rightOffset, 0);
        return true;

    }

    private boolean handleScrollToCenterActionForDpadUp(View nextView) {
        int upOffset = getHeight() / 2 - (nextView.getBottom() - nextView.getHeight() / 2);
        this.scrollBy(0, -upOffset);
        return true;
    }

    private boolean handleScrollToCenterActionForDpadDown(View nextView) {
        int downOffset = nextView.getTop() + nextView.getHeight() / 2 - getHeight() / 2;
        this.scrollBy(0, downOffset);
        return true;
    }

    private boolean handleSmoothScrollToCenterActionForDpadUp(View nextView) {
        int upOffset = getHeight() / 2 - (nextView.getBottom() - nextView.getHeight() / 2);
        this.customSmoothScrollBy(0, -upOffset);
        return true;
    }

    private boolean handleSmoothScrollToCenterActionForDpadDown(View nextView) {
        int downOffset = nextView.getTop() + nextView.getHeight() / 2 - getHeight() / 2;
        this.customSmoothScrollBy(0, downOffset);
        return true;
    }

    public void scrollToCenter(int desPos) {
        int firstItemPosition = getChildLayoutPosition(getChildAt(0));
        int lastItemPosition = getChildLayoutPosition(getChildAt(getChildCount() - 1));
        if (mHolderFocusView != null) {
            mHolderFocusView.setFocusable(true);
            mHolderFocusView.requestFocus();
        }
        if (desPos > lastItemPosition) {
//            Log.d(TAG, "scrollToCenter: case 1");
            getLayoutManager().scrollToPosition(desPos);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    View desView = getLayoutManager().findViewByPosition(desPos);
                    if (desView == null) {
                        postDelayed(this, 10);
                        return;
                    }
                    desView.requestFocus();
                    if (mHolderFocusView != null) {
                        mHolderFocusView.setFocusable(false);
                    }
                    handleScrollToCenterActionForDpadDown(desView);
                }
            }, 10);
        } else if (desPos < firstItemPosition) {
//            Log.d(TAG, "scrollToCenter: case 2");

            getLayoutManager().scrollToPosition(desPos);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    View desView = getLayoutManager().findViewByPosition(desPos);
                    if (desView == null) {
                        postDelayed(this, 10);
                        return;

                    }
                    desView.requestFocus();
                    if (mHolderFocusView != null) {
                        mHolderFocusView.setFocusable(false);
                    }
                    handleScrollToCenterActionForDpadUp(desView);
                }
            }, 10);
        } else {
//            Log.d(TAG, "scrollToCenter: case 3");

            View desView = getLayoutManager().findViewByPosition(desPos);
            desView.requestFocus();
            handleScrollToCenterActionForDpadDown(desView);
            if (mHolderFocusView != null) {
                mHolderFocusView.setFocusable(false);
            }
        }
    }

    public void smoothToCenterAgainForUp(View focusView) {
        handleSmoothScrollToCenterActionForDpadUp(focusView);
    }

    public void smoothToCenterAgainForDown(View focusView) {
        handleSmoothScrollToCenterActionForDpadDown(focusView);
    }

    public void smoothToCenterAgainForLeft(View focusView) {
        handleSmoothScrollToCenterActionForDpadLeft(focusView);
    }

    public void smoothToCenterAgainForRight(View focusView) {
        handleSmoothScrollToCenterActionForDpadRight(focusView);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return super.onInterceptTouchEvent(e);
    }

    //防止Activity时,RecyclerView崩溃
    @Override
    protected void onDetachedFromWindow() {
        if (getLayoutManager() != null) {
            super.onDetachedFromWindow();
        }
    }

    /**
     * 是否是最右边的item，如果是竖向，表示右边，如果是横向表示下边
     *
     * @param childPosition
     * @return
     */
    public boolean isRightEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {

            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();
            int totalItemCount = gridLayoutManager.getItemCount();
            int childSpanCount = 0;

            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                if (childSpanCount % gridLayoutManager.getSpanCount() == 0) {
                    return true;
                }
            } else {
                int lastColumnSize = totalItemCount % totalSpanCount;
                if (lastColumnSize == 0) {
                    lastColumnSize = totalSpanCount;
                }
                if (childSpanCount > totalItemCount - lastColumnSize) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return true;
            } else {
                return childPosition == getLayoutManager().getItemCount() - 1;
            }
        }

        return false;
    }

    /**
     * 是否是最左边的item，如果是竖向，表示左方，如果是横向，表示上边
     *
     * @param childPosition
     * @return
     */
    public boolean isLeftEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();
            int childSpanCount = 0;
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                if (childSpanCount % gridLayoutManager.getSpanCount() == 1) {
                    return true;
                }
            } else {
                if (childSpanCount <= totalSpanCount) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return true;
            } else {
                return childPosition == 0;
            }

        }

        return false;
    }

    /**
     * 是否是最上边的item，以recyclerview的方向做参考
     *
     * @param childPosition
     * @return
     */
    public boolean isTopEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();

            int childSpanCount = 0;
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }

            if (isVertical()) {
                if (childSpanCount <= totalSpanCount) {
                    return true;
                }
            } else {
                if (childSpanCount % totalSpanCount == 1) {
                    return true;
                }
            }


        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return childPosition == 0;
            } else {
                return true;
            }

        }

        return false;
    }

    /**
     * 是否是最下边的item，以recyclerview的方向做参考
     *
     * @param childPosition
     * @return
     */
    public boolean isBottomEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();
            int itemCount = gridLayoutManager.getItemCount();
            int childSpanCount = 0;
            int totalSpanCount = gridLayoutManager.getSpanCount();
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                //最后一行item的个数
                int lastRowCount = itemCount % totalSpanCount;
                if (lastRowCount == 0) {
                    lastRowCount = gridLayoutManager.getSpanCount();
                }
                if (childSpanCount > itemCount - lastRowCount) {
                    return true;
                }
            } else {
                if (childSpanCount % totalSpanCount == 0) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return childPosition == getLayoutManager().getItemCount() - 1;
            } else {
                return true;
            }

        }
        return false;
    }

    public interface OnInterceptListener {
        boolean onIntercept(KeyEvent event);
    }

    /**
     * 判断是否已经滑动到底部
     *
     * @param recyclerView
     * @return
     */
    private boolean isVisBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1) {
            return true;
        } else {
            return false;
        }
    }

    private void customSmoothScrollBy(int dx, int dy) {
        this.smoothScrollBy(dx, dy, null, 100);
    }

    public interface OnBackPressListener {

        /**
         * 返回
         */
        void onBackPress();
    }

    public interface OnNoNextFocusListener {
        boolean enforceHandleFocusLeft(View currentFocus);

        boolean enforceHandleFocusRight(View currentFocus);

        boolean enforceHandleFocusUp(View currentFocus);

        boolean enforceHandleFocusDown(View currentFocus);
    }

    private OnNoNextFocusListener mOnNoNextFocusListener;


    public void setOnNoNextFocusListener(OnNoNextFocusListener onNoNextFocusListener) {
        mOnNoNextFocusListener = onNoNextFocusListener;
    }

    private OnBackPressListener mOnBackPressListener;

    public void setOnBackPressListener(OnBackPressListener onBackPressListener) {
        mOnBackPressListener = onBackPressListener;
    }
}