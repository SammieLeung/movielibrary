package com.firefly.dlna.device.dmr;

import android.util.Log;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;

import java.net.URI;

public class DmrNoMediaPresent extends NoMediaPresent<DmrAVTransport> {
    private static final String TAG = DmrNoMediaPresent.class.getSimpleName();

    public DmrNoMediaPresent(DmrAVTransport transport) {
        super(transport);
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> setTransportURI(URI uri, String metaData) {
        DmrUtils.initAVTransport(getTransport(), uri, metaData);

        return DmrStopped.class;
    }
}
