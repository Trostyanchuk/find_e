package io.sympli.find_e.ui.main;

import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityMainBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.SensorEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.dialog.DialogClickListener;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.ui.fragment.MapFragment;
import io.sympli.find_e.ui.fragment.OnDontDisturbOptionsListener;
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

public class MainActivity extends BaseActivity implements ButtonClickListener, OnDontDisturbOptionsListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private Fragment childFragment;

    @Inject
    IBroadcast broadcast;
    private InfoPopup infoPopup;
    private DialogInfo dialogInfo;
    private DialogClickListener listener = new DialogClickListener() {
        @Override
        public void onPositiveBtnClickListener() {
            startConnecting();
        }

        @Override
        public void onNegativeBtnClickListener() {
            UIUtil.showSnackBar(binding.getRoot(), getString(R.string.unable_to_continue_general),
                    Snackbar.LENGTH_INDEFINITE, R.string.continue_close, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
        }
    };

    private ConnectionState connectionState = ConnectionState.HAPPY;
    private String excellentLabel;
    private String highLabel;
    private String mediumLabel;
    private String lowLabel;

    private String tapToBeepLabel;
    private String tapToStopLabel;
    private String tapToShowLastLocLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ApplicationController.getComponent().inject(this);
        ApplicationController.setLastInstance(Instance.MAIN);

        excellentLabel = getString(R.string.excellent);
        highLabel = getString(R.string.high);
        mediumLabel = getString(R.string.medium);
        lowLabel = getString(R.string.low);

        tapToBeepLabel = getString(R.string.tap_to_beep);
        tapToStopLabel = getString(R.string.tap_to_stop);
        tapToShowLastLocLabel = getString(R.string.tap_to_show_last_loc);

        setupMenu();

        infoPopup = new InfoPopup(this, listener);
        dialogInfo = new DialogInfo()
                .setBody(getString(R.string.unable_to_find))
                .setBtnPositiveText(getString(R.string.retry_recognition_btn))
                .setBtnNegativeText(getString(R.string.cancel_recognition_btn));

        binding.messageView.setOnProtipClickListener(new DialogMessageWidget.OnProtipClickListener() {
            @Override
            public void openTips() {
                hideMessage();
                replaceShadowingFragment(Screen.SETTINGS);
            }

            @Override
            public void openBleSettings() {
                hideMessage();
                startConnecting();
            }
        });
        binding.messageView.setVisibility(View.INVISIBLE);
        binding.robotIvState.switchImageByState(ConnectionState.HAPPY);
        binding.batteryLife.setText(getPowerLevel() + "%");
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
        Log.d(TAG, "onGPSLocationUnavailable");
    }

    @Override
    public void onLocationAvailable() {
        Log.d(TAG, "onLocationAvailable");
    }

    @Override
    public void onBleUnavailable() {
        showMessageForBleDisconnected();
        setConnectionState(ConnectionState.DISCONNECTED);
        Log.d(TAG, "onLocationAvailable");
    }

    @Override
    public void onBleBecomeAvailable() {
        Log.d(TAG, "onBleBecomeAvailable");
    }

    @Override
    public void onUnsuccessfulSearch() {
        Log.d(TAG, "onUnsuccessfulSearch");
        infoPopup.show(dialogInfo);
    }

    @Override
    public void onDeviceDiscovered() {
        Log.d(TAG, "onDeviceDiscovered");
        setConnectionState(ConnectionState.SEARCHING);
    }

    @Override
    public void onTagReady() {
        Log.d(TAG, "onTagReady");
        setConnectionState(ConnectionState.CONNECTED);
    }

    @Override
    public void onRssiRead(int rssi) {
        rssi = -rssi;
        String signal = "";
        if (rssi < 25) {
            signal = excellentLabel;
        } else if (rssi < 50) {
            signal = highLabel;
        } else if (rssi < 75) {
            signal = mediumLabel;
        } else {
            signal = lowLabel;
        }
        binding.signalQuality.setText(signal);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        //TODO get last location
        LocationServices locationServices = LocationServices.getLocationServices(this);
        Location location = locationServices.getLastLocation();
        if (location != null) {
            Log.d(TAG, "Location " + location.getLatitude() + " " + location.getLongitude());
            LocalStorageUtil.saveLastPosition(location.getLatitude(), location.getLongitude());
        }
        service.searchKey();
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    @Override
    public void playMobileSound(boolean turnOn) {
        if (deviceIsBeeping) {
            SoundUtil.resetPlayer();
            deviceIsBeeping = false;
        } else {
            SoundUtil.playPhoneLocator(this);
            deviceIsBeeping = true;
        }
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
        hideMessage();
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

    boolean beeping = false;

    @Override
    public void onButtonClick() {
        hideMessage();
        switch (connectionState) {
            case CONNECTED:
                setConnectionState(ConnectionState.CONNECTED);
                if (beeping) {
                    beeping = false;
                    binding.btnMsg.setText(tapToBeepLabel);
                    sendDataToTag(TagAction.IMMEDIATE_ALERT_TURN_OFF);
                } else {
                    beeping = true;
                    binding.btnMsg.setText(tapToStopLabel);
                    sendDataToTag(TagAction.IMMEDIATE_ALERT_TURN_ON);
                    if (LocalStorageUtil.getEntranceCount() == 0) {
                        showStartMessageForStopBeeping();
                    }
                }
                break;
            case SEARCHING:
                binding.btnMsg.setText(tapToShowLastLocLabel);
                setConnectionState(ConnectionState.SEARCHING);
                broadcast.postEvent(new ChangeScreenEvent(Screen.MAP, ChangeScreenEvent.ScreenGroup.SHADOWING));
                break;
            case DISCONNECTED:
                binding.btnMsg.setText(tapToShowLastLocLabel);
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

    private void showStartMessageForStopBeeping() {
        hideMessage();
        binding.messageView.setUIForStopBeeping();
        binding.messageView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.dialog_fade_in);
        binding.messageView.startAnimation(animation);
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
        Log.d(TAG, "setConnectionState " + connectionState.name());
        this.connectionState = connectionState;
        hideMessage();
        SoundUtil.resetPlayer();
        binding.btnView.setConnectionState(connectionState);
        binding.btnView.setOnButtonClickListener(this);
        binding.robotIvState.switchImageByState(connectionState);
        binding.btnView.animateSelf();
        if (connectionState == ConnectionState.DISCONNECTED) {
            SoundUtil.playDisconnected(this);
            showMessageForDisconnected();
        }
        if (connectionState == ConnectionState.CONNECTED) {

        }
        if (connectionState == ConnectionState.SEARCHING) {

        }
    }

    @Override
    public boolean isDontDisturb() {
        return isDontDisturbMode();
    }

    @Override
    public void turnOnDontDisturb() {
        sendDataToTag(TagAction.TURN_ON_DONT_DISTURB);
    }

    @Override
    public void turnOffDontDisturb() {
        sendDataToTag(TagAction.TURN_OFF_DONT_DISTURB);
    }
}
