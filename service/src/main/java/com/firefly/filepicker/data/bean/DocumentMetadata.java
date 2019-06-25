package com.firefly.filepicker.data.bean;

import android.provider.DocumentsContract;
import android.support.annotation.IntDef;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.PlaylistItem;
import org.fourthline.cling.support.model.item.TextItem;
import org.fourthline.cling.support.model.item.VideoItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by rany on 18-1-4.
 */

public class DocumentMetadata<T extends DIDLObject> {
    @IntDef({FILE, DIR, AUDIO, IMAGE, PLAYLIST, TEXT, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}
    public final static int FILE = 1;
    public final static int DIR = 2;
    public final static int AUDIO = 3;
    public final static int IMAGE = 4;
    public final static int PLAYLIST = 5;
    public final static int TEXT = 6;
    public final static int VIDEO = 7;

//    private String id;
//    private String name;
//    private String creator;
//    private String albumArtURI;
//    private String description;
//    private String protocolInfo;
//    private String size;
//    private String uri;
//    private String parent;
    private String deviceId;
    private int type;
    protected T object;

    public String getId() {
        return object.getId();
    }

    public String getName() {
        return object.getTitle();
    }

    public String getCreator() {
        return object.getCreator();
    }

    public String getAlbumArtURI() {
        for (DIDLObject.Property property: object.getProperties()){
            if (property instanceof DIDLObject.Property.UPNP.ALBUM_ART_URI){
                return  ((DIDLObject.Property.UPNP.ALBUM_ART_URI) property).getValue().toString();
            }
        }

        return null;
    }

    public String getParent() {
        return object.getParentID();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Type
    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public T getItem() {
        return object;
    }

    public void setItem(T object) {
        this.object = object;
    }

    public String getMimeType() {
        Res res = object.getFirstResource();

        switch (type) {
            case DIR:
                return DocumentsContract.Document.MIME_TYPE_DIR;
            default:
                if (res != null) {
                    return res.getProtocolInfo().getContentFormat();
                } else {
                    return "unknown";
                }
        }
    }

    public Long getSize() {
        Res res = object.getFirstResource();
        if (res != null) {
            return res.getSize();
        }

        return null;
    }

    public String getLastModified() {
        List<DIDLObject.Property> properties = object.getProperties();
        for (DIDLObject.Property property : properties) {
            if (property instanceof DIDLObject.Property.DC.DATE) {
                return ((DIDLObject.Property.DC.DATE)property).getValue();
            }
        }

        return null;
    }

    public String getUrl() {
        Res res = object.getFirstResource();
        if (res != null) {
            return res.getValue();
        }

        return null;
    }

    public static int checkType(DIDLObject object) {
        int type = FILE;

        if (object instanceof AudioItem) {
            type = AUDIO;
        } else if (object instanceof ImageItem) {
            type = IMAGE;
        } else if (object instanceof PlaylistItem) {
            type = PLAYLIST;
        } else if (object instanceof TextItem) {
            type = TEXT;
        } else if (object instanceof VideoItem) {
            type = VIDEO;
        } else if (object instanceof Container) {
            type = DIR;
        }

        return type;
    }
}
