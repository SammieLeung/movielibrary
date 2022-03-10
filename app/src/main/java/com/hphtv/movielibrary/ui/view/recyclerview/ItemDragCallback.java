package com.hphtv.movielibrary.ui.view.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * author: Sam Leung
 * date:  2022/3/10
 */
public class ItemDragCallback extends ItemTouchHelper.Callback {
    //    private boolean isDraggable;
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        //鼠标可以直接拖动,使用感受比较好.
//        if(recyclerView instanceof DraggableRecyclerView){
//            DraggableRecyclerView draggableRecyclerView= (DraggableRecyclerView) recyclerView;
//            isDraggable=draggableRecyclerView.isDraggable();
//        }
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getBindingAdapterPosition();   //拖动的position
        int toPosition = target.getBindingAdapterPosition(); //释放的position
        if (recyclerView instanceof DraggableRecyclerView) {
            DraggableRecyclerView draggableRecyclerView = (DraggableRecyclerView) recyclerView;
            draggableRecyclerView.onItemMove(fromPosition, toPosition);
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
