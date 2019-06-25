package com.firefly.filepicker.picker.browse;

import com.firefly.filepicker.data.bean.Node;

/**
 * Created by rany on 18-2-28.
 */

interface ItemEventListener {
    void onClick(Node node);
    void onAddButtonClick(Node node);
    void onLongClick(Node node);
    void onFocusChange(Node node, boolean focus);
}