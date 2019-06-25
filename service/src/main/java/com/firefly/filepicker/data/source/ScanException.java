package com.firefly.filepicker.data.source;

import static com.firefly.filepicker.data.source.AbstractScanFiles.UNKNOWN;

/**
 * Created by rany on 18-1-9.
 */

public class ScanException extends Exception {
    @AbstractScanFiles.Status
    private int status = UNKNOWN;

    public ScanException(String msg) {
        this(UNKNOWN, msg);
    }

    public ScanException(@AbstractScanFiles.Status int status, String msg) {
        super(msg);

        this.status = status;
    }

    @AbstractScanFiles.Status
    public int getStatus() {
        return status;
    }

    public void setStatus(@AbstractScanFiles.Status int status) {
        this.status = status;
    }
}
