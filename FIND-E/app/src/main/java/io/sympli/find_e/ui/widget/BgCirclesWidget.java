package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

public class BgCirclesWidget extends View {

    private Paint linePaint;
    private RectF lineRectF;

    private float xOffset;
    private float yOffset;

    private boolean invalidateInProgress;
    private Animation anim;

    private int actionBarHeight;
    private int viewWidth;
    private int viewHeight;
    private int btnWidth;
    private int btnHeight;
    private final float buttonWidthPercent = 1.9f;

    public BgCirclesWidget(Context context) {
        super(context);
        init();
    }

    public BgCirclesWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BgCirclesWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1.5f);

        lineRectF = new RectF();

        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        viewWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 1);
        viewHeight = (int) (MeasureSpec.getSize(heightMeasureSpec) * 1);

        this.btnWidth = (int) (viewWidth / buttonWidthPercent);
        this.btnHeight = this.btnWidth;

        setMeasuredDimension(viewWidth, viewHeight);
    }

    public void setOffset(float x, float y) {
        xOffset = x;
        yOffset = y;
        if (!invalidateInProgress) {
            invalidate();
        }
    }

    private void createAnimation(Canvas canvas) {
        anim = new RotateAnimation(0, 360, getWidth() / 2, getHeight() / 2);
        anim.setDuration(10000);
        anim.setRepeatMode(Animation.RESTART);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        startAnimation(anim);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        invalidateInProgress = true;

        int circleRectWidth = btnWidth + 100;
        int circleRectHeight = btnHeight + 200;
        int radius = circleRectHeight / 2;
        float cX = viewWidth / 2;
        float cY = viewHeight / 2;

        for (int index = 0; index < 6; index++) {

            cX = cX + xOffset * 2 + xOffset * 2 * index;
            cY = cY + yOffset * 2 + yOffset * 2 * index;

            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);

            for (int step = 0; step <= (index + 1) * 6; step++) {
                float angle = 30 / (index + 1) + index;
                float startAngle = step * 2 * angle;
                canvas.drawArc(lineRectF, startAngle, angle, false, linePaint);
            }

            circleRectHeight += 220;
            radius = circleRectHeight / 2;
        }

        invalidateInProgress = false;
    }
}