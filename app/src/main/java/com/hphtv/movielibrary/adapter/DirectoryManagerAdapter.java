package com.hphtv.movielibrary.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.data.ConstData;

import java.util.HashMap;
import java.util.List;

/**
 * @author lxp
 */

public class DirectoryManagerAdapter extends RecyclerView.Adapter<DirectoryManagerAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "DirectoryManagerAdapter";
    private Context mContext;


    private List<HashMap<String, Object>> mDataList;

    public DirectoryManagerAdapter(Context context, List<HashMap<String, Object>> datas) {
        this.mDataList = datas;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.deivce_setting_listview_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final Directory directory = (Directory) mDataList.get(i).get(ConstData.DIRECTORY);
        final Device device= (Device) mDataList.get(i).get(ConstData.DEVICE);
        int isEncrypted= (int) mDataList.get(i).get(ConstData.DEVICE_IS_ENCRYPTED);
        viewHolder.deviceName.setText(directory.getName());
        switch (directory.getScan_state()) {
            case ConstData.DirectoryState.SCANNED:
                stopScanAnim(viewHolder.deviceStatus);
                viewHolder.deviceStatus.setImageResource(R.mipmap.ic_p_green);
                break;
            case ConstData.DirectoryState.UNSCAN:
                stopScanAnim(viewHolder.deviceStatus);
                viewHolder.deviceStatus.setImageResource(R.mipmap.ic_p_gray);
                break;
            case ConstData.DirectoryState.SCANNING:
                startScanAnim(viewHolder.deviceStatus, mContext);
                viewHolder.deviceStatus.setImageResource(R.mipmap.ic_p_red);
                break;
        }
        if(isEncrypted==0){
            viewHolder.deviceIcon.setImageResource(R.mipmap.folder);
        }else {
            viewHolder.deviceIcon.setImageResource(R.mipmap.private_folder);
        }
        if(i%2==1){
            viewHolder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_list_view_282828));
        }
        else {
            viewHolder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_list_view_242424));
        }
        int vCount= (int) mDataList.get(i).get(ConstData.DEVICE_VIDEO_COUNT);
        int mCount= (int) mDataList.get(i).get(ConstData.DEVICE_MATCHED_VIDEO);
        viewHolder.deviceVideoCount.setText(String.valueOf(mCount) + "/" + String.valueOf(vCount));
        viewHolder.deviceCheckBox.setChecked((Boolean) mDataList.get(i).get(ConstData.DEVICE_CHECK_STATUS));
        //保存数据到view的Tag上
        viewHolder.itemView.setTag(i);
        viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int i = (int) v.getTag();
                    if (i >= 0 && i < mDataList.size()) {
//                        Device device = (Device) mDataList.get(i).get(ConstData.DEVICE);
                        mFocusListener.OnItemFocus(device,directory,false);
                    }

                }
            }
        });
        viewHolder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                int i = (int) v.getTag();
                if (i >= 0 && i < mDataList.size()) {
//                    Device device = (Device) mDataList.get(i).get(ConstData.DEVICE);
                    mFocusListener.OnItemFocus(device,directory,true);
                }
                return false;
            }
        });
        viewHolder.deviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (mListener != null) {
                    mDataList.get(i).put(ConstData.DEVICE_CHECK_STATUS, isChecked);
                    mListener.OnItemCheked(mDataList, (Integer) ((View) v.getParent()).getTag(), isChecked);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.cb_dm_select);
        checkBox.toggle();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        ImageView deviceStatus;
        TextView deviceVideoCount;
        CheckBox deviceCheckBox;
        ImageView deviceIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceStatus = (ImageView) itemView.findViewById(R.id.iv_dm_status);
            deviceVideoCount = (TextView) itemView.findViewById(R.id.tv_dm_video);
            deviceCheckBox = (CheckBox) itemView.findViewById(R.id.cb_dm_select);
            deviceIcon= (ImageView) itemView.findViewById(R.id.usb_img);
        }
    }


    OnItemCheckedChangeListener mListener;
    OnItemFocusListener mFocusListener;

    public interface OnItemCheckedChangeListener {
        public void OnItemCheked(List<HashMap<String, Object>> dataSet, int pos, boolean isChecked);
    }

    public interface OnItemFocusListener {
        public void OnItemFocus(Device device,Directory directory,boolean isHover);
    }

    public void setOnItemCheckedListener(OnItemCheckedChangeListener listener) {
        this.mListener = listener;
    }

    public void setOnItemFocusListener(OnItemFocusListener listener) {
        this.mFocusListener = listener;
    }

    private void startScanAnim(View v,Context context) {
        Animation operatingAnim = null;
        if (v != null) {
            operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_scan);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            if (operatingAnim != null)
                v.startAnimation(operatingAnim);
            else {
                v.setAnimation(operatingAnim);
                v.startAnimation(operatingAnim);
            }

        }

    }

    private void stopScanAnim(View v) {
        if (v != null) {
            v.clearAnimation();
        }
    }

}
