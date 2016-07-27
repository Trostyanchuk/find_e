package io.sympli.find_e.ui.main;

import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.greenrobot.eventbus.Subscribe;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityMainBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.ui.fragment.ConnectedFragment;
import io.sympli.find_e.ui.fragment.ConnectionFragment;
import io.sympli.find_e.ui.fragment.MainUsageFragment;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.ui.fragment.SettingsFragment;
import io.sympli.find_e.ui.fragment.SetupFragment;
import io.sympli.find_e.ui.fragment.SplashFragment;
import io.sympli.find_e.ui.fragment.TipsFragment;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.ui.widget.parallax.SensorAnalyzer;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.PermissionsUtil;
import jp.wasabeef.blurry.Blurry;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding bindingObject;
    private Fragment childFragment;
    private FloatArrayEvaluator evaluator = new FloatArrayEvaluator(2);
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorAnalyzer sensorAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingObject = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ApplicationController.getComponent().inject(this);

        setupMenu();

        Screen startScreen = LocalStorageUtil.isFirstLaunch() ? Screen.SPLASH :
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !PermissionsUtil.permissionsGranted(this, PermissionsFragment.REQUIRED_PERMISSIONS) ?
                        Screen.PERMISSIONS : Screen.PAIR;

        replaceMainFragment(startScreen);
    }

    private void setupMenu() {
        bindingObject.toolbar.inflateMenu(R.menu.menu_main);
        bindingObject.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blurView();
                bindingObject.settings.setEnabled(false);
                replaceShadowingFragment(Screen.SETTINGS);
            }
        });
    }

    private void blurView() {
        Blurry.with(MainActivity.this)
                .radius(25)
                .sampling(1)
                .async()
                .capture(bindingObject.root)
                .into(bindingObject.blurBackground);
    }

    @Subscribe
    public void onChangeScreenEvent(ChangeScreenEvent event) {
        if (event.getScreenGroup() == ChangeScreenEvent.ScreenGroup.MAIN) {
            replaceMainFragment(event.getNewState());
        } else {
            replaceShadowingFragment(event.getNewState());
        }
    }

    @Subscribe
    public void onAnimationFinishedEvent(AnimationFinishedEvent event) {
        switch (event.getScreen()) {
            case SETTINGS: {
                bindingObject.settings.setEnabled(true);
                break;
            }
            case PERMISSIONS: {
                bindingObject.toolbar.setVisibility(View.VISIBLE);
                break;
            }
            case MAIN_USAGE: {
                bindingObject.settings.setVisibility(View.VISIBLE);
                bindingObject.circlesBg.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void replaceMainFragment(Screen screen) {
        Fragment fragment = null;
        boolean toolbarVisible = true;
        switch (screen) {
            case SPLASH:
                toolbarVisible = false;
                fragment = new SplashFragment();
                break;
            case PERMISSIONS:
                toolbarVisible = false;
                fragment = new PermissionsFragment();
                break;
            case PAIR:
                fragment = new ConnectionFragment();
//                fragment = new PairFragment();
                break;
            case SETUP:
                fragment = new SetupFragment();
                break;
            case CONNECTED:
                fragment = new ConnectedFragment();
                break;
            case MAIN_USAGE:
                fragment = new MainUsageFragment();
                break;
        }
        bindingObject.toolbar.setVisibility(toolbarVisible ? View.VISIBLE : View.INVISIBLE);
        bindingObject.settings.setVisibility(View.INVISIBLE);

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
        boolean showBgBlur = false;
        switch (screen) {
            case MAP:
                if (childFragment instanceof SettingsFragment) {
                    return;
                }
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
                    showBgBlur = true;
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
            case TIPS_CAMERA:
                fragment = TipsFragment.getInstance(TipsFragment.TipsArea.TIPS_CAMERA);
                appearanceAnimation = R.anim.slide_in_right;
                disappearanceAnimation = R.anim.slide_out_left;
                break;
            case NONE:
                removeShadowingFragment();
                return;
        }
        childFragment = fragment;
        if (showBgBlur) {
            openBlurWithAnim();
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(appearanceAnimation, disappearanceAnimation)
                .replace(R.id.shadow_container, fragment)
                .disallowAddToBackStack()
                .commit();
    }

    private void removeShadowingFragment() {
        if (childFragment != null && childFragment instanceof SettingsFragment) {
            closeBlur();
        }

        if (this.childFragment != null
                && getSupportFragmentManager().findFragmentById(R.id.shadow_container) != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .remove(childFragment)
                    .commit();
            childFragment = null;
        }
    }

    private void openBlurWithAnim() {
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        bindingObject.blurBackground.setVisibility(View.VISIBLE);
        bindingObject.blurBackground.startAnimation(fadeInAnim);

        Animation partlyTransparent = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        bindingObject.partlyTransparentBackground.setVisibility(View.VISIBLE);
        bindingObject.partlyTransparentBackground.startAnimation(partlyTransparent);
    }

    private void closeBlur() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bindingObject.blurBackground.setVisibility(View.INVISIBLE);
            }
        });
        bindingObject.blurBackground.startAnimation(animation);

        Animation partlyTransparent = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        partlyTransparent.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bindingObject.partlyTransparentBackground.setVisibility(View.INVISIBLE);
            }
        });
        bindingObject.partlyTransparentBackground.startAnimation(partlyTransparent);
    }
}
