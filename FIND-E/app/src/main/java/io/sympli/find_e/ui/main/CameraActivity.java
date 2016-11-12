package io.sympli.find_e.ui.main;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.event.MakePictureEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.services.impl.BluetoothLeService;
import io.sympli.find_e.ui.fragment.ProgressDialogFragment;
import io.sympli.find_e.ui.widget.Camera1Handler;
import io.sympli.find_e.utils.CameraUtil;
import io.sympli.find_e.utils.FilesUtil;
import io.sympli.find_e.utils.LocalStorageUtil;
import io.sympli.find_e.utils.UIUtil;

public class CameraActivity extends AppCompatActivity implements Camera1Handler.CameraStateListener {

    private static final String TAG = CameraActivity.class.getSimpleName();

    private ProgressDialogFragment progressDialogFragment;
    private Camera1Handler camera1Handler;

    @Inject
    IBroadcast broadcast;

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = FilesUtil.getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("TAG", "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(LocalStorageUtil.getLastDeviceId());
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ApplicationController.getComponent().inject(this);

        camera1Handler = new Camera1Handler(CameraUtil.getBackFacingCameraId());
        camera1Handler.setup(this.findViewById(android.R.id.content), this);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideProgressDialog();
        showProgressDialog("Opening camera...", "", false);
        camera1Handler.openCamera();
        broadcast.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera1Handler.release();
        broadcast.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    protected void hideProgressDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            Fragment prev = fragmentManager.findFragmentByTag(ProgressDialogFragment.TAG);
            if (prev != null) {
                ProgressDialogFragment df = (ProgressDialogFragment) prev;
                df.dismiss();
                progressDialogFragment = null;
            } else {
                if (progressDialogFragment != null) {
                    progressDialogFragment.dismiss();
                    progressDialogFragment = null;
                }
            }
        }
    }

    protected void showProgressDialog(String title, String message, boolean modal) {
        progressDialogFragment = ProgressDialogFragment.newInstance(title, message).setModal(modal);
        progressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }

    @Override
    public void onStateChanged(Camera1Handler.CAMERA_STATE state) {
        switch (state) {
            case OPEN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
                break;
            case ERROR:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        UIUtil.showSnackBar(CameraActivity.this.findViewById(android.R.id.content),
                                "Camera is busy by other process and could not be opened",
                                Snackbar.LENGTH_INDEFINITE);
                    }
                });
                break;
        }
    }

    @Subscribe
    public void onMakePictureEvent(MakePictureEvent event) {
        broadcast.removeStickyEvent(MakePictureEvent.class);
        if (camera1Handler != null && camera1Handler.isCameraPrepared())
        camera1Handler.makePicture(mPictureCallback);
    }
}
