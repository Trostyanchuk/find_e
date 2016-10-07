package io.sympli.find_e.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.List;

public final class ConnectivityUtil {

    private static WifiManager wifiManager;
    private static ConnectivityManager connectivityManager;

    public static void setSilentWiFiArea(Context context, boolean turnOn) {
        wifiManager = getWifiManager(context);
        connectivityManager = getConnectivityManager(context);
        String currentSSID = getCurrentNetworkSSID(context);
        List<String> savedSSIDs = LocalStorageUtil.getSilentWifiSSIDs();

    }

    public static boolean isWiFiInSilentArea(Context context) {
        wifiManager = getWifiManager(context);
        connectivityManager = getConnectivityManager(context);
        String currentSSID = getCurrentNetworkSSID(context);
        List<String> savedSSIDs = LocalStorageUtil.getSilentWifiSSIDs();
        return savedSSIDs.contains(currentSSID);
    }

    public static String getCurrentNetworkSSID(Context context) {
        wifiManager = getWifiManager(context);
        connectivityManager = getConnectivityManager(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            if (!wifiManager.isWifiEnabled()) {
                return null;
            } else {
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                    return connectionInfo.getSSID();
                }
            }
        }
        return null;
    }


    private static WifiManager getWifiManager(Context context) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager;
    }

    private static ConnectivityManager getConnectivityManager(Context context) {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connectivityManager;
    }

}
