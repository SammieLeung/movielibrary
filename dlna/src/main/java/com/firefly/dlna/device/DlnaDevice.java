package com.firefly.dlna.device;

import org.fourthline.cling.model.meta.LocalDevice;

public abstract class DlnaDevice {
    private LocalDevice mDevice;

    public DlnaDevice() {
        mDevice = null;
    }

    /**
     * Set DLNA local device
     * @param localDevice LocalDevice object wanted to add
     */
    public void setDevice(LocalDevice localDevice) {
        mDevice = localDevice;
    }

    public LocalDevice getDevice() {
        return mDevice;
    }
}
