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
    public IBroadcast provideBroadcastService() {
        return new EventBusBroadcastImpl(EventBus.getDefault());
    }

    @Provides
    public IBluetoothManager provideBleService(IBroadcast broadcast) {
        return new BleManagerAPI18Impl(context, broadcast);
    }
}
