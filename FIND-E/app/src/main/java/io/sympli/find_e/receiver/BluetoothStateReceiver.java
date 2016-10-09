package io.sympli.find_e.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.NotificationUtils;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothStateReceiver.class.getSimpleName();

    @Inject
    IBroadcast broadcast;

    public BluetoothStateReceiver() {
        ApplicationController.getComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, action);
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    broadcast.postEvent(new BluetoothAvailableEvent(false));
                    if (!ApplicationController.isAppInForeground()) {
                        if (!LocalStorageUtil.isSilentArea() ||
                                (LocalStorageUtil.isSilentArea() && !ApplicationController.getWifiEnabled())) {
                            NotificationUtils.sendDisconnectedNotification(context);
                        }
                    }
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    broadcast.postEvent(new BluetoothAvailableEvent(true));
                }
                break;
            case BluetoothDevice.ACTION_FOUND: {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                break;
            }
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String mDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                Log.d(TAG, "DEVICE - " + mDeviceName + " " + RSSI);
                break;
        }
    }
}
