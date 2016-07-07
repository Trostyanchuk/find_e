package io.sympli.find_e.ui.widget;

import android.view.animation.Animation;

public abstract class AbstractAnimationListener implements Animation.AnimationListener {
    @Override
    public abstract void onAnimationStart(Animation animation);

    @Override
    public abstract void onAnimationEnd(Animation animation);

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
