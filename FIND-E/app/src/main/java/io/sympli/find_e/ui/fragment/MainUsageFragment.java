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
import io.sympli.find_e.databinding.FragmentMainUsageBinding;
import io.sympli.find_e.services.IBroadcast;

public class MainUsageFragment extends Fragment {

    private FragmentMainUsageBinding binding;

    @Inject
    IBroadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_usage, container, false);

        return binding.getRoot();
    }
}
