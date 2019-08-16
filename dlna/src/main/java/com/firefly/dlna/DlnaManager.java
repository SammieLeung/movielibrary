package com.firefly.dlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.firefly.dlna.device.dmr.DmrDevice;
import com.firefly.dlna.device.dms.DmsDevice;
import com.firefly.dlna.httpserver.FileServer;
import com.firefly.dlna.httpserver.IServer;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

import java.util.ArrayList;
import java.util.List;

public final class DlnaManager {
    private static final DlnaManager sINSTANCE = new DlnaManager();

    private AndroidUpnpService mService;
    private List<ServiceConnectionListener> mServiceConnectionListeners;
    private DmrDevice mDmrDevice;
    private DmsDevice mDmsDevice;

    private IServer mServer;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = (AndroidUpnpService) iBinder;

//            mService.getRegistry().addListener(new URegistry());

            invokeOnConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mConnection = null;
            mService = null;

            invokeOnDisconnected();
        }

    };

    private DlnaManager() {
        mService = null;
        mServiceConnectionListeners = new ArrayList<>();
    }

    private void invokeOnConnected() {
        for (ServiceConnectionListener listener : mServiceConnectionListeners) {
            listener.onConnected(this);
        }
    }

    private void invokeOnDisconnected() {
        for (ServiceConnectionListener listener : mServiceConnectionListeners) {
            listener.onDisconnected(this);
        }
    }

    public static DlnaManager getInstance() {
        return sINSTANCE;
    }

    /**
     * 开启DLNA服务
     * @param context {@link Context}
     */
    public void startService(Context context, ServiceConnectionListener serviceConnectionListener) {
        Intent serviceIntent = new Intent(context, DLNAService.class);

        if (serviceConnectionListener != null) {
            registerServiceConnectionListener(serviceConnectionListener);
        }

        context.startService(serviceIntent);
        context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        mServer = new FileServer(context, 0);
        mServer.start();
    }

    /**
     * 开启DLNA服务
     * @param context {@link Context}
     */
    public void startService(Context context) {
        startService(context, null);
    }

    /**
     * 停止DLNA服务
     * @param context {@link Context}
     */
    public void stopService(Context context) {
        Intent serviceIntent = new Intent(context, DLNAService.class);

        context.stopService(serviceIntent);

        mServer.destroy();
    }

    /**
     * 检查DLNA服务是否开启
     * @return true DLNA服务开启，false DLNA服务未开启
     */
    public boolean isReady() {
        return mService != null;
    }

    /**
     * 注册服务监听
     * @param serviceConnectionListener 监听实例
     */
    public void registerServiceConnectionListener(ServiceConnectionListener serviceConnectionListener) {
        mServiceConnectionListeners.add(serviceConnectionListener);
    }

    /**
     * 注消服务监听
     * @param serviceConnectionListener 监听实例
     */
    public void unregisterServiceConnectionListener(ServiceConnectionListener serviceConnectionListener) {
        mServiceConnectionListeners.remove(serviceConnectionListener);
    }

    /**
     * Add new DLNA device
     * @param device LocalDevice to be added
     */
    public void addDevice(LocalDevice device) {
        checkService();
        mService.getRegistry().addDevice(device);
    }

    public void addDmrDevice(DmrDevice dmrDevice) {
        checkService();
        mDmrDevice = dmrDevice;
        mService.getRegistry().addDevice(dmrDevice.getDevice());
    }

    public DmrDevice getDmrDevice() {
        return mDmrDevice;
    }

    public void addDmsDevice(DmsDevice dmsDevice) {
        checkService();
        mDmsDevice = dmsDevice;
        mService.getRegistry().addDevice(dmsDevice.getDevice());
    }

    public DmsDevice getDmsDevice() {
        return mDmsDevice;
    }

    private void checkService() {
        assert mService != null;
    }

    public IServer getServer() {
        return mServer;
    }

    public interface ServiceConnectionListener {
        void onConnected(DlnaManager dlnaManager);
        void onDisconnected(DlnaManager dlnaManager);
    }

//    class URegistry implements RegistryListener {
//        private final String TAG = URegistry.class.getSimpleName();
//
//        @Override
//        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//
//        }
//
//        @Override
//        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
//
//        }
//
//        @Override
//        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
//            Log.e(TAG, device.getDisplayString());
//        }
//
//        @Override
//        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
//
//        }
//
//        @Override
//        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
//
//        }
//
//        @Override
//        public void localDeviceAdded(Registry registry, LocalDevice device) {
//            Log.e(TAG, ">>>>>>>>: " + device.getDisplayString());
//        }
//
//        @Override
//        public void localDeviceRemoved(Registry registry, LocalDevice device) {
//
//        }
//
//        @Override
//        public void beforeShutdown(Registry registry) {
//
//        }
//
//        @Override
//        public void afterShutdown() {
//
//        }
//    }
}
