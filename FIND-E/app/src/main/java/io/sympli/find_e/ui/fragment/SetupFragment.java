package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
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
import io.sympli.find_e.services.IBroadcast;
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

        //TODO remove when will have video
        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showSuccessfullyConnectedUI();
            }
        });

        return binding.getRoot();
    }

    private void showSuccessfullyConnectedUI() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.CONNECTED, ChangeScreenEvent.ScreenGroup.MAIN));
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }
        return animation;
    }
}
