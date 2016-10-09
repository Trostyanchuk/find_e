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
import io.sympli.find_e.databinding.FragmentTipsBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.CameraUtil;

public class TipsFragment extends Fragment {

    private static final String TIPS_AREA_POINTER = "tips_area";

    public enum TipsArea {
        DO_NOT_DISTURB, SILENT_AREA, TIPS_CAMERA, LOCATE_PHONE, CHANGE_BATTERY
    }

    public static TipsFragment getInstance(TipsArea area) {
        TipsFragment fragment = new TipsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(TIPS_AREA_POINTER, area.ordinal());
        fragment.setArguments(arguments);
        return fragment;
    }

    @Inject
    IBroadcast broadcast;

    private FragmentTipsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tips, container, false);

        setupToolbar();
        setupUI(TipsArea.values()[getArguments().getInt(TIPS_AREA_POINTER)]);


        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToSettings();
            }
        });
        binding.toolbar.getMenu().clear();
    }

    private void setupUI(final TipsArea area) {

        switch (area) {
            case DO_NOT_DISTURB: {
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_dont_disturb, binding.contentContainer, false));
                binding.actionBtn.setText(getString(R.string.go_to_settings_btn));
                binding.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        returnToSettings();
                    }
                });
                break;
            }
            case SILENT_AREA: {
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_silent_area, binding.contentContainer, false));
                binding.actionBtn.setText(getString(R.string.try_now_btn));
                binding.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        returnToSettings();
                    }
                });
                break;
            }
            case TIPS_CAMERA: {
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tip_camera, binding.contentContainer, false));
                binding.actionBtn.setText(getString(R.string.try_now_btn));
                binding.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CameraUtil.openCameraApp(getContext());
                    }
                });
                break;
            }
            case LOCATE_PHONE:
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_locating_phone, binding.contentContainer, false));
                binding.actionBtn.setText("GOT IT");
                binding.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        returnToSettings();
                    }
                });
                break;
            case CHANGE_BATTERY:
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_changing_battery, binding.contentContainer, false));
                binding.actionBtn.setText("GOT IT");
                binding.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        returnToSettings();
                    }
                });
                break;
        }
    }

    private void returnToSettings() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETTINGS, ChangeScreenEvent.ScreenGroup.SHADOWING));
    }
}
