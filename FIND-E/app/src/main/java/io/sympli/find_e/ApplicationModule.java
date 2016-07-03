package io.sympli.find_e;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import io.sympli.find_e.services.IBleService;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BleServiceImpl;
import io.sympli.find_e.services.impl.EventBusBroadcastImpl;

@Module
public class ApplicationModule {

    private Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    public IBleService provideBleService() {
        return new BleServiceImpl();
    }

    @Provides
    public IBroadcast provideBroadcastService() {
        return new EventBusBroadcastImpl(EventBus.getDefault());
    }
}
