package io.sympli.find_e.ui.widget;

import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ViewPermissionsBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.dialog.DialogInfo;
import io.sympli.find_e.ui.dialog.InfoPopup;
import io.sympli.find_e.ui.fragment.Screen;
import io.sympli.find_e.utils.PermissionsUtil;
import io.sympli.find_e.utils.UIUtil;


public class ViewPermissions extends LinearLayout {

    public static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ViewPermissionsBinding binding;
    private InfoPopup infoPopup;
    private DialogInfo bleDialogInfo;
    private DialogInfo locationDialogInfo;
    private DialogInfo storageDialogInfo;

    private OnPermissionsRequiredListener listener;
    private boolean blePermissionGranted;
    private boolean locationPermissionGranted;
    private boolean cameraPermissionGranted;
    private boolean storagePermissionGranted;

    @Inject
    IBroadcast broadcast;

    public ViewPermissions(Context context) {
        super(context);
        init();
    }

    public ViewPermissions(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPermissions(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.view_permissions, this, true);
        ApplicationController.getComponent().inject(this);
        requestLayout();

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
        binding.storageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPopup.show(storageDialogInfo);
            }
        });
        binding.locationGrantSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CompoundButton) view).isChecked();
                if (isChecked) {
                    if (listener != null) {
                        listener.requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }
            }
        });
        binding.cameraGrantSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CompoundButton) view).isChecked();
                if (isChecked) {
                    if (listener != null) {
                        listener.requestPermissions(Manifest.permission.CAMERA);
                    }
                }
            }
        });
        binding.storageGrantSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CompoundButton) view).isChecked();
                if (isChecked) {
                    listener.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.continueBtn.setEnabled(false);

        blePermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.BLUETOOTH_ADMIN);
        locationPermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        cameraPermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.CAMERA);
        storagePermissionGranted = PermissionsUtil.permissionsGranted(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        binding.setBlePermissionGranted(blePermissionGranted);
        binding.setLocationPermissionGranted(locationPermissionGranted);
        binding.setCameraPermissionGranted(cameraPermissionGranted);
        binding.setStoragePermissionGranted(storagePermissionGranted);

        bleDialogInfo = new DialogInfo()
                .setBody(getContext().getString(R.string.ble_dialog_explain))
                .setBtnPositiveText(getContext().getString(R.string.ok));
        locationDialogInfo = new DialogInfo()
                .setBody(getContext().getString(R.string.location_dialog_explain))
                .setBtnPositiveText(getContext().getString(R.string.ok));
        storageDialogInfo = new DialogInfo()
                .setBody(getContext().getString(R.string.storage_dialog_info))
                .setBtnPositiveText(getContext().getString(R.string.ok));
    }

    public void setPermissionsListener(OnPermissionsRequiredListener listener) {
        this.listener = listener;
    }

    public void setOnContinueClickListener(View.OnClickListener listener) {
        this.binding.continueBtn.setOnClickListener(listener);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        broadcast.register(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        broadcast.unregister(this);
    }

    @Subscribe
    public void onPermissionsGrantResultEvent(PermissionsGrantResultEvent event) {
        broadcast.removeStickyEvent(PermissionsGrantResultEvent.class);
        UIUtil.hideSnackBar();
        if (event.permissionsGranted) {
            if (event.permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                binding.setLocationPermissionGranted(true);
            }
            if (event.permissions.contains(Manifest.permission.CAMERA)) {
                binding.setCameraPermissionGranted(true);
            }
            if (event.permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                binding.setStoragePermissionGranted(true);
            }
            if (binding.getCameraPermissionGranted() && binding.getLocationPermissionGranted()
                    && binding.getStoragePermissionGranted()) {
                showContinueBtn();
            }
        } else {
            if (event.permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                binding.setLocationPermissionGranted(false);
                UIUtil.showSnackBar(binding.getRoot(), getContext().getString(R.string.permission_not_granted),
                        Snackbar.LENGTH_LONG);
            }
            if (event.permissions.contains(Manifest.permission.CAMERA)) {
                binding.setCameraPermissionGranted(false);
                UIUtil.showSnackBar(binding.getRoot(), getContext().getString(R.string.permission_not_granted),
                        Snackbar.LENGTH_LONG);
            }
            if (event.permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                binding.setStoragePermissionGranted(false);
                UIUtil.showSnackBar(binding.getRoot(), getContext().getString(R.string.permission_not_granted),
                        Snackbar.LENGTH_LONG);
            }
        }
    }

    private void showContinueBtn() {
        binding.continueBtn.setEnabled(true);
        binding.continueBtn.setBackgroundResource(R.drawable.btn_blue_selector);
        binding.continueBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        binding.continueBtn.setPadding((int) getResources().getDimension(R.dimen.btn_continue_left),
                (int) getResources().getDimension(R.dimen.btn_continue_top),
                (int) getResources().getDimension(R.dimen.btn_continue_right),
                (int) getResources().getDimension(R.dimen.btn_continue_bottom));
    }

    public interface OnPermissionsRequiredListener {
        void requestPermissions(String... permissions);
    }
}
