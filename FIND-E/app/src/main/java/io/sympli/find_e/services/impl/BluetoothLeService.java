package io.sympli.find_e.services.impl;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.event.BleDeviceFoundEvent;
import io.sympli.find_e.event.FixLocationLostEvent;
import io.sympli.find_e.event.GattUpdateEvent;
import io.sympli.find_e.event.MakePictureEvent;
import io.sympli.find_e.event.OnButtonTouchEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.BLEUtil;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {

    private static final String TAG = BluetoothLeService.class.getSimpleName();
    private static final long RSSI_READ_DELAY = 3000;
    private static final String NAME = "Security Tag";

    @Inject
    IBroadcast broadcast;

    private Handler readRSSIHandler;
    private Runnable readRSSIRunnable;

    private boolean isScanning;
    private Handler scanningHandler;
    private BluetoothDevice targetDevice;

    private boolean isDontDisturbMode;
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = BleServiceConstant.STATE_DISCONNECTED;
    private int powerLevel = 100;  //default power level
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null && device.getName().equals(NAME) && isScanning) {
                isScanning = false;
                targetDevice = device;
                mBluetoothAdapter.stopLeScan(leScanCallback);
                broadcast.postEvent(new BleDeviceFoundEvent(true, targetDevice)); //TODO
            }
        }
    };
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            Log.d(TAG, "onCharacteristicChanged " + characteristic.getUuid().toString() + " " +
                    data[0]);

            if (characteristic.getUuid().toString().equals(BleState.SINGLE_TAP)) {
                if (mConnectionState == BleServiceConstant.STATE_BEEPING) {
                    mConnectionState = BleServiceConstant.STATE_CONNECTED;
                    broadcast.postEvent(new OnButtonTouchEvent(false));
                } else if (mConnectionState == BleServiceConstant.STATE_CONNECTED) {
                    mConnectionState = BleServiceConstant.STATE_BEEPING;
                    broadcast.postEvent(new OnButtonTouchEvent(true));
                }
                broadcast.postEvent(new MakePictureEvent());
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            Log.d(TAG, "onCharacteristicRead " + characteristic.getUuid().toString());
            if (characteristic.getUuid().toString().equalsIgnoreCase(BleState.BATTERY_SERVICE) &&
                    characteristic.getValue() != null) {
                powerLevel = characteristic.getValue()[0];
            }
            readCharacteristic(BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.BATTERY_SERVICE));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcast.postEvent(new GattUpdateEvent(BleServiceConstant.ACTION_DATA_AVAILABLE));
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.d(TAG, "onDescriptorRead " + descriptor.getUuid().toString());
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcast.postEvent(new GattUpdateEvent(BleServiceConstant.ACTION_GATT_CONNECTED));
                mConnectionState = BleServiceConstant.STATE_CONNECTING;

                mBluetoothGatt.readRemoteRssi();
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                broadcast.postEvent(new GattUpdateEvent(BleServiceConstant.ACTION_GATT_DISCONNECTED));
                broadcast.postEvent(new FixLocationLostEvent());
                mConnectionState = BleServiceConstant.STATE_DISCONNECTED;
                discoverDevices();
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> deviceList = getSupportedGattServices();
                String uuid;
                mGattCharacteristics = new ArrayList<>();

                for (final BluetoothGattService gattService : deviceList) {
                    final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        charas.add(gattCharacteristic);
                        setCharacteristicNotification(gattCharacteristic, true);
                        uuid = gattCharacteristic.getUuid().toString();
                        if (uuid.equalsIgnoreCase(BleState.BATTERY_SERVICE)) {
                            readCharacteristic(gattCharacteristic);
                        }
                    }

                    mGattCharacteristics.add(charas);
                }
                mConnectionState = BleServiceConstant.STATE_CONNECTED;
                broadcast.postEvent(new GattUpdateEvent(BleServiceConstant.ACTION_GATT_SERVICES_DISCOVERED));
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcast.postEvent(new GattUpdateEvent(BleServiceConstant.EXTRA_RSSI)
                        .setRssi(rssi));
                readRemoteRSSIHandler();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationController.getComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopReadRemoteRSSIHandler();
        close();
    }

    //__________________________________________________________________________

    public void discoverDevices() {
        scanningHandler = new Handler(Looper.getMainLooper());
        scanningHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScanning) {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                    broadcast.postEvent(new BleDeviceFoundEvent(false, null));
                }
            }
        }, BleServiceConstant.SCAN_PERIOD);
        isScanning = true;
        mBluetoothAdapter.startLeScan(leScanCallback);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {

            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BleServiceConstant.STATE_CONNECTED;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = BleServiceConstant.STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean isDisconnected() {
        return mConnectionState == BleServiceConstant.STATE_DISCONNECTED;
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isDontDisturbMode() {
        return isDontDisturbMode;
    }

    public int getPowerLevel() {
        return this.powerLevel >= 0 && this.powerLevel <= 100 ? powerLevel : 100;
    }

    public void beep() {
        if (mConnectionState == BleServiceConstant.STATE_CONNECTED) {
            mConnectionState = BleServiceConstant.STATE_BEEPING;
            sendCharacteristicUpdateToDevice(TagAction.IMMEDIATE_ALERT_TURN_ON);
        } else if (mConnectionState == BleServiceConstant.STATE_BEEPING) {
            mConnectionState = BleServiceConstant.STATE_CONNECTED;
            sendCharacteristicUpdateToDevice(TagAction.IMMEDIATE_ALERT_TURN_OFF);
        }
    }

    public void sendCharacteristicUpdateToDevice(TagAction tagAction) {
        BluetoothGattCharacteristic characteristic = null;
        switch (tagAction) {
            case IMMEDIATE_ALERT_TURN_ON: {
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.IMMEDIATE_ALERT);
                if (characteristic != null) {
                    characteristic.setValue(new byte[]{2});
                }
                break;
            }
            case IMMEDIATE_ALERT_TURN_OFF: {
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.IMMEDIATE_ALERT);
                if (characteristic != null) {
                    characteristic.setValue(new byte[]{0});
                }
                break;
            }
            case TURN_ON_DONT_DISTURB:
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.LINK_LOSS);
                if (characteristic != null) {
                    isDontDisturbMode = true;
                    characteristic.setValue(new byte[]{(byte) 0xa0});
                }
                break;
            case TURN_OFF_DONT_DISTURB:
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.LINK_LOSS);
                if (characteristic != null) {
                    isDontDisturbMode = false;
                    characteristic.setValue(new byte[]{(byte) 0xa1});
                }
                break;

        }
        if (characteristic != null) {
            writeCharacteristic(characteristic);
        }
    }

    private void readRemoteRSSIHandler() {
        readRSSIHandler = new Handler(this.getMainLooper());
        readRSSIHandler.postDelayed(readRSSIRunnable = new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readRemoteRssi();
            }
        }, RSSI_READ_DELAY);
    }

    private void stopReadRemoteRSSIHandler() {
        if (readRSSIHandler != null) {
            readRSSIHandler.removeCallbacks(readRSSIRunnable);
            readRSSIHandler = null;
            readRSSIRunnable = null;
        }
    }

    private void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    private void writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private void setCharacteristicNotification(final BluetoothGattCharacteristic characteristic, final boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
