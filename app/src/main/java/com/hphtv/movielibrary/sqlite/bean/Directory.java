package com.hphtv.movielibrary.sqlite.bean;

/**
 * @author lxp
 * @date 19-3-28
 */
public class Directory {
    private String id;
    private String name;
    private long parent_id;//device 的id
    private int video_number=0;
    private String uri;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private int matched_video=0;
    private int is_encrypted;//0 false 1 true
    private int scan_state;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getVideo_number() {
        return video_number;
    }

    public void setVideoNumber(int video_number) {
        this.video_number = video_number;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getParent_id() {
        return parent_id;
    }

    public void setParentId(long parent_id) {
        this.parent_id = parent_id;
    }

    public int getScan_state() {
        return scan_state;
    }

    public void setScanState(int scan_state) {
        this.scan_state = scan_state;
    }

    public int getMatched_video() {
        return matched_video;

    }

    public void setMatchedVideo(int matched_video) {
        this.matched_video = matched_video;
    }

    public int getIsEncrypted() {
        return is_encrypted;
    }

    public void setIsEcrypted(int is_encrypted) {
        this.is_encrypted = is_encrypted;
    }

    @Override
    public String toString() {
        return "Directory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parent_id=" + parent_id +
                ", video_number=" + video_number +
                ", uri='" + uri + '\'' +
                ", path='" + path + '\'' +
                ", matched_video=" + matched_video +
                ", is_encrypted=" + is_encrypted +
                ", scan_state=" + scan_state +
                '}';
    }
}
