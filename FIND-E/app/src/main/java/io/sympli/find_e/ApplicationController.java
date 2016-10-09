package io.sympli.find_e;

import android.app.Application;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import com.mapbox.mapboxsdk.MapboxAccountManager;

import io.sympli.find_e.utils.LocalStorageUtil;

public class ApplicationController extends Application {

    private static ApplicationController instance;
    private static int activityCounter = 0;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this)).build();
        component.inject(this);

        LocalStorageUtil.increaseEntranceCount();

        setupMaps();
    }

    private void setupMaps() {
        MapboxAccountManager.start(this, getString(R.string.mapbox_token));
    }

    public static ApplicationComponent getComponent() {
        return instance.component;
    }

    public static Context getInstance() {
        return instance;
    }

    public static void increaseActivityCounter() {
        activityCounter++;
    }

    public static void decreaseActivityCounter() {
        activityCounter--;
    }

    public static boolean isAppInForeground() {
        return activityCounter > 0;
    }

    private void startConnectionService() {
        startService(new Intent(getBaseContext(), BluetoothGattService.class));
    }
}
