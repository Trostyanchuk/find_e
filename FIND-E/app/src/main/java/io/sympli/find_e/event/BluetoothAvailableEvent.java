package io.sympli.find_e.event;

public class BluetoothAvailableEvent {

    private final boolean isAvailable;

    public BluetoothAvailableEvent(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
