package com.firefly.dlna.device.dmr;

import android.util.Log;

import com.firefly.dlna.Utils;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class DmrPlaying extends Playing<DmrAVTransport> {
    public DmrPlaying(DmrAVTransport transport) {
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

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> pause() {
        getTransport().getPlayer().onPause();

        return DmrPaused.class;
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> next() {
        getTransport().getPlayer().onNext();

        return DmrStopped.class;
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> previous() {
        getTransport().getPlayer().onPrev();

        return DmrStopped.class;
    }

    @Override
    public Class<? extends AbstractState<DmrAVTransport>> seek(SeekMode unit, String target) {
        Log.e(">>>>", unit.toString());
        int time = Utils.timeStrToMsec(target);

        getTransport().getPlayer().onSeek(time);

        return DmrPlaying.class;
    }
}
