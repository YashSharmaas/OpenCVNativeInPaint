package com.example.yrmultimediaco.opencvnativeinpaint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Magnifier;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private Paint mCanvasPaint;
    private Path currentPath;
    private float mBrushSize = 15f;
    private final int color = Color.parseColor("#80F80202"); // SEMI_TRANSPARENT_RED "#80F80202" "#FFFFFF"
    int alphaValue = 128;
    int semiTransparentColor = Color.argb(alphaValue, Color.red(color), Color.green(color), Color.blue(color));
    private Matrix transformMatrix = new Matrix();
    private ArrayList<TouchPointsHolder> mPathsUndoo = new ArrayList<>();
    private TouchPointsHolder mTouchPointsHolder;
    private ArrayList<TouchPointsHolder> mHolderArrayList = new ArrayList<>();
    private float imageWidth;
    private float imageHeight;
    Context context;
    private Magnifier magnifier;
    private String originalExtension = ".png";
    private float currentX, currentY;
    private static final String TAG = "DrawingView";
    private Paint mFilledRectanglePaint;
    private boolean isStraightLineMode = false;
    private boolean isFreeHandMode = false;
    private PointF startStraightLinePoint;
    private boolean isRectangleMode = false;
    private float startX, startY, endX, endY;
    private boolean isDraggingRectangle = false;
    private float lastTouchX, lastTouchY;
    private RectF photoViewBounds; // A field to store the photoView bounds
    Float scaleFactor;
    BrushPreviewView mBrushPreviewView;


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

        currentPath = new Path();

            if (mTouchPointsHolder != null){
                mTouchPointsHolder = new TouchPointsHolder(semiTransparentColor, mTouchPointsHolder.getBrushThickness(), false, false);
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        mBrushPreviewView = new BrushPreviewView(context);

    }

    public void setOriginalExtension(String extension) {
        this.originalExtension = extension;
    }

    public void setImageDimensions(float width, float height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public void setBrushSize(float size) {
        this.mBrushSize = size;
    }


    public void setTransformMatrix(Matrix matrix) {
        transformMatrix.set(matrix);
        invalidate();
    }

    public void setupDrawing() {

        if (mCanvasPaint == null && mFilledRectanglePaint == null) {
            mCanvasPaint = new Paint();
            mCanvasPaint.setColor(semiTransparentColor);
            mCanvasPaint.setAntiAlias(true);
            mCanvasPaint.setStyle(Paint.Style.STROKE);
            mCanvasPaint.setStrokeJoin(Paint.Join.ROUND);
            mCanvasPaint.setStrokeCap(Paint.Cap.ROUND);
            mCanvasPaint.setStrokeWidth(mBrushSize);

            mFilledRectanglePaint = new Paint();
            mFilledRectanglePaint.setColor(semiTransparentColor);
            mFilledRectanglePaint.setStyle(Paint.Style.FILL);
            mFilledRectanglePaint.setAntiAlias(true);
            mFilledRectanglePaint.setStrokeJoin(Paint.Join.ROUND);
            mFilledRectanglePaint.setStrokeCap(Paint.Cap.ROUND);

        }
        mPhotoView = ((Activity) getContext()).findViewById(R.id.photo_view);

    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setStraightLineMode(boolean straightLineMode) {
        isStraightLineMode = straightLineMode;
        //startStraightLinePoint = null;
        invalidate();
    }

    public void setFreeHandMode(boolean freeHandMode) {
        isFreeHandMode = freeHandMode;
        invalidate();
    }

    public void setRectangleMode(boolean rectangleMode) {
        isRectangleMode = rectangleMode;
        invalidate();
    }

    public void setPhotoView(PhotoView photoView){
        if (mPhotoView != null){
            mPhotoView = photoView;
        }
    }

    public void setPhotoViewBounds(RectF rect) {
        this.photoViewBounds = rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("DrawingView", "onDraw() called");

        // Clip the canvas to the photoView bounds
        if (photoViewBounds != null) {
            canvas.clipRect(photoViewBounds);
        }

        canvas.save();
        canvas.concat(transformMatrix);
        Log.d(TAG, "Transformation Matrix: " + transformMatrix.toString());

        for (TouchPointsHolder touchPointsHolder : mHolderArrayList) {

            if (isStraightLineMode && startStraightLinePoint != null) {

                mCanvasPaint.setColor(touchPointsHolder.getColor());
                mCanvasPaint.setStrokeWidth(touchPointsHolder.getBrushThickness());

                canvas.drawPath(touchPointsHolder.getPath(), mCanvasPaint);
            }else if (isRectangleMode && touchPointsHolder.isRectangle()) {

                mFilledRectanglePaint.setColor(touchPointsHolder.getColor());
                mFilledRectanglePaint.setStrokeWidth(touchPointsHolder.getBrushThickness());

                canvas.drawPath(touchPointsHolder.getPath(), mFilledRectanglePaint);

            }  else {

                mCanvasPaint.setColor(touchPointsHolder.getColor());
                mCanvasPaint.setStrokeWidth(touchPointsHolder.getBrushThickness());

                canvas.drawPath(touchPointsHolder.getPath(), mCanvasPaint);
            }

            //canvas.drawPath(touchPointsHolder.getPath(), mCanvasPaint);
        }

        if (currentPath != null && mTouchPointsHolder != null) {

            if (isRectangleMode){
                mFilledRectanglePaint.setColor(semiTransparentColor);
                mFilledRectanglePaint.setStrokeWidth(mTouchPointsHolder.getBrushThickness());

                canvas.drawPath(currentPath, mFilledRectanglePaint);
            } else if (isStraightLineMode) {
                mCanvasPaint.setColor(semiTransparentColor);
                mCanvasPaint.setStrokeWidth(mTouchPointsHolder.getBrushThickness());

                canvas.drawPath(currentPath, mCanvasPaint);
            } else {
                mCanvasPaint.setColor(semiTransparentColor);
                mCanvasPaint.setStrokeWidth(mTouchPointsHolder.getBrushThickness());

                canvas.drawPath(currentPath, mCanvasPaint);
            }
        }

        canvas.restore();

        Log.d("DrawingView", "onDraw() ended");

    }


    public File createMask(String originalImagePath) {
        Bitmap originalImage = BitmapFactory.decodeFile(originalImagePath);
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        //updateBrushSizeForImage(originalWidth, originalHeight);

        Drawable drawable = mPhotoView.getDrawable();
        int resizedWidth = drawable.getIntrinsicWidth();
        int resizedHeight = drawable.getIntrinsicHeight();

        // Create the mask image with original image dimensions
        Bitmap maskImg = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(maskImg);
        canvas.drawColor(Color.BLACK);

        Paint tempPaint = new Paint(mCanvasPaint);
        mCanvasPaint.setColor(Color.WHITE);
        mFilledRectanglePaint.setColor(Color.WHITE);

        // Scale the path to match the original image dimensions
        Matrix matrix = new Matrix();
        matrix.postScale((float) originalWidth / resizedWidth, (float) originalHeight / resizedHeight);

        for (TouchPointsHolder touchPointsHolder : mHolderArrayList) {

            Path path = touchPointsHolder.getPath();
            path.transform(matrix);

            if (touchPointsHolder.isStraightLine()) {
                // Handle straight line case

                // Draw the straight line on the canvas
                mCanvasPaint.setStrokeWidth(touchPointsHolder.getBrushThickness());
                canvas.drawPath(path, mCanvasPaint);

            } else if (touchPointsHolder.isRectangle()) {
                mFilledRectanglePaint.setStrokeWidth(touchPointsHolder.getBrushThickness());
                canvas.drawPath(path, mFilledRectanglePaint);

            } else {
                // Handle freehand drawing path case

                // Draw the path on the canvas
                mCanvasPaint.setStrokeWidth(touchPointsHolder.getBrushThickness());
                canvas.drawPath(path, mCanvasPaint);
            }
        }


        // Save the mask image to the cache folder
        File privateDir = new File(context.getFilesDir(), "RemoveObj");
        File maskImagesDir = new File(privateDir, "maskImages");
        if (!maskImagesDir.exists()) {
            maskImagesDir.mkdirs();
        }

        File maskFile = saveBitmapToCache(maskImg, maskImagesDir);

        // Recycle the bitmaps to free up memory
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

        if (pointerCount > 1) {
            isPinchZooming = true;
            isDrawingEnabled = false;
            if (mPhotoView != null) {
                mPhotoView.dispatchTouchEvent(event);
            }

            return true;
        }
        else {

           if(isPinchZooming){
               isPinchZooming = false;
               isDrawingEnabled = true;
               return true;
           }else {
               Matrix inverseMatrix = new Matrix();
               transformMatrix.invert(inverseMatrix);

               float[] touchPoint = { event.getX(), event.getY() };
               inverseMatrix.mapPoints(touchPoint);

               float touchX = touchPoint[0];
               float touchY = touchPoint[1];

               Log.d(TAG, "Transformed Touch Coordinates: (" + touchX + ", " + touchY + ")");

               switch (event.getAction()) {
                   case MotionEvent.ACTION_DOWN:

                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                           magnifier.show(event.getX(), event.getY());
                       }

                       if (isRectangleMode){

                           boolean found = false;
                           for (TouchPointsHolder holder : mHolderArrayList) {
                               Path path = holder.getPath();
                               // Create a Region object from the path
                               Region region = new Region();
                               region.setPath(path, new Region(0, 0, getWidth(), getHeight()));
                               // Use the contains method of the Region class to check if it contains the touch point
                               if (region.contains((int) touchX, (int) touchY)) {
                                   // Select the existing rectangle and set the dragging flag to true
                                   currentPath = path;
                                   RectF bounds = new RectF();
                                   path.computeBounds(bounds, true);
                                   startX = bounds.left;
                                   startY = bounds.top;
                                   endX = bounds.right;
                                   endY = bounds.bottom;
                                   isDraggingRectangle = true;
                                   lastTouchX = touchX;
                                   lastTouchY = touchY;
                                   found = true;
                                   break;
                               }
                           }
                           if (!found) {
                               // No existing rectangle found, check if drawing is enabled
                               if (!isDrawingEnabled) {
                                   // Drawing is disabled, return false to ignore the event
                                   return false;
                               }
                               // Drawing is enabled, create a new rectangle as before
                               startRectangleDrawing(touchX, touchY);
                           }

                       }
                       else if (isStraightLineMode) {
                           startStraightLineDrawing(touchX, touchY);

                       } else {
                           startDrawing(touchX, touchY);
                       }

                       invalidate();
                       break;

                   case MotionEvent.ACTION_MOVE:

                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                           magnifier.show(event.getX(), event.getY());
                       }

                           if (isRectangleMode){

                                   if (isDraggingRectangle) {
                                       // Moving an existing rectangle, just update the currentPath by the displacement
                                       float dx = touchX - lastTouchX;
                                       float dy = touchY - lastTouchY;
                                       currentPath.offset(dx, dy);
                                       startX += dx;
                                       startY += dy;
                                       endX += dx;
                                       endY += dy;
                                       lastTouchX = touchX;
                                       lastTouchY = touchY;
                                   } else {
                                       // Drawing a new rectangle, call the moveRectangle method as before

                                       continueRectangleDrawing(touchX, touchY);
                                   }

                           }
                           else if (isStraightLineMode) {

                               continueStraightLineDrawing(touchX, touchY);

                           }else {

                               continueDrawing(touchX, touchY);

                           }

                       invalidate();
                       break;

                   case MotionEvent.ACTION_UP:
                   case MotionEvent.ACTION_CANCEL:

                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                           magnifier.dismiss();
                       }

                       if (isRectangleMode){
                           if (isDraggingRectangle) {
                               // Moving an existing rectangle, do nothing
                               // Reset the dragging flag to false
                               isDraggingRectangle = false;
                           } else {
                               // Drawing a new rectangle, call the stopRectangleDrawing method as before
                               stopRectangleDrawing();
                           }
                       }
                       else if (isStraightLineMode) {
                           stopStraightLineDrawing();

                       } else {
                           stopDrawing();

                       }

                       break;
                   default:
                       return true;
               }
           }
        }
        return true;
    }

    private void startStraightLineDrawing(float x, float y) {
        currentPath = new Path();
        currentPath.moveTo(x, y);
        startStraightLinePoint = new PointF(x, y);
    }

    private void continueStraightLineDrawing(float x, float y) {
        currentPath.reset();
        currentPath.moveTo(startStraightLinePoint.x, startStraightLinePoint.y);

        currentPath.lineTo(x, y);
    }


    private void stopStraightLineDrawing() {

        if (mTouchPointsHolder != null) {
            TouchPointsHolder touchPointsHolder = new TouchPointsHolder(semiTransparentColor, mTouchPointsHolder.getBrushThickness(), false, false);
            touchPointsHolder.getPath().set(currentPath);
            mHolderArrayList.add(touchPointsHolder);
        }

        currentPath = null;
    }

    private void startRectangleDrawing(float x, float y) {
        // Initialize the starting coordinates of the rectangle
        startX = x;
        startY = y;
        endX = x;
        endY = y;

        // Create a new path for the rectangle
        currentPath = new Path();
        currentPath.moveTo(startX, startY);
        currentPath.lineTo(endX, endY);
    }



    private void continueRectangleDrawing(float x, float y) {
        // Update the ending coordinates of the rectangle
        endX = x;
        endY = y;

        // Reset the path and redraw the rectangle
        currentPath.reset();
        currentPath.moveTo(startX, startY);
        currentPath.lineTo(endX, startY);
        currentPath.lineTo(endX, endY);
        currentPath.lineTo(startX, endY);
        currentPath.lineTo(startX, startY);
        currentPath.close();

        // Invalidate the view to trigger redraw
        invalidate();
    }

    private void stopRectangleDrawing() {
        // Calculate the bounds of the rectangle
        float left = Math.min(startX, endX);
        float top = Math.min(startY, endY);
        float right = Math.max(startX, endX);
        float bottom = Math.max(startY, endY);

        // Create a new path for the rectangle
        currentPath = new Path();
        currentPath.moveTo(left, top);
        currentPath.lineTo(right, top);
        currentPath.lineTo(right, bottom);
        currentPath.lineTo(left, bottom);
        currentPath.close();

        if (mTouchPointsHolder != null) {
            // Create a TouchPointsHolder to hold the rectangle path
            TouchPointsHolder rectangleHolder = new TouchPointsHolder(semiTransparentColor, mTouchPointsHolder.getBrushThickness(), false, true);
            rectangleHolder.getPath().set(currentPath);


            // Add the rectangle to the list of drawings
            mHolderArrayList.add(rectangleHolder);
        }

        // Reset the rectangle coordinates
        startX = startY = endX = endY = 0;

        currentPath.reset();
        // Invalidate the view to trigger redraw
        invalidate();
    }


    private void startDrawing(float x, float y) {
        currentPath = new Path();
        currentPath.moveTo(x, y);
        currentX = x;
        currentY = y;

//        mCanvasPaint.setColor(semiTransparentColor);
//        mCanvasPaint.setStrokeWidth(mBrushSize);

        if (mTouchPointsHolder != null) {
            mTouchPointsHolder.setBrushThickness(mBrushSize);
        }

    }

    private void continueDrawing(float x, float y) {

        float cx = (x + currentX) / 2;
        float cy = (y + currentY) / 2;

        currentPath.quadTo(currentX, currentY, cx, cy);

        currentX = x;
        currentY = y;

        if (mTouchPointsHolder == null) {
            mTouchPointsHolder = new TouchPointsHolder(semiTransparentColor, mBrushSize, false, false);
            mHolderArrayList.add(mTouchPointsHolder);
        }

        mTouchPointsHolder.setBrushThickness(mBrushSize);
        //mTouchPointsHolder.getPath().set(currentPath);

    }

    private void stopDrawing() {

        if (mTouchPointsHolder != null) {
            TouchPointsHolder touchPointsHolder = new TouchPointsHolder(semiTransparentColor, mTouchPointsHolder.getBrushThickness(), false, false);
            touchPointsHolder.getPath().set(currentPath);
            mHolderArrayList.add(touchPointsHolder);
        }

        if (mTouchPointsHolder != null) {
            mTouchPointsHolder.setBrushThickness(mBrushSize);
        }

        currentPath.reset();
    }


    public void setSizeForBrush(float progress) {

        float minBrushSize = 15f;
        float maxBrushSize = 50f;

        float resolutionFactor = 0.02f;
        float adjustedBrushSize = progress + resolutionFactor * progress;

        // multiply the brush size by the scale factor
        adjustedBrushSize *= scaleFactor;

        mBrushSize = Math.max(minBrushSize, Math.min(adjustedBrushSize, maxBrushSize));

        mCanvasPaint.setStrokeWidth(mBrushSize);

        mBrushPreviewView.setBrushSize(mBrushSize);

    }

    /*public void updateBrushSizeForImage(float imageWidth, float imageHeight) {
        // Define the threshold for image dimensions
        float thresholdDimension = 2000f;

        // Define the base brush size for normal cases
        float baseBrushSize = 10f;

        // Define the maximum visible brush size
        float maxVisibleBrushSize = 50f;

        // Calculate the average dimension to get a single representative value
        float averageDimension = (imageWidth + imageHeight) / 2f;

        // Check if the image dimensions exceed the threshold
        if (averageDimension > thresholdDimension) {
            baseBrushSize = calculateBaseBrushSize(imageWidth, imageHeight);
            float adjustedBrushSize = calculateAdjustedBrushSize(baseBrushSize, maxVisibleBrushSize, averageDimension, thresholdDimension);
            setSizeForBrush(adjustedBrushSize);
        } else {
            // Set the size for the brush for normal cases
            setSizeForBrush(baseBrushSize);
        }
    }

    private float calculateBaseBrushSize(float imageWidth, float imageHeight) {
        // Adjust this factor based on experimentation to get a suitable base brush size for larger images
        float baseBrushSizeFactor = 0.010f;
        return (imageWidth + imageHeight) / 2f * baseBrushSizeFactor;
    }

    private float calculateAdjustedBrushSize(float baseBrushSize, float maxVisibleBrushSize, float averageDimension, float thresholdDimension) {
        float resolutionFactor = 1f - Math.min(1f, averageDimension / thresholdDimension);
        float adjustedBrushSize = baseBrushSize + baseBrushSize * resolutionFactor;
        return Math.min(adjustedBrushSize, maxVisibleBrushSize);
    }*/

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
        String filename = timeStamp + "." + originalExtension;

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

}


