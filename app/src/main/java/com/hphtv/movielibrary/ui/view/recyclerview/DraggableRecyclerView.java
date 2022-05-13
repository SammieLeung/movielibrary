package com.hphtv.movielibrary.ui.view.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.adapter.BaseAdapter2;

import java.util.Collections;

/**
 * author: Sam Leung
 * date:  2022/3/9
 */
public class DraggableRecyclerView extends RecyclerView {
    private boolean isDraggable;
    private ObservableInt mSelectPos = new ObservableInt(-1);

    public DraggableRecyclerView(@NonNull Context context) {
        super(context);
    }

    public DraggableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isDraggable) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        onItemMoveUp();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        onItemMoveDown();
                        return true;
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ESCAPE:
                        isDraggable = false;
                        mSelectPos.set(-1);
                        return true;
                    default:
                        return true;
                }
            }
            else {
                if (event.getRepeatCount() > 0) {//如果是长按
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            isDraggable = true;
                            View child = getFocusedChild();
                            int pos =  getLayoutManager().getPosition(child);
                            mSelectPos.set(pos);
                            return true;
                        default:
                            return super.dispatchKeyEvent(event);
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public ObservableInt getSelectPos() {
        return mSelectPos;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (getAdapter() instanceof BaseAdapter2) {
            BaseAdapter2 baseAdapter = (BaseAdapter2) getAdapter();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(baseAdapter.getDatas(), i, i + 1);
                }
                baseAdapter.notifyItemRangeChanged(fromPosition, Math.abs(fromPosition - toPosition) + 1);

            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(baseAdapter.getDatas(), i, i - 1);
                }
                baseAdapter.notifyItemRangeChanged(toPosition, Math.abs(fromPosition - toPosition) + 1);
            }
            baseAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    private void onItemMoveUp() {
        int fromPosition = mSelectPos.get();
        if (fromPosition > 0) {
            mSelectPos.set(fromPosition - 1);
            onItemMove(fromPosition, fromPosition - 1);
        }
    }

    private void onItemMoveDown() {
        int fromPosition = mSelectPos.get();
        if (fromPosition > -1 && fromPosition < getAdapter().getItemCount() - 1) {
            mSelectPos.set(fromPosition + 1);
            onItemMove(fromPosition, fromPosition + 1);
        }
    }

}
