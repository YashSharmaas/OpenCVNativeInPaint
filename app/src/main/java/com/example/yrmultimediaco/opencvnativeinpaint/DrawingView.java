package com.example.yrmultimediaco.opencvnativeinpaint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Magnifier;


import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DrawingView extends View {

    private PhotoView mPhotoView;
    private boolean isPinchZooming = false;
    private boolean isDrawingEnabled = true;
    private PointF previewPoint;
    private Paint mCanvasPaint;
    private float mBrushSize = 5f;
    private final int color = Color.parseColor("#80F80202"); // SEMI_TRANSPARENT_RED "#80F80202" "#FFFFFF"
    int alphaValue = 128;
    int semiTransparentColor = Color.argb(alphaValue, Color.red(color), Color.green(color), Color.blue(color));
    private Matrix transformMatrix = new Matrix();
    private PointF mPreviousPoint;
    private static final float VELOCITY_TOLERANCE = 2.0f;
    private ArrayList<TouchPointsHolder> mPathsUndoo = new ArrayList<>();
    TouchPointsHolder mTouchPointsHolder;
    ArrayList<TouchPointsHolder> mHolderArrayList = new ArrayList<>();
    private float imageWidth;
    private float imageHeight;
    Context context;
    Magnifier magnifier;


    public DrawingView(Context context) {
        super(context);
        sharedContent();
    }

    public DrawingView(Context context, AttributeSet attr) {
        super(context, attr);
        sharedContent();
    }

    public DrawingView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        sharedContent();
    }


    private void sharedContent() {
        context = this.getContext();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                magnifier = new Magnifier.Builder(DrawingView.this)
                    .setInitialZoom(2.0f)
                        .setElevation(160.0f)
                        .setCornerRadius(200.0f)
                        .setSize(200,200)
                        .setDefaultSourceToMagnifierOffset(100,-150)
                        .setOverlay(new CircularOverlayDrawable(getContext()))
                        .build();
            }
    }

    public void setImageDimensions(float width, float height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    private boolean isWithinImageBounds(float x, float y) {
        return x >= 0 && x <= imageWidth && y >= 0 && y <= imageHeight;
    }

    public void setupDrawing() {

        if (mCanvasPaint == null) {
            mCanvasPaint = new Paint();
            mCanvasPaint.setColor(semiTransparentColor);
            mCanvasPaint.setAntiAlias(true);
            mCanvasPaint.setStyle(Paint.Style.FILL);
            mCanvasPaint.setStrokeJoin(Paint.Join.ROUND);
            mCanvasPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        mPhotoView = ((Activity) getContext()).findViewById(R.id.photo_view);

    }

    public void setPhotoView(PhotoView photoView){
        if (mPhotoView != null){
            mPhotoView = photoView;
        }
    }

    public void setTransformMatrix(Matrix matrix) {
        transformMatrix.set(matrix);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("DrawingView", "onDraw() called");

        canvas.save();
        canvas.concat(transformMatrix);

        Paint paint = mCanvasPaint;
        for (TouchPointsHolder touchPointsHolder : mHolderArrayList) {
            for (Point point : touchPointsHolder.pointsList) {

                float adjustedX = (float) point.x;
                float adjustedY = (float) point.y;

                // Log the brush thickness for each drawn circle
                Log.d("BrushThickness", "Brush Thickness: " + touchPointsHolder.getBrushThickness());

                paint.setStrokeWidth(touchPointsHolder.brushThickness);
                // Add this code in your for loop before calling canvas.drawCircle()
                Log.d("TransformedCoordinates", "Transformed coordinates: " + adjustedX + ", " + adjustedY);

                canvas.drawCircle(adjustedX, adjustedY, touchPointsHolder.getBrushThickness(), paint);
            }
        }

        canvas.restore();

        Log.d("DrawingView", "onDraw() ended");

    }
    public File createMask(){

                Drawable drawable = mPhotoView.getDrawable();
                int imageWidth = drawable.getIntrinsicWidth();
                int imageHeight = drawable.getIntrinsicHeight();

                Bitmap maskImg = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(maskImg);
                canvas.drawColor(Color.BLACK);

                Paint tempPaint = new Paint(mCanvasPaint); // Create a temporary paint to store the color
                mCanvasPaint.setColor(Color.WHITE);

                for (TouchPointsHolder touchPointsHolder : mHolderArrayList) {
                    for (Point point : touchPointsHolder.pointsList) {

                        float[] eventXY = new float[]{(float) point.x, (float) point.y};
                        //invertMatrix.mapPoints(eventXY);
                            canvas.drawCircle(eventXY[0], eventXY[1], touchPointsHolder.getBrushThickness(), mCanvasPaint);
                        }

                }

                    File privateDir = new File(context.getFilesDir(), "RemoveObj");
                    File maskImagesDir = new File(privateDir, "maskImages");
                    if (!maskImagesDir.exists()) {
                        maskImagesDir.mkdirs();
                    }

                File maskFile = saveBitmapToCache(maskImg, maskImagesDir);
                maskImg.recycle();

                // Reset the canvas color back to transparent
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                // Reset the paint color to its original state
                mCanvasPaint.setColor(tempPaint.getColor());

        return maskFile;
    }

    public void clearDrawing() {
        // Clear the drawing by resetting the canvas
        mHolderArrayList.clear();
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerCount = event.getPointerCount();
        /*int action = event.getActionMasked();
        // Update the flag whenever the action changes
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                // If there is more than one pointer, the user is zooming
                isPinchZooming = pointerCount > 1;
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // If there is only one pointer, the user is drawing
                isPinchZooming = false;
                break;
        }*/

        if (pointerCount > 1) {
            isDrawingEnabled = false;
            if (mPhotoView != null) {
                mPhotoView.dispatchTouchEvent(event);
            }
            //Disabling draweing
//            isPinchZooming = true;
            isDrawingEnabled = false;
            return true;
        }  else {

            isDrawingEnabled = true;

            Matrix inverseMatrix = new Matrix();
            transformMatrix.invert(inverseMatrix);

            float[] touchPoint = { event.getX(), event.getY() };
            inverseMatrix.mapPoints(touchPoint);

            float touchX = touchPoint[0];
            float touchY = touchPoint[1];

            if (!isWithinImageBounds(touchX, touchY)) {
                // If touch point is outside the image boundaries, return false to ignore the event
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        magnifier.show(event.getX(), event.getY());
                    }

                    if (mTouchPointsHolder == null) {
                        mTouchPointsHolder = new TouchPointsHolder(semiTransparentColor, mBrushSize);
                        mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));
                    }
                    mPreviousPoint = new PointF(touchX, touchY);
                    invalidate();
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        magnifier.show(event.getX(), event.getY());
                    }

                    float dx = touchX - mPreviousPoint.x;
                    float dy = touchY - mPreviousPoint.y;
                    float velocity = (float) Math.sqrt(dx * dx + dy * dy);

                    // Add intermediate points based on velocity for smoother drawing
                    if (velocity > VELOCITY_TOLERANCE) {
                        int steps = (int) (velocity / VELOCITY_TOLERANCE);
                        float stepX = dx / steps;
                        float stepY = dy / steps;

                        for (int i = 0; i < steps; i++) {
                            float interpolatedX = mPreviousPoint.x + stepX * i;
                            float interpolatedY = mPreviousPoint.y + stepY * i;
                            if (isWithinImageBounds(interpolatedX, interpolatedY)) {
                                mTouchPointsHolder.addPoint(new Point((int) interpolatedX, (int) interpolatedY));

                                // Log points here for verification
                                Log.d("Point", "Added point: " + interpolatedX + ", " + interpolatedY);
                            }
                        }
                    }else {

                        mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));
                    }

                        //mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));

                        // Update previous point and invalidate to trigger redraw
                        mPreviousPoint.set(touchX, touchY);
                       /* if (mTouchPointsHolder != null) {
                            mHolderArrayList.add(mTouchPointsHolder);
                            mTouchPointsHolder = new TouchPointsHolder(semiTransparentColor, mBrushSize);
                            mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));
                            invalidate();
                        }*/

                    // Update previous point and invalidate to trigger redraw
                    /*mPreviousPoint.set(touchX, touchY);
                    if (mTouchPointsHolder != null) {
                        mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));
                        mHolderArrayList.add(mTouchPointsHolder);
                        invalidate();
                    }*/
                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        magnifier.dismiss();
                    }

                    if (mTouchPointsHolder != null) {
                        mTouchPointsHolder.addPoint(new Point((int) touchX, (int) touchY));
                        mHolderArrayList.add(mTouchPointsHolder);
                        mTouchPointsHolder = null;
                        invalidate();
                    }
                    //new newInpaintActivity.CreateMaskTask().execute();
                    break;

                default:
                    return true;
            }

        }

        return true;
    }

    public void setSizeForBrush(float progress) {
        // Define minimum and maximum brush sizes
        float minBrushSize = 5f;
        float maxBrushSize = 50f;

        // Limit the brush size within the specified range
        if (progress < minBrushSize) {
            mBrushSize = minBrushSize;
        } else if (progress > maxBrushSize) {
            mBrushSize = maxBrushSize;
        } else {
            mBrushSize = progress;
        }

        // Set the stroke width for the paint
        mCanvasPaint.setStrokeWidth(mBrushSize);
    }

   /* public void setSizeForBrush(float progress) {
        float minBrushSize = 5f; // Minimum brush size
        float maxBrushSize = 100f; // Maximum brush size

        // Adjust the brush size based on progress
        float newSize = minBrushSize + (maxBrushSize - minBrushSize) * (progress / 100f);

        // Limit the brush size to the minimum value
        newSize = Math.max(minBrushSize, newSize);

        mBrushSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                newSize,
                getResources().getDisplayMetrics()
        );
        mCanvasPaint.setStrokeWidth(mBrushSize);
    }*/

    /*public void setColor(String newColor) {
        color = Color.parseColor(newColor);
        mDrawPaint.setColor(color);
    }*/
    private File saveBitmapToCache(Bitmap bitmap, File directory) {
        long timeStamp = System.currentTimeMillis();
        String filename = timeStamp + ".png";

        File file = new File(directory, filename);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file;
    }

    public void undo() {
        if (mHolderArrayList.size() > 0) {
            int lastIndex = mHolderArrayList.size() - 1;
            mPathsUndoo.add(mHolderArrayList.get(lastIndex));
            mHolderArrayList.remove(lastIndex);
            invalidate();
        }
    }

    public void redo() {
        if (mPathsUndoo.size() > 0) {
            int lastIndex = mPathsUndoo.size() - 1;
            mHolderArrayList.add(mPathsUndoo.get(lastIndex));
            mPathsUndoo.remove(lastIndex);
            invalidate();
        }
    }

    /*public void undo() {
        if (mHolderArrayList.size() > 0) {
            int lastIndex = mHolderArrayList.size() - 1;
            TouchPointsHolder removedHolder = mHolderArrayList.remove(lastIndex);
            mPathsUndoo.add(removedHolder);
            invalidate();
        }
    }

    public void redo() {
        if (mPathsUndoo.size() > 0) {
            int lastIndex = mPathsUndoo.size() - 1;
            TouchPointsHolder redoHolder = mPathsUndoo.remove(lastIndex);
            mHolderArrayList.add(redoHolder);
            invalidate();
        }
    }*/



}


