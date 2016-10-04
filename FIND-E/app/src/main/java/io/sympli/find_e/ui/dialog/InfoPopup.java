package io.sympli.find_e.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;

import io.sympli.find_e.R;

public class InfoPopup {
    private final Context context;

    private AlertDialog dialog;
    private DialogClickListener listener;

    public InfoPopup(Context context) {
        this.context = context;
    }

    public InfoPopup(Context context, DialogClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(DialogInfo info) {
        if (dialog != null) this.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.ThemeDialog))
                .setTitle(info.title)
                .setMessage(info.body)
                .setPositiveButton(info.btnPositiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dialog = null;
                        if (listener != null) {
                            listener.onPositiveBtnClickListener();
                        }
                    }
                })
                .setCancelable(true);
        if (!TextUtils.isEmpty(info.btnNegativeText)) {
            builder.setNegativeButton(info.btnNegativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    dialog = null;
                    if (listener != null) {
                        listener.onNegativeBtnClickListener();
                    }
                }
            });
        }
        dialog = builder.create();
        dialog.show();
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
