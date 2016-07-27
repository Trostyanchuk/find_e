package io.sympli.find_e.services;

import android.app.Activity;

public interface IBluetoothManager {

    int REQUEST_ENABLE_BT = 1;
    long SCAN_PERIOD = 10000;

    boolean isBluetoothEnabled();

    void enableBluetooth(Activity activity);

    void searchKey();

    void connect();

    void disconnect();
}
