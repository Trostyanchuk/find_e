package io.sympli.find_e.services.impl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import io.sympli.find_e.event.BleDeviceFoundEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManagerAPI18Impl implements IBluetoothManager {

    private static final String NAME = "Security Tag";

    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;
    private IBroadcast broadcast;
    private BluetoothDevice targetDevice;

    private BluetoothManager mBluetoothManager;

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null && device.getName().equals(NAME) && scanning) {
                scanning = false;
                targetDevice = device;
                bluetoothAdapter.stopLeScan(leScanCallback);
                broadcast.postEvent(new BleDeviceFoundEvent(true, targetDevice));
            }
        }
    };

    public BleManagerAPI18Impl(Context context, IBroadcast broadcast) {
        mBluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = mBluetoothManager.getAdapter();
        this.handler = new Handler();
        this.broadcast = broadcast;
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanning) {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    broadcast.postEvent(new BleDeviceFoundEvent(false, null));
                }
            }
        }, SCAN_PERIOD);
        scanning = true;
        bluetoothAdapter.startLeScan(leScanCallback);
    }
}
