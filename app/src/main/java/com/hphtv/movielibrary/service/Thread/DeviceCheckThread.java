package com.hphtv.movielibrary.service.Thread;

import android.content.Context;
import android.os.AsyncTask;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.util.DeviceUtil;

import java.util.List;

/**
 * @author lxp
 * @date 19-3-27
 */
public class DeviceCheckThread extends AsyncTask<Object, Integer, Integer> {
    private List<Device> mConnectedDeviceList;
    private Callback mCallback;
    private Context mContext;
    private int isEncryted;

    public DeviceCheckThread(Context context,Callback callback,int isEncryted){
        mContext=context;
        mCallback=callback;
        this.isEncryted=isEncryted;
    }

    public interface Callback {
        public void onFinish(List<Device> list);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(Object... objects) {
        mConnectedDeviceList = DeviceUtil.getConnectedDevices(mContext, isEncryted);
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null) {
            mCallback.onFinish(mConnectedDeviceList);
        }
    }
}
