package io.sympli.find_e.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.HashMap;

public final class UIUtil {

    private static HashMap<String, Typeface> typefaces = new HashMap<>();
    private static Snackbar snackbar;

    /**
     * Method for getting typeface from font file
     *
     * @param context      Current context
     * @param typefaceName Path to specific font file in assets
     * @return specific typeface
     */
    public static Typeface getTypeface(Context context, String typefaceName) {
        Typeface requiredTypeface = typefaces.get(typefaceName);
        if (requiredTypeface == null) {
            requiredTypeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            typefaces.put(typefaceName, requiredTypeface);
        }
        return requiredTypeface;
    }

    public static void showSnackBar(View root, String message, int duration) {
        snackbar = Snackbar.make(root, message, duration);
        snackbar.show();
    }

    public static void hideSnackBar() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    //TODO temrorary solution
    public static void runTaskWithDelay(long delay, final DelayTaskListener task) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                task.onFinished();
            }
        }, delay);
    }

    public interface DelayTaskListener {
        void onFinished();
    }
}
