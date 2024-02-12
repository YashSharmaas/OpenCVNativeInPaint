package com.example.yrmultimediaco.opencvnativeinpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class BrushPreviewView extends View {
    private Paint previewPaint;
    private PointF previewPoint;
    private float previewBrushSize;
    private final int color = Color.parseColor("#80F80202");
    int alphaValue = 128;
    int semiTransparentColor = Color.argb(alphaValue, Color.red(color), Color.green(color), Color.blue(color));


    public BrushPreviewView(Context context) {
        super(context);
        init();
    }

    public BrushPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrushPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBrushSize(float size) {
        // assign the parameter to the field
        this.previewBrushSize = size;
        // redraw the view with the new size
        invalidate();
    }

    private void init() {
        previewPaint = new Paint();
        previewPaint.setColor(semiTransparentColor);
        previewPaint.setStyle(Paint.Style.FILL);
        previewPaint.setStrokeJoin(Paint.Join.ROUND);
        previewPaint.setStrokeCap(Paint.Cap.ROUND);
        previewPaint.setStrokeWidth(previewBrushSize);
    }

    public void updatePreview(PointF point, float size) {
        previewPoint = point;

        previewBrushSize = size;
        /*float minimumPreviewBrushSize = 15f;

        if (size < minimumPreviewBrushSize){
            previewBrushSize = minimumPreviewBrushSize;
        } else {
            previewBrushSize = size;
        }*/



        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (previewPoint != null) {
            canvas.drawCircle(previewPoint.x, previewPoint.y, previewBrushSize / 2 , previewPaint);
        }
    }
}

