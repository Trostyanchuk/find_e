package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentSetupBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.UIUtil;

public class SetupFragment extends Fragment {

    @Inject
    IBroadcast broadcast;
    @Inject
    IBluetoothManager service;

    private FragmentSetupBinding binding;
    private int lastPosition = -1;
    private boolean videoFinished;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoFinished = true;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false);

        //TODO remove when will have video
        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showSuccessfullyConnectedUI();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadVideo();
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }
        return animation;
    }

    @Override
    public void onStart() {
        super.onStart();
        service.connect();
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

    private void showSuccessfullyConnectedUI() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.CONNECTED, ChangeScreenEvent.ScreenGroup.MAIN));
    }
}
