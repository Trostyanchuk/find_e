package io.sympli.find_e.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public final class PermissionsUtil {

    public static boolean permissionsGranted(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(AppCompatActivity activity, int requestCode, String... permission) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }
}
