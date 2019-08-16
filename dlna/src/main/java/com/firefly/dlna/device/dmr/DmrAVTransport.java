package com.firefly.dlna.device.dmr;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;

public class DmrAVTransport extends AVTransport {
    private IPlayer mPlayer;

    public DmrAVTransport(UnsignedIntegerFourBytes instanceID,
                          LastChange lastChange,
                          StorageMedium possiblePlayMedium,
                          IPlayer player) {
        super(instanceID, lastChange, possiblePlayMedium);

        mPlayer = player;
    }

    public IPlayer getPlayer() {
        return mPlayer;
    }

    @Override
    public PositionInfo getPositionInfo() {
        int position = mPlayer.getCurrentPosition();
        positionInfo = new PositionInfo(1,
                ModelUtil.toTimeString(mPlayer.getDuration() / 1000),
                getMediaInfo().getCurrentURIMetaData(),
                mediaInfo.getCurrentURI(), ModelUtil.toTimeString(position/1000),
                ModelUtil.toTimeString(position/1000),
                Integer.MAX_VALUE,
                Integer.MAX_VALUE);

        return positionInfo;
    }
}
