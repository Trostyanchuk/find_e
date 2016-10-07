package io.sympli.find_e.utils;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.sympli.find_e.services.impl.BleState;

public final class BLEUtil {

    private static Map<String, BluetoothGattCharacteristic> cachedCharacteristics = new HashMap<>();

    public static BluetoothGattCharacteristic getBluetoothGattCharacteristicByUUID(
            List<List<BluetoothGattCharacteristic>> characteristics, String required) {

        BluetoothGattCharacteristic characteristicToReturn = cachedCharacteristics.get(required);
        if (characteristicToReturn != null) {
            return characteristicToReturn;
        }
        for (List<BluetoothGattCharacteristic> list : characteristics) {
            for (BluetoothGattCharacteristic characteristic : list) {
                if (characteristic.getUuid().equals(UUID.fromString(BleState.IMMEDIATE_ALERT))) {
                    characteristicToReturn = characteristic;
                }
            }
        }

        if (characteristicToReturn != null) {
            cachedCharacteristics.put(required, characteristicToReturn);
        }
        return characteristicToReturn;
    }
}
