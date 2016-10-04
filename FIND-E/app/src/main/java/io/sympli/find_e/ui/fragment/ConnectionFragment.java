package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentConnectionBinding;
import io.sympli.find_e.event.BleDeviceFoundEvent;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.dialog.DialogClickListener;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.utils.UIUtil;

public class ConnectionFragment extends Fragment {

    private FragmentConnectionBinding binding;
    private boolean alreadyRequested;

    @Inject
    IBluetoothManager service;
    @Inject
    IBroadcast broadcast;

    private InfoPopup infoPopup;
    private DialogInfo dialogInfo;
    private DialogClickListener listener = new DialogClickListener() {
        @Override
        public void onPositiveBtnClickListener() {
            service.searchKey();
        }

        @Override
        public void onNegativeBtnClickListener() {
        }
    };
    private int lastPosition = -1;
    private boolean videoFinished;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoFinished = true;
            startConnecting();
        }
    };
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false);
        infoPopup = new InfoPopup(getContext(), listener);
        dialogInfo = new DialogInfo()
                .setBody(getString(R.string.unable_to_find))
                .setBtnPositiveText(getString(R.string.retry_recognition_btn))
                .setBtnNegativeText(getString(R.string.cancel_recognition_btn));

        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                onSuccessfullyScanned();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadVideo();
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcast.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
    }

    @Override
    public void onStop() {
        super.onStop();
        service.disconnect();
        broadcast.unregister(this);
    }

    @Subscribe
    public void onBluetoothAvailableEvent(BluetoothAvailableEvent event) {
        broadcast.removeStickyEvent(BluetoothAvailableEvent.class);
        UIUtil.hideSnackBar();
        if (event.isAvailable()) {
            service.searchKey();
        } else {
            UIUtil.showSnackBar(binding.getRoot(), getString(R.string.msg_bluetooth_unavailable_alert),
                    Snackbar.LENGTH_INDEFINITE, R.string.label_turn_on, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            service.enableBluetooth(getActivity());
                        }
                    });
        }
    }

    @Subscribe
    public void onBleDeviceFoundEvent(BleDeviceFoundEvent event) {
        broadcast.removeStickyEvent(BleDeviceFoundEvent.class);
        if (event.isFound()) {
            onSuccessfullyScanned();
        } else {
            infoPopup.show(dialogInfo);
        }
    }

    private void startConnecting() {
        if (service.isBluetoothEnabled()) {
            service.searchKey();
        } else {
            if (!alreadyRequested) {
                service.enableBluetooth(getActivity());
                alreadyRequested = true;
            }
        }
    }

    private void loadVideo() {
        String videoResource = "android.resource://" + getContext().getPackageName() + "/" + R.raw.main_video;
        binding.videoPlayer.setVideoURI(Uri.parse(videoResource));
        binding.videoPlayer.setOnCompletionListener(onCompletionListener);
        binding.videoPlayer.setOnErrorListener(onErrorListener);
        binding.videoPlayer.start();
    }

    private void resumeVideo() {
        if (lastPosition != -1 && !videoFinished) {
            binding.videoPlayer.seekTo(lastPosition);
            binding.videoPlayer.start();
        } else {
            binding.videoPlayer.seekTo(binding.videoPlayer.getDuration());
        }
    }

    private void pauseVideo() {
        if (binding.videoPlayer.canPause()) {
            lastPosition = binding.videoPlayer.getCurrentPosition();
            binding.videoPlayer.pause();
        }
    }

    private void onSuccessfullyScanned() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETUP, ChangeScreenEvent.ScreenGroup.MAIN));
    }
}
