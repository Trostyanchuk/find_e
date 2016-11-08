package io.sympli.find_e.services.impl;

public interface BleServiceConstant {

    String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    String ACTION_RSSI = "com.example.bluetooth.le.ACTION_RSSI";
    String EXTRA_DATA_RAW = "com.example.bluetooth.le.EXTRA_DATA_RAW";
    String EXTRA_UUID_CHAR = "com.example.bluetooth.le.EXTRA_UUID_CHAR";
    String EXTRA_RSSI = "com.example.bluetooth.le.EXTRA_RSSI";

    int STATE_DISCONNECTED = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;

    String LIST_NAME = "NAME";
    String LIST_UUID = "UUID";

    int REQUEST_ENABLE_BT = 1;

    long SCAN_PERIOD = 10000;
}
