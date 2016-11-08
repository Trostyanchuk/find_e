package io.sympli.find_e.event;

public class GattUpdateEvent {

    private String updateId;

    private int rssi = 0;

    public GattUpdateEvent(String id) {
        this.updateId = id;
    }

    public String getUpdateId() {
        return updateId;
    }

    public int getRssi() {
        return rssi;
    }

    public GattUpdateEvent setRssi(int rssi) {
        this.rssi = rssi;
        return this;
    }
}
