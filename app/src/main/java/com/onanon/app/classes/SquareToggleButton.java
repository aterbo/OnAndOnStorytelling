package com.onanon.app.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

/**
 * Created by ATerbo on 10/16/16.
 */
public class SquareToggleButton extends ToggleButton {
    public SquareToggleButton(Context context) {
        super(context);
    }

    public SquareToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size); // make it square
    }
}
