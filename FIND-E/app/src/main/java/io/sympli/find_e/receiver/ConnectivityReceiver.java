package io.sympli.find_e.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import io.sympli.find_e.ApplicationController;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static WifiManager wifiManager;
    private static ConnectivityManager connectivityManager;

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;

        wifiManager = getWifiManager(context);
        connectivityManager = getConnectivityManager(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            ApplicationController.setWifiEnabled(wifiManager.isWifiEnabled());
        } else {
            ApplicationController.setWifiEnabled(false);
        }
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
