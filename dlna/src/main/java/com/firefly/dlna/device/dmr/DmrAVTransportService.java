package com.firefly.dlna.device.dmr;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportInfo;

public class DmrAVTransportService extends AVTransportService {
    private IPlayer mPlayer;

    public DmrAVTransportService(Class stateMachineDefinition,
                                 Class initialState,
                                 Class transportClass,
                                 IPlayer player) {
        super(stateMachineDefinition, initialState, transportClass);

        mPlayer = player;
    }

    protected AVTransport createTransport(UnsignedIntegerFourBytes instanceId, LastChange lastChange) {
        return new DmrAVTransport(instanceId, lastChange, StorageMedium.NETWORK, mPlayer);
    }

    public void setTransportInfo(TransportInfo transportInfo) throws AVTransportException {
        findStateMachine(new UnsignedIntegerFourBytes(0))
                .getCurrentState().getTransport().setTransportInfo(transportInfo);
    }
}
