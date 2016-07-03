package io.sympli.find_e.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

import io.sympli.find_e.R;

public class InfoPopup {
    private final Context context;

    private AlertDialog dialog;

    public InfoPopup(Context context) {
        this.context = context;
    }

    public void show(DialogInfo info) {
        if (dialog != null) this.dismiss();

        dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.ThemeDialog))
                .setTitle(info.title)
                .setMessage(info.body)
                .setPositiveButton(info.btnPositiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dialog = null;
                    }
                })
                .setCancelable(true)
                .show();
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
