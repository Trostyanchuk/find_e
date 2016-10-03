package io.sympli.find_e.ui.widget.states;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.sympli.find_e.R;

public abstract class ViewStateBase extends View {

    protected static final int CIRCLES = 6;
    protected static final int ALPHA_MAX = 255;
    protected static final int DEGREES_MAX = 360;

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
//        Resources res = getContext().getResources();
//        bitmapSrc = BitmapFactory.decodeResource(res, R.drawable.btn_common);

        circleOffset = (int) getContext().getResources().getDimension(R.dimen.circle_offset);
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
        //TODO
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
