package io.sympli.find_e.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import io.sympli.find_e.R;
import io.sympli.find_e.utils.UIUtil;

public class TypefaceEditText extends EditText {
    public TypefaceEditText(Context context) {
        super(context);
        init(context, null);
    }

    public TypefaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setTypefaceFromAttrs(context, attrs);
    }

    private void setTypefaceFromAttrs(Context context, AttributeSet attrs) {
        if (this.isInEditMode()) {
            return;
        }

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextView);
        if (array != null) {
            String typefacePath = array.getString(R.styleable.TypefaceTextView_fontPath);
            if (typefacePath != null) {
                setTypeface(UIUtil.getTypeface(context, typefacePath));
            }
            array.recycle();
        }
    }
}
