package com.firefly.filepicker.data.bean.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rany on 18-3-30.
 */

@Root
public class MediaDirs {
    @ElementList
    private List<SaveItem> saveItems = null;

    public List<SaveItem> getSaveItems() {
        return saveItems;
    }

    public void addItem(SaveItem saveItem) {
        if (saveItems == null) {
            saveItems = new ArrayList<>();
        }

        if (!saveItems.contains(saveItem)) {
            saveItems.add(saveItem);
        }
    }

    public List<SaveItem> getDLNAs() {
        return getItems(SaveItem.TYPE_DLNA);
    }

    public List<SaveItem> getLocals() {
        return getItems(SaveItem.TYPE_LOCAL);
    }

    public List<SaveItem> getSambas() {
        return getItems(SaveItem.TYPE_SMB);
    }

    private List<SaveItem> getItems(@SaveItem.TYPE int type) {
        List<SaveItem> result = new ArrayList<>();

        if (saveItems == null) {
            return result;
        }

        for (SaveItem saveItem : saveItems) {
            if (saveItem.getType() == type) {
                result.add(saveItem);
            }
        }

        return result;
    }
}
