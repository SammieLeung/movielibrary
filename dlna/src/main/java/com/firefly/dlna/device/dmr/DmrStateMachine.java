package com.firefly.dlna.device.dmr;

import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.seamless.statemachine.States;

@States({
        DmrNoMediaPresent.class,
        DmrPaused.class,
        DmrPlaying.class,
        DmrStopped.class
})
interface DmrStateMachine extends AVTransportStateMachine {}
