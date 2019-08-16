package com.firefly.dlna.device.dms;

import com.firefly.dlna.device.AbstractDeviceBuilder;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import java.util.UUID;

public final class DmsBuilder extends AbstractDeviceBuilder<DmsBuilder, DmsDevice> {
    private final static String TAG = DmsBuilder.class.getSimpleName();
    private final static String DEVICE_TYPE = "MediaServer";
    private final static int DEVICE_VERSION = 1;

    private IFileStore mFileStore = null;

    public DmsBuilder() {
        super("Station OS DMS", "Station OS DMS");
    }

    /**
     * 设置文件信息获取接口，**必须设置**
     * @param fileStore 实现{@link IFileStore}的类的实例
     */
    public DmsBuilder setFileStore(IFileStore fileStore) {
        mFileStore = fileStore;

        return this;
    }

    @Override
    public DmsBuilder getObject() {
        return this;
    }

    @Override
    public DmsDevice create() {
        if (mFileStore == null) {
            throw new IllegalArgumentException("FileStore must be set.");
        }

        DeviceType type = new UDADeviceType(DEVICE_TYPE, DEVICE_VERSION);
        UDN udn = new UDN(UUID.nameUUIDFromBytes(mName.getBytes()));
        Icon[] icons = mIcons.toArray(new Icon[0]);
        DmsDevice device = new DmsDevice();
        DLNADoc[] dlnaDocs = null;
        DLNACaps dlnaCaps = null;
        LocalService[] services = null;
        dlnaDocs = mDLNADocs.toArray(new DLNADoc[0]);
        dlnaCaps = new DLNACaps(mDLNACaps.toArray(new String[0]));

        DeviceDetails details = new DeviceDetails(mFriendlyName,
                new ManufacturerDetails(mManufacturer),
                new ModelDetails(mName, mDesc, "v1", mModelUrl),
                dlnaDocs,
                dlnaCaps
                );

        LocalService service = new AnnotationLocalServiceBinder()
                .read(ContentDirectoryService.class);
        service.setManager(new DefaultServiceManager<ContentDirectoryService>(service,
                ContentDirectoryService.class) {
            @Override
            protected ContentDirectoryService createServiceInstance() throws Exception {
                return new ContentDirectoryService(device);
            }
        });

        mExtraService.add(service);
        services = mExtraService.toArray(new LocalService[0]);


        try {
            LocalDevice localDevice = new LocalDevice(
                    new DeviceIdentity(udn), type, details, icons, services);

            device.setDevice(localDevice);
        } catch (ValidationException e) {
            return null;
        }
        device.setFileStore(mFileStore);

        return device;
    }
}
