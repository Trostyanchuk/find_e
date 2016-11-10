package io.sympli.find_e.ui.main;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ActivityStartBinding;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.impl.BluetoothLeService;
import io.sympli.find_e.ui.dialog.DialogClickListener;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.ui.fragment.PermissionsFragment;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.AbstractEasyVideoCallback;
import io.sympli.find_e.ui.widget.ViewPermissions;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.PermissionsUtil;
import io.sympli.find_e.utils.UIUtil;

import static io.sympli.find_e.services.impl.BleServiceConstant.REQUEST_ENABLE_BT;
import static io.sympli.find_e.ui.fragment.Screen.CONNECTING;

public class StartActivity extends BaseActivity {

    public static final long SAVE_STATE_TIMEOUT = 2 * 1000;
    public static final long PAIRED_TIMER = 5000;
    public static final int ANIM_DURATION = 700;

//    @Inject
//    IBluetoothManager service;

    private ActivityStartBinding bindingObject;
    private int lastPosition = 0;
    private boolean videoFinished;
    private CountDownTimer splashCountDownTimer;
    private CountDownTimer pairedTimer;

    private InfoPopup infoPopup;
    private DialogInfo dialogInfo;
    private DialogClickListener listener = new DialogClickListener() {
        @Override
        public void onPositiveBtnClickListener() {
            startConnecting();
        }

        @Override
        public void onNegativeBtnClickListener() {
            UIUtil.showSnackBar(bindingObject.getRoot(), getString(R.string.unable_to_continue_general),
                    Snackbar.LENGTH_INDEFINITE, R.string.continue_close, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
        }
    };

    private View.OnClickListener onContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (lastScreen == Screen.SPLASH) {
                LocalStorageUtil.saveFirstLaunch();
                updateUIState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !PermissionsUtil.permissionsGranted(StartActivity.this, PermissionsFragment.REQUIRED_PERMISSIONS) ?
                        Screen.PERMISSIONS : CONNECTING);
            }
            if (lastScreen == Screen.HOW_TO_USE) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishActivity();
            }
        }
    };

    private ViewPermissions.OnPermissionsRequiredListener viewPermissionsListener = new ViewPermissions.OnPermissionsRequiredListener() {
        @Override
        public void requestPermissions(String... permissions) {
            PermissionsUtil.requestPermissions(StartActivity.this, 0, permissions);
        }
    };

    private Screen lastScreen;
    private AbstractEasyVideoCallback abstractEasyVideoCallback = new AbstractEasyVideoCallback() {
        @Override
        public void onPrepared(EasyVideoPlayer player) {
            Log.d("TAG", "onPrepared ");
            bindingObject.player.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStarted(EasyVideoPlayer player) {
            Log.d("TAG", "onStarted");
        }

        @Override
        public void onCompletion(EasyVideoPlayer player) {
            Log.d("TAG", "onCompleted " + player.getCurrentPosition() + " " + player.getDuration());

            if (player.getCurrentPosition() >= 1800 && player.getCurrentPosition() <= player.getDuration()) {
                if (lastScreen == Screen.SPLASH) {
                    videoFinished = true;
                    continueAnimationForSplash();
                }
                if (lastScreen == Screen.PAIR) {
                    bindingObject.player.reset();
                    bindingObject.player.setInitialPosition(0);

                    String videoResource = "android.resource://" + getPackageName() + "/" + R.raw.iphone_connecting;
                    bindingObject.player.setSource(Uri.parse(videoResource));
                    videoFinished = false;
                    lastPosition = 0;
                }
            }
        }

        @Override
        public void onDestroyed() {
            Log.d("TAG", "onDestroyed");
        }
    };

    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("TAG", "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(LocalStorageUtil.getLastDeviceId());
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        bindingObject = DataBindingUtil.setContentView(this, R.layout.activity_start);
        ApplicationController.getComponent().inject(this);
        ApplicationController.setLastInstance(Instance.START);

        Screen startScreen = LocalStorageUtil.isFirstLaunch() ? Screen.SPLASH :
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !PermissionsUtil.permissionsGranted(this, PermissionsFragment.REQUIRED_PERMISSIONS) ?
                        Screen.PERMISSIONS : CONNECTING;

        bindingObject.viewPermissions.setPermissionsListener(viewPermissionsListener);
        bindingObject.viewPermissions.setOnContinueClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastScreen == Screen.PERMISSIONS) {
                    updateUIState(CONNECTING);
                }
            }
        });

        bindingObject.continueBtn.setOnClickListener(onContinueClickListener);

        final RelativeLayout.LayoutParams textureLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        textureLp.setMargins(0, 0, 0, actionBarHeight * 3);
        bindingObject.playerImgStubParent.setLayoutParams(textureLp);

        infoPopup = new InfoPopup(this, listener);
        dialogInfo = new DialogInfo()
                .setBody(getString(R.string.unable_to_find))
                .setBtnPositiveText(getString(R.string.retry_recognition_btn))
                .setBtnNegativeText(getString(R.string.cancel_recognition_btn));

        bindingObject.player.setCallback(abstractEasyVideoCallback);
        bindingObject.player.setAutoPlay(true);
        bindingObject.player.disableControls();

        updateUIState(startScreen);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void updateUIState(Screen screen) {
        bindingObject.setScreen(screen);
        switch (screen) {
            case SPLASH:
                bindingObject.mainLabel.setVisibility(View.VISIBLE);
                bindingObject.playerImgStubParent.setVisibility(View.VISIBLE);
                startSplashWithTimer();
                break;
            case PERMISSIONS:
                bindingObject.explanationParent.setVisibility(View.INVISIBLE);
                bindingObject.continueBtn.setVisibility(View.INVISIBLE);
                startAnimationForPermission();
                break;
            case CONNECTING:
                bindingObject.viewPermissions.setVisibility(View.GONE);
//                bindingObject.player.setVisibility(View.INVISIBLE);
                bindingObject.continueBtn.setVisibility(View.INVISIBLE);
                bindingObject.playerImgStubParent.setVisibility(View.VISIBLE);
                bindingObject.playerImgStub.setImageResource(R.drawable.connecting);
                startAnimationForPairing();
                break;
            case PAIR:
                bindingObject.explanationParent.setVisibility(View.VISIBLE);
                bindingObject.explanationTitle.setText(getString(R.string.pairing_label));
                bindingObject.explanationText.setText(getString(R.string.pairing_text));
                bindingObject.playerImgStubParent.setVisibility(View.INVISIBLE);
                bindingObject.player.setVisibility(View.VISIBLE);

                bindingObject.player.reset();
                bindingObject.player.setInitialPosition(0);

                String videoResource = "android.resource://" + getPackageName() + "/" + R.raw.iphone_connecting;
                bindingObject.player.setSource(Uri.parse(videoResource));
                videoFinished = false;
                lastPosition = 0;
                break;
            case CONNECTED:
                bindingObject.player.stop();
                bindingObject.player.setVisibility(View.INVISIBLE);
                bindingObject.explanationParent.setVisibility(View.VISIBLE);
                bindingObject.explanationTitle.setText(getString(R.string.connected_label));
                bindingObject.explanationText.setText(getString(R.string.connected_text));
                bindingObject.icReady.setVisibility(View.VISIBLE);
                bindingObject.playerImgStubParent.setVisibility(View.VISIBLE);
                startTagTimer();
                break;
            case HOW_TO_USE:
                bindingObject.icReady.setVisibility(View.INVISIBLE);
                bindingObject.explanationTitle.setText(getString(R.string.how_to_use_label));
                bindingObject.explanationText.setText(getString(R.string.how_to_use_text));
                bindingObject.playerImgStub.setImageResource(R.drawable.staff);
                startAnimationForHowToUse();
                break;
        }

        lastScreen = screen;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("TAG", "On resume " + lastPosition);
        if (lastScreen == Screen.SPLASH || lastScreen == Screen.PAIR) {
            if (lastPosition != 0) {
                if (videoFinished) {
                    lastPosition = bindingObject.player.getDuration();
                }
                bindingObject.player.seekTo(lastPosition);
                bindingObject.player.start();
            } else {
                bindingObject.player.start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("TAG", "On pause " + bindingObject.player.getCurrentPosition());
        lastPosition = bindingObject.player.getCurrentPosition();
        if (lastPosition == bindingObject.player.getDuration()) {
            videoFinished = true;
        }
        bindingObject.player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    @Override
    public void onGPSLocationUnavailable() {
        UIUtil.showSnackBar(bindingObject.getRoot(), getString(R.string.msg_location_unavailable_alert),
                Snackbar.LENGTH_INDEFINITE, R.string.label_turn_on, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkGPSEnabled();
                    }
                });
    }

    @Override
    public void onLocationAvailable() {
    }

    @Override
    public void onUnsuccessfulSearch() {
        infoPopup.show(dialogInfo);
    }

    @Override
    public void onBleUnavailable() {
        UIUtil.showSnackBar(bindingObject.getRoot(), getString(R.string.msg_bluetooth_unavailable_alert),
                Snackbar.LENGTH_INDEFINITE, R.string.label_turn_on, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
    }

    @Override
    public void onBleBecomeAvailable() {
        updateUIState(CONNECTING);
    }

    @Override
    public void onDeviceDiscovered() {
        updateUIState(Screen.PAIR);
    }

    @Override
    public void onTagReady() {
        updateUIState(Screen.CONNECTED);
    }

    @Override
    public void onRssiRead() {
    }

    @Override
    public void onDisconnected() {
        updateUIState(CONNECTING);
    }

    @Override
    public void playMobileSound(boolean turnOn) {
        //STUB
    }

    private void stopSplashTimer() {
        if (splashCountDownTimer != null) {
            splashCountDownTimer.cancel();
            splashCountDownTimer = null;
        }
    }

    private void hideSplashLogoWithAnim() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bindingObject.mainLabel.setVisibility(View.GONE);
                hideSplashImageWithAnim();
            }
        });
        bindingObject.mainLabel.startAnimation(animation);
    }

    private void hideSplashImageWithAnim() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                String videoResource = "android.resource://" + getPackageName() + "/" + R.raw.iphone_splash;
                bindingObject.player.setSource(Uri.parse(videoResource));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bindingObject.playerImgStubParent.setVisibility(View.GONE);
            }
        });
        bindingObject.playerImgStubParent.startAnimation(animation);
    }

    private void startSplashWithTimer() {
        stopSplashTimer();
        splashCountDownTimer = new CountDownTimer(SAVE_STATE_TIMEOUT, SAVE_STATE_TIMEOUT) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                hideSplashLogoWithAnim();
            }
        }.start();
    }

    private void continueAnimationForSplash() {
        bindingObject.explanationTitle.setText(getString(R.string.title_info));
        bindingObject.explanationText.setText(getString(R.string.subtitle_info));
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.explanationParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showContinueBtn();
            }
        });
        bindingObject.explanationParent.startAnimation(animation);
    }

    private void showContinueBtn() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        bindingObject.continueBtn.startAnimation(animation);
    }

    private void startAnimationForPermission() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.viewPermissions.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        bindingObject.viewPermissions.startAnimation(animation);
    }

    private void startAnimationForPairing() {
        bindingObject.explanationTitle.setText(getString(R.string.pair_label));
        bindingObject.explanationText.setText(getString(R.string.pair_text));
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.explanationParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                checkGPSEnabled();
            }
        });
        bindingObject.explanationParent.startAnimation(animation);
    }

    private void stopPairedTimer() {
        if (pairedTimer != null) {
            pairedTimer.cancel();
            pairedTimer = null;
        }
    }

    private void startTagTimer() {
        sendDataToTag(TagAction.TURN_ON_DONT_DISTURB);
        sendDataToTag(TagAction.IMMEDIATE_ALERT_TURN_ON);
        stopPairedTimer();
        pairedTimer = new CountDownTimer(PAIRED_TIMER, PAIRED_TIMER) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                sendDataToTag(TagAction.IMMEDIATE_ALERT_TURN_OFF);
                updateUIState(Screen.HOW_TO_USE);
            }
        }.start();
    }

    private void startAnimationForHowToUse() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.explanationParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startAnimationForHowToUseBtn();
            }
        });
        bindingObject.explanationParent.startAnimation(animation);
    }

    private void startAnimationForHowToUseBtn() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bindingObject.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        bindingObject.continueBtn.startAnimation(animation);
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
