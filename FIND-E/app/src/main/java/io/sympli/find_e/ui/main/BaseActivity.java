package io.sympli.find_e.ui.main;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.sympli.find_e.R;
import io.sympli.find_e.event.BleDeviceFoundEvent;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.event.LocationChangedEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.event.SendDataToTagEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BleState;
import io.sympli.find_e.services.impl.BluetoothLeService;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.ui.widget.parallax.SensorAnalyzer;
import io.sympli.find_e.utils.BLEUtil;
import io.sympli.find_e.utils.LocationPermissionUtil;
import io.sympli.find_e.utils.MyLocationListener;
import io.sympli.find_e.utils.NotificationUtils;
import io.sympli.find_e.utils.UIUtil;
import uk.co.alt236.bluetoothlelib.resolvers.GattAttributeResolver;
import uk.co.alt236.bluetoothlelib.util.ByteUtils;

import static io.sympli.find_e.utils.LocationPermissionUtil.LOCATION_INTENT_CODE;

public abstract class BaseActivity extends AbstractCounterActivity implements SensorEventListener,
        MyLocationListener {

    private FloatArrayEvaluator evaluator = new FloatArrayEvaluator(2);
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorAnalyzer sensorAnalyzer;

    @Inject
    IBroadcast broadcast;
    @Inject
    IBluetoothManager service;

    private String mDeviceAddress;
    private boolean mConnected;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private List<List<Map<String, String>>> mGattCharacteristicData;
    private int lastRSSI;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("TAG", "Unable to initialize Bluetooth");
                //TODO
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d("TAG", "Service disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("TAG", "Service discovered");
                List<BluetoothGattService> deviceList = mBluetoothLeService.getSupportedGattServices();
                String uuid;
                final String unknownServiceString = getResources().getString(R.string.unknown_service);
                final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
                final List<Map<String, String>> gattServiceData = new ArrayList<>();
                mGattCharacteristicData = new ArrayList<>();
                mGattCharacteristics = new ArrayList<>();

                for (final BluetoothGattService gattService : deviceList) {
                    final Map<String, String> currentServiceData = new HashMap<>();
                    uuid = gattService.getUuid().toString();
                    currentServiceData.put(BluetoothLeService.LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
                    currentServiceData.put(BluetoothLeService.LIST_UUID, uuid);
                    gattServiceData.add(currentServiceData);

                    final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
                    final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

                    // Loops through available Characteristics.
                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        charas.add(gattCharacteristic);
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                        final Map<String, String> currentCharaData = new HashMap<>();
                        uuid = gattCharacteristic.getUuid().toString();
                        currentCharaData.put(BluetoothLeService.LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                        currentCharaData.put(BluetoothLeService.LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                    }

                    mGattCharacteristics.add(charas);
                    mGattCharacteristicData.add(gattCharacteristicGroupData);
                }
                onTagReady();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);
                readDataAndNotify(uuid, dataArr);
            } else if (BluetoothLeService.ACTION_RSSI.equals(action)) {
                final int rssi = intent.getIntExtra(BluetoothLeService.EXTRA_RSSI, 0);
                lastRSSI = rssi;
                onRssiRead(rssi);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationUtils.removeAllNotifications(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorAnalyzer = new SensorAnalyzer(evaluator);
        final int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        sensorAnalyzer.remapAxis(rotation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register(this);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null && !TextUtils.isEmpty(mDeviceAddress)
                && mBluetoothLeService.isDisconnected()) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d("TAG", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        broadcast.unregister(this);
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] newOffsetItems = sensorAnalyzer.normalizeAxisDueToEvent(sensorEvent);
        if (newOffsetItems != null && newOffsetItems[0] < 2 && newOffsetItems[1] < 2) {
            broadcast.postEvent(new io.sympli.find_e.event.SensorEvent(newOffsetItems[0], newOffsetItems[1]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Subscribe
    public void onBleDeviceFoundEvent(BleDeviceFoundEvent event) {
        broadcast.removeStickyEvent(BleDeviceFoundEvent.class);
        Log.d("TAG", "finding status " + event.isFound());
        if (event.isFound()) {
            onDeviceDiscovered();
            mDeviceAddress = event.getDevice().getAddress();
            final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        } else {
            onUnsuccessfulSearch();
        }
    }

    @Subscribe
    public void onSendDataToTagEvent(SendDataToTagEvent event) {
        broadcast.removeStickyEvent(SendDataToTagEvent.class);
        BluetoothGattCharacteristic characteristic = null;
        switch (event.getTagAction()) {
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
            case LINK_LOSS:
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.LINK_LOSS);
                break;

        }
        writeDataByEvent(characteristic);
    }

    public void sendDataToTag(TagAction tagAction) {
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
            case LINK_LOSS:
                characteristic = BLEUtil.getBluetoothGattCharacteristicByUUID(mGattCharacteristics, BleState.LINK_LOSS);
                break;

        }
        writeDataByEvent(characteristic);
    }

    @Subscribe
    public void onBluetoothAvailableEvent(BluetoothAvailableEvent event) {
        broadcast.removeStickyEvent(BluetoothAvailableEvent.class);
        UIUtil.hideSnackBar();
        if (!event.isAvailable()) {
            onBleUnavailable();
        } else {
            onBleBecomeAvailable();
        }
    }

    @Subscribe
    public void onEventMainThread(LocationChangedEvent event) {
        if (!isGPSEnabled()) {
            onGPSLocationUnavailable();
        } else {
            onLocationAvailable();
        }
    }

    private void writeDataByEvent(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            mBluetoothLeService.writeCharacteristic(characteristic);
        }
    }

    private void readDataAndNotify(String uuid, byte[] dataArr) {
        String data = ByteUtils.byteArrayToHexString(dataArr);
        switch (uuid) {
            case BleState.BATTERY_SERVICE:
                break;
            case BleState.POWER_LEVEL:
                break;
        }


    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onSettingsAlertCancelled() {
        onGPSLocationUnavailable();
    }

    public void checkGPSEnabled() {
        if (!isGPSEnabled()) {
            LocationPermissionUtil.showSettingsAlert(this, this);
        } else {
            startConnecting();
        }
    }

    public void startConnecting() {
        if (service.isBluetoothEnabled()) {
            service.searchKey();
        } else {
            service.enableBluetooth(this);
        }
    }

    public boolean isGPSEnabled() {
        return ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            broadcast.postEvent(new PermissionsGrantResultEvent(false, permissions));
        } else {
            broadcast.postEvent(new PermissionsGrantResultEvent(true, permissions));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IBluetoothManager.REQUEST_ENABLE_BT: {
                broadcast.postEvent(new BluetoothAvailableEvent(resultCode == RESULT_OK));
                if (resultCode == RESULT_OK) {
                    service.searchKey();
                } else {
                    onBleUnavailable();
                }
                break;
            }
            case LOCATION_INTENT_CODE: {
                if (isGPSEnabled()) {
                    startConnecting();
                } else {
                    onGPSLocationUnavailable();
                }
                break;
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_RSSI);
        return intentFilter;
    }

    public abstract void onGPSLocationUnavailable();

    public abstract void onLocationAvailable();

    public abstract void onBleUnavailable();

    public abstract void onBleBecomeAvailable();

    public abstract void onUnsuccessfulSearch();

    public abstract void onDeviceDiscovered();

    public abstract void onTagReady();

    public abstract void onRssiRead(int rssi);

    public int getLastRSSI() {
        return lastRSSI;
    }
}
