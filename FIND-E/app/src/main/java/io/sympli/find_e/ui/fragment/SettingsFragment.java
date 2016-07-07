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

import eightbitlab.com.blurview.RenderScriptBlur;
import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentSettingsBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;

public class SettingsFragment extends Fragment {

    @Inject
    IBroadcast broadcast;

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        binding.setDoNotDisturb(false);
        binding.setSilentArea(false);
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.NONE, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        final float radius = 16;

        final View decorView = getActivity().getWindow().getDecorView();
        final View rootView = decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
        binding.scrollableParent.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurAlgorithm(new RenderScriptBlur(getContext(), true))
                .blurRadius(radius);

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

        return binding.getRoot();
    }
}
