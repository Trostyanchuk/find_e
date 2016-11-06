package io.sympli.find_e.services.impl;


public interface BleState {

    String SINGLE_TAP = "00001c0f-d102-11e1-9b23-000efb0000a7";

    //R
    String DEVICE_NAME = "00002A00-0000-1000-8000-00805F9B34FB";
    //RW (0 - dont beep, 1 - beep   do not disturb mode.)
    String LINK_LOSS = "00002A06-0000-1000-8000-00805F9B34FB";
    //W (2 - turn on, 0 - turn off)
    String IMMEDIATE_ALERT = "00002A06-0000-1000-8000-00805F9B34FB";
    //R
    String POWER_LEVEL = "00002A07-0000-1000-8000-00805F9B34FB";
    //RN ()
    String BATTERY_SERVICE = "00002A19-0000-1000-8000-00805F9B34FB";
    //TODO camera
}
