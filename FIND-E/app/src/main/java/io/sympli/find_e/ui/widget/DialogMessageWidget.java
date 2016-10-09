package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import io.sympli.find_e.R;
import io.sympli.find_e.databinding.ViewDialogMessageBinding;

public class DialogMessageWidget extends RelativeLayout {

    /* Показуємо при першому заході в апп, поки юзер не тепне на кнопку */
    private String textT1 = ""; //DONE

    /* Показуємо після Т1 по тепу на кнопку */
    private String textT2 = ""; //DONE

    /* Показуємо при другому і третьому заході в апп, якщо Т1 і Т2 були пройдені */
    /* (i) Tap to learn more */
    private String textT3 = "";

    /* Показуємо після першого тепу на робота */
    private String textT4 = "";

    /* Показуємо при четвертому і п’ятому заході в апп */
    /* (i) Tap to learn more */
    private String textT5 = ""; //DONE

    /* Показуємо при 6 і 7 заході в апп */
    /* (i) Tap to learn more */
    private String textT6 = ""; //DONE

    /* Показуємо при 8 і 9 заході в апп */
    /* (i) Tap to learn more */
    private String textT7 = "";//DONE


    /* Показуємо при втраті сигналу */
    /* (х) Disconnected */
    private String textN1 = ""; // DONE

    /* Показуємо, якщо сигнал відновлено в момент, коли апп відкрито */
    private String textN2 = ""; // DONE

    /* Показуємо, якщо юзер відключив блютус вручну і зайшов в апп */
    /* (i) Tap to go to settings */
    private String textN3 = ""; // DONE

    /* Показуємо завжди, якщо заряд батареї >5% */
    /* (i) Tap to learn more */
    private String textN4 = ""; //DONE

    private Warning lastState;

    public enum Warning {
        INFO,
        ERROR,
        ERROR_BLE,
        ERROR_BATTERY
    }

    private ViewDialogMessageBinding binding;
    private OnProtipClickListener onProtipClickListener;

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

    public void setOnProtipClickListener(OnProtipClickListener listener) {
        this.onProtipClickListener = listener;
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.view_dialog_message, this, true);
        requestLayout();

        textT1 = getResources().getString(R.string.text1);
        textT2 = getResources().getString(R.string.text2);
        textT3 = getResources().getString(R.string.text3);
        textT4 = getResources().getString(R.string.text4);
        textT5 = getResources().getString(R.string.text5);
        textT6 = getResources().getString(R.string.text6);
        textT7 = getResources().getString(R.string.text7);

        textN1 = getResources().getString(R.string.textN1);
        textN2 = getResources().getString(R.string.textN2);
        textN3 = getResources().getString(R.string.textN3);
        textN4 = getResources().getString(R.string.textN4);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onProtipClickListener != null) {
                    if (lastState == Warning.INFO) {
                        onProtipClickListener.openTips();
                    }
                    if (lastState == Warning.ERROR_BLE) {
                        onProtipClickListener.openBleSettings();
                    }
                }
            }
        });
    }

    public void setUIForEntranceCount(int entranceCount) {
        String text = "";
        switch (entranceCount) {
            case 0:
                text = textT1;
                break;
            case 1:
                text = textT5;
                break;
            case 2:
                text = textT5;
                break;
            case 3:
                text = textT6;
                break;
            case 4:
                text = textT6;
                break;
            case 5:
                text = textT7;
                break;
            case 6:
                text = textT7;
                break;
        }

        lastState = Warning.INFO;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        binding.message.setText(text);
        binding.tapToLearnMoreText.setVisibility(VISIBLE);
        binding.warning.setVisibility(GONE);
    }

    public void setUIForStopBeeping() {
        lastState = Warning.INFO;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        binding.message.setText(textT2);
        binding.tapToLearnMoreText.setVisibility(VISIBLE);
        binding.warning.setVisibility(GONE);
    }

    public void setUIForDisconnected() {
        lastState = Warning.ERROR;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        binding.message.setText(textN1);
        binding.warning.setVisibility(VISIBLE);
        binding.tapToLearnMoreText.setVisibility(GONE);
    }

    public void setUIForConnected() {
        lastState = Warning.ERROR;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        binding.message.setText(textN2);
        binding.tapToLearnMoreText.setVisibility(VISIBLE);
        binding.warning.setVisibility(GONE);
    }

    public void setUIForTurnedOffBluetooth() {
        lastState = Warning.ERROR_BLE;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        binding.message.setText(textN3);
        binding.warning.setVisibility(VISIBLE);
        binding.tapToLearnMoreText.setVisibility(GONE);
    }

    public void setUIForBatteryLow() {
        lastState = Warning.ERROR_BATTERY;
        binding.topLine.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        binding.message.setText(textN4);
        binding.warning.setVisibility(VISIBLE);
        binding.tapToLearnMoreText.setVisibility(GONE);
    }

    public interface OnProtipClickListener {
        void openTips();

        void openBleSettings();
    }
}
