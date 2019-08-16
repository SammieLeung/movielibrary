package com.firefly.dlna.device.dmr;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.StorageMedium;

import java.net.URI;

public class DmrUtils {
    public static void initAVTransport(DmrAVTransport avTransport, URI uri, String metaData) {
        avTransport.getPlayer().setUri(uri, MetaData.fromDIDLObject(metaData));
        // At this time, same times can't get the duration
        String duration = ModelUtil.toTimeString(avTransport.getPlayer().getDuration() / 1000);
        MediaInfo mediaInfo = new MediaInfo(uri.toString(), metaData,
                new UnsignedIntegerFourBytes(1), duration, StorageMedium.NETWORK);

        avTransport.setMediaInfo(mediaInfo);
    }
}
