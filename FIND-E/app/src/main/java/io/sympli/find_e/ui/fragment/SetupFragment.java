package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
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
import io.sympli.find_e.databinding.FragmentSetupBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.utils.UIUtil;

public class SetupFragment extends Fragment {

    private static final int ANIM_DURATION = 700;

    @Inject
    IBroadcast broadcast;

    private FragmentSetupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false);

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.MAIN_USAGE, ChangeScreenEvent.ScreenGroup.MAIN));
            }
        });

        //TODO remove when will have video
        UIUtil.runTaskWithDelay(1000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showPairUI();
            }
        });

        return binding.getRoot();
    }

    private void showPairUI() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.pairLt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UIUtil.runTaskWithDelay(1000, new UIUtil.DelayTaskListener() {
                    @Override
                    public void onFinished() {
                        showSetupUI();
                    }
                });
            }
        });
        binding.pairLt.startAnimation(animation);
    }

    private void showSetupUI() {
        binding.videoViewStubIv.setImageResource(R.drawable.setup_stub);
        binding.pairLt.setVisibility(View.GONE);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.setupLt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UIUtil.runTaskWithDelay(1000, new UIUtil.DelayTaskListener() {
                    @Override
                    public void onFinished() {
                        showSuccessfullyConnectedUI();
                    }
                });
            }
        });
        binding.setupLt.startAnimation(animation);
    }

    private void showSuccessfullyConnectedUI() {
        binding.setupLt.setVisibility(View.INVISIBLE);
        binding.pairLt.setVisibility(View.INVISIBLE);
        binding.videoViewStubIv.setVisibility(View.INVISIBLE);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.sucessfullyConnectedLt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showBtnGotIt();
            }
        });
        binding.sucessfullyConnectedLt.startAnimation(animation);
    }

    private void showBtnGotIt() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
        binding.continueBtn.startAnimation(animation);
    }
}
