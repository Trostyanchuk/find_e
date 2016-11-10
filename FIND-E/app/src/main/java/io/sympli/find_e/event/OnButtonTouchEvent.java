package io.sympli.find_e.event;

public class OnButtonTouchEvent {

    private boolean isBeeping;

    public OnButtonTouchEvent(boolean isBeeping) {
        this.isBeeping = isBeeping;
    }

    public boolean isBeeping() {
        return isBeeping;
    }
}
