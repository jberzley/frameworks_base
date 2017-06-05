package com.android.systemui.secondscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SecondScreenView extends LinearLayout {

    public SecondScreenView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public SecondScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public SecondScreenView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
