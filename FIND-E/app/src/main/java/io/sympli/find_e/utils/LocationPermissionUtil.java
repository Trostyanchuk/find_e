package io.sympli.find_e.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import io.sympli.find_e.R;

public final class LocationPermissionUtil {

    public static final int LOCATION_INTENT_CODE = 3;

    /**
     * Function to show settings popup_sos dialog On pressing SettingsActivity button will
     * launch SettingsActivity Options
     */
    public static void showSettingsAlert(final Activity activity, final MyLocationListener listener) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(activity);
        alertDialog.setTitle(R.string.location_dialog_title);
        alertDialog.setMessage(R.string.location_dialog_text);
        alertDialog.setPositiveButton(activity.getString(R.string.location_dialog_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(intent, LOCATION_INTENT_CODE);
                    }
                });
        alertDialog.setNegativeButton(activity.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        listener.onSettingsAlertCancelled();
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }
}
