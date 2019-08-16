package com.firefly.dlna.httpserver;

import org.fourthline.cling.support.model.ProtocolInfo;

public class Registration {
    private ProtocolInfo mProtocolInfo;
    private String mUri;

    public Registration(ProtocolInfo protocolInfo, String uri) {
        mProtocolInfo = protocolInfo;
        mUri = uri;
    }


    public ProtocolInfo getProtocolInfo() {
        return mProtocolInfo;
    }

    public String getUri() {
        return mUri;
    }
}
