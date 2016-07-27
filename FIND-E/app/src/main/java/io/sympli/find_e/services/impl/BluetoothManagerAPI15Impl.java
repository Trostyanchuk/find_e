package io.sympli.find_e.services.impl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.Set;

import io.sympli.find_e.services.IBluetoothManager;

public class BluetoothManagerAPI15Impl implements IBluetoothManager {

    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    public BluetoothManagerAPI15Impl(Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    @Override
    public void enableBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void searchKey() {
        bluetoothAdapter.startDiscovery();
        //TODO calllback is in receiver
    }

    @Override
    public void connect() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress()); //TODO
            }
        }
    }

    @Override
    public void disconnect() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
