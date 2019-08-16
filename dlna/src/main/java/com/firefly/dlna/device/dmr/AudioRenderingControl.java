package com.firefly.dlna.device.dmr;

import android.util.Log;

import com.firefly.dlna.Utils;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

class AudioRenderingControl extends AbstractAudioRenderingControl {
    private IPlayer mPlayer;

    public AudioRenderingControl(LastChange renderingControlLastChange, IPlayer player) {
        mPlayer = player;
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName)
            throws RenderingControlException {
        return mPlayer.getMute();
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId,
                        String channelName,
                        boolean desiredMute) throws RenderingControlException {
        mPlayer.setMute(Channel.valueOf(channelName), desiredMute);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId,
                                             String channelName) throws RenderingControlException {
        int volume = mPlayer.getVolume(Channel.valueOf(channelName));
        return new UnsignedIntegerTwoBytes(deviceVolumeToController(volume));
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName,
                          UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        int volume = Math.toIntExact(desiredVolume.getValue());
        mPlayer.setVolume(Channel.valueOf(channelName), controllerVolumeToDevice(volume));
    }

    @Override
    protected Channel[] getCurrentChannels() {
        return new Channel[0];
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[0];
    }

    private int deviceVolumeToController(int volume) {
        return Utils.rescaling(volume, mPlayer.getMinVolume(),
                mPlayer.getMaxVolume(), 0, 100);
    }

    private int controllerVolumeToDevice(int volume) {
        return Utils.rescaling(volume, 0, 100,
                mPlayer.getMinVolume(), mPlayer.getMaxVolume());
    }
}
