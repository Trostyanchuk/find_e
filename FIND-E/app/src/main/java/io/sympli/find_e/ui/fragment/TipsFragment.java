package io.sympli.find_e.ui.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
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

public class TipsFragment extends Fragment {

    private static final String TIPS_AREA_POINTER = "tips_area";

    public enum TipsArea {
        DO_NOT_DISTURB, SILENT_AREA, TIPS_CAMERA
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
                binding.setTitle(getString(R.string.dont_disturb_title));
                binding.setSubtitle(getString(R.string.dont_disturb_subtitle));
                binding.setTextInfo(getString(R.string.dont_disturb_text));
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_dont_disturb, binding.contentContainer, false));
                break;
            }
            case SILENT_AREA: {
                binding.setTitle(getString(R.string.silent_title));
                binding.setSubtitle(getString(R.string.silent_subtitle));
                binding.setTextInfo(getString(R.string.silent_text));
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_silent_area, binding.contentContainer, false));

                break;
            }
            case TIPS_CAMERA: {
                binding.setTitle(getString(R.string.camera_title));
                binding.setSubtitle(getString(R.string.camera_subtitle));
                binding.contentContainer.addView(LayoutInflater.from(getContext())
                        .inflate(R.layout.view_tips_camera, binding.contentContainer, false));
                binding.contentContainer.findViewById(R.id.try_now_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openCameraApp();
                    }
                });
                binding.info.setVisibility(View.GONE);
                break;
            }
        }
        binding.actionBtn.setVisibility(area == TipsArea.TIPS_CAMERA ? View.GONE : View.VISIBLE);
        binding.actionBtn.setText(getString(R.string.go_to_settings_btn));
        binding.actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToSettings();
            }
        });
    }

    private void returnToSettings() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETTINGS, ChangeScreenEvent.ScreenGroup.SHADOWING));
    }

    private void openCameraApp() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            PackageManager pm = getContext().getPackageManager();

            final ResolveInfo mInfo = pm.resolveActivity(i, 0);

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            startActivity(intent);
        } catch (Exception e) {

        }
    }
}
