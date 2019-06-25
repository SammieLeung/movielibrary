package com.firefly.filepicker.data;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by rany on 18-1-2.
 */

public class Constants {
    public final static int DEVICE_NOT_SCAN = 0;
    public final static int DEVICE_SCAN_STARTED = 1;
    public final static int DEVICE_SCAN_FINISHED = 2;

    public static UpnpService upnpService;

    public static List<Device> devices = new CopyOnWriteArrayList<>();
    public static Map<String, Device> deviceHashMap = new ConcurrentHashMap<>();
    public static Map<String, Integer> newDevices = new HashMap<>();
}
