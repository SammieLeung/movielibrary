package com.firefly.dlna.httpserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class RegistrationFactory implements IRegistrationFactory {
    private final static String TAG = RegistrationFactory.class.getSimpleName();

    private List<String> mIps = new ArrayList<>();
    private IServer mServer;
    private Context mContext;
    private ServerCache mServerCache;

    private class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, ">>>>>>>>>>>>>>>>: " + intent.getAction());

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()) ||
                    WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                refreshIps();
            }
        }
    }

    private void refreshIps() {
        List<String> ips = new ArrayList<>();

        try {
            InetAddress inetAddress;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    inetAddress = enumIpAddr.nextElement();
                    // 过滤loop和ipv6
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                            && inetAddress instanceof Inet4Address) {
                        Log.d(TAG, ">>> ip: " + inetAddress.getHostAddress());
                        ips.add(inetAddress.getHostAddress());
                    }
                }
            }

            synchronized (this) {
                mIps.clear();
                mIps.addAll(ips);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    RegistrationFactory(Context context, ServerCache cache, IServer server) {
        mContext = context;
        mServerCache = cache;
        mServer = server;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        context.registerReceiver(new NetworkChangeReceiver(), intentFilter);
    }

    @Override
    public Registration[] generate(String uri, String mimeType) {
        List<Registration> list = new ArrayList<>();
        String uuid = UUID.nameUUIDFromBytes(uri.getBytes()).toString();

        synchronized (this) {
            for (String ip : mIps) {
                ProtocolInfo protocolInfo = new ProtocolInfo(MimeType.valueOf(mimeType));
                String _url = "http://" + ip + ":" + mServer.getPort() + "/"
                        + mimeType.substring(0, mimeType.indexOf('/')) + "/"
                        + uuid + "/"
                        + Uri.encode(uri.substring(uri.lastIndexOf('/') + 1));

                list.add(new Registration(protocolInfo, _url));

                CacheValue cacheValue = new CacheValue(uri, mimeType);
                mServerCache.set(uuid, cacheValue);
            }
        }


        return list.toArray(new Registration[0]);
    }

    @Override
    public Registration[] generate(String uri) {
        String mimeType = URLConnection.guessContentTypeFromName(uri);

        return generate(uri, mimeType);
    }
}
