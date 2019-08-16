package com.firefly.dlna.device.dmr;

import android.util.Log;

import com.firefly.dlna.device.DlnaDevice;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

public final class DmrDevice extends DlnaDevice {
    private static final String TAG = DmrDevice.class.getSimpleName();
    private static final int LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;
    private static final int LAST_CHANGE_FIRING_INTERVAL_LONG_MILLISECONDS = 1500;

    private DmrAVTransportService mDmrAVTransportService;
    private LastChangeAwareServiceManager mAvTransportServiceManager;
    private LastChangeAwareServiceManager mRenderingControlManager;
    private Thread mLastChangePushTread;

    public DmrDevice() {
        runLastChangePushThread();
    }

    public void setAvTransportServiceManager(LastChangeAwareServiceManager serviceManager) {
        mAvTransportServiceManager = serviceManager;
        mDmrAVTransportService = (DmrAVTransportService) mAvTransportServiceManager.getImplementation();
    }

    public void setRenderingControlManager(LastChangeAwareServiceManager serviceManager) {
        mRenderingControlManager = serviceManager;
    }

    public void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        mLastChangePushTread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // These operations will NOT block and wait for network responses
                        mAvTransportServiceManager.fireLastChange();
                        mRenderingControlManager.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    } catch (Exception ex) {
                        try {
                            Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_LONG_MILLISECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };

        mLastChangePushTread.start();
    }

    private TransportInfo getTransportInfo() {
        TransportInfo transportInfo = null;

        try {
            transportInfo = mDmrAVTransportService.getTransportInfo(new UnsignedIntegerFourBytes(0));
        } catch (AVTransportException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return transportInfo;
    }

    private void setTransportInfo(TransportInfo transportInfo) {
        try {
            mDmrAVTransportService.setTransportInfo(transportInfo);
        } catch (AVTransportException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * 同步停止状态到控制端
     */
    public void stop() {
        setTransportInfo(new TransportInfo(TransportState.STOPPED));
    }

    /**
     * 同步播放状态到控制端
     */
    public void play() {
        setTransportInfo(new TransportInfo(TransportState.PLAYING));
    }

    /**
     * 同步加载状态到控制端
     */
    public void prepare() {
        setTransportInfo(new TransportInfo(TransportState.TRANSITIONING));
    }

    /**
     * 同步暂停状态到控制端
     */
    public void pause() {
        setTransportInfo(new TransportInfo(TransportState.PAUSED_PLAYBACK));
    }

    /**
     * 同步暂停录制状态到控制端
     */
    public void pauseRecording() {
        setTransportInfo(new TransportInfo(TransportState.PAUSED_RECORDING));
    }

    /**
     * 同步录制状态到控制端
     */
    public void record() {
        setTransportInfo(new TransportInfo(TransportState.RECORDING));
    }

    /**
     * 同步无播放资源状态到控制端
     */
    public void noMedia() {
        setTransportInfo(new TransportInfo(TransportState.NO_MEDIA_PRESENT));
    }
}
