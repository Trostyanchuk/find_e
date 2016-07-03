package io.sympli.find_e;

import android.app.Application;
import android.content.Context;

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

    }

    public static ApplicationComponent getComponent() {
        return instance.component;
    }

    public static Context getInstance() {
        return instance;
    }
}
