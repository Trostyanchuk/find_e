package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.sympli.find_e.R;
import io.sympli.find_e.utils.UIUtil;

public class ButtonWidget extends View {

    private final float buttonWidthPercent = 1.9f;
    private final float buttonHeightPercent = 2.9f;

    private int viewWidth;
    private int viewHeight;
    private int btnWidth;
    private int btnHeight;

    //appearance
    private int btnStartGradient;
    private int btnEndGradient;
    private Paint bgBtnPaint, textBtnPaint, usualTextPaint, errorTextPaint;
    private Rect btnTextRect;

    private String lostMsg;
    private String bottomMsg;

    private float usualTextSize;
    private int bottomMsgTextSize;
    private float lineSpacing;

    public ButtonWidget(Context context) {
        super(context);
        init();
    }

    public ButtonWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.btnStartGradient = ContextCompat.getColor(getContext(), R.color.btn_gradient_start_color);
        this.btnEndGradient = ContextCompat.getColor(getContext(), R.color.btn_gradient_ent_color);

        bgBtnPaint = new Paint();
        bgBtnPaint.setStyle(Paint.Style.FILL);
        bgBtnPaint.setAntiAlias(true);

        textBtnPaint = new Paint();
        textBtnPaint.setColor(ContextCompat.getColor(getContext(), R.color.btn_text_color));
        textBtnPaint.setTypeface(UIUtil.getTypeface(getContext(), getContext().getString(R.string.font_regular)));
        textBtnPaint.setTextSize(getContext().getResources().getDimension(R.dimen.btn_text_size));

        btnTextRect = new Rect();

        usualTextPaint = new Paint();
        usualTextPaint.setColor(Color.WHITE);
//        usualTextPaint.setTypeface(UIUtil.getTypeface(getContext(), getContext().getString(R.string.font_regular)));

        errorTextPaint = new Paint();
        errorTextPaint.setColor(Color.RED);
        errorTextPaint.setTypeface(UIUtil.getTypeface(getContext(), getContext().getString(R.string.font_regular)));

        lostMsg = getContext().getString(R.string.lost_usual_msg);

        usualTextSize = getContext().getResources().getDimension(R.dimen.fail_to_reconnect_text_size);
        bottomMsgTextSize = (int) getContext().getResources().getDimension(R.dimen.bottom_bar_text_size);
        lineSpacing = getContext().getResources().getDimension(R.dimen.line_spacing);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.btnWidth = (int) (viewWidth / buttonWidthPercent);
        this.btnHeight = this.btnWidth;

        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator();

        drawStatusText(canvas);

        drawButton(canvas);
    }

    private void drawIndicator() {

    }

    private void drawStatusText(Canvas canvas) {

        usualTextPaint.setTextSize(usualTextSize);

        String[] textToDraw = lostMsg.split("\n");
        float textX = 0;
        float textY = 0;
        int textHeight = 0;
//        for (int i = 0; i < textToDraw.length; i++) {
////            textBtnPaint.getTextBounds(textToDraw[0], 0, textToDraw[0].length(), btnTextRect);
////            textX = viewWidth / 2 - btnTextRect.width() / 2;
////            textY = viewHeight / 2 + btnHeight / 2 + btnTextRect.height() + textHeight * 0 + lineSpacing;
////            canvas.drawText(textToDraw[0], textX, textY, usualTextPaint);
//
//            btnTextRect = new Rect();
//            String firstLine = textToDraw[i];
//            textBtnPaint.getTextBounds(firstLine, 0, firstLine.length(), btnTextRect);
//            textX = viewWidth / 2 - btnTextRect.width() / 2;
//            textY = viewHeight / 2 + btnHeight / 2 + btnTextRect.height() + lineSpacing + textHeight;
//            canvas.drawText(firstLine, textX, textY, usualTextPaint);
//
//            textHeight = btnTextRect.height();
//        }

        float magicPadding = getContext().getResources().getDimension(R.dimen.magic_padding_fix_it);

        //TODO
        String firstLine = "FIND-E will reconnect again";
        textBtnPaint.getTextBounds(firstLine, 0, firstLine.length(), btnTextRect);
        textX = (float) (viewWidth / 2 - btnTextRect.width() / 2 - magicPadding * 1.2);
        textY = viewHeight / 2 + btnHeight / 2 + btnTextRect.height() + lineSpacing;
        canvas.drawText(firstLine, textX, textY, usualTextPaint);
        textHeight = btnTextRect.height();

        String secondLine = "once it's close enough to you";
        textBtnPaint.getTextBounds(secondLine, 0, secondLine.length(), btnTextRect);
        textX = (float) (viewWidth / 2 - btnTextRect.width() / 2 - magicPadding * 1.5);
        textY = viewHeight / 2 + btnHeight / 2 + btnTextRect.height() + lineSpacing * 2 + textHeight;
        canvas.drawText(secondLine, textX, textY, usualTextPaint);

    }

    private void drawButton(Canvas canvas) {

        int startX = viewWidth / 2 - btnWidth / 2;
        int startY = viewHeight / 2 - btnHeight / 2;
        int endX = viewWidth / 2 - btnWidth / 2;
        int endY = viewHeight / 2 + btnHeight / 2;
        bgBtnPaint.setShader(new LinearGradient(startX, startY, endX, endY, btnStartGradient, btnEndGradient, Shader.TileMode.MIRROR));
        canvas.drawCircle(viewWidth / 2, viewHeight / 2, btnWidth / 2, bgBtnPaint);

        String firstLine = "TAP TO SHOW";
        textBtnPaint.getTextBounds(firstLine, 0, firstLine.length(), btnTextRect);
        float textX = viewWidth / 2 - btnTextRect.width() / 2;
        float textY = viewHeight / 2;
        canvas.drawText(firstLine, textX, textY, textBtnPaint);

        String secondLine = "LAST LOCATION";
        textBtnPaint.getTextBounds(secondLine, 0, secondLine.length(), btnTextRect);
        textX = viewWidth / 2 - btnTextRect.width() / 2;
        textY = viewHeight / 2 + btnTextRect.height() + lineSpacing;
        canvas.drawText(secondLine, textX, textY, textBtnPaint);
    }

//    public void setOnClickListener(io.sympli.find_e.ui.widget.OnClickListener listener) {
//        this.listener = listener;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getX() > viewWidth / 2 - btnWidth / 2
                && motionEvent.getX() < viewWidth / 2 + btnWidth / 2
                && motionEvent.getY() > viewHeight / 2 - btnHeight / 2
                && motionEvent.getY() < viewHeight / 2 + btnHeight / 2) {
//            if (listener != null) {
//                listener.onButtonCLick();
//            }
        }
        return super.onTouchEvent(motionEvent);
    }
}
