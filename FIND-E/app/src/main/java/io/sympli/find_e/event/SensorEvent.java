package io.sympli.find_e.event;

public class SensorEvent {
    private final float xOffset, yOffset;

    public SensorEvent(float xOffset, float yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public float getxOffset() {
        return xOffset;
    }

    public float getyOffset() {
        return yOffset;
    }
}
