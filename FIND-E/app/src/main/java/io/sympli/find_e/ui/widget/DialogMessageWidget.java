package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ViewDialogMessageBinding;

public class DialogMessageWidget extends RelativeLayout {

    public enum Warning {
        INFO,
        ERROR
    }

    private ViewDialogMessageBinding binding;

    private String message;
    private String warning;
    private Warning warningType;

    public DialogMessageWidget(Context context) {
        super(context);
        init();
    }

    public DialogMessageWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DialogMessageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.view_dialog_message, this, true);
        requestLayout();
    }

    // --- public setters --
    public DialogMessageWidget setMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            binding.message.setText(message);
        } else {
            binding.message.setVisibility(GONE);
        }
        return this;
    }

    public DialogMessageWidget setWarning(String warning, Warning warningType) {
        if (!TextUtils.isEmpty(warning)) {
            binding.warning.setText(warning);
            //TODO set icon due to type
            binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(),
                    warningType == Warning.ERROR ? android.R.color.holo_red_dark : R.color.light_blue));
            binding.warning.setTextColor(ContextCompat.getColor(getContext(),
                    warningType == Warning.ERROR ? android.R.color.holo_red_dark : R.color.light_blue));
        } else {
            binding.warning.setVisibility(GONE);
            binding.topLine.setVisibility(GONE);
        }
        return this;
    }

    public void show(boolean animate) {
        binding.root.setVisibility(VISIBLE);
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_in);
            binding.root.startAnimation(animation);
        }
    }

    public void hide(boolean animate) {
        binding.root.setVisibility(GONE);
    }
}
