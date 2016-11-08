package io.sympli.find_e.ui.main;

import android.bluetooth.BluetoothAdapter;
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

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import io.sympli.find_e.event.BleDeviceFoundEvent;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.event.GattUpdateEvent;
import io.sympli.find_e.event.LocationChangedEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BleServiceConstant;
import io.sympli.find_e.services.impl.BleState;
import io.sympli.find_e.services.impl.BluetoothLeService;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.ui.widget.parallax.SensorAnalyzer;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.LocationPermissionUtil;
import io.sympli.find_e.utils.MyLocationListener;
import io.sympli.find_e.utils.NotificationUtils;
import io.sympli.find_e.utils.UIUtil;
import uk.co.alt236.bluetoothlelib.util.ByteUtils;

import static io.sympli.find_e.services.impl.BleServiceConstant.REQUEST_ENABLE_BT;
import static io.sympli.find_e.services.impl.BleState.SINGLE_TAP;
import static io.sympli.find_e.utils.LocationPermissionUtil.LOCATION_INTENT_CODE;

public abstract class BaseActivity extends AbstractCounterActivity implements SensorEventListener,
        MyLocationListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private FloatArrayEvaluator evaluator = new FloatArrayEvaluator(2);
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorAnalyzer sensorAnalyzer;

    @Inject
    IBroadcast broadcast;

    private LocationServices locationServices;
    private LocationManager locationManager;
    private LatLng myLastLocation;

    private String mDeviceAddress;
    private boolean mConnected;
    private BluetoothLeService mBluetoothLeService;
    private int lastRSSI;
    private int powerLevel = 100;  //default power level
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(LocalStorageUtil.getLastDeviceId());
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationServices = LocationServices.getLocationServices(this);
        locationServices.toggleGPS(true);
        locationServices.addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    myLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    LocalStorageUtil.saveLastPosition(location.getLatitude(), location.getLongitude());
                }
            }
        });

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register(this);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (mBluetoothLeService != null && !TextUtils.isEmpty(LocalStorageUtil.getLastDeviceId())
                && mBluetoothLeService.isDisconnected()) {
            final boolean result = mBluetoothLeService.connect(LocalStorageUtil.getLastDeviceId());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        broadcast.unregister(this);
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
        if (newOffsetItems != null) {
//            broadcast.postEvent(new io.sympli.find_e.event.SensorEvent(newOffsetItems[0], newOffsetItems[1]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Subscribe
    public void onBleDeviceFoundEvent(BleDeviceFoundEvent event) {
        broadcast.removeStickyEvent(BleDeviceFoundEvent.class);
        Log.d(TAG, "onBleDeviceFoundEvent: found " + event.isFound());
        if (event.isFound()) {
            onDeviceDiscovered();
            mDeviceAddress = event.getDevice().getAddress();
            LocalStorageUtil.setLastDeviceId(mDeviceAddress);
            mBluetoothLeService.connect(mDeviceAddress);
        } else {
            onUnsuccessfulSearch();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGattUpdateEvent(GattUpdateEvent event) {
        Log.d(TAG, "GattUpdateEvent " + event.getUpdateId());
        broadcast.removeStickyEvent(GattUpdateEvent.class);
        if (event.getUpdateId().equals(BleServiceConstant.EXTRA_RSSI)) {
            lastRSSI = event.getRssi();
            onRssiRead();
        }
        switch (event.getUpdateId()) {
            case BleServiceConstant.ACTION_GATT_CONNECTED:
                mConnected = true;
                break;
            case BleServiceConstant.ACTION_GATT_DISCONNECTED:
                mConnected = false;
                onDisconnected();
                break;
            case BleServiceConstant.ACTION_GATT_SERVICES_DISCOVERED:
                //TODO read characteristics in service
                onTagReady();
                break;
            case BleServiceConstant.ACTION_DATA_AVAILABLE:
//                final String uuid = intent.getStringExtra(BleServiceConstant.EXTRA_UUID_CHAR);
//                final byte[] dataArr = intent.getByteArrayExtra(BleServiceConstant.EXTRA_DATA_RAW);
//                readDataAndNotify(uuid, dataArr);
                //TODO
//                onTagReady();
                break;
            case BleServiceConstant.ACTION_RSSI:
                lastRSSI = event.getRssi();
                onRssiRead();
                break;
        }
    }

    public void sendDataToTag(TagAction tagAction) {
        //TODO send data directly
        mBluetoothLeService.sendCharacteristicUpdateToDevice(tagAction);
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

    boolean deviceIsBeeping = false;

    private void readDataAndNotify(String uuid, byte[] dataArr) {
        String data = ByteUtils.byteArrayToHexString(dataArr);
        Log.d(TAG, "DATA " + data + " uuid " + uuid);
        switch (uuid) {
            case BleState.POWER_LEVEL:
                int level = dataArr[0];
                if (level > 0) {
                    powerLevel = level;
                }
                break;
            case SINGLE_TAP: {
                deviceIsBeeping = !deviceIsBeeping;
                playMobileSound(deviceIsBeeping);
                break;
            }
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
        if (mBluetoothLeService.isBluetoothEnabled()) {
            mBluetoothLeService.discoverDevices();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
            case REQUEST_ENABLE_BT: {
                broadcast.postEvent(new BluetoothAvailableEvent(resultCode == RESULT_OK));
                if (resultCode == RESULT_OK) {
                    mBluetoothLeService.discoverDevices();
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
        intentFilter.addAction(BleServiceConstant.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleServiceConstant.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleServiceConstant.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleServiceConstant.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleServiceConstant.ACTION_RSSI);
        return intentFilter;
    }

    public abstract void onGPSLocationUnavailable();

    public abstract void onLocationAvailable();

    public abstract void onBleUnavailable();

    public abstract void onBleBecomeAvailable();

    public abstract void onUnsuccessfulSearch();

    public abstract void onDeviceDiscovered();

    public abstract void onTagReady();

    public abstract void onRssiRead();

    public abstract void onDisconnected();

    public abstract void playMobileSound(boolean turnOn);

    public int getLastRSSI() {
        return lastRSSI;
    }

    public boolean isDontDisturbMode() {
        return mBluetoothLeService.isDontDisturbMode();
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getConnectionState() {
        if (mBluetoothLeService != null) {
            return mBluetoothLeService.getConnectionState();
        }
        return BleServiceConstant.STATE_CONNECTED;
    }
}
