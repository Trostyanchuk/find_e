package io.sympli.find_e;

import dagger.Component;
import io.sympli.find_e.receiver.BluetoothStateReceiver;
import io.sympli.find_e.ui.fragment.MainUsageFragment;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.SettingsFragment;
import io.sympli.find_e.ui.fragment.TipsFragment;
import io.sympli.find_e.ui.main.MainActivity;
import io.sympli.find_e.ui.main.StartActivity;
import io.sympli.find_e.ui.widget.ViewPermissions;

@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);

    void inject(StartActivity startActivity);

    void inject(ViewPermissions viewPermissions);

    void inject(ApplicationController controller);

    void inject(PermissionsFragment fragment);

    void inject(MapFragment fragment);

    void inject(MainUsageFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(TipsFragment fragment);

    void inject(BluetoothStateReceiver receiver);
}
