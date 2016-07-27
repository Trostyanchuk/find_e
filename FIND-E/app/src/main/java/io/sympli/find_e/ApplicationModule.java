package io.sympli.find_e;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BleManagerAPI18Impl;
import io.sympli.find_e.services.impl.EventBusBroadcastImpl;

@Module
public class ApplicationModule {

    private Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    public IBluetoothManager provideBleService() {
        //TODO return due to API version
        return new BleManagerAPI18Impl(context);
    }

    @Provides
    public IBroadcast provideBroadcastService() {
        return new EventBusBroadcastImpl(EventBus.getDefault());
    }
}
