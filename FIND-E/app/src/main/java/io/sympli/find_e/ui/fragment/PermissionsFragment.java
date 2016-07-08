package io.sympli.find_e.ui.fragment;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentPermissionsBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.utils.PermissionsUtil;
import io.sympli.find_e.utils.UIUtil;

public class PermissionsFragment extends Fragment {

    public static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int ANIM_DURATION = 700;

    @Inject
    IBroadcast broadcast;

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
        binding.locationGrantSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CompoundButton) view).isChecked();
                if (isChecked) {
                    PermissionsUtil.requestPermissions((AppCompatActivity) getActivity(), 0, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        });
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.PAIR, ChangeScreenEvent.ScreenGroup.MAIN));
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
    public void onResume() {
        super.onResume();
        broadcast.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        infoPopup.dismiss();
        broadcast.unregister(this);
    }

    @Subscribe(sticky = true)
    public void onPermissionsGrantResultEvent(PermissionsGrantResultEvent event) {
        broadcast.removeStickyEvent(PermissionsGrantResultEvent.class);
        UIUtil.hideSnackBar();
        if (event.permissionsGranted && event.permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            binding.setLocationPermissionGranted(true);
            showContinueBtn();
        } else {
            binding.setLocationPermissionGranted(false);
            UIUtil.showSnackBar(binding.getRoot(), getString(R.string.permission_not_granted),
                    Snackbar.LENGTH_LONG);
        }
    }

    private void showContinueBtn() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIM_DURATION);
        animation.setAnimationListener(new AbstractAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.continueBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        binding.continueBtn.startAnimation(animation);
    }
}
