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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentSplashBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;

public class SplashFragment extends Fragment {

    @Inject
    IBroadcast broadcast;

    private FragmentSplashBinding splashBinding;

    private int lastPosition = -1;
    private boolean videoFinished;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoFinished = true;
            showLabels();
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
        splashBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false);
        splashBinding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.PERMISSIONS));
            }
        });

        return splashBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadVideo();
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
        String videoResource = "android.resource://" + getContext().getPackageName() + "/" + R.raw.test;
        splashBinding.videoPlayer.setVideoURI(Uri.parse(videoResource));
        splashBinding.videoPlayer.setOnCompletionListener(onCompletionListener);
        splashBinding.videoPlayer.setOnErrorListener(onErrorListener);
        splashBinding.videoPlayer.start();
    }

    private void resumeVideo() {
        if (lastPosition != -1 && !videoFinished) {
            splashBinding.videoPlayer.seekTo(lastPosition);
            splashBinding.videoPlayer.start();
        } else {
            splashBinding.videoPlayer.seekTo(splashBinding.videoPlayer.getDuration());
        }
    }

    private void pauseVideo() {
        if (splashBinding.videoPlayer.canPause()) {
            lastPosition = splashBinding.videoPlayer.getCurrentPosition();
            splashBinding.videoPlayer.pause();
        }
    }

    private void showLabels() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.infoParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showBtn();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashBinding.infoParent.startAnimation(animation);
    }

    private void showBtn() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashBinding.continueBtn.startAnimation(animation);
    }
}
