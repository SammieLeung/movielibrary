package com.firefly.dlna.device.dmr;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;

public class MetaData {
    private DIDLObject mDIDLObject;

    public MetaData(@NonNull DIDLObject object) {
        mDIDLObject = object;
    }

    public static MetaData fromDIDLObject(DIDLObject object) {
        return new MetaData(object);
    }

    @Nullable
    public static MetaData fromDIDLObject(String xml) {
        if (xml == null || xml.length() == 0
                || "NOT_IMPLEMENTED".equals(xml)) {
            return null;
        }

        try {
            DIDLParser parser = new DIDLParser();

            DIDLContent content = parser.parse(xml);

            if (content.getItems().size() > 0) {
                return new MetaData(content.getItems().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public DIDLObject getDIDLObject() {
        return mDIDLObject;
    }

    public String getId() {
        return mDIDLObject.getId();
    }

    public String getTitle() {
        return mDIDLObject.getTitle();
    }

    public String getCreator() {
        return mDIDLObject.getCreator();
    }

    public String getParentId() {
        return mDIDLObject.getParentID();
    }

    public long getSize() {
        Long size = mDIDLObject.getFirstResource().getSize();
        return size == null ? 0 : size;
    }

    public String getMimeType() {
        return mDIDLObject.getFirstResource().getProtocolInfo().getContentFormat();
    }

    public String getUri() {
        return mDIDLObject.getFirstResource().getValue();
    }

    public String getDuration() {
        return mDIDLObject.getFirstResource().getDuration();
    }

    public String getResolution() {
        return mDIDLObject.getFirstResource().getResolution();
    }

    public String[] getAlbum() {
        DIDLObject.Property<String>[] property = mDIDLObject.getProperties(DIDLObject.Property.UPNP.ALBUM.class);
        String[] albums = new String[property.length];

        for (int i = 0; i < albums.length; ++i) {
            albums[i] = property[i].getValue();
        }

        return albums;
    }
}
