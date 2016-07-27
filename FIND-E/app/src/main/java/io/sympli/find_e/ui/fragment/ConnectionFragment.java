package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentConnectionBinding;
import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.UIUtil;

public class ConnectionFragment extends Fragment {

    private FragmentConnectionBinding binding;
    private boolean alreadyRequested;

    @Inject
    IBluetoothManager service;
    @Inject
    IBroadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcast.register(this);
        if (service.isBluetoothEnabled()) {
            service.searchKey();
        } else {
            if (!alreadyRequested) {
                service.enableBluetooth(getActivity());
                alreadyRequested = true;
            }
        }

        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                onSuccessfullyScanned();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        service.disconnect();
        broadcast.unregister(this);
    }

    @Subscribe
    public void onBluetoothAvailableEvent(BluetoothAvailableEvent event) {
        UIUtil.hideSnackBar();
        if (event.isAvailable()) {
            service.searchKey();
        } else {
            UIUtil.showSnackBar(binding.getRoot(), getString(R.string.msg_bluetooth_unavailable_alert),
                    Snackbar.LENGTH_INDEFINITE, R.string.label_turn_on, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            service.enableBluetooth(getActivity());
                        }
                    });
        }
    }

    private void onSuccessfullyScanned() {
        broadcast.postEvent(new ChangeScreenEvent(Screen.SETUP, ChangeScreenEvent.ScreenGroup.MAIN));
    }
}
