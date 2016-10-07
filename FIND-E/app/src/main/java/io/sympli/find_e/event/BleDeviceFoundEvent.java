package io.sympli.find_e.event;

import android.bluetooth.BluetoothDevice;

public class BleDeviceFoundEvent {

    private boolean found;
    private BluetoothDevice device;

    public BleDeviceFoundEvent(boolean found, BluetoothDevice device) {
        this.found = found;
        this.device = device;
    }

    public boolean isFound() {
        return found;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
