package com.location.tracking.helper;

/**
 * Created by Ajith on 15/3/2018.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SlidingPanel extends LinearLayout
{
    private Paint innerPaint, borderPaint ;

    public SlidingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlidingPanel(Context context) {
        super(context);
        init();
    }

    private void init() {
        innerPaint = new Paint();
        innerPaint.setColor(Color.TRANSPARENT);
        innerPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(Color.TRANSPARENT);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setStrokeWidth(5);
    }

    public void setInnerPaint(Paint innerPaint) {
        this.innerPaint = innerPaint;
    }

    public void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        RectF drawRect = new RectF();
        drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());


        canvas.drawRoundRect(drawRect, 20, 20, innerPaint);
        canvas.drawRoundRect(drawRect, 20, 20, borderPaint);


        super.dispatchDraw(canvas);
    }
}