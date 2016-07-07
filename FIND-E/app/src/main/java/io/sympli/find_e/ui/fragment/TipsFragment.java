package io.sympli.find_e.ui.fragment;

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
        DO_NOT_DISTURB, SILENT_AREA
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

        final float radius = 16;

        final View decorView = getActivity().getWindow().getDecorView();
        final View rootView = decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
//        binding.blurParent.setupWith(rootView)
//                .windowBackground(windowBackground)
//                .blurAlgorithm(new RenderScriptBlur(getContext(), true))
//                .blurRadius(radius);

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

    private void setupUI(TipsArea area) {

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
        }

        binding.goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToSettings();
            }
        });
    }

    private void returnToSettings() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETTINGS, ChangeScreenEvent.ScreenGroup.SHADOWING));
    }
}
