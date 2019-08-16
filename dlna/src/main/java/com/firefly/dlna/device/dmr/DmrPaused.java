package com.firefly.dlna.device.dmr;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;

import java.net.URI;

public class DmrPaused extends PausedPlay<DmrAVTransport> {
    public DmrPaused(DmrAVTransport transport) {
        super(transport);
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> setTransportURI(URI uri, String metaData) {
        DmrUtils.initAVTransport(getTransport(), uri, metaData);

        return DmrStopped.class;
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> stop() {
        getTransport().getPlayer().onStop();

        return DmrStopped.class;
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> play(String speed) {
        getTransport().getPlayer().onPlay(speed);

        return DmrPlaying.class;
    }
}
