package com.firefly.filepicker.data.source;

import android.support.annotation.NonNull;

import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rany on 18-1-8.
 */

public abstract class AbstractScanFiles implements IScanFiles {
    private ScanListener mListener;
    private Set<FileItem> mFileItems = new HashSet<>();
    protected Node mNode;

    protected int mFilterType = -1;
    protected String mFilterArg;
    protected volatile boolean isCancel = false;
    protected boolean mDeepSearch = false;

    @Override
    public void setListener(ScanListener listener) {
        mListener = listener;
    }

    @Override
    public void setNode(Node node) {
        mNode = node;
    }

    @Override
    public void setFilterType(@FileItem.FileType int type) {
        mFilterType = type;
    }

    @Override
    public void setFilter(String filter) {
        mFilterArg = filter;
    }

    @Override
    public void setDeepSearch(boolean enable) {
        mDeepSearch = enable;
    }

    protected void addResultItem(@NonNull FileItem item) {
        if ((mFilterType != -1 && item.getType() != mFilterType)) {
            return;
        }

        mFileItems.add(item);

        if (mListener != null) {
            mListener.foundItem(item);
        }
    }

    @Override
    public void begin() {
        if (mNode == null) {
            if (mListener != null) {
                mListener.error(NODE_NULL, "Node can't be null.");
            }
            return;
        }

        try {
            scan();
        } catch (ScanException e) {
            e.printStackTrace();

            if (mListener != null) {
                mListener.error(e.getStatus(), e.getMessage());
                mListener.finish();
            }
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    protected void error(@AbstractScanFiles.Status int status, String msg) {
        if (mListener != null) {
            mListener.error(status, msg);
            mListener.finish();
        }
    }

    protected void finish() {
        if (mListener != null) {
            if (!isCancel) {
                ArrayList<FileItem> list = new ArrayList<>();
                list.addAll(mFileItems);
                mListener.result(list);
            }

            mListener.finish();
        }
    }

    abstract protected void scan() throws ScanException;
}
