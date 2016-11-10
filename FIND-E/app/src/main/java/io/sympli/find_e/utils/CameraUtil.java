package io.sympli.find_e.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

public final class CameraUtil {

    public static void openCameraApp(Context context) {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            PackageManager pm = context.getPackageManager();

            final ResolveInfo mInfo = pm.resolveActivity(i, 0);

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static int getBackFacingCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    public static int getFrontFacingCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isFrontFacingCameraAvailable() {
        return getFrontFacingCameraId() != -1;
    }

    public static List<Camera.Size> getSupportedVideoSizesForFrontCamera() {
        return getSupportedVideoSizesForCameraId(getFrontFacingCameraId());
    }

    public static List<Camera.Size> getSupportedVideoSizesForBackCamera() {
        return getSupportedVideoSizesForCameraId(getBackFacingCameraId());
    }

    public static List<Camera.Size> getSupportedVideoSizesForCameraId(int cameraId) {
        List<Camera.Size> sizes = new ArrayList<>();
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
            Camera.Parameters params = camera.getParameters();
            sizes = params.getSupportedPreviewSizes();
            camera.release();
            camera = null;
        } catch (RuntimeException e) {
            //failed to connect to camera service, return empty list
            if (camera != null) {
                camera.release();
            }
        }
        return sizes;
    }
}
