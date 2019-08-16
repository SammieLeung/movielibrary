package com.firefly.dlna.device;

import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractDeviceBuilder<T extends AbstractDeviceBuilder<T, D>, D> {
    protected String mName;
    protected String mFriendlyName;
    protected String mManufacturer;
    protected String mDesc;
    protected String mModelUrl;
    protected ArrayList<DLNADoc> mDLNADocs = new ArrayList<>();
    protected ArrayList<String> mDLNACaps = new ArrayList<>();
    protected ArrayList<Icon> mIcons = new ArrayList<>();
    protected ArrayList<LocalService> mExtraService = new ArrayList<>();

    public AbstractDeviceBuilder(String name, String friendlyName) {
        mName = name;
        mFriendlyName = friendlyName;
        mModelUrl = "http://www.t-firefly.com";
    }

    /**
     * Set the device name
     * @param name device name
     * @return object of subclass
     */
    public final T setName(String name) {
        mName = name;

        return getObject();
    }

    /**
     * Set friendlyName of device, which is a human readable string
     * @param friendlyName friend name string
     * @return object of subclass
     */
    public final T setFriendlyName(String friendlyName) {
        mFriendlyName = friendlyName;

        return getObject();
    }


    public final T setManufacturer(String manufacturer) {
        mManufacturer = manufacturer;

        return getObject();
    }

    public final T setDescription(String description) {
        mDesc = description;

        return getObject();
    }

    public final T setModeUrl(String url) {
        mModelUrl = url;

        return getObject();
    }

    public final T addDLNADoc(DLNADoc dlnaDoc) {
        mDLNADocs.add(dlnaDoc);

        return getObject();
    }

    public final T addDDLNACaps(String dlnaCap) {
        mDLNACaps.add(dlnaCap);

        return getObject();
    }

    public final T setDDLNACaps(String[] dlnaCaps) {
        mDLNACaps.clear();
        mDLNACaps.addAll(Arrays.asList(dlnaCaps));

        return getObject();
    }

    public final T addIcon(Icon icon) {
        mIcons.add(icon);

        return getObject();
    }

    /**
     * Add extra service to DLNA device
     * @param service LocalService wanted to add
     * @return object of subclass
     */
    public final T addExtraService(LocalService service) {
        mExtraService.add(service);

        return getObject();
    }

    /**
     * Get subclass object
     * @return object of subclass
     */
    public abstract T getObject();

    /**
     * Create target object
     * @return target object
     */
    public abstract D create();
}
