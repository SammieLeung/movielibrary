package com.firefly.dlna.device.dmr;

import com.firefly.dlna.device.AbstractDeviceBuilder;

import org.fourthline.cling.binding.LocalServiceBinder;
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
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.util.UUID;

public final class DmrBuilder extends AbstractDeviceBuilder<DmrBuilder, DmrDevice> {
    private final LocalServiceBinder mBinder = new AnnotationLocalServiceBinder();
    private final LastChange mRenderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    private IPlayer mPlayer = null;

    public DmrBuilder() {
        super("Station OS DMR", "Station OS DMR");
    }

    public DmrBuilder addPlayer(IPlayer player) {
        mPlayer = player;

        return this;
    }

    @Override
    public DmrBuilder getObject() {
        return this;
    }

    @Override
    public final DmrDevice create() {
        DmrDevice device = new DmrDevice();
        UDN udn = new UDN(UUID.nameUUIDFromBytes(mName.getBytes()));
        DLNADoc[] dlnaDocs = null;
        DLNACaps dlnaCaps = null;
        Icon[] icons = mIcons.toArray(new Icon[0]);

        mDLNADocs.add(new DLNADoc("DMR", DLNADoc.Version.V1_5));
        dlnaDocs = mDLNADocs.toArray(new DLNADoc[0]);
        mDLNACaps.add("av-upload");
        mDLNACaps.add("image-upload");
        mDLNACaps.add("audio-upload");
        dlnaCaps = new DLNACaps(mDLNACaps.toArray(new String[0]));


        LocalService[] services = null;

        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService<DmrConnectionManagerService> connectionManagerService =
                mBinder.read(DmrConnectionManagerService.class);
        DefaultServiceManager connectionManager =
                new DefaultServiceManager<DmrConnectionManagerService>(connectionManagerService) {
                    @Override
                    protected DmrConnectionManagerService createServiceInstance() throws Exception {
                        return new DmrConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<DmrAVTransportService> avTransportService = mBinder.read(DmrAVTransportService.class);
        LastChangeAwareServiceManager avTransportManager =
                new LastChangeAwareServiceManager<DmrAVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected DmrAVTransportService createServiceInstance() throws Exception {
                        return new DmrAVTransportService(DmrStateMachine.class,
                                DmrNoMediaPresent.class, DmrAVTransport.class, mPlayer);
                    }
                };
        avTransportService.setManager(avTransportManager);

        // The Rendering Control just passes the calls on to the backend players
        LocalService<AudioRenderingControl> renderingControlService = mBinder.read(AudioRenderingControl.class);
        LastChangeAwareServiceManager renderingControlManager =
                new LastChangeAwareServiceManager<AudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected AudioRenderingControl createServiceInstance() throws Exception {
                        return new AudioRenderingControl(mRenderingControlLastChange, mPlayer);
                    }
                };
        renderingControlService.setManager(renderingControlManager);

        mExtraService.add(connectionManagerService);
        mExtraService.add(avTransportService);
        mExtraService.add(renderingControlService);
        services = mExtraService.toArray(new LocalService[0]);

        try {
            LocalDevice localDevice = new LocalDevice(
                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            mFriendlyName,
                            new ManufacturerDetails(mManufacturer),
                            new ModelDetails(mName, mDesc, "1", mModelUrl),
                            dlnaDocs,
                            dlnaCaps
                    ),
                    icons,
                    services
            );
            device.setDevice(localDevice);
        } catch (ValidationException e) {
            e.printStackTrace();
            return null;
        }
        device.setAvTransportServiceManager(avTransportManager);
        device.setRenderingControlManager(renderingControlManager);
        device.runLastChangePushThread();

        return device;
    }
}
