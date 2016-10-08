package io.sympli.find_e.event;

public class RSSIUpdateEvent {

    private int rssi;

    public RSSIUpdateEvent(int rssi) {
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }
}
