package io.sympli.find_e.ui.widget.states;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.sympli.find_e.R;

public abstract class ViewStateBase extends View {

    protected static final int CIRCLES = 6;
    protected static final int ALPHA_MAX = 255;
    protected static final int DEGREES_MAX = 360;
    protected static final int ANIM_DELAY = 20;

    protected float xOffset = 0.f;
    protected float yOffset = 0.f;
    protected int viewWidth;
    protected int viewHeight;
    protected int btnWidth;
    protected int btnHeight;
    protected int circleOffset;

    private final float buttonWidthPercent = 2.0f;
    private Bitmap bitmapSrc = null;
    private Bitmap recBitmap = null;
    private ButtonClickListener listener;

    protected Paint lineConnectedPaint;
    protected Paint lineDisonnectedPaint;
    protected Paint searchingLinePaint;
    protected RectF lineRectF;

    public ViewStateBase(Context context) {
        super(context);
        init();
    }

    public ViewStateBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewStateBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewStateBase setOnButtonClickListener(ButtonClickListener listener) {
        this.listener = listener;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewStateBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        circleOffset = (int) getContext().getResources().getDimension(R.dimen.circle_offset);

        lineConnectedPaint = new Paint();
        lineConnectedPaint.setAntiAlias(true);
        lineConnectedPaint.setStyle(Paint.Style.STROKE);
        lineConnectedPaint.setColor(ContextCompat.getColor(getContext(), R.color.line_default));
        lineConnectedPaint.setStrokeWidth(1.5f);

        lineDisonnectedPaint = new Paint();
        lineDisonnectedPaint.setAntiAlias(true);
        lineDisonnectedPaint.setStyle(Paint.Style.STROKE);
        lineDisonnectedPaint.setColor(Color.WHITE);
        lineDisonnectedPaint.setStrokeWidth(1.5f);

        int circleWidth = (int) getResources().getDimension(R.dimen.circle_width);
        searchingLinePaint = new Paint();
        searchingLinePaint.setAntiAlias(true);
        searchingLinePaint.setStyle(Paint.Style.STROKE);
        searchingLinePaint.setAlpha((int) (0.9 * 255));
        searchingLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.line_searching));
        searchingLinePaint.setStrokeWidth(circleWidth);

        lineRectF = new RectF();
    }

    public abstract void setParallaxOffset(float x, float y);

    public abstract void animateSelf();

    public abstract void stopAnimation();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.btnWidth = (int) (viewWidth / buttonWidthPercent);
        this.btnHeight = this.btnWidth;

        bitmapSrc = BitmapFactory.decodeResource(getResources(), R.drawable.btn_common);
        if (bitmapSrc != null && recBitmap == null) {
            recBitmap = Bitmap.createScaledBitmap(bitmapSrc, btnWidth, btnHeight, true);
        }

        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBtn(canvas);
    }

    private void drawBtn(Canvas canvas) {
        int startX = viewWidth / 2 - btnWidth / 2;
        int startY = viewHeight / 2 - btnHeight / 2;
        canvas.drawBitmap(recBitmap, startX + xOffset * 2, startY + yOffset * 2, new Paint());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getX() > viewWidth / 2 - btnWidth / 2
                && motionEvent.getX() < viewWidth / 2 + btnWidth / 2
                && motionEvent.getY() > viewHeight / 2 - btnHeight / 2
                && motionEvent.getY() < viewHeight / 2 + btnHeight / 2) {
            if (listener != null) {
                listener.onButtonClick();
            }
        }
        return super.onTouchEvent(motionEvent);
    }
}
