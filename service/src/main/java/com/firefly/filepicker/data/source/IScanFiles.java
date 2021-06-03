package com.firefly.filepicker.data.source;

import androidx.annotation.IntDef;

import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;

import java.util.ArrayList;

/**
 * Created by rany on 18-1-9.
 */

public interface IScanFiles {
    @IntDef({UNKNOWN, NOT_FOUND, SERVER_ERROR, NODE_NULL, NOT_SUPPORT, UNKNOWN_USER})
    @interface Status {}
    int UNKNOWN = 0;
    int NOT_FOUND = 1;
    int SERVER_ERROR = 2;
    int NODE_NULL = 3;
    int NOT_SUPPORT = 4;
    int UNKNOWN_USER = 5;

    void setListener(ScanListener listener);
    void begin();
    void cancel();

    void setNode(Node node);
    void setFilterType(int type);
    void setFilter(String filter);
    void setDeepSearch(boolean enable);

    interface ScanListener {
        void error(@AbstractScanFiles.Status int status, String msg);
        void foundItem(FileItem item);
        void result(ArrayList<FileItem> files);
        void finish();
    }
}
