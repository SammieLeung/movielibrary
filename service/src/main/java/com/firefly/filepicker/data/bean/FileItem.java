package com.firefly.filepicker.data.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

/**
 * Created by rany on 18-1-9.
 */

public class FileItem implements Parcelable {
    @IntDef({OTHER, AUDIO, IMAGE, TEXT, VIDEO})
    public @interface FileType {}
    public static final int OTHER = 0;
    public static final int AUDIO = 1;
    public static final int IMAGE = 2;
    public static final int TEXT = 3;
    public static final int VIDEO = 4;

    @FileType
    private int type;
    private String name;
    private String path;
    private String thumb;
    private String mimeType;
    private String date;
    private long size;

    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };

    public FileItem() {

    }

    public FileItem(@FileType int type, String name, String path,
                    String thumb, String mimeType, String date, long size) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.thumb = thumb;
        this.mimeType = mimeType;
        this.date = date;
        this.size = size;
    }

    protected FileItem(Parcel in) {
        type = in.readInt();
        name = in.readString();
        path = in.readString();
        thumb = in.readString();
        mimeType = in.readString();
        date = in.readString();
        size = in.readLong();
    }

    public int getType() {
        return type;
    }

    public void setType(@FileType int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public static int mimeTypeToType(@NonNull String mimeType) {
        if (mimeType.startsWith("audio")) {
            return AUDIO;
        } else if (mimeType.startsWith("image")) {
            return IMAGE;
        } else if (mimeType.startsWith("video")) {
            return VIDEO;
        } else if (mimeType.startsWith("text")) {
            return TEXT;
        }

        return OTHER;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(thumb);
        dest.writeString(mimeType);
        dest.writeString(date);
        dest.writeLong(size);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + path.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileItem)) {
            return false;
        }
        FileItem item = (FileItem) obj;

        if (item.getPath().equals(path)
                && item.getName().equals(name)) {
            return true;
        }

       return true;
    }
}
