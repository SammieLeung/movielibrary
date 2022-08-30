package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.RvItemEpisodeLayoutBinding;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/5/27
 */
public class EpisodeItemListAdapter extends RecyclerView.Adapter<EpisodeItemListAdapter.ViewHolder> implements View.OnFocusChangeListener, View.OnClickListener, View.OnHoverListener {
    protected float mZoomRatio = 1.15f;
    private List<List<VideoFile>> mEpisodeList;
    private List<VideoFile> mOtherEpisodeList=new ArrayList<>();
    private int mSelectTabPos = 0;
    private Context mContext;
    protected OnRecyclerViewItemActionListener mOnItemClickListener = null;
    protected ObservableInt mLastPlayEpisodePos;
    private boolean isVarietyShow=false;

    public EpisodeItemListAdapter(Context context, List<List<VideoFile>> list) {
        mContext = context;
        mEpisodeList = list;
    }

    public void setSelectTabPos(int pos) {
        mSelectTabPos = pos;
    }

    public int getSelectTabPos() {
        return mSelectTabPos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvItemEpisodeLayoutBinding binding = RvItemEpisodeLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false);
        binding.setLastPlayEpisodePos(mLastPlayEpisodePos);
        ViewHolder viewHolder = new ViewHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RvItemEpisodeLayoutBinding binding = holder.mBinding;
        binding.getRoot().setTag(position);
        if(!isVarietyShow) {
            binding.setText(String.valueOf(mSelectTabPos * 10 + position + 1));
            binding.setItemPos(mSelectTabPos * 10 + position);
            if (mEpisodeList.get(position).size() == 0) {
                binding.getRoot().setEnabled(false);
            } else {
                binding.getRoot().setEnabled(true);
            }
        }else{
            if(mEpisodeList.size()>0&& mEpisodeList.get(position).size()>0) {
                VideoFile videoFile = mEpisodeList.get(position).get(0);
                if (videoFile != null) {
                    binding.getRoot().setEnabled(true);
                    binding.setText(videoFile.aired);
                    binding.setItemPos(position);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return mEpisodeList.size();
    }

    public void addAll(List<List<VideoFile>> dataList) {
        mEpisodeList.clear();
        mEpisodeList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addAllUnknowns(List<VideoFile> videoFiles){
        notifyDataSetChanged();
    }

    public void setZoomRatio(float zoomRatio) {
        mZoomRatio = zoomRatio;
    }

    public void setOnItemClickListener(OnRecyclerViewItemActionListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setLastPlayEpisodePos(ObservableInt lastPlayEpisodePos) {
        mLastPlayEpisodePos = lastPlayEpisodePos;
    }

    public void setVarietyShow(boolean varietyShow) {
        isVarietyShow = varietyShow;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null && v.getTag() != null) {
            int position = (int) v.getTag();
            int realPosition= mSelectTabPos *10+position;
            mLastPlayEpisodePos.set(realPosition);
            List<VideoFile> data = mEpisodeList.get(position);
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, realPosition, data);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
            if (mOnItemClickListener != null) {
                int pos = (int) v.getTag();
                List<VideoFile> data = mEpisodeList.get(pos);
                mOnItemClickListener.onItemFocus(v, pos, data);
            }
        } else {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
            if (mOnItemClickListener != null) {
                int pos = (int) v.getTag();
                List<VideoFile> data = mEpisodeList.get(pos);
                mOnItemClickListener.onItemFocus(v, pos, data);
            }
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
        return false;
    }

    public interface OnRecyclerViewItemActionListener {
        void onItemClick(View view, int position, List<VideoFile> data);

        void onItemFocus(View view, int position, List<VideoFile> data);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RvItemEpisodeLayoutBinding mBinding;

        public ViewHolder(RvItemEpisodeLayoutBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnHoverListener(EpisodeItemListAdapter.this);
            mBinding.getRoot().setOnFocusChangeListener(EpisodeItemListAdapter.this);
            mBinding.getRoot().setOnClickListener(EpisodeItemListAdapter.this);
        }
    }

}
