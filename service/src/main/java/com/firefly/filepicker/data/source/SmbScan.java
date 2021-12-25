package com.firefly.filepicker.data.source;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.LongDef;

import com.archos.filecorelibrary.filecorelibrary.jcifs.JcifsUtils;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.roomdb.Credential;
import com.firefly.filepicker.utils.SambaAuthHelper;
import com.station.kit.util.LogUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
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
//            SmbFile smbFile = new SmbFile(mNode.getId(), SingletonContext.getInstance().withAnonymousCredentials());
//            Log.d(TAG, "scan:userInfo: " + smbFile.getURL().getUserInfo());
//            if (smbFile.getURL().getUserInfo() == null) {
//                CIFSContext cifsContext =
//                        SambaAuthHelper.read(mContext, SambaAuthHelper.getSmbAuthKey(smbFile));
            CIFSContext cifsContext=null;
            Credential credential = SambaAuthHelper.getInstance().getCredential(mNode.getId());
            if(credential==null){
                    Uri tmpUri=Uri.parse(mNode.getId());
                    String userInfo=tmpUri.getUserInfo();
                    if(!TextUtils.isEmpty(userInfo)){
                        String[] splitStrs=userInfo.split(":");
                        if(splitStrs.length>1){
                            cifsContext = JcifsUtils.getBaseContext(true).withCredentials(new NtlmPasswordAuthenticator(splitStrs[0],splitStrs[1]));
                        }
                    }
            }else{
                cifsContext=SambaAuthHelper.getInstance().getCIFSContext(mNode.getId());
            }
//            if (TextUtils.isEmpty(credential.getUsername()) && TextUtils.isEmpty(credential.getPassword())) {
//                cifsContext=JcifsUtils.getBaseContext(true).withAnonymousCredentials();
//            } else {
//                cifsContext = JcifsUtils.getBaseContext(true).withCredentials(new NtlmPasswordAuthenticator(mNode.getId(), credential.getUsername(), credential.getPassword()));
//            }

            SmbFile smbFile = new SmbFile(mNode.getId(), cifsContext);
//            }

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
            e.printStackTrace();
            Log.d(TAG, "Can't scan the path: " + smbFile.getName());
            return;
        }

        for (SmbFile file : smbFiles) {
            if (isCancel) {
                return;
            } else if (file.isDirectory()) {
//                if (depth > 0) {//TODO 需要增加搜索深度限制
                    scan(file, depth - 1);
//                }
            } else {
                FileItem item = null;
                try {
                    String contentType = file.getContentType() != null ?
                            file.getContentType()
                            : URLConnection.guessContentTypeFromName(file.getName());
                    Log.d(TAG, "scan: contentType "+contentType+" name:"+file.getName());

                    if (mContentTypePre == null
                            || (contentType != null && contentType.startsWith(mContentTypePre))) {
                        item = new FileItem(
                                mType,
                                file.getName(),
                                file.getCanonicalPath(),
                                null,
                                contentType,
                                String.valueOf(smbFile.getLastModified()),
                                file.length(),
                                FileItem.SMB);

                        addResultItem(item);
                    }
                }catch (Exception e){
                    Log.e(TAG,"error:name "+file.getPath());

                }


            }
        }
    }
}
