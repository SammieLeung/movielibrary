package com.hphtv.movielibrary.sqlite.bean.others;

import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;

/**
 * @author lxp
 * @date 19-4-12
 */
public class ParseFile {
    Device device;
    Directory directory;
    VideoFile videoFile;
    MovieNameInfo mni;

    public VideoFile getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(VideoFile videoFile) {
        this.videoFile = videoFile;
    }

    public MovieNameInfo getMni() {
        return mni;
    }

    public void setMni(MovieNameInfo mni) {
        this.mni = mni;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }
}
