package io.sympli.find_e.ui.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityMainBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.SendDataToTagEvent;
import io.sympli.find_e.event.SensorEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.ui.fragment.SettingsFragment;
import io.sympli.find_e.ui.fragment.TipsFragment;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.DialogMessageWidget;
import io.sympli.find_e.ui.widget.states.ButtonClickListener;
import io.sympli.find_e.ui.widget.states.ConnectionState;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.SoundUtil;
import io.sympli.find_e.utils.UIUtil;
import jp.wasabeef.blurry.Blurry;

public class MainActivity extends BaseActivity implements ButtonClickListener {

    private ActivityMainBinding binding;
    private Fragment childFragment;

    @Inject
    IBroadcast broadcast;

    private ConnectionState connectionState = ConnectionState.HAPPY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ApplicationController.getComponent().inject(this);

        setupMenu();

        binding.messageView.setOnProtipClickListener(new DialogMessageWidget.OnProtipClickListener() {
            @Override
            public void openTips() {
                hideMessage();
                replaceShadowingFragment(Screen.SETTINGS);
            }

            @Override
            public void openBleSettings() {
                hideMessage();
                //TODO open ble
            }
        });
        binding.messageView.setVisibility(View.INVISIBLE);
        binding.robotIvState.switchImageByState(ConnectionState.HAPPY);
        showMessageForEntranceCount();
        UIUtil.runTaskWithDelay(2664, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                connectionState = ConnectionState.CONNECTED;
                setConnectionState(connectionState);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.btnView.animateSelf();
        setConnectionState(connectionState);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.btnView.stopAnimation();
    }

    @Subscribe
    public void onSensorEvent(SensorEvent event) {
        broadcast.removeStickyEvent(SensorEvent.class);
        binding.btnView.setParallaxOffset(event.getxOffset(), event.getyOffset());
    }

    @Override
    public void onGPSLocationUnavailable() {

    }

    @Override
    public void onLocationAvailable() {

    }

    @Override
    public void onBleUnavailable() {
        showMessageForBleDisconnected();
    }

    @Override
    public void onBleBecomeAvailable() {

    }

    @Override
    public void onUnsuccessfulSearch() {

    }

    @Override
    public void onDeviceDiscovered() {

    }

    @Override
    public void onTagReady() {

    }

    @Override
    public void onRssiRead(int rssi) {

    }


    @Override
    public void onStop() {
        super.onStop();
    }

    private void setupMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_main);
        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blurView();
                binding.settings.setEnabled(false);
                replaceShadowingFragment(Screen.SETTINGS);
            }
        });
    }

    private void blurView() {
        Blurry.with(MainActivity.this)
                .radius(25)
                .sampling(1)
                .async()
                .capture(binding.root)
                .into(binding.blurBackground);
    }

    @Subscribe
    public void onChangeScreenEvent(ChangeScreenEvent event) {
        replaceShadowingFragment(event.getNewState());
    }

    @Subscribe
    public void onAnimationFinishedEvent(AnimationFinishedEvent event) {
        switch (event.getScreen()) {
            case SETTINGS: {
                binding.settings.setEnabled(true);
                break;
            }
            case MAIN_USAGE: {
                binding.settings.setVisibility(View.VISIBLE);
                break;
            }
        }
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
            case TIPS_LOCATING:
                fragment = TipsFragment.getInstance(TipsFragment.TipsArea.LOCATE_PHONE);
                appearanceAnimation = R.anim.slide_in_right;
                disappearanceAnimation = R.anim.slide_out_left;
                break;
            case TIPS_BATTERY:
                fragment = TipsFragment.getInstance(TipsFragment.TipsArea.CHANGE_BATTERY);
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
        binding.blurBackground.setVisibility(View.VISIBLE);
        binding.blurBackground.startAnimation(fadeInAnim);

        Animation partlyTransparent = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.partlyTransparentBackground.setVisibility(View.VISIBLE);
        binding.partlyTransparentBackground.startAnimation(partlyTransparent);
    }

    private void closeBlur() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.blurBackground.setVisibility(View.INVISIBLE);
            }
        });
        binding.blurBackground.startAnimation(animation);

        Animation partlyTransparent = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        partlyTransparent.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.partlyTransparentBackground.setVisibility(View.INVISIBLE);
            }
        });
        binding.partlyTransparentBackground.startAnimation(partlyTransparent);
    }

    @Override
    public void onButtonClick() {
        hideMessage();
        switch (connectionState) {
            case SEARCHING:
                setConnectionState(ConnectionState.CONNECTED);
                broadcast.postEvent(new SendDataToTagEvent(TagAction.IMMEDIATE_ALERT_TURN_OFF));
                break;
            case CONNECTED:
                setConnectionState(ConnectionState.SEARCHING);
                broadcast.postEvent(new SendDataToTagEvent(TagAction.IMMEDIATE_ALERT_TURN_ON));
                break;
            case DISCONNECTED:
                broadcast.postEvent(new ChangeScreenEvent(Screen.MAP, ChangeScreenEvent.ScreenGroup.SHADOWING));
                break;
        }
    }


    private void showMessageForEntranceCount() {
        hideMessage();
        if (LocalStorageUtil.getEntranceCount() < 7) {
            binding.messageView.setUIForEntranceCount(LocalStorageUtil.getEntranceCount());
            binding.messageView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.dialog_fade_in);
            binding.messageView.startAnimation(animation);
        }
    }

    private void showMessageForDisconnected() {
        hideMessage();
        binding.messageView.setUIForDisconnected();
        binding.messageView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.dialog_fade_in);
        binding.messageView.startAnimation(animation);
    }

    private void showMessageForBleDisconnected() {
        hideMessage();
        binding.messageView.setUIForTurnedOffBluetooth();
        binding.messageView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.dialog_fade_in);
        binding.messageView.startAnimation(animation);
    }

    private void hideMessage() {
        if (binding.messageView.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.dialog_fade_out);
            binding.messageView.startAnimation(animation);
            binding.messageView.setVisibility(View.INVISIBLE);
        }
    }

    private void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        binding.btnView.setConnectionState(connectionState);
        binding.btnView.setOnButtonClickListener(this);
        binding.robotIvState.switchImageByState(connectionState);
        binding.btnView.animateSelf();
        if (connectionState == ConnectionState.DISCONNECTED) {
            SoundUtil.playDisconnected(this);
            showMessageForDisconnected();
        }
        if (connectionState == ConnectionState.CONNECTED) {
            SoundUtil.playPhoneLocator(this);
        }
        if (connectionState == ConnectionState.SEARCHING) {

        }
    }
}
