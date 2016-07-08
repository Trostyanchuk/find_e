package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.google.repacked.antlr.v4.misc.Utils;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentSplashBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.UIUtil;

public class SplashFragment extends Fragment {

    private static final int ANIM_DURATION = 700;

    @Inject
    IBroadcast broadcast;

    private FragmentSplashBinding splashBinding;

    private int lastPosition = -1;
    private boolean videoFinished;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoFinished = true;
            showLabel();
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
                broadcast.postEvent(new ChangeScreenEvent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        Screen.PERMISSIONS : Screen.PAIR, ChangeScreenEvent.ScreenGroup.MAIN));
            }
        });

        LocalStorageUtil.saveFirstLaunch();

        //TODO remove when will have video
        UIUtil.runTaskWithDelay(1000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showLabel();
            }
        });
        return splashBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //TODO uncomment when will have video
//        loadVideo();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO uncomment when will have video
//        resumeVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO uncomment when will have video
//        pauseVideo();
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

    private void showLabel() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.label.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showInfoTitle();
            }
        });
        splashBinding.label.startAnimation(animation);
    }

    private void showInfoTitle() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.info.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showInfoDetail();
            }
        });
        splashBinding.info.startAnimation(animation);
    }

    private void showInfoDetail() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.infoDetail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showBtn();
            }
        });
        splashBinding.infoDetail.startAnimation(animation);
    }

    private void showBtn() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashBinding.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        splashBinding.continueBtn.startAnimation(animation);
    }
}
