package com.firefly.filepicker;

import com.firefly.filepicker.commom.listener.DeviceRegistryListener;
import com.firefly.filepicker.data.Constants;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.registry.RegistryListener;

public class DLNAService extends AndroidUpnpServiceImpl {
    private static final String TAG = DLNAService.class.getSimpleName();

    private RegistryListener mRegistryListener;

    public DLNAService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRegistryListener = new DeviceRegistryListener(getApplicationContext());

        upnpService.getRegistry().addListener(mRegistryListener);
        upnpService.getControlPoint().search();

        Constants.upnpService = upnpService;
    }
}
