package io.sympli.find_e.event;

public class BleDeviceFoundEvent {

    private boolean found;

    public BleDeviceFoundEvent(boolean found) {
        this.found = found;
    }

    public boolean isFound() {
        return found;
    }
}
