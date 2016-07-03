package io.sympli.find_e.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import io.sympli.find_e.R;
import io.sympli.find_e.utils.UIUtil;

public class TypefaceTextView extends TextView {

    public TypefaceTextView(Context context) {
        super(context);
        setTypefaceFromAttrs(context, null);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypefaceFromAttrs(context, attrs);
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypefaceFromAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
