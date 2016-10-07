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
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import io.sympli.find_e.event.OnTagReadyEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.event.SendDataToTagEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BleState;
import io.sympli.find_e.services.impl.BluetoothLeService;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.ui.widget.parallax.SensorAnalyzer;
import io.sympli.find_e.utils.BLEUtil;
import uk.co.alt236.bluetoothlelib.resolvers.GattAttributeResolver;
import uk.co.alt236.bluetoothlelib.util.ByteUtils;

public class BaseActivity extends AppCompatActivity implements SensorEventListener {

    private FloatArrayEvaluator evaluator = new FloatArrayEvaluator(2);
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorAnalyzer sensorAnalyzer;

    @Inject
    IBroadcast broadcast;

    private String mDeviceAddress;
    private boolean mConnected;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private List<List<Map<String, String>>> mGattCharacteristicData;
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
//                        if (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
//                        }
                        final Map<String, String> currentCharaData = new HashMap<>();
                        uuid = gattCharacteristic.getUuid().toString();
                        currentCharaData.put(BluetoothLeService.LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                        currentCharaData.put(BluetoothLeService.LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                    }

                    mGattCharacteristics.add(charas);
                    mGattCharacteristicData.add(gattCharacteristicGroupData);
                }
                broadcast.postEvent(new OnTagReadyEvent());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);
                readDataAndNotify(uuid, dataArr);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (mBluetoothLeService != null && !TextUtils.isEmpty(mDeviceAddress)) {
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
            mDeviceAddress = event.getDevice().getAddress();
            final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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

//        for (List<BluetoothGattCharacteristic> list : mGattCharacteristics) {
//            for (BluetoothGattCharacteristic charact : list) {
//                if (charact.getUuid().equals(UUID.fromString(BleState.IMMEDIATE_ALERT))) {
//                    characteristic = charact;
//                }
//            }
//        }
//        if (characteristic != null) {
//            characteristic.setValue(new byte[]{1});
//            mBluetoothLeService.writeCharacteristic(characteristic);
//        }
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(false, permissions));
        } else {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(true, permissions));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IBluetoothManager.REQUEST_ENABLE_BT: {
                broadcast.postEvent(new BluetoothAvailableEvent(resultCode == RESULT_OK));
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
        return intentFilter;
    }
}
