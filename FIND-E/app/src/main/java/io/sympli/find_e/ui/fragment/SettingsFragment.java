package io.sympli.find_e.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentSettingsBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.main.CameraActivity;
import io.sympli.find_e.utils.LocalStorageUtil;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    @Inject
    IBroadcast broadcast;

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        binding.setDoNotDisturb(((OnDontDisturbOptionsListener) getActivity()).isDontDisturb());
        binding.setSilentArea(LocalStorageUtil.isSilentArea());

        binding.silentAreaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalStorageUtil.setSilentArea(b);
            }
        });
        binding.doNotDicturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ((OnDontDisturbOptionsListener) getActivity()).turnOnDontDisturb();
                } else {
                    ((OnDontDisturbOptionsListener) getActivity()).turnOffDontDisturb();
                }
            }
        });
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.NONE, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        binding.goToCameraParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

        binding.remoteCameraShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.TIPS_CAMERA, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        binding.silentAreaParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.TIPS_SILENT, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        binding.doNotDicturbParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.TIPS_DISTURB, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });
        binding.locatingPhoneParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.TIPS_LOCATING, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        binding.changindBatteryParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.TIPS_BATTERY, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcast.postEvent(new AnimationFinishedEvent(Screen.SETTINGS));
    }
}
