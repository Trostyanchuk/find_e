package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentMainUsageBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.event.SendDataToTagEvent;
import io.sympli.find_e.event.SensorEvent;
import io.sympli.find_e.event.TagAction;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.DialogMessageWidget;
import io.sympli.find_e.ui.widget.states.ButtonClickListener;
import io.sympli.find_e.ui.widget.states.ConnectionState;
import io.sympli.find_e.utils.UIUtil;

public class MainUsageFragment extends Fragment implements ButtonClickListener {

    private FragmentMainUsageBinding binding;

    @Inject
    IBroadcast broadcast;

    private ConnectionState connectionState = ConnectionState.SEARCHING;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_usage, container, false);

        binding.messageView.setVisibility(View.INVISIBLE);

        UIUtil.runTaskWithDelay(3000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                setConnectionState(ConnectionState.DISCONNECTED);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcast.register(this);
        binding.btnView.animateSelf();
        setConnectionState(connectionState);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcast.unregister(this);
        binding.btnView.stopAnimation();
    }

    @Subscribe
    public void onSensorEvent(SensorEvent event) {
        broadcast.removeStickyEvent(SensorEvent.class);
        binding.btnView.setParallaxOffset(event.getxOffset(), event.getyOffset());
    }

    private void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        binding.btnView.setConnectionState(connectionState);
        binding.btnView.setOnButtonClickListener(this);
        binding.robotIvState.switchImageByState(connectionState);
        binding.btnView.animateSelf();
        hideMessage();
        if (connectionState == ConnectionState.DISCONNECTED) {
            showMessage(getString(R.string.message_connection_lost),
                    getString(R.string.warning_text_connection_lost),
                    DialogMessageWidget.Warning.ERROR);
        }
        if (connectionState == ConnectionState.CONNECTED) {
            showMessage(null,
                    getString(R.string.warning_text_profit),
                    DialogMessageWidget.Warning.INFO);
        }
    }

    private void showMessage(String message, String warning, DialogMessageWidget.Warning type) {
        binding.messageView.setMessage(message).setWarning(warning, type);
        binding.messageView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_in);
        binding.messageView.startAnimation(animation);
    }

    private void hideMessage() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_out);
        binding.messageView.startAnimation(animation);
        binding.messageView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }
        if (animation != null) {
            getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
            animation.setAnimationListener(new AbstractAnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (enter) {
                        broadcast.postEvent(new AnimationFinishedEvent(Screen.MAIN_USAGE));
                    }
                }
            });
        }
        return animation;
    }

    @Override
    public void onButtonClick() {
        hideMessage();
        switch (connectionState) {
            case SEARCHING:
                setConnectionState(ConnectionState.CONNECTED);
                broadcast.postEvent(new SendDataToTagEvent(TagAction.IMMEDIATE_ALERT_TURN_OFF));
                break;
            case CONNECTED:
                setConnectionState(ConnectionState.SEARCHING);
                broadcast.postEvent(new SendDataToTagEvent(TagAction.IMMEDIATE_ALERT_TURN_ON));
                break;
            case DISCONNECTED:
                broadcast.postEvent(new ChangeScreenEvent(Screen.MAP, ChangeScreenEvent.ScreenGroup.SHADOWING));
                break;
        }
    }
}
