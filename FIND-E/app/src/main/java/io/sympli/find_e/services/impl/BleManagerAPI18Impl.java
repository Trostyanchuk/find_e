package io.sympli.find_e.services.impl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.UUID;

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
    private Context context;
    private BluetoothDevice targetDevice;
    private UUID deviceUUId;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    private final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    private final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    private final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    private final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                deviceUUId = gatt.getServices().get(0).getCharacteristics().get(0).getUuid();
//                broadcast.postEvent(new BleDeviceFoundEvent(true));
                List<BluetoothGattCharacteristic> characteristics = gatt.getServices().get(0).getCharacteristics();
                Log.d("TAG", "TAG");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }
    };

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null && device.getName().equals(NAME) && scanning) {
                scanning = false;
                targetDevice = device;
                mBluetoothGatt = targetDevice.connectGatt(context, false, mGattCallback);
                bluetoothAdapter.stopLeScan(leScanCallback);
//                broadcast.postEvent(new BleDeviceFoundEvent(true));
            }
        }
    };

    public BleManagerAPI18Impl(Context context, IBroadcast broadcast) {
        this.context = context;
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
                    broadcast.postEvent(new BleDeviceFoundEvent(false));
                }
            }
        }, SCAN_PERIOD);
        scanning = true;
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    @Override
    public void connect() {
        if (targetDevice != null) {
            mBluetoothGatt = targetDevice.connectGatt(context, false, mGattCallback);
        }
    }

    @Override
    public void disconnect() {
        scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }
}
