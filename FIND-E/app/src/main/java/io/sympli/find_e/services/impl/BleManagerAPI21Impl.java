package io.sympli.find_e.services.impl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.utils.UIUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleManagerAPI21Impl implements IBluetoothManager {

    private BluetoothAdapter bluetoothAdapter;
    public BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler;
    private Context context;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d("TAG", "");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("TAG", "");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("TAG", "");
        }
    };

    public BleManagerAPI21Impl(Context context) {
        this.context = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
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
        bluetoothLeScanner.startScan(leScanCallback);
    }

    @Override
    public void connect() {
    }
    
    @Override
    public void disconnect() {
        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallback);
    }
}
