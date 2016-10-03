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
import io.sympli.find_e.event.SensorEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.DialogMessageWidget;
import io.sympli.find_e.ui.widget.states.ButtonClickListener;
import io.sympli.find_e.ui.widget.states.ConnectionState;
import io.sympli.find_e.ui.widget.states.ViewStateBase;
import io.sympli.find_e.ui.widget.states.ViewStateDefault;
import io.sympli.find_e.ui.widget.states.ViewStateDisconnect;
import io.sympli.find_e.ui.widget.states.ViewStateSearching;
import io.sympli.find_e.utils.UIUtil;

public class MainUsageFragment extends Fragment implements ButtonClickListener {

    private FragmentMainUsageBinding binding;

    @Inject
    IBroadcast broadcast;

    private ViewStateBase viewStateBase;
    private ConnectionState connectionState = ConnectionState.CONNECTED;

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
                showMessage();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcast.register(this);
        setConnectionState(connectionState);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcast.unregister(this);
        viewStateBase.stopAnimation();
    }

    @Subscribe
    public void onSensorEvent(SensorEvent event) {
        viewStateBase.setParallaxOffset(event.getxOffset(), event.getyOffset());
    }

    private void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        binding.viewContainer.removeAllViews();
        switch (connectionState) {
            case CONNECTED:
                viewStateBase = new ViewStateDefault(getContext());
                break;
            case SEARCHING:
                viewStateBase = new ViewStateSearching(getContext());
                break;
            case DISCONNECTED:
                viewStateBase = new ViewStateDisconnect(getContext());
                break;
        }
        viewStateBase.setOnButtonClickListener(this);
        binding.viewContainer.addView(viewStateBase);
        binding.viewContainer.requestLayout();
        binding.robotIvState.switchImageByState(connectionState);
        viewStateBase.animateSelf();
    }

    private void showMessage() {
        binding.messageView.setMessage("CONNECTION LOST")
                .setWarning("Tap on button to find last location", DialogMessageWidget.Warning.ERROR);
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
        if (connectionState == ConnectionState.DISCONNECTED) {
            broadcast.postEvent(new ChangeScreenEvent(Screen.MAP, ChangeScreenEvent.ScreenGroup.SHADOWING));
        }
    }
}
