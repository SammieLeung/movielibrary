package com.firefly.dlna.device.dms;

import com.firefly.dlna.device.DlnaDevice;

public class DmsDevice extends DlnaDevice {
    private IFileStore mFileStore;

    public IFileStore getFileStore() {
        return mFileStore;
    }

    public void setFileStore(IFileStore fileStore) {
        mFileStore = fileStore;
    }
}
