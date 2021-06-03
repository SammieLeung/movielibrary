package com.firefly.filepicker.data.bean.xml;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rany on 18-3-30.
 */

@Root
public class SaveItem {
    @IntDef({TYPE_DLNA, TYPE_LOCAL, TYPE_SMB})
    public @interface TYPE {};
    public static final int TYPE_DLNA = 0;
    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_SMB = 2;

    @Text
    private String dir;
    @Attribute
    @TYPE
    private int type;

    public SaveItem() {
    }

    public SaveItem(@TYPE int type, @NonNull String dir) {
        this.type = type;
        this.dir = dir;
    }

    @NonNull
    public String getDir() {
        return dir;
    }

    public void setDir(@NonNull String dir) {
        this.dir = dir;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SaveItem) {
            SaveItem saveItem = (SaveItem) obj;

            return saveItem.type == type && saveItem.dir.equals(dir);
        }

        return false;
    }
}
