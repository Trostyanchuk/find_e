package io.sympli.find_e;

import android.app.Application;
import android.content.Context;

import com.mapbox.mapboxsdk.MapboxAccountManager;

public class ApplicationController extends Application {

    private static ApplicationController instance;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this)).build();
        component.inject(this);

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
}
