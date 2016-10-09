package io.sympli.find_e.ui.widget.states;


import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import io.sympli.find_e.utils.SoundUtil;

public class ButtonView extends ViewStateBase {

    private static final String TAG = ButtonView.class.getSimpleName();

    private boolean invalidateInProgress;

    private float rotationAngle = 0;
    private int circleStartRadius = -1;
    private int circleRadius;
    private float circleStartAlpha = 0.9f;
    private float circleAlpha;
    private float circleEndAlpha = 0.1f;
    private float circleAlphaStep;

    private ConnectionState lastState;
    private Handler handler;
    private Runnable runnable;

    public ButtonView(Context context) {
        super(context);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    public void setConnectionState(ConnectionState state) {
        this.lastState = state;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float circleAlphaDiff = circleStartAlpha - circleEndAlpha;
        circleAlphaStep = circleAlphaDiff / (viewHeight - btnHeight) * 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidateInProgress = true;

        switch (lastState) {
            case HAPPY:
            case CONNECTED:
                drawConnected(canvas);
                break;
            case DISCONNECTED:
                drawDisconnected(canvas);
                break;
            case SEARCHING:
                drawSearching(canvas);
                break;
        }

        invalidateInProgress = false;
    }

    private void drawConnected(Canvas canvas) {
        int circleRectHeight = btnHeight + circleOffset;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = circleRectHeight / 2f;
        float cX, cY;
        int alpha = ALPHA_MAX;

        for (int index = 0; index < CIRCLES; index++) {
            lineConnectedPaint.setAlpha(alpha);
            cX = x + xOffset * 2 + xOffset * 2 * index;
            cY = y + yOffset * 2 + yOffset * 2 * index;

            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);
            canvas.drawArc(lineRectF, 0, DEGREES_MAX, false, lineConnectedPaint);
            circleRectHeight += circleOffset;
            radius = circleRectHeight / 2 + index * 6;
            alpha -= 40;
        }
    }

    private void drawDisconnected(Canvas canvas) {

        int circleRectHeight = btnHeight + circleOffset;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = circleRectHeight / 2f;
        float cX, cY;
        int alpha = ALPHA_MAX;

        float stepOffset = DEGREES_MAX / 18 * 0.9f;
        float stepEmpty = DEGREES_MAX / 18 * 0.1f;
        int sectors = DEGREES_MAX / 18;
        for (int index = 0; index < CIRCLES; index++) {
            cX = x + xOffset * 2 + xOffset * 2 * index;
            cY = y + yOffset * 2 + yOffset * 2 * index;

            lineDisonnectedPaint.setAlpha(alpha);
            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);

            for (int step = 0; step < 18; step++) {
                float startAngle = step == 0 ? stepEmpty / 2 + rotationAngle : sectors * step + rotationAngle;
                canvas.drawArc(lineRectF, startAngle, stepOffset, false, lineDisonnectedPaint);
            }
            circleRectHeight += circleOffset;
            radius = circleRectHeight / 2 + index * 6;
            alpha -= 40;
        }
    }

    private void drawSearching(Canvas canvas) {
        int circleRectHeight = btnHeight + circleOffset;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = circleRectHeight / 2f;
        if (circleStartRadius == -1) {
            circleStartRadius = (int) radius;
        }
        float cX, cY;
        int alpha = ALPHA_MAX;

        for (int index = 0; index < CIRCLES; index++) {
            lineConnectedPaint.setAlpha(alpha);
            cX = x + xOffset * 2 + xOffset * 2 * index;
            cY = y + yOffset * 2 + yOffset * 2 * index;

            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);
            canvas.drawArc(lineRectF, 0, DEGREES_MAX, false, lineConnectedPaint);
            circleRectHeight += circleOffset;
            radius = circleRectHeight / 2 + index * 6;
            alpha -= 40;
        }

        cX = x + xOffset * 2;
        cY = y + yOffset * 2;
        searchingLinePaint.setAlpha(circleAlpha / 1.5 <= 0 ? 0 : (int) (circleAlpha * 255 / 1.5));
        lineRectF.set(cX - circleRadius, cY - circleRadius, cX + circleRadius, cY + circleRadius);
        canvas.drawArc(lineRectF, 0, DEGREES_MAX, false, searchingLinePaint);
    }

    public void increaseRotationAngle() {
        if (rotationAngle == DEGREES_MAX) {
            rotationAngle = 0.25f;
        } else {
            this.rotationAngle += 0.25f;
        }
        invalidate();
    }

    @Override
    public void setParallaxOffset(float x, float y) {
        xOffset = x;
        yOffset = y;
        if (!invalidateInProgress) {
            invalidate();
        }
    }

    @Override
    public void animateSelf() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (lastState == ConnectionState.CONNECTED) {
                    return;
                }
                if (lastState == ConnectionState.DISCONNECTED) {
                    increaseRotationAngle();
                }
                if (lastState == ConnectionState.SEARCHING) {
                    circleRadius += 5;
                    circleAlpha -= circleAlphaStep * 4;
                    if (circleRadius >= viewHeight / 2) {
                        circleRadius = circleStartRadius;
                        circleAlpha = circleStartAlpha;
                    }
                    invalidate();
                }
                if (handler != null) {
                    handler.postDelayed(this, ANIM_DELAY);
                }
            }
        };
        handler.postDelayed(runnable, ANIM_DELAY);
    }

    @Override
    public void stopAnimation() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = null;
        handler = null;
        xOffset = 0;
        yOffset = 0;
    }
}
