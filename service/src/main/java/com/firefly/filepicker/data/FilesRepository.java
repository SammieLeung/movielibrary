package com.firefly.filepicker.data;

import android.content.Context;

import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.data.source.DLNAScan;
import com.firefly.filepicker.data.source.ExternalScan;
import com.firefly.filepicker.data.source.IScanFiles;
import com.firefly.filepicker.data.source.SmbScan;

import static com.firefly.filepicker.data.bean.Node.SAMBA_DEVICE;

/**
 * Created by rany on 18-2-27.
 */

public class FilesRepository implements IFileSource {

    private Context mContext;
    private boolean mDeepSearch = false;

    public FilesRepository(Context context) {
        mContext = context;
    }


    @Override
    public void scanFiles(Node node, int type, IScanFiles.ScanListener listener) {
        IScanFiles svf = null;

        switch (node.getType()) {
            case Node.DLNA:
            case Node.DLNA_DEVICE:
                svf = new DLNAScan();
                break;
            case Node.EXTERNAL:
            case Node.EXTERNAL_DEVICE:
                svf = new ExternalScan(mContext);
                break;
//            case Node.DLNA_CATEGORY:
//                svf = new ExtraVolumeScan(mContext);
//                break;
            case Node.INTERNAL:
            case Node.INTERNAL_DEVICE:

            case Node.SAMBA:
            case SAMBA_DEVICE:
                svf = new SmbScan(mContext);
                break;
            case Node.ROOT:
            default:
                listener.error(IScanFiles.NOT_SUPPORT, "Not support yet.");
                listener.finish();
                break;
        }

        if (svf != null) {
            svf.setDeepSearch(mDeepSearch);
            svf.setFilterType(type);
            svf.setNode(node);
            svf.setListener(listener);
            svf.begin();
        }
    }

    @Override
    public void setDeepSearch(boolean enable) {
        mDeepSearch = enable;
    }
}
