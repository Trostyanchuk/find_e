package io.sympli.find_e.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.sympli.find_e.ApplicationController;

public final class LocalStorageUtil {

    private static final String FIRST_LAUNCH = "first_launch";
    private static final String LAST_POSITION_LAT = "last_position_lat";
    private static final String LAST_POSITION_LON = "last_position_lon";
    private static final String LAST_POSITION_TIME = "last_position_time";
    private static final String ENTRANCE_COUNT = "entrance_count";
    private static final String SILENT_AREA = "silent_area";
    private static final String LAST_DEVICE_ID = "last_device_id";

    public static boolean isFirstLaunch() {
        return !getPreferences().contains(FIRST_LAUNCH);
    }

    public static void saveFirstLaunch() {
        getEditor().putBoolean(FIRST_LAUNCH, true).apply();
    }

    public static void saveLastPosition(double lat, double lon) {
        getEditor().putString(LAST_POSITION_LAT, String.valueOf(lat)).apply();
        getEditor().putString(LAST_POSITION_LON, String.valueOf(lon)).apply();
        getEditor().putLong(LAST_POSITION_TIME, new Date().getTime()).apply();
    }

    public static LatLng getLastPosition() {
        return new LatLng(Double.parseDouble(getPreferences().getString(LAST_POSITION_LAT, "50.440055")),
                Double.parseDouble(getPreferences().getString(LAST_POSITION_LON, "30.548027")));
    }

    public static Date getLastPositionTime() {
        Date lastDate = new Date();
        lastDate.setTime(getPreferences().getLong(LAST_POSITION_TIME, new Date().getTime()));
        return lastDate;
    }

    public static List<String> getSilentWifiSSIDs() {
        return new ArrayList<>();
    }

    public static int getEntranceCount() {
        return getPreferences().getInt(ENTRANCE_COUNT, -1);
    }

    public static void increaseEntranceCount() {
        int entranceCount = getEntranceCount() + 1;
        getEditor().putInt(ENTRANCE_COUNT, entranceCount).apply();
    }

    public static boolean isSilentArea() {
        return getPreferences().getBoolean(SILENT_AREA, false);
    }

    public static void setSilentArea(boolean silentArea) {
        getEditor().putBoolean(SILENT_AREA, silentArea).apply();
    }

    public static void cleanLastDeviceId() {
        getEditor().remove(LAST_DEVICE_ID).commit();
    }

    public static String getLastDeviceId() {
        return getPreferences().getString(LAST_DEVICE_ID, null);
    }

    public static void setLastDeviceId(String lastDeviceId) {
        getEditor().putString(LAST_DEVICE_ID, lastDeviceId).apply();
    }

    private static SharedPreferences.Editor getEditor() {
        return getPreferences().edit();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
    }
}
