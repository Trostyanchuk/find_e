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

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentMainUsageBinding;
import io.sympli.find_e.event.AnimationFinishedEvent;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.AbstractAnimationListener;
import io.sympli.find_e.ui.widget.DialogMessageWidget;
import io.sympli.find_e.ui.widget.OnClickListener;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.utils.UIUtil;

public class MainUsageFragment extends Fragment {

    private FragmentMainUsageBinding binding;

    @Inject
    IBroadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_usage, container, false);

        binding.btnView.setOnClickListener(new OnClickListener() {
            @Override
            public void onButtonCLick() {
                broadcast.postEvent(new ChangeScreenEvent(Screen.MAP, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        binding.messageView.setVisibility(View.INVISIBLE);

        UIUtil.runTaskWithDelay(1000, new UIUtil.DelayTaskListener() {
            @Override
            public void onFinished() {
                showMessage();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showMessage() {
        binding.messageView.setMessage("ERROR, ERROR")
                .setWarning("We lost it", DialogMessageWidget.Warning.ERROR);
        binding.messageView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_in);
        binding.messageView.startAnimation(animation);
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
}
