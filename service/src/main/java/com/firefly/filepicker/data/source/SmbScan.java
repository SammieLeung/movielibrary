package com.firefly.filepicker.data.source;

import android.content.Context;
import android.util.Log;

import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.utils.SambaAuthHelper;

import java.net.MalformedURLException;
import java.net.URLConnection;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by rany on 18-3-27.
 */

public class SmbScan extends AbstractScanFiles {
    private static final String TAG = SmbScan.class.getSimpleName();
    private static final int DEPTH = 5;

    private Context mContext;
    private int mType = FileItem.OTHER;
    private String mContentTypePre = null;

    public SmbScan(Context context) {
        mContext = context;

    }

    @Override
    protected void scan() throws ScanException {
        switch (mFilterType) {
            case FileItem.AUDIO:
                mContentTypePre = "audio";
                mType = FileItem.AUDIO;
                break;
            case FileItem.IMAGE:
                mContentTypePre = "image";
                mType = FileItem.IMAGE;
                break;
            case FileItem.VIDEO:
                mContentTypePre = "video";
                mType = FileItem.VIDEO;
                break;
        }

        try {
            SmbFile smbFile = new SmbFile(mNode.getId());

            if (smbFile.getURL().getUserInfo() == null) {
                NtlmPasswordAuthentication authentication =
                        SambaAuthHelper.read(mContext, SambaAuthHelper.getSmbAuthKey(smbFile));
                smbFile = new SmbFile(smbFile.getURL(), authentication);
            }

            scan(smbFile, DEPTH);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            error(NOT_SUPPORT, "Url is illegal.");
        } catch (SmbException e) {
            e.printStackTrace();
            switch (e.getNtStatus()) {
                case SmbException.NT_STATUS_WRONG_PASSWORD:
                case SmbException.NT_STATUS_LOGON_FAILURE:
                case SmbException.NT_STATUS_ACCOUNT_RESTRICTION:
                case SmbException.NT_STATUS_INVALID_LOGON_HOURS:
                case SmbException.NT_STATUS_INVALID_WORKSTATION:
                case SmbException.NT_STATUS_PASSWORD_EXPIRED:
                case SmbException.NT_STATUS_ACCOUNT_DISABLED:
                case SmbException.NT_STATUS_ACCOUNT_LOCKED_OUT:
                case SmbException.NT_STATUS_ACCESS_DENIED:
                    error(UNKNOWN_USER, "permission denied.");
                    break;
                default:
                    error(UNKNOWN, "Unknown error");
                    break;
            }
        }
    }

    private void scan(SmbFile smbFile, int depth) throws SmbException {
        SmbFile[] smbFiles = null;

        try {
            smbFiles = smbFile.listFiles();
        } catch (SmbException e) {
            Log.d(TAG, "Can't scan the path: " + smbFile.getName());
            return;
        }

        for (SmbFile file : smbFiles) {
            if (isCancel) {
                return;
            } else if (file.isDirectory()) {
                if (depth > 0) {
                    scan(file, depth - 1);
                }
            } else {
                FileItem item = null;
                String contentType = file.getContentType() != null ?
                        file.getContentType()
                        : URLConnection.guessContentTypeFromName(file.getName());

                if (mContentTypePre == null
                        || (contentType != null && contentType.startsWith(mContentTypePre))) {
                    item = new FileItem(
                            mType,
                            file.getName(),
                            file.getCanonicalPath(),
                            null,
                            contentType,
                            String.valueOf(smbFile.getLastModified()),
                            file.getContentLength());

                    addResultItem(item);
                }
            }
        }
    }
}
