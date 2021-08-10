package com.firefly.filepicker.picker.browse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.firefly.filepicker.R;
import com.firefly.filepicker.commom.listener.DeviceRegistryListener;
import com.firefly.filepicker.data.Constants;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.data.bean.xml.SaveItem;
import com.firefly.filepicker.utils.Base64Helper;
import com.firefly.filepicker.utils.MediaDirHelper;
import com.firefly.filepicker.utils.SambaAuthHelper;
import com.firefly.filepicker.utils.SmbFileHelper;
import com.firelfy.util.StorageHelper;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.seamless.util.logging.LoggingUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by rany on 18-1-12.
 */

public class BrowsePathPresenter implements BrowsePathContract.Presenter {
    private static final String TAG = BrowsePathPresenter.class.getSimpleName();
    private static final String DEFAULT_ADDRESS = "smb://";

    public static final int MSG_UPDATE_TREE_VIEW = 0;
    public static final int MSG_SET_LOADING_VIEW = 1;
    public static final int MSG_ERROR = 2;
    public static final int MSG_SHOW_AUTH_DIALOG = 3;
    public static final int MSG_SHOW_SET_PRIVATE_DIALOG = 4;

    @SelectType
    private int mSelectType = SELECT_DIR;
    private boolean mSupportNet;
    private boolean mEnableSelectConfirm;

    private BrowsePathContract.View mView;
    private Context mContext;

    private Node mLocalRoot;
    private Node mDLNARoot;
    private Node mSambaRoot;

    private SparseArray<Node> mCancelTasks = new SparseArray<>();

    private Map<String, CIFSContext> mSmbAuthMap;

    private BroadcastReceiver mDeviceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (DeviceRegistryListener.DEVICE_CHANGED_BROADCAST.equals(action)) {
                mDLNARoot.setChildren(null);
                DLNADevice(mDLNARoot);
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
                    || Intent.ACTION_MEDIA_MOUNTED.equals(action)
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                mLocalRoot.setChildren(null);
                localDevice(mLocalRoot);
            }
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TREE_VIEW:
                    Node node = (Node) msg.obj;
                    mView.updateTreeView(node);
                    break;
                case MSG_SET_LOADING_VIEW:
                    mView.setLoadingViewVisible((Boolean) msg.obj);
                    break;
                case MSG_ERROR:
                    mView.showError((String) msg.obj);
                    break;
                case MSG_SHOW_AUTH_DIALOG:
                    Bundle bundle = msg.getData();
                    mView.showAuthDialog((Node) bundle.get("node"), bundle);
                    break;
                case MSG_SHOW_SET_PRIVATE_DIALOG:
                    mView.showOnSelectConfirm((Node) msg.obj);
                    break;
            }
        }
    };

    public BrowsePathPresenter(BrowsePathContract.View view, Context context) {
        mView = view;
        mContext = context;

        mView.setPresenter(this);
    }

    @Override
    public void init() {
        Node node = new Node("root", "root", Node.ROOT, null);
        mLocalRoot = new Node("localRoot",
                mContext.getString(R.string.device),
                Node.EXTERNAL_CATEGORY,
                null);
        mDLNARoot = new Node("DNLARoot",
                mContext.getString(R.string.dlna_node),
                Node.DLNA_CATEGORY,
                null);
        mSambaRoot = new Node("sambaRoot",
                mContext.getString(R.string.samba_node),
                Node.SAMBA_CATEGORY,
                null);

        node.addChild(mLocalRoot);

        if (mSupportNet) {
            node.addChild(mDLNARoot);
            node.addChild(mSambaRoot);
        }

        localDevice(mLocalRoot);

        updateTreeView(node);

        IntentFilter deviceChangedFilter = new IntentFilter();
        deviceChangedFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        deviceChangedFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        deviceChangedFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        deviceChangedFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        deviceChangedFilter.addDataScheme("file");

        IntentFilter deviceChangedFilter1 = new IntentFilter();
        deviceChangedFilter1.addAction(DeviceRegistryListener.DEVICE_CHANGED_BROADCAST);

        mContext.registerReceiver(mDeviceChangedReceiver, deviceChangedFilter);
        mContext.registerReceiver(mDeviceChangedReceiver, deviceChangedFilter1);

        mSmbAuthMap = SambaAuthHelper.readAll(mContext);
    }

    @Override
    public void deinit() {
        mContext.unregisterReceiver(mDeviceChangedReceiver);

        SambaAuthHelper.save(mContext, mSmbAuthMap);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void getChildren(Node node) {
        setLoadingViewVisible(true);
        switch (node.getType()) {
            case Node.EXTERNAL_CATEGORY:
            case Node.EXTERNAL_DEVICE:
            case Node.EXTERNAL:
            case Node.PCIE_DEVICE:
            case Node.SATA_DEVICE:
            case Node.SDCARD_DEVICE:
            case Node.USB_DEVICE:
                if (node.getType() == Node.EXTERNAL_CATEGORY) {
                    localDevice(node);
                } else if (mSelectType == SELECT_DEVICE) {
                    onSelect(node, false, false);
                } else if (mSelectType == SELECT_DIR) {
                    localPath(node);
                } else {
                    onSelectFile(node);
                }
                break;
            case Node.DLNA_CATEGORY:
            case Node.DLNA_DEVICE:
            case Node.DLNA:
                if (mSelectType == SELECT_DIR) {
                    DLNAPath(node);
                } else {
                    onSelectFile(node);
                }
                break;
            case Node.SAMBA_CATEGORY:
            case Node.SAMBA_DEVICE:
            case Node.SAMBA:
                if (mSelectType == SELECT_DIR) {
                    sambaPath(node);
                } else {
                    onSelectFile(node);
                }
                break;
            case Node.INTERNAL_CATEGORY:
            case Node.INTERNAL_DEVICE:
            case Node.INTERNAL:
                break;
            case Node.ROOT:
                break;
        }
    }

    @Override
    public void onSelect(Node node, boolean isPrivate, boolean confirm) {
        if (mSelectType == SELECT_DIR || mSelectType == SELECT_DEVICE) {
            if (!mEnableSelectConfirm) {//设置是否开启确认选择窗口
                onSelectDir(node, false);
            } else {
                if (confirm) {
                    onSelectDir(node, isPrivate);
                } else {
                    mView.showOnSelectConfirm(node);
                }
            }
        } else {
            onSelectFile(node);
        }
    }

    @Override
    public void setBrowseType(int type) {
        if (type != SELECT_DIR && type != SELECT_FILE && type != SELECT_DEVICE) {
            throw new IllegalArgumentException("The parameter browseType is invalid.");
        }

        mSelectType = type;
    }

    @Override
    public void setSupportNet(boolean supportNet) {
        mSupportNet = supportNet;
    }

    @Override
    public void setEnableSelectConfirm(boolean enableSelectConfirm) {
        mEnableSelectConfirm = enableSelectConfirm;
    }

    @Override
    public void cancelTask(Node node) {
        mCancelTasks.append(node.getFlag(), node);
    }

    private void onSelectFile(Node node) {
        String url = null;

        switch (node.getType()) {
            case Node.DLNA_CATEGORY:
            case Node.DLNA_DEVICE:
            case Node.DLNA:
                if (node.getItem() instanceof Item) {
                    url = ((Item) node.getItem()).getFirstResource().getValue();
                } else {
                    DLNAPath(node);
                }
                break;
            case Node.EXTERNAL_CATEGORY:
            case Node.EXTERNAL_DEVICE:
            case Node.EXTERNAL:
            case Node.PCIE_DEVICE:
            case Node.SATA_DEVICE:
            case Node.USB_DEVICE:
            case Node.SDCARD_DEVICE:
                if (node.getItem() instanceof File) {
                    File file = (File) node.getItem();
                    if (file.isFile()) {
                        try {
                            url = file.getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        localPath(node);
                    }
                } else {
                    localPath(node);
                }
                break;
            case Node.INTERNAL_CATEGORY:
            case Node.INTERNAL_DEVICE:
            case Node.INTERNAL:
                break;
            case Node.SAMBA_CATEGORY:
            case Node.SAMBA_DEVICE:
            case Node.SAMBA:
                if (node.getItem() instanceof SmbFile) {
                    SmbFile smbFile = (SmbFile) node.getItem();
                    try {
                        if (SmbFileHelper.isFile(smbFile)) {
                            url = createSmbBasicAuthUrl(smbFile);
                        } else {
                            sambaPath(node);
                        }
                    } catch (SmbException e) {
                        e.printStackTrace();
                    }
                } else {
                    sambaPath(node);
                }
                break;
            case Node.ROOT:
                break;
        }

        if (url != null) {
            Bundle bundle = new Bundle();
            bundle.putString("data", url);

            mView.setResult(Activity.RESULT_OK, bundle);
        }
    }

    private void onSelectDir(Node node, boolean isPrivate) {
        Bundle bundle = new Bundle();
        String deviceType = "";
        StringBuilder uriBuilder = new StringBuilder("content://com.firefly.filepicker/");
        String identity = node.getId();

        switch (node.getType()) {
            case Node.EXTERNAL_CATEGORY:
            case Node.EXTERNAL_DEVICE:
            case Node.EXTERNAL:
            case Node.USB_DEVICE:
            case Node.SDCARD_DEVICE:
            case Node.PCIE_DEVICE:
            case Node.SATA_DEVICE:
                MediaDirHelper.addAndSave(mContext, new SaveItem(SaveItem.TYPE_LOCAL, identity));
                identity = Base64Helper.encode(identity);
                deviceType = "local";
                break;
            case Node.DLNA_CATEGORY:
            case Node.DLNA_DEVICE:
                identity = identity + ":0";
            case Node.DLNA:
                Device device = (Device) node.getItem();
                if (!identity.endsWith(":0")) {
                    identity = device.getIdentity().getUdn().getIdentifierString() + ":" + identity;
                    identity += ":" + node.getTitle();
                } else {
                    identity += ":" + device.getDetails().getFriendlyName();
                }

                MediaDirHelper.addAndSave(mContext, new SaveItem(SaveItem.TYPE_LOCAL, identity));
                identity = Base64Helper.encode(identity);
                deviceType = "dlna";
                break;
            case Node.SAMBA_CATEGORY:
            case Node.SAMBA_DEVICE:
            case Node.SAMBA:
                deviceType = "samba";
                if (node.getItem() instanceof SmbFile) {
                    identity = createSmbId((SmbFile) node.getItem());
                } else {
                    identity = node.getItem().toString();
                }
                break;
            case Node.INTERNAL_CATEGORY:
            case Node.INTERNAL_DEVICE:
            case Node.INTERNAL:
                break;
            case Node.ROOT:
                break;
        }

        uriBuilder.append(deviceType);
        uriBuilder.append('/');
        uriBuilder.append(identity);
        uriBuilder.append('/');
        uriBuilder.append(FileItem.VIDEO);
        uriBuilder.append('/');
        uriBuilder.append(isPrivate ? "1" : "0");

        bundle.putString("data", uriBuilder.toString());

        mView.setResult(Activity.RESULT_OK, bundle);
    }

    @Override
    public void checkAuthData(Node node, Bundle data) {
        switch (node.getType()) {
            case Node.DLNA_CATEGORY:
            case Node.DLNA_DEVICE:
            case Node.DLNA:
                break;
            case Node.SAMBA_CATEGORY:
            case Node.SAMBA_DEVICE:
            case Node.SAMBA:
                checkSmbAuthData(node, data);
                break;
        }
    }

    @Override
    public boolean isAlreadyAuth(Node node) {
        SmbFile smbFile = (SmbFile) node.getItem();

        return mSmbAuthMap.containsKey(SambaAuthHelper.getSmbAuthKey(smbFile));
    }

    private String createSmbId(@NonNull SmbFile smbFile) {
        String raw = createSmbBasicAuthUrl(smbFile);

        return Base64Helper.encode(raw);
    }

    private String createSmbBasicAuthUrl(SmbFile smbFile) {
        StringBuilder builder = new StringBuilder(DEFAULT_ADDRESS);
        CIFSContext cifsContext =
                mSmbAuthMap.get(SambaAuthHelper.getSmbAuthKey(smbFile));

        MediaDirHelper.addAndSave(mContext, new SaveItem(SaveItem.TYPE_SMB, smbFile.getCanonicalPath()));
        if (cifsContext == null || cifsContext.getCredentials().isAnonymous()) {
            return smbFile.getCanonicalPath();
        }

        if (!TextUtils.isEmpty(((NtlmPasswordAuthenticator) cifsContext.getCredentials()).getName())) {
            builder.append(((NtlmPasswordAuthenticator) cifsContext.getCredentials()).getName().replace('\\', ';'));
            builder.append(':');
            builder.append(((NtlmPasswordAuthenticator) cifsContext.getCredentials()).getPassword());
            builder.append('@');
        }

        return smbFile.getCanonicalPath().replaceFirst(DEFAULT_ADDRESS, builder.toString());
    }

    private void checkSmbAuthData(final Node node, Bundle data) {
        CIFSContext cifsContext = null;
        boolean isAnonymous = data.getBoolean("anonymous");
        boolean checkOnly = data.getBoolean("checkOnly");

        if (isAnonymous) {
            cifsContext = SingletonContext.getInstance().withAnonymousCredentials();
        } else {
            String username = data.getString("username");
            String password = data.getString("password");
            String domain = "";
            int i = 0;

            if ((i = username.indexOf('\\')) != -1) {
                domain = username.substring(0, i);
                username = username.substring(i + 1);
            }

            cifsContext = SingletonContext.getInstance().withCredentials(new NtlmPasswordAuthenticator(domain, username, password));
        }

        SmbFile smbFile = null;
        if (node.getItem() instanceof SmbFile) {
            SmbFile f = (SmbFile) node.getItem();
            try {
                smbFile = new SmbFile(f.getURL(), cifsContext);
                node.setItem(smbFile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

        if (checkOnly) {
            smbAuth(node, cifsContext, data);
        } else {
            sambaGetPath(node, cifsContext);
        }
    }

    private void smbAuth(final Node node,
                         final CIFSContext cifsContext,
                         final Bundle data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SmbFile smbFile = null;

                if (node.getItem() instanceof SmbFile) {
                    smbFile = (SmbFile) node.getItem();
                    if (smbFile == null) {
                        Log.e(TAG, "Item of node can't be null.");
                        return;
                    }
                } else {
                    setLoadingViewVisible(false);
                    return;
                }


                try {
                    if (mSelectType == SELECT_DIR) {
                        if (!smbFile.getPath().endsWith("/")) {
                            showError(node, mContext.getString(R.string.must_end_with_divide));
                            showAuthDialog(node, data);
                            return;
                        } else if (SmbFileHelper.getType(smbFile) != SmbFile.TYPE_SHARE) {
                            showError(node, mContext.getString(R.string.not_a_directory));
                            showAuthDialog(node, data);
                            return;
                        }
                    }

                    smbFile.exists();
                } catch (SmbException e) {
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
                            if (cifsContext.getCredentials().isAnonymous()) {
                                try {
                                    node.setItem(new SmbFile(smbFile.getURL(), SambaAuthHelper.GUEST));
                                } catch (MalformedURLException ex) {
                                    ex.printStackTrace();
                                }
                                smbAuth(node, SambaAuthHelper.GUEST, data);
                            } else {
                                showError(node, mContext.getString(R.string.login_failed_try_again));
                                showAuthDialog(node, data);
                            }
                            return;
                        case SmbException.NT_STATUS_UNSUCCESSFUL:
                            showError(node, mContext.getString(R.string.can_not_access));
                            showAuthDialog(node, data);
                            return;
                    }
                } catch (Exception e) {
                    showError(node, mContext.getString(R.string.can_not_access));
                    showAuthDialog(node, data);
                    return;
                }
                mSmbAuthMap.put(SambaAuthHelper.getSmbAuthKey(smbFile), cifsContext);
                if (mSelectType == SELECT_DIR) {
                    showSetPrivateDialog(node);
                } else {
                    onSelectFile(node);
                }

                setLoadingViewVisible(false);
            }
        }).start();
    }

    private void localDevice(Node parent) {
        String external = StorageHelper.getFlashStoragePath(mContext);
        if (!TextUtils.isEmpty(external)) {
            String dirName = external.substring(external.lastIndexOf('/') + 1);
            Node node = new Node(external, dirName, currentNodeType(parent), null);
            Log.d("BrowsePathPresenter udisk ", dirName);
            parent.addChild(node);
        }

        List<String> UDisks = StorageHelper.getUSBPaths(mContext);
        List<String> sdCards = StorageHelper.getSdCardPaths(mContext);
        List<String> SSDs = StorageHelper.getPciePaths(mContext);
        List<String> hardDisks = StorageHelper.getHardDiskPaths(mContext);

        for (String path : UDisks) {
            String dirName = path.substring(path.lastIndexOf('/') + 1);
            Node node = new Node(path, dirName, Node.USB_DEVICE, null);
            Log.d("BrowsePathPresenter udisk ", dirName);
            parent.addChild(node);
        }

        for (String path : sdCards) {
            String dirName = path.substring(path.lastIndexOf('/') + 1);
            Node node = new Node(path, dirName, Node.SDCARD_DEVICE, null);
            Log.d("BrowsePathPresenter sdcard ", dirName);
            parent.addChild(node);
        }
        for (String path : SSDs) {
            String dirName = path.substring(path.lastIndexOf('/') + 1);
            Node node = new Node(path, dirName, Node.PCIE_DEVICE, null);
            Log.d("BrowsePathPresenter SSD ", dirName);
            parent.addChild(node);
        }
        for (String path : hardDisks) {
            String dirName = path.substring(path.lastIndexOf('/') + 1);
            Node node = new Node(path, dirName, Node.SATA_DEVICE, null);
            Log.d("BrowsePathPresenter HardDisk ", dirName);
            parent.addChild(node);
        }
        updateTreeView(parent);
    }

    private void localPath(Node parent) {
        File parentFile = new File(parent.getId());
        File[] files = parentFile.listFiles();

        if (files == null) {
            updateTreeView(parent);
            return;
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if ((o1.isFile() && o2.isFile())
                        || (o1.isDirectory() && o2.isDirectory())) {
                    return o1.getName().compareTo(o2.getName());
                } else if (o1.isFile()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        for (File f : files) {
            if (mSelectType == SELECT_DIR && !f.isDirectory()) {
                continue;
            }

            Node node = new Node(f.getPath(), f.getName(), currentNodeType(parent), f);
            parent.addChild(node);
        }

        updateTreeView(parent);
    }

    private int currentNodeType(Node parent) {
        int type = Node.EXTERNAL;
        if (parent.getType() == Node.EXTERNAL_CATEGORY) {
            type = Node.EXTERNAL_DEVICE;
        }

        return type;
    }

    private void DLNAPath(Node node) {
        if (node.getType() == Node.DLNA_CATEGORY) {
            DLNADevice(node);
        } else {
            DLNAGetPath(node);
        }
    }

    private void DLNADevice(Node parent) {
        if (Constants.devices == null)
            Constants.init();
        for (Device device : Constants.devices) {
            Service service = device.findService(new UDAServiceType("ContentDirectory"));
            if (service == null) {
                continue;
            }

            Node node = new Node(device.getIdentity().getUdn().getIdentifierString(),
                    device.getDetails().getFriendlyName(),
                    Node.DLNA_DEVICE,
                    device);

            parent.addChild(node);
        }

        updateTreeView(parent);
    }

    private void DLNAGetPath(final Node node) {
        String containerId;
        final Device device = (Device) node.getItem();

        if (node.getType() == Node.DLNA_DEVICE) {
            containerId = "0";
        } else {
            containerId = node.getId();
        }

        Service service = device.findService(new UDAServiceType("ContentDirectory"));

        Constants.upnpService.getControlPoint().execute(
                new Browse(service, containerId, BrowseFlag.DIRECT_CHILDREN,
                        Browse.CAPS_WILDCARD, 0, null,
                        new SortCriterion(true, "dc:title")) {
                    @Override
                    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                        List<Container> containers = didl.getContainers();

                        if (containers != null) {
                            for (Container container : containers) {
                                Node child = new Node(
                                        container.getId(),
                                        container.getTitle(),
                                        Node.DLNA,
                                        device);

                                node.addChild(child);
                            }
                        }

                        if (mSelectType != SELECT_DIR) {
                            List<Item> items = didl.getItems();
                            if (items != null) {
                                for (Item item : items) {
                                    Node child = new Node(item.getId(), item.getTitle(), Node.DLNA, item);

                                    node.addChild(child);
                                }
                            }
                        }

                        updateTreeView(node);
                    }

                    @Override
                    public void updateStatus(Status status) {

                    }

                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation,
                                        String defaultMsg) {
                        showError(node, defaultMsg);
                        mView.fallBackParent(node);
                    }
                });
    }

    private void sambaPath(Node node) {
        SmbFile smbFile = null;
        node.setChildren(null);

        switch (node.getType()) {
            case Node.SAMBA_DEVICE:
                sambaGetPath(node, SingletonContext.getInstance().withAnonymousCredentials());
                break;
            default:
                CIFSContext cifsContext = null;
                if (node.getItem() instanceof SmbFile) {
                    smbFile = (SmbFile) node.getItem();
                    cifsContext = mSmbAuthMap.get(SambaAuthHelper.getSmbAuthKey(smbFile));

                    try {
                        if (cifsContext == null
                                && SmbFileHelper.getType(smbFile) == SmbFile.TYPE_SHARE) {
                            showAuthDialog(node, false);
                            return;
                        }
                    } catch (SmbException e) {
                        e.printStackTrace();
                    }
                }
                sambaGetPath(node, cifsContext);
                break;
        }
    }

    private void sambaGetPath(final Node node, final CIFSContext cifsContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SmbFile smbFile = node.getItem() instanceof SmbFile
                        ? (SmbFile) node.getItem() : null;
                String url = node.getId();
                if (!url.startsWith("smb://")) {
                    url = DEFAULT_ADDRESS;
                }


                try {
                    int type = Node.SAMBA_DEVICE;

                    if (cifsContext != null) {
                        smbFile = new SmbFile(url, cifsContext);
                        mSmbAuthMap.put(SambaAuthHelper.getSmbAuthKey(smbFile), cifsContext);
                    } else {
                        smbFile = new SmbFile(url, SingletonContext.getInstance().withAnonymousCredentials());
                    }

                    if (node.getType() != Node.SAMBA_CATEGORY) {
                        if (SmbFileHelper.getType(smbFile) != SmbFile.TYPE_WORKGROUP) {
                            type = Node.SAMBA;
                        }
                    }

                    SmbFile[] files = smbFile.listFiles();

                    for (SmbFile f : files) {
                        int fileType = SmbFileHelper.getType(f);
                        if ((mSelectType == SELECT_DIR && SmbFileHelper.isFile(f))
                                || fileType == SmbFile.TYPE_NAMED_PIPE
                                || fileType == SmbFile.TYPE_COMM
                                || fileType == SmbFile.TYPE_PRINTER) {
                            continue;
                        }

                        Node child = new Node(
                                f.getCanonicalPath(),
                                f.getName().replace('/', '\0'),
                                type,
                                f);

                        node.addChild(child);
                    }
                    updateTreeView(node);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mView.fallBackParent(node);
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
                            if (cifsContext == null || cifsContext.getCredentials().isAnonymous()) {
                                // 尝试使用GUEST帐号
                                sambaGetPath(node, SambaAuthHelper.GUEST);
                            } else {
                                showError(node, mContext.getString(R.string.login_failed_try_again));
                                mSmbAuthMap.remove(SambaAuthHelper.getSmbAuthKey(smbFile));
                                showAuthDialog(node, false);
                            }
                            break;
                        default:
                            showError(node, mContext.getString(R.string.samba_access_error));
                            mView.fallBackParent(node);
                            break;
                    }
                }
            }
        }).start();
    }

    private void updateTreeView(Node root) {
        if (isAvailable(root)) {
            Message message = mHandler.obtainMessage(MSG_UPDATE_TREE_VIEW, root);
            message.sendToTarget();
        }
    }

    private void setLoadingViewVisible(boolean show) {
        Message message = mHandler.obtainMessage(MSG_SET_LOADING_VIEW, show);
        message.sendToTarget();
    }

    private void showError(Node node, String msg) {
        if (isAvailable(node)) {
            Message message = mHandler.obtainMessage(MSG_ERROR, msg);
            message.sendToTarget();
        }

    }

    private void showAuthDialog(Node node, @NonNull Bundle bundle) {
        if (isAvailable(node)) {
            Message message = mHandler.obtainMessage(MSG_SHOW_AUTH_DIALOG);

            bundle.putSerializable("node", node);

            message.setData(bundle);
            message.sendToTarget();
        }
    }

    private void showAuthDialog(Node node, boolean checkOnly) {
        if (isAvailable(node)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("checkOnly", false);
            showAuthDialog(node, bundle);
        }
    }

    private void showSetPrivateDialog(Node node) {
        if (isAvailable(node)) {
            Message message = mHandler.obtainMessage(MSG_SHOW_SET_PRIVATE_DIALOG, node);
            message.sendToTarget();
        }
    }

    private boolean isAvailable(Node node) {
        if (mCancelTasks.get(node.getFlag()) != null) {
            mCancelTasks.remove(node.getFlag());
            return false;
        }

        return true;
    }
}
