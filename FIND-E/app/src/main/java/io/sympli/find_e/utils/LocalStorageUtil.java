package io.sympli.find_e.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.sympli.find_e.ApplicationController;

public final class LocalStorageUtil {

    private static final String FIRST_LAUNCH = "first_launch";


    public boolean isFirstLaunch() {
        return getPreferences().contains(FIRST_LAUNCH);
    }

    public void saveFirstLaunch() {
        getEditor().putBoolean(FIRST_LAUNCH, true).apply();
    }


    private static SharedPreferences.Editor getEditor() {
        return getPreferences().edit();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
    }
}
