package com.example.yrmultimediaco.opencvnativeinpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class CircularOverlayDrawable extends Drawable {
    private Paint filledPaint;
    private Paint hollowPaint;

    public CircularOverlayDrawable(Context context) {
        filledPaint = new Paint();
        filledPaint.setColor(ContextCompat.getColor(context, R.color.magnifier));
        filledPaint.setStyle(Paint.Style.FILL);

        hollowPaint = new Paint();
        hollowPaint.setColor(ContextCompat.getColor(context, R.color.magnifier));
        hollowPaint.setStyle(Paint.Style.STROKE);
        hollowPaint.setStrokeWidth(10);

    }

    @Override
    public void draw(Canvas canvas) {

        int centerX = getBounds().width() / 2;
        int centerY = getBounds().height() / 2;
//        int radius = Math.min(centerX, centerY);

        int radius = 5;

        canvas.drawCircle(centerX, centerY, radius, filledPaint);

        int hollowRadius = Math.min(centerX, centerY);
        canvas.drawCircle(centerX, centerY, hollowRadius, hollowPaint);


    }

    @Override
    public void setAlpha(int alpha) {
        // Not needed for this example
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Not needed for this example
    }

    @Override
    public int getOpacity() {
        // Not needed for this example
        return PixelFormat.TRANSLUCENT;
    }
}

