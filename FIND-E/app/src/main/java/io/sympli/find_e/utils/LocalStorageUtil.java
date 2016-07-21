package io.sympli.find_e.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import io.sympli.find_e.ApplicationController;

public final class LocalStorageUtil {

    private static final String FIRST_LAUNCH = "first_launch";
    private static final String LAST_POSITION_LAT = "last_position_lat";
    private static final String LAST_POSITION_LON = "last_position_lon";

    public static boolean isFirstLaunch() {
        return !getPreferences().contains(FIRST_LAUNCH);
    }

    public static void saveFirstLaunch() {
        getEditor().putBoolean(FIRST_LAUNCH, true).apply();
    }

    public static void saveLastPosition(LatLng lastPosition) {
        getEditor().putString(LAST_POSITION_LAT, String.valueOf(lastPosition.latitude)).apply();
        getEditor().putString(LAST_POSITION_LON, String.valueOf(lastPosition.longitude)).apply();
    }

    public static LatLng getLastPosition() {
        return new LatLng(Double.parseDouble(getPreferences().getString(LAST_POSITION_LAT, "50.440055")),
                Double.parseDouble(getPreferences().getString(LAST_POSITION_LON, "30.548027")));
    }

    private static SharedPreferences.Editor getEditor() {
        return getPreferences().edit();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
    }
}
