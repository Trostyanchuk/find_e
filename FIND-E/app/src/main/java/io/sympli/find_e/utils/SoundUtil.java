package io.sympli.find_e.utils;

import android.content.Context;
import android.media.MediaPlayer;

import io.sympli.find_e.R;

public final class SoundUtil {

    private static MediaPlayer mediaPlayer;

    public static void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void playBatteryLow(Context context) {
        resetPlayer();
        mediaPlayer = MediaPlayer.create(context, R.raw.battery_low);
        mediaPlayer.start();
    }

    public static void playDisconnected(Context context) {
        resetPlayer();
        mediaPlayer = MediaPlayer.create(context, R.raw.disconnected);
        mediaPlayer.start();
    }

    public static void playPhoneLocator(Context context) {
        resetPlayer();
        mediaPlayer = MediaPlayer.create(context, R.raw.phone_locator);
        mediaPlayer.start();
    }

    public static void playReconnected(Context context) {
        resetPlayer();
        mediaPlayer = MediaPlayer.create(context, R.raw.reconnected);
        mediaPlayer.start();
    }

}
