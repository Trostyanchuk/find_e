package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentPairBinding;
import io.sympli.find_e.databinding.FragmentSetupBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.UIUtil;

public class PairFragment extends Fragment {

    private static final int ANIM_DURATION = 700;

    @Inject
    IBroadcast broadcast;

    private FragmentPairBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pair, container, false);

//        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                broadcast.postEvent(new ChangeScreenEvent(Screen.MAIN_USAGE, ChangeScreenEvent.ScreenGroup.MAIN));
//            }
//        });

        //TODO remove when will have video
        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showSetupFragment();
            }
        });

        return binding.getRoot();
    }

    private void showSetupFragment() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETUP, ChangeScreenEvent.ScreenGroup.MAIN));
    }
}
