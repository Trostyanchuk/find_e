package io.sympli.find_e;

import dagger.Component;
import io.sympli.find_e.ui.fragment.MainUsageFragment;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.SettingsFragment;
import io.sympli.find_e.ui.fragment.SetupFragment;
import io.sympli.find_e.ui.fragment.SplashFragment;
import io.sympli.find_e.ui.fragment.TipsFragment;
import io.sympli.find_e.ui.main.MainActivity;

@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);

    void inject(ApplicationController controller);

    void inject(SplashFragment fragment);

    void inject(PermissionsFragment fragment);

    void inject(SetupFragment fragment);

    void inject(MapFragment fragment);

    void inject(MainUsageFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(TipsFragment fragment);
}
