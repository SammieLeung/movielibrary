package com.firefly.filepicker.data;

import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.data.source.IScanFiles;

/**
 * Created by rany on 18-2-27.
 */

public interface IFileSource {
    void scanFiles(Node node, int type, IScanFiles.ScanListener listener);
    void setDeepSearch(boolean enable);
}
