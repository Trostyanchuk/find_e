package io.sympli.find_e.ui.fragment;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.security.Permission;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentPermissionsBinding;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.utils.PermissionsUtil;

public class PermissionsFragment extends Fragment {

    private FragmentPermissionsBinding binding;

    private InfoPopup infoPopup;
    private DialogInfo bleDialogInfo;
    private DialogInfo locationDialogInfo;

    private boolean blePermissionGranted;
    private boolean locationPermissionGranted;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permissions, container, false);
        infoPopup = new InfoPopup(getContext());
        binding.bleInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoPopup.show(bleDialogInfo);
            }
        });
        binding.locationInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoPopup.show(locationDialogInfo);
            }
        });
        binding.locationGrantSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PermissionsUtil.requestPermissions((AppCompatActivity) getActivity(), 0, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
        blePermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.BLUETOOTH_ADMIN);
        locationPermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        binding.setBlePermissionGranted(blePermissionGranted);
        binding.setLocationPermissionGranted(locationPermissionGranted);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bleDialogInfo = new DialogInfo()
                .setBody(getString(R.string.ble_dialog_explain))
                .setBtnPositiveText(getString(R.string.ok));
        locationDialogInfo = new DialogInfo()
                .setBody(getString(R.string.ble_dialog_explain))
                .setBtnPositiveText(getString(R.string.ok));
    }

    @Override
    public void onPause() {
        super.onPause();
        infoPopup.dismiss();
    }
}
