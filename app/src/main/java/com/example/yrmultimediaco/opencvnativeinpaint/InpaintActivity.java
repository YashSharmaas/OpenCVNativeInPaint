package com.example.yrmultimediaco.opencvnativeinpaint;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InpaintActivity extends AppCompatActivity {


    private PhotoView photoView;
    private ImageView selectedImageView;
    private static DrawingView drawingView ;
//    private Uri selectedImageUri;
    private ImageView undo, redo, compareImg;
    FrameLayout frameLayout;
    private ImageView selectImage, inPaintSaveMaskButton, brushImage;
    private static final int PICK_IMAGE_FROM_GALLERY = 1;
    SeekBar seekBar;
    RelativeLayout expandedLayout;
    ProgressBar mProgressBar;
    Context context;
    private Bitmap inpaintedBitmap;
    private Bitmap originalBitmap;
    boolean isOriginalImageDisplayed = true;
    float newSize;
    BrushPreviewView mBrushPreviewView;
    String sourcePathStr;
    private TextView brushSizeText;


    static {
        System.loadLibrary("native-lib");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inpaint);


        context = this;
        photoView = findViewById(R.id.photo_view);
        drawingView = findViewById(R.id.drawingView);

        undo = findViewById(R.id.undoImage);
        redo = findViewById(R.id.redoImage);
////        //frameLayout = findViewById(R.id.frameLayoutOverlay);
        selectImage = findViewById(R.id.closeImage);
        seekBar = findViewById(R.id.seekBar);
        inPaintSaveMaskButton = findViewById(R.id.doneImage);
        expandedLayout = findViewById(R.id.expandedLayout);
        brushImage = findViewById(R.id.brushImage);
        compareImg = findViewById(R.id.compareImage);
        mProgressBar = findViewById(R.id.progress_bar);
        mBrushPreviewView = findViewById(R.id.brushPreviewView);
        brushSizeText = findViewById(R.id.brushSizeTextView);

        mProgressBar.setVisibility(View.GONE);

        drawingView.setPhotoView(photoView);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handleImageClick(selectImage);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, PICK_IMAGE_FROM_GALLERY);

            }
        });

        inPaintSaveMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(inPaintSaveMaskButton);
                new InPaint().execute();
                //drawingView.createMask();

            }
        });

        compareImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:

                        if (originalBitmap != null) {
                            photoView.setImageBitmap(originalBitmap);
                            isOriginalImageDisplayed = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        if (inpaintedBitmap != null) {
                            photoView.setImageBitmap(inpaintedBitmap);
                            isOriginalImageDisplayed = false;
                        }
                        break;
                }
                return true;
            }
        });

        drawingView.setupDrawing();

        brushImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushImage);
                if (expandedLayout.getVisibility() == View.VISIBLE) {

                    expandedLayout.setVisibility(View.GONE);
                } else {

                    expandedLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                newSize = progress;
                String brushTextSize = "Size: " + newSize;
                brushSizeText.setText(brushTextSize);

                Log.i("SeekBar Progress", "Progress: " + progress);
                PointF previewPoint = new PointF(drawingView.getWidth() / 2f, drawingView.getHeight() / 2f);
                mBrushPreviewView.updatePreview(previewPoint, newSize);


                drawingView.setSizeForBrush(newSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBrushPreviewView.updatePreview(null, 0);
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                handleImageClick(undo);
                drawingView.undo();
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(redo);
                drawingView.redo();
            }
        });

        photoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                // Get the new matrix from the photoView
                //Toast.makeText(context, "Called", Toast.LENGTH_SHORT).show();
                Matrix newMatrix = new Matrix();
                photoView.getDisplayMatrix(newMatrix);
                float displayedImageWidth = rect.width();
                float displayedImageHeight = rect.height();

                drawingView.setImageDimensions(displayedImageWidth, displayedImageHeight);

                drawingView.updateBrushSizeForImage(displayedImageWidth, displayedImageHeight);

                drawingView.setTransformMatrix(newMatrix);
//              drawingView.invalidate();
            }
        });

        photoView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {

            }
        });

    }

    private File getSourceDIr(){
        File sourceImgDir = new File(getAppDir(), "sourceImages");
        if (!sourceImgDir.exists()){
            sourceImgDir.mkdirs();
        }
        return sourceImgDir;
    }

    private File getMaskDIr(){
        File maskImagesDir = new File(getAppDir(), "maskImages");
        if (!maskImagesDir.exists()) {
            maskImagesDir.mkdirs();
        }
        return maskImagesDir;
    }

 private File resultDir(){
        File resultImagesDir = new File(getAppDir(), "result");
        if (!resultImagesDir.exists()) {
            resultImagesDir.mkdirs();
        }
        return resultImagesDir;
    }

    private File getAppDir(){
        File privateDir = new File(context.getFilesDir(), "RemoveObj");
        if(!privateDir.exists()) { context.getCacheDir();
            privateDir.mkdirs();
        }
        return privateDir;
    }

    private void startInPiating(Uri maskURI) {

        /*File sourceImgDir = new File(getAppDir(), "sourceImages");
        if (!sourceImgDir.exists()){
            sourceImgDir.mkdirs();
        }

        File maskImagesDir = new File(getAppDir(), "maskImages");
        if (!maskImagesDir.exists()) {
            maskImagesDir.mkdirs();
        }

        File inPaintedImgDir = new File(getAppDir(), "result");
        if (!inPaintedImgDir.exists()){
            inPaintedImgDir.mkdirs();
        }*/

        File inpaintFile = new File(getAppDir(), System.currentTimeMillis() + ".png");

        try {
            File[] maskFiles = getMaskDIr().listFiles();

            File lastModifiedMaskFile = null;
            long lastModifiedTime = Long.MIN_VALUE;

            if (maskFiles != null) {
                for (File file : maskFiles) {
                    if (file.isFile() && file.getName().endsWith(".png") && file.lastModified() > lastModifiedTime) {
                        lastModifiedTime = file.lastModified();
                        lastModifiedMaskFile = file;
                    }
                }
            }

            if (lastModifiedMaskFile != null) {
                // Using here last modified mask file
                MyInPaintExample(sourcePathStr, lastModifiedMaskFile.getPath(), inpaintFile.getPath(), (int) newSize);
                Log.i("Brush Size : ", String.valueOf(newSize));
            } else {

                InputStream inputStream2 = context.getContentResolver().openInputStream(maskURI);
                File maskFile = new File(getMaskDIr(), System.currentTimeMillis() + ".png");
                FileOutputStream outStream2 = new FileOutputStream(maskFile);
                IOUtils.copy(inputStream2, outStream2);

                inpaintFile = new File(resultDir(), System.currentTimeMillis() + ".png");


                MyInPaintExample(sourcePathStr, maskFile.getPath(), inpaintFile.getPath(), (int) newSize);
                Log.i("Brush Size : ", String.valueOf(newSize));
            }

            long time = System.currentTimeMillis();
            //MyInPaintExample(sourceFile.getPath(), maskFile.getPath(), inpaintFile.getPath(), 19);
            Log.v("Remove_Time", String.valueOf(System.currentTimeMillis() - time));

        } catch (Exception e) {
            Log.e( "InPainting Error1 : ",e.getMessage());
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            Log.e("InPainting Error2 : ",e.getMessage());
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if(inpaintFile.exists()){
                sourcePathStr = inpaintFile.getPath();
                Log.v("InPainting Done : ", "Done");
                //Toast.makeText(context, "DOne", Toast.LENGTH_SHORT).show();
                Bitmap bmp = BitmapFactory.decodeFile(inpaintFile.getPath());
                photoView.setImageBitmap(bmp);

                inpaintedBitmap = bmp;
                Glide.with(this)
                        .load(bmp)
                        .into(photoView);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null){

            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());

                //String originalExtension = getFileExtension(data.getData());

                File sourceFile = new File(getSourceDIr(), System.currentTimeMillis() + ".png");
                FileOutputStream outputStream = new FileOutputStream(sourceFile);
                IOUtils.copy(inputStream, outputStream);

                if(sourceFile.exists()){
                    sourcePathStr = sourceFile.getPath();
                Glide.with(this)
                        .asBitmap()
                        .load(sourcePathStr)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                photoView.setImageBitmap(resource);
                                originalBitmap = resource;
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(String path) {
        if (path != null && path.lastIndexOf(".") != -1) {
            return path.substring(path.lastIndexOf("."));
        }
        return ""; // Returns an empty string if extension is not found
    }

    public class InPaint extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (!TextUtils.isEmpty(sourcePathStr)) {
                    // Get the mask image path from the DrawingView
                    File maskImagePath = drawingView.createMask();

                    if (maskImagePath.exists()){

                        Uri maskUri = Uri.fromFile(maskImagePath);

                        mProgressBar.setVisibility(View.VISIBLE);

                        startInPiating( maskUri);

                    }else {
                        Log.e("InPainting Error : ", "Mask image path does not exist");
                        //Toast.makeText(getApplicationContext(), "Mask image path does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("InPainting Error : ", "Please select an image from the gallery");
                    //Toast.makeText(getApplicationContext(), "Please select an image from the gallery", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Log.e("InPainting Error3 : ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            mProgressBar.setVisibility(View.INVISIBLE);

            drawingView.clearDrawing();
        }
    }

  /*  private String getFileExtension(Uri uri) {
        String extension = "";

        String fileName = null;

        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1);
        }

        return extension;
    }*/

    private void handleImageClick(ImageView imageView) {
        if (selectedImageView != null) {
            selectedImageView.setColorFilter(ContextCompat.getColor(this, R.color.icon_color), PorterDuff.Mode.SRC_IN);
        }

        imageView.setColorFilter(ContextCompat.getColor(this, R.color.sky_blue), PorterDuff.Mode.SRC_IN);

        selectedImageView = imageView;
    }

    public native void MyInPaintExample(String sourceImg, String maskImg, String inpaintImg, int patchSize);

    private float mapValue(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

}