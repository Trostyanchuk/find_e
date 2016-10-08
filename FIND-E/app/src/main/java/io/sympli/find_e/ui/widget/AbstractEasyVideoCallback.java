package io.sympli.find_e.ui.widget;

import android.net.Uri;
import android.util.Log;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;

public abstract class AbstractEasyVideoCallback implements EasyVideoCallback {

    @Override
    public void onPaused(EasyVideoPlayer player) {
        Log.d("TAG", "onPaused");
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        Log.d("TAG", "onPaused");
    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        Log.d("TAG", "onError");
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        Log.d("TAG", "onRetry");
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        Log.d("TAG", "onSubmit");
    }
}
