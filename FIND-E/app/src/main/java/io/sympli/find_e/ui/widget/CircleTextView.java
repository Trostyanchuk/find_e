package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import io.sympli.find_e.R;
import io.sympli.find_e.utils.UIUtil;

public class CircleTextView extends View {

    private int width;
    private int height;
    private float strokeWidth;
    private float textSize;

    private Paint borderPaint;
    private Paint textPaint;

    private String textInside = "";

    public CircleTextView(Context context) {
        super(context);
        init(null);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        strokeWidth = getContext().getResources().getDimension(R.dimen.circle_number_stroke_width);
        textSize = getContext().getResources().getDimension(R.dimen.circle_number_text_size);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(strokeWidth);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(UIUtil.getTypeface(getContext(), getContext()
                .getString(R.string.font_regular)));

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleTextView);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CircleTextView_text:
                    textInside = a.getString(attr);
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        this.width = MeasureSpec.getSize(widthMeasureSpec);
        this.height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - strokeWidth, borderPaint);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(textInside, 0, textInside.length(), textBounds);
        float textX = width / 2 - textBounds.width() / 2;
        float textY = height / 2 + textBounds.height() / 2;
        canvas.drawText(textInside, textX, textY, textPaint);
    }
}
