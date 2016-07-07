package io.sympli.find_e.ui.main;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import eightbitlab.com.blurview.RenderScriptBlur;
import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityMainBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.fragment.MainUsageFragment;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.ui.fragment.SettingsFragment;
import io.sympli.find_e.ui.fragment.SetupFragment;
import io.sympli.find_e.ui.fragment.SplashFragment;
import io.sympli.find_e.ui.fragment.TipsFragment;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.PermissionsUtil;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bindingObject;
    private Fragment childFragment;


    @Inject
    IBroadcast broadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingObject = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ApplicationController.getComponent().inject(this);

        setupMenu();

        Screen startScreen = LocalStorageUtil.isFirstLaunch() ? Screen.SPLASH :
                PermissionsUtil.permissionsGranted(this, PermissionsFragment.REQUIRED_PERMISSIONS) ?
                        Screen.SETUP : Screen.PERMISSIONS;

        replaceMainFragment(startScreen);
    }

    private void setupMenu() {
        bindingObject.toolbar.inflateMenu(R.menu.menu_main);
        bindingObject.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceShadowingFragment(Screen.SETTINGS);
            }
        });
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
        if (event.getScreenGroup() == ChangeScreenEvent.ScreenGroup.MAIN) {
            replaceMainFragment(event.getNewState());
        } else {
            replaceShadowingFragment(event.getNewState());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(false, permissions));
        } else {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(true, permissions));
        }
    }

    private void replaceMainFragment(Screen screen) {
        Fragment fragment = null;
        boolean settingsVisible = true;
        switch (screen) {
            case SPLASH:
                fragment = new SplashFragment();
                break;
            case PERMISSIONS:
                settingsVisible = false;
                fragment = new PermissionsFragment();
                break;
            case SETUP:
                settingsVisible = false;
                fragment = new SetupFragment();
                break;
            case MAIN_USAGE:
                settingsVisible = true;
                fragment = new MainUsageFragment();
        }
        bindingObject.toolbar.setVisibility(fragment instanceof SplashFragment ? View.INVISIBLE : View.VISIBLE);
        bindingObject.settings.setVisibility(settingsVisible ? View.VISIBLE : View.INVISIBLE);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.main_container, fragment)
                .disallowAddToBackStack()
                .commit();
    }

    private void replaceShadowingFragment(Screen screen) {
        Fragment fragment = null;
        int appearanceAnimation = 0;
        int disappearanceAnimation = 0;
        switch (screen) {
            case MAP:
                fragment = new MapFragment();
                appearanceAnimation = R.anim.slide_in_right;
                disappearanceAnimation = R.anim.slide_out_right;
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                if (childFragment instanceof TipsFragment) {
                    appearanceAnimation = R.anim.slide_in_left;
                    disappearanceAnimation = R.anim.slide_out_right;
                } else {
                    appearanceAnimation = R.anim.slide_in_right;
                    disappearanceAnimation = R.anim.slide_out_right;
                }
                break;
            case TIPS_DISTURB:
                fragment = TipsFragment.getInstance(TipsFragment.TipsArea.DO_NOT_DISTURB);
                appearanceAnimation = R.anim.slide_in_right;
                disappearanceAnimation = R.anim.slide_out_left;
                break;
            case TIPS_SILENT:
                fragment = TipsFragment.getInstance(TipsFragment.TipsArea.SILENT_AREA);
                appearanceAnimation = R.anim.slide_in_right;
                disappearanceAnimation = R.anim.slide_out_left;
                break;
            case NONE:
                removeShadowingFragment();
                return;
        }

        childFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(appearanceAnimation, disappearanceAnimation)
                .replace(R.id.shadow_container, fragment)
                .disallowAddToBackStack()
                .commit();
    }

    private void removeShadowingFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                .remove(childFragment)
                .commit();
    }
}
