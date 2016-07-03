package io.sympli.find_e.ui.main;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationComponent;
import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityMainBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.ui.fragment.SplashFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bindingObject;

    @Inject
    IBroadcast broadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingObject = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ApplicationController.getComponent().inject(this);

        setSupportActionBar(bindingObject.toolbar);

        replaceFragment(Screen.SPLASH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        broadcast.unregister(this);
    }

    @Subscribe
    public void onChangeScreenEvent(ChangeScreenEvent event) {
        replaceFragment(event.getNewState());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            broadcast.postEvent(new PermissionsGrantResultEvent(false, permissions));
        } else {
            broadcast.postEvent(new PermissionsGrantResultEvent(true, permissions));
        }
    }

    private void replaceFragment(Screen screen) {
        Fragment fragment = null;
        switch (screen) {
            case SPLASH:
                fragment = new SplashFragment();
                break;
            case PERMISSIONS:
                fragment = new PermissionsFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.main_container, fragment)
                .commit();
    }
}
