package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.sympli.find_e.R;
import io.sympli.find_e.services.impl.BleServiceConstant;

public class EmotionsImageView extends ImageView {


    public EmotionsImageView(Context context) {
        super(context);
        init();
    }

    public EmotionsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmotionsImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EmotionsImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void switchImageByState(int connectionState) {
        Drawable drawable = null;
        switch (connectionState) {
            case BleServiceConstant.STATE_DISCONNECTED:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.animation_list_dead);
                break;
            case BleServiceConstant.STATE_CONNECTED:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.animation_list_standby);
                break;
            case BleServiceConstant.STATE_CONNECTING:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.animation_list_searching);
                break;
            case BleServiceConstant.STATE_BEEPING:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.animation_list_searching);
                break;
        }

        if (drawable != null) {
            setBackgroundDrawable(drawable);
            ((AnimationDrawable) getBackground()).start();
        }
    }

    public void setHappy() {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.animation_list_happy);
        if (drawable != null) {
            setBackgroundDrawable(drawable);
            ((AnimationDrawable) getBackground()).start();
        }
    }
}
