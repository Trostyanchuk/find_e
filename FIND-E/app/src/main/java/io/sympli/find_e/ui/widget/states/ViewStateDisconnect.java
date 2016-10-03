package io.sympli.find_e.ui.widget.states;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;

public class ViewStateDisconnect extends ViewStateBase {

    private static final int ANIM_DELAY = 20;

    private Paint linePaint;
    private RectF lineRectF;
    private boolean invalidateInProgress;
    private float rotationAngle = 0;
    private Handler handler;
    private Runnable runnable;

    public ViewStateDisconnect(Context context) {
        super(context);
        init();
    }

    public ViewStateDisconnect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewStateDisconnect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewStateDisconnect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1.5f);

        lineRectF = new RectF();
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
                increaseRotationAngle();
                handler.postDelayed(this, ANIM_DELAY);
            }
        };
        handler.postDelayed(runnable, ANIM_DELAY);
    }

    @Override
    public void stopAnimation() {
        handler.removeCallbacks(runnable);
        runnable = null;
        handler = null;
        xOffset = 0;
        yOffset = 0;
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
    public void onDraw(Canvas canvas) {
        invalidateInProgress = true;
        super.onDraw(canvas);

        int circleRectHeight = btnHeight + circleOffset;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = circleRectHeight / 2f;
        float cX, cY;
        int alpha = ALPHA_MAX;

        float stepOffset = DEGREES_MAX / 8 * 0.9f;
        float stepEmpty = DEGREES_MAX / 8 * 0.1f;
        int sectors = DEGREES_MAX / 8;
        for (int index = 0; index < CIRCLES; index++) {

            cX = x + xOffset * 2 + xOffset * 2 * index;
            cY = y + yOffset * 2 + yOffset * 2 * index;

            linePaint.setAlpha(alpha);
            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);

            for (int step = 0; step < 8; step++) {
                float startAngle = step == 0 ? stepEmpty / 2 + rotationAngle : sectors * step + rotationAngle;
                canvas.drawArc(lineRectF, startAngle, stepOffset, false, linePaint);
            }

            circleRectHeight += circleOffset;
            radius = circleRectHeight / 2 + index * 6;
            alpha -= 40;
        }

        invalidateInProgress = false;
    }
}
