package io.sympli.find_e.ui.widget.states;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import io.sympli.find_e.R;

public class ViewStateDefault extends ViewStateBase {

    private boolean invalidateInProgress;
    private Paint linePaint;
    private RectF lineRectF;

    public ViewStateDefault(Context context) {
        super(context);
        init();
    }

    public ViewStateDefault(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewStateDefault(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewStateDefault(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.line_default));
        linePaint.setStrokeWidth(1.5f);

        lineRectF = new RectF();
    }

    @Override
    public void setParallaxOffset(float x, float y) {
        this.xOffset = x;
        this.yOffset = y;
        if (!invalidateInProgress) {
            invalidate();
        }
    }

    @Override
    public void animateSelf() {
    }

    @Override
    public void stopAnimation() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        invalidateInProgress = true;
        super.onDraw(canvas);

        int circleRectHeight = btnHeight + circleOffset;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = circleRectHeight / 2f;
        float cX, cY;
        int alpha = ALPHA_MAX;

        for (int index = 0; index < CIRCLES; index++) {

            linePaint.setAlpha(alpha);
            cX = x + xOffset * 2 + xOffset * 2 * index;
            cY = y + yOffset * 2 + yOffset * 2 * index;

            lineRectF.set(cX - radius, cY - radius, cX + radius, cY + radius);
            canvas.drawArc(lineRectF, 0, DEGREES_MAX, false, linePaint);
            circleRectHeight += circleOffset;
            radius = circleRectHeight / 2 + index * 6;
            alpha -= 40;
        }

        invalidateInProgress = false;
    }
}
