package io.sympli.find_e.utils;

import android.location.Location;

public interface MyLocationListener {
    void onLocationChanged(Location location);

    void onSettingsAlertCancelled();
}
