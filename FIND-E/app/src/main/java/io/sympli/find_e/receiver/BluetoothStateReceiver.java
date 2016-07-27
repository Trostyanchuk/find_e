package io.sympli.find_e.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.services.IBroadcast;

public class BluetoothStateReceiver extends BroadcastReceiver {

    @Inject
    IBroadcast broadcast;

    public BluetoothStateReceiver() {
        ApplicationController.getComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    broadcast.postEvent(new BluetoothAvailableEvent(false));
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    broadcast.postEvent(new BluetoothAvailableEvent(true));
                }
                break;
            case BluetoothDevice.ACTION_FOUND: {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                break;
            }
        }
    }
}
