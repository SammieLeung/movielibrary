package com.firefly.dlna;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;

public class DLNAService extends AndroidUpnpServiceImpl {

    public DLNAService() {
        // needed?
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }
}
