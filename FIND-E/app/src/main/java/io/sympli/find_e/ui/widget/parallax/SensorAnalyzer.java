package io.sympli.find_e.ui.widget.parallax;

import android.hardware.SensorEvent;
import android.view.Surface;

public class SensorAnalyzer {


    private float[] mLastAcceleration = new float[]{0.0f, 0.0f};
    private float[] mLastTranslation = new float[]{0.0f, 0.0f};

    /**
     * constant use to convert nano second into second
     */
    private static final float NS2S = 1.0f / 1000000000.0f;
    /**
     * boundary minimum to avoid noise
     */
    private static final float TRANSLATION_NOISE = 0.15f;
    /**
     * boundary maximum, over it phone rotates
     */
    private static final float MAXIMUM_ACCELERATION = 3.00f;
    /**
     * duration for translation animation
     */
    private static final int ANIMATION_DURATION_IN_MILLI = 200;
    /**
     * smoothing ratio for Low-Pass filter algorithm
     */
    private static final float LOW_PASS_FILTER_SMOOTHING = 3.0f;
    /**
     * ratio used to determine radius according to ZOrder
     */
    private static final int DEFAULT_RADIUS_RATIO = 12;

    /**
     * remapped axis X according to current device orientation
     */
    private int mRemappedViewAxisX;

    /**
     * remapped axis Y according to current device orientation
     */
    private int mRemappedViewAxisY;

    /**
     * remapped orientation X according to current device orientation
     */
    private int mRemappedViewOrientationX;

    /**
     * remapped orientation Y according to current device orientations
     */
    private int mRemappedViewOrientationY;

    private FloatArrayEvaluator evaluator;

    /**
     * use to calculate dT
     */
    private long mTimeStamp = 0;

    public SensorAnalyzer(FloatArrayEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void remapAxis(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                mRemappedViewAxisX = 0;
                mRemappedViewAxisY = 1;
                mRemappedViewOrientationX = +1;
                mRemappedViewOrientationY = -1;
                break;

            case Surface.ROTATION_90:
                mRemappedViewAxisX = 1;
                mRemappedViewAxisY = 0;
                mRemappedViewOrientationX = -1;
                mRemappedViewOrientationY = -1;
                break;

            case Surface.ROTATION_270:
                mRemappedViewAxisX = 1;
                mRemappedViewAxisY = 0;
                mRemappedViewOrientationX = +1;
                mRemappedViewOrientationY = +1;
                break;
        }
    }

    public float[] normalizeAxisDueToEvent(SensorEvent event) {

        final float accelerationX = event.values[mRemappedViewAxisX];
        final float accelerationY = event.values[mRemappedViewAxisY];
        float[] translation = new float[]{0.0f, 0.0f};
        if (mTimeStamp != 0) {
            final float dT = (event.timestamp - mTimeStamp) * NS2S;

            if (Math.abs(accelerationX) > MAXIMUM_ACCELERATION) {
                translation[mRemappedViewAxisX] = mLastAcceleration[mRemappedViewAxisX] + 0.5f * MAXIMUM_ACCELERATION * dT * dT;
            } else {
                translation[mRemappedViewAxisX] = mLastAcceleration[mRemappedViewAxisX] + 0.5f * accelerationX * dT * dT;
                mLastAcceleration[mRemappedViewAxisX] = accelerationX;
            }

            if (Math.abs(accelerationY) > MAXIMUM_ACCELERATION) {
                translation[mRemappedViewAxisY] = mLastAcceleration[mRemappedViewAxisY] + 0.5f * MAXIMUM_ACCELERATION * dT * dT;
            } else {
                translation[mRemappedViewAxisY] = mLastAcceleration[mRemappedViewAxisY] + 0.5f * accelerationY * dT * dT;
                mLastAcceleration[mRemappedViewAxisY] = accelerationY;
            }

            final float normalizerX = (Math.abs(mLastTranslation[mRemappedViewAxisX]) + Math.abs(translation[mRemappedViewAxisX])) / 2;
            final float normalizerY = (Math.abs(mLastTranslation[mRemappedViewAxisY]) + Math.abs(translation[mRemappedViewAxisY])) / 2;

            final float translationDifX = Math.abs(mLastTranslation[mRemappedViewAxisX] - translation[mRemappedViewAxisX]) / normalizerX;
            final float translationDifY = Math.abs(mLastTranslation[mRemappedViewAxisY] - translation[mRemappedViewAxisY]) / normalizerY;

            final float dynamicNoiseX = TRANSLATION_NOISE / normalizerX;
            final float dynamicNoiseY = TRANSLATION_NOISE / normalizerY;

            float[] newTranslation = null;

            if (translationDifX > dynamicNoiseX && translationDifY > dynamicNoiseY) {
                newTranslation = translation.clone();
            } else if (translationDifX > dynamicNoiseX) {
                newTranslation = new float[2];
                newTranslation[mRemappedViewAxisX] = translation[mRemappedViewAxisX];
                newTranslation[mRemappedViewAxisY] = mLastTranslation[mRemappedViewAxisY];
            } else if (translationDifY > dynamicNoiseY) {
                newTranslation = new float[2];
                newTranslation[mRemappedViewAxisX] = mLastTranslation[mRemappedViewAxisX];
                newTranslation[mRemappedViewAxisY] = translation[mRemappedViewAxisY];
            }

            /**
             * if new translation aren't noise apply Low-Pass filter algorithm and animate parallax
             * items
             */
            if (newTranslation != null) {

                newTranslation[mRemappedViewAxisX] = mLastTranslation[mRemappedViewAxisX] + (newTranslation[mRemappedViewAxisX] - mLastTranslation[mRemappedViewAxisX]) / LOW_PASS_FILTER_SMOOTHING;
                newTranslation[mRemappedViewAxisY] = mLastTranslation[mRemappedViewAxisY] + (newTranslation[mRemappedViewAxisY] - mLastTranslation[mRemappedViewAxisY]) / LOW_PASS_FILTER_SMOOTHING;

                float[] evaluated = evaluator.evaluate(1, mLastTranslation.clone(), newTranslation.clone());

                mLastTranslation[mRemappedViewAxisX] = newTranslation[mRemappedViewAxisX];
                mLastTranslation[mRemappedViewAxisY] = newTranslation[mRemappedViewAxisY];

                return new float[]{evaluated[0], evaluated[1]};
            }
        }

        mTimeStamp = event.timestamp;

        return null;
    }
}
