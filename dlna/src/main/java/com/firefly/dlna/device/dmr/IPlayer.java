package com.firefly.dlna.device.dmr;

import androidx.annotation.Nullable;

import org.fourthline.cling.support.model.Channel;

import java.net.URI;

public interface IPlayer {
    /**
     * 设置爆发的URI
     * @param uri 视频URI地址
     * @param metaData 视频meta信息
     */
    void setUri(URI uri, @Nullable MetaData metaData);

    /**
     * 设置下一个播放的URI
     * @param uri 下一个播放的URI地址
     * @param metaData 视频meta信息
     */
    void setNextUri(URI uri, @Nullable MetaData metaData);

    /**
     * 控制播放视频时被调用
     * @param speed 视频播放的速度
     */
    void onPlay(String speed);

    /**
     * 控制视频暂停时被调用
     */
    void onPause();

    /**
     * 控制视频停止时被调用
     */
    void onStop();

    /**
     * 想要播放下一个视频时被调用
     */
    void onNext();

    /**
     * 想要播放上一个视频时被调用
     */
    void onPrev();

    /**
     * 控制视频播放的时间
     * @param position 视频播放的位置
     */
    void onSeek(int position);

    /**
     * 设置静音播放
     */
    void setMute(Channel channel, boolean state);

    /**
     * 获取当前是否为静音状态
     * @return true为静音状态，false为非静音状态
     */
    boolean getMute();

    /**
     * 设置声音大小
     * @param channel 声道, 参考{@link org.fourthline.cling.support.model.Channel}
     * @param value 声音大小
     */
    void setVolume(Channel channel, int value);

    /**
     * 获取声音大小
     * @param channel 声道, 参考{@link org.fourthline.cling.support.model.Channel}
     * @return 声音大小
     */
    int getVolume(Channel channel);

    /**
     * 获取最大声音值
     * @return 声音最大值
     */
    int getMaxVolume();

    /**
     * 获取最小声音值
     * @return 声音最小值
     */
    int getMinVolume();

    /**
     * 获取当前播放时间
     * @return 当前播放时间
     */
    int getCurrentPosition();

    /**
     * 获取媒体时间长度
     * @return 媒体时间长度
     */
    int getDuration();
}
