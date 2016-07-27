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
import android.util.Log;

import io.sympli.find_e.services.IBluetoothManager;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManagerAPI18Impl implements IBluetoothManager {

    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;
    private Context context;

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //TODO post event about results
            Log.d("TAG", "");
        }
    };

    public BleManagerAPI18Impl(Context context) {
        this.context = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.handler = new Handler();
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
                scanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }, SCAN_PERIOD);
        scanning = true;
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {
        scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }
}
