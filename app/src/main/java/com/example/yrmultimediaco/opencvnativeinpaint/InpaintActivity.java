package com.example.yrmultimediaco.opencvnativeinpaint;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
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
    private TextView selectedTextColor;
    private DrawingView drawingView ;
    private ImageView undo, redo, compareImg;
    private LinearLayout seekBarSelectionBtnLayout, inPaintedImageBtnLayout, brushTypeStraightLineBtnLayout, brushTypeFreeHandBtnLayout, brushTypeRectangleBtnLayout, brushContainer, resetLayout;
    private ImageView selectImage, inPaintButton, brushImage, brushTypeImage, brushFreeHand, brushRectangle, brushStraightLine, resetImage;
    private TextView seekBarTextColor, inPaintTextColor, brushTypeText, brushFreeText, brushRectText, brushStraightLineText, resetText;
    private static final int PICK_IMAGE_FROM_GALLERY = 1;
    private SeekBar seekBar;
    private RelativeLayout expandedLayout, brushTypeExpandedLayout;
    private ProgressBar mProgressBar;
    private RelativeLayout progressBarLayout;
    private Context context;
    private Bitmap inpaintedBitmap;
    private Bitmap originalBitmap;
    private boolean isOriginalImageDisplayed = true;
    float newSize;
    BrushPreviewView mBrushPreviewView;
    private String sourcePathStr;
    private String resizePathStr;
    private ImageView resultScreen;
    private TextView brushSizeText;
    private String fileExtension;
    private Toolbar mToolbar;
    private AlertDialog resetDialog;
    private boolean isImageResized = false;
    private String originalImagePath;
    boolean isReset = true;



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
        inPaintButton = findViewById(R.id.inPaintImage);
        expandedLayout = findViewById(R.id.expandedLayout);
        brushImage = findViewById(R.id.brushImage);
        compareImg = findViewById(R.id.compareImage);
        mProgressBar = findViewById(R.id.progress_bar);
        progressBarLayout = findViewById(R.id.progressBarLayout);
        mBrushPreviewView = findViewById(R.id.brushPreviewView);
        brushSizeText = findViewById(R.id.brushSizeTextView);
        resultScreen = findViewById(R.id.done);
        seekBarSelectionBtnLayout = findViewById(R.id.seekBarSelectionBtnLayout);
        inPaintedImageBtnLayout = findViewById(R.id.inPaintedImageBtnLayout);
        seekBarTextColor = findViewById(R.id.seekBarTxt);
        inPaintTextColor = findViewById(R.id.inPaintTxt);
        brushTypeText = findViewById(R.id.brushTypeIconTxt);
        brushTypeImage = findViewById(R.id.brushTypeIconImage);
        brushFreeHand = findViewById(R.id.brushTypeFreeHandImage);
        brushRectangle = findViewById(R.id.brushTypeRectangleImage);
        brushStraightLine = findViewById(R.id.brushTypeStraightLineImage);
        brushFreeText = findViewById(R.id.brushTypeFreeHandTxt);
        brushRectText = findViewById(R.id.brushTypeRectangleTxt);
        brushStraightLineText = findViewById(R.id.brushTypeStraightLineTxt);
        brushContainer = findViewById(R.id.brushTypeCointainer);
        brushTypeExpandedLayout = findViewById(R.id.brushTypeExpandedLayout);
        resetLayout = findViewById(R.id.resetLayout);
        resetImage = findViewById(R.id.resetImage);
        resetText = findViewById(R.id.resetTxt);
        brushTypeStraightLineBtnLayout = findViewById(R.id.brushTypeStraightLineBtn);
        brushTypeFreeHandBtnLayout = findViewById(R.id.brushTypeFreeHandBtnLayout);
        brushTypeRectangleBtnLayout = findViewById(R.id.brushTypeRectangleBtnLayout);
        mToolbar = findViewById(R.id.toolbar);

        //mProgressBar.setVisibility(View.GONE);
        progressBarLayout.setVisibility(View.GONE);

        setSupportActionBar(mToolbar);

        drawingView.setLayoutParams(photoView.getLayoutParams());

        drawingView.setPhotoView(photoView);

        resultScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sourcePathStr != null && !sourcePathStr.isEmpty()){
                    Intent intent = new Intent(getApplicationContext(), ResultImage.class);
                    intent.putExtra("inpaintedImagePath", sourcePathStr);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(context, "Please select the image first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handleImageClick(selectImage, null);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, PICK_IMAGE_FROM_GALLERY);

            }
        });

        inPaintedImageBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(inPaintButton, inPaintTextColor);
                new InPaint().execute();

            }
        });

        brushTypeRectangleBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushRectangle, brushRectText);
                drawingView.setRectangleMode(true);
                drawingView.setFreeHandMode(false);
                drawingView.setStraightLineMode(false);
            }
        });

        brushTypeStraightLineBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushStraightLine, brushStraightLineText);
                activateLineTool();
                drawingView.setFreeHandMode(false);
                drawingView.setRectangleMode(false);
            }
        });

        brushTypeFreeHandBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushFreeHand, brushFreeText);
                drawingView.setFreeHandMode(true);
                drawingView.setStraightLineMode(false);
                drawingView.setRectangleMode(false);
            }
        });

        resetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(resetImage, resetText);
                showResetConfirmationDialog();
                //Toast.makeText(context, "Wait for implement this feature", Toast.LENGTH_SHORT).show();
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
                            Log.d("Bitmap", "Inpainted Bitmap is not null");
                            photoView.setImageBitmap(inpaintedBitmap);
                            isOriginalImageDisplayed = false;
                        }
                        break;
                }
                return true;
            }
        });

        drawingView.setupDrawing();

        brushContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushTypeImage, brushTypeText);
                if (brushTypeExpandedLayout.getVisibility() == View.VISIBLE){
                    brushTypeExpandedLayout.setVisibility(View.GONE);
                } else {
                    brushTypeExpandedLayout.setVisibility(View.VISIBLE);

                    if (expandedLayout.getVisibility() == View.VISIBLE){
                        expandedLayout.setVisibility(View.GONE);
                    }

                }
            }
        });

        seekBarSelectionBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(brushImage, seekBarTextColor);
                if (expandedLayout.getVisibility() == View.VISIBLE) {
                    expandedLayout.setVisibility(View.GONE);
                } else {
                    expandedLayout.setVisibility(View.VISIBLE);

                    if (brushTypeExpandedLayout.getVisibility() == View.VISIBLE){
                        brushTypeExpandedLayout.setVisibility(View.GONE);
                    }
                }
            }
        });

        float scaleFactor = photoView.getScaleX();
        // pass the scale factor to the drawing view
        drawingView.setScaleFactor(scaleFactor);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int imageWidth = drawingView.getWidth();
                int imageHeight = drawingView.getHeight();

                newSize = mapValue(progress, 0, 100, 10, 50);
                String brushTextSize = "Size: " + newSize;
                brushSizeText.setText(brushTextSize);

                Log.i("SeekBar Progress", "Progress: " + progress);
                PointF previewPoint = new PointF(drawingView.getWidth() / 2f, drawingView.getHeight() / 2f);
                mBrushPreviewView.updatePreview(previewPoint, newSize);

                drawingView.setSizeForBrush(progress);

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
                handleImageClick(undo, null);
                drawingView.undo();
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(redo, null);
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

                drawingView.setTransformMatrix(newMatrix);
                // Pass the photoView bounds to the drawingView
                drawingView.setPhotoViewBounds(rect);
            }
        });

    }

    private void activateLineTool() {
        drawingView.setStraightLineMode(true);
    }


    private void showResetConfirmationDialog(){

        if (resetDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_reset_process,
                    findViewById(R.id.layoutResetContainer)
            );
            builder.setView(view);
            builder.setCancelable(false);

            resetDialog = builder.create();
            if (resetDialog.getWindow() != null){
                resetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textYes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetToOriginalImage();

                    resetDialog.dismiss();
                    resetDialog = null;
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetDialog.dismiss();
                    resetDialog = null;
                }
            });
            resetDialog.show();
        }
    }

    private void resetToOriginalImage(){
        if (originalBitmap != null){
            inpaintedBitmap = null;

            resetSourceImagePath();

            File originalFile = new File(originalImagePath);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalFile.getPath(), options);

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            if (originalWidth > 1500 || originalHeight > 1500) {
                // Image needs to be resized
                loadResizedImage(originalFile);
            }else {
                Glide.with(this)
                        .asBitmap()
                        .load(originalImagePath)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                photoView.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            }



        } else {
            Toast.makeText(context, "First remove the objects you want", Toast.LENGTH_SHORT).show();
        }
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

    private File resizeImageDIr(){
        File resizeImagesDir = new File(getAppDir(), "resizeImages");
        if (!resizeImagesDir.exists()) {
            resizeImagesDir.mkdirs();
        }
        return resizeImagesDir;
    }

    private File resultDir(){
        File resultImagesDir = new File(getAppDir(), "resultImages");
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

        File inpaintFile = new File(resultDir(), System.currentTimeMillis() + "." + fileExtension);

        try {
            File[] maskFiles = getMaskDIr().listFiles();

            File lastModifiedMaskFile = null;
            long lastModifiedTime = Long.MIN_VALUE;

            if (maskFiles != null) {
                for (File file : maskFiles) {
                    if (file.isFile() && file.getName().endsWith("." + fileExtension) && file.lastModified() > lastModifiedTime) {
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

                File maskFile = saveImageToInternal(maskURI, getMaskDIr(), fileExtension);

                MyInPaintExample(sourcePathStr, maskFile.getPath(), inpaintFile.getPath(), (int) newSize);
                Log.i("Brush Size : ", String.valueOf(newSize));
            }

            long time = System.currentTimeMillis();

            Log.v("Remove_Time", String.valueOf(System.currentTimeMillis() - time));

        } catch (Exception e) {
            Log.e( "InPainting Error1 : ",e.getMessage());
        } catch (Throwable e) {
            Log.e("InPainting Error2 : ",e.getMessage());

        } finally {
            if(inpaintFile.exists()){
                sourcePathStr = inpaintFile.getPath();
                Log.v("InPainting Done : ", "Done");

                Bitmap bmp = BitmapFactory.decodeFile(inpaintFile.getPath());
                if (bmp != null) {
                    inpaintedBitmap = bmp;

                    int desiredWidth = 1500;
                    int desiredHeight = 1500;

                    int originalWidth = inpaintedBitmap.getWidth();
                    int originalHeight = inpaintedBitmap.getHeight();

                    if (originalWidth > desiredWidth || originalHeight > desiredHeight) {
                        float aspectRatio = (float) originalWidth / originalHeight;
                        int newWidth, newHeight;

                        if (aspectRatio > 1) {
                            // Landscape image
                            newWidth = desiredWidth;
                            newHeight = Math.round(desiredWidth / aspectRatio);
                        } else {
                            // Portrait or square image
                            newHeight = desiredHeight;
                            newWidth = Math.round(desiredHeight * aspectRatio);
                        }

                        inpaintedBitmap = Bitmap.createScaledBitmap(inpaintedBitmap, newWidth, newHeight, true);

                        File resizeFile = new File(resizeImageDIr(), System.currentTimeMillis() + "_resized." + fileExtension);
                        saveBitmapToFile(inpaintedBitmap, resizeFile);

                        // Loading the resized inpainted image from path
                        Bitmap resizedBitmap = BitmapFactory.decodeFile(resizeFile.getPath());
                        photoView.setImageBitmap(resizedBitmap);
                        //originalBitmap = resizedBitmap;
                       // drawingView.updateBrushSizeForImage(resizedBitmap.getWidth(), resizedBitmap.getHeight());

                        Glide.with(this).load(resizedBitmap).into(photoView);
                    } else{
                        photoView.setImageBitmap(bmp);
                        Glide.with(this)
                                .load(bmp)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        // Handle Glide loading failure
                                        if (e != null) {
                                            e.printStackTrace();
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        // Image loaded successfully
                                        return false;
                                    }
                                })
                                .into(photoView);
                        }

                    }
                    else {
                    Log.e("Bitmap", "Inpainted Bitmap is null after decoding");
                }
            }
        }
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetSourceImagePath() {
        sourcePathStr = originalImagePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri,filePathColumn, null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            File file = new File(filePath);
            fileExtension = file.getName().substring(file.getName().lastIndexOf(".")+1);
            drawingView.setOriginalExtension(fileExtension);
        } else {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();

        }


        if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                File sourceFile = saveImageToInternal(data.getData(), getSourceDIr(), fileExtension);

                if (sourceFile.exists()) {
                    originalImagePath = sourceFile.getPath();
                    sourcePathStr = sourceFile.getPath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(sourceFile.getPath(), options);

                    int originalWidth = options.outWidth;
                    int originalHeight = options.outHeight;

                    if (originalWidth > 1500 || originalHeight > 1500) {
                        // Image needs to be resized
                        loadResizedImage(sourceFile);
                    } else {
                        // Image doesn't need resizing
                        loadOriginalImage(sourceFile);
                    }
                }

                /*if (sourceFile.exists()) {
                    sourcePathStr = sourceFile.getPath();
                    Glide.with(this)
                            .asBitmap()
                            .load(sourcePathStr)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                    int desiredWidth = 1500;
                                    int desiredHeight = 1500;

                                    int originalWidth = resource.getWidth();
                                    int originalHeight = resource.getHeight();

                                    float aspectRatio = (float) originalWidth / originalHeight;

                                    int newWidth, newHeight;

                                    if (aspectRatio > 1) {
                                        // Landscape image
                                        newWidth = desiredWidth;
                                        newHeight = Math.round(desiredWidth / aspectRatio);
                                    } else {
                                        // Portrait or square image
                                        newHeight = desiredHeight;
                                        newWidth = Math.round(desiredHeight * aspectRatio);
                                    }

                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, newWidth, newHeight, true);

                                    // Save the resized image to a file
                                    File resizeFile = new File(resizeImageDIr(), System.currentTimeMillis() + "." + fileExtension);
                                    saveBitmapToFile(resizedBitmap, resizeFile);

                                    resizePathStr = resizeFile.getPath();

                                    if (resource.getWidth() > desiredWidth || resource.getHeight() > desiredHeight) {

                                        Glide.with(context)
                                                .asBitmap()
                                                .load(resizePathStr)
                                                .override(newWidth, newHeight)
                                                .into(new CustomTarget<Bitmap>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Bitmap resizedBitmap, @Nullable Transition<? super Bitmap> transition) {

                                                        isImageResized = true;
                                                        //loadResizedImage(resizePathStr);

                                                        photoView.setImageBitmap(resizedBitmap);
                                                        originalBitmap = BitmapFactory.decodeFile(resizePathStr);

                                                       // drawingView.updateBrushSizeForImage(resizedBitmap.getWidth(), resizedBitmap.getHeight());

                                                    }
                                                    @Override
                                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                                        // Handle bitmap clearing if needed
                                                    }
                                                });
                                    } else {

                                        photoView.setImageBitmap(resource);
                                        originalBitmap = BitmapFactory.decodeFile(sourcePathStr);

                                        //drawingView.updateBrushSizeForImage(resource.getWidth(), resource.getHeight());

                                        Log.d("Image Dimensions", "photoView dimensions - Width: " + photoView.getWidth() + ", Height: " + photoView.getHeight());
                                        Log.d("Image Dimensions", "Drawing dimensions - Width: " + drawingView.getWidth() + ", Height: " + drawingView.getHeight());


                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @NonNull
    private File saveImageToInternal(Uri data, File sourceDir, String fileExtension) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(data);

        File sourceFile = new File(sourceDir, System.currentTimeMillis() + "." + fileExtension);
        FileOutputStream outputStream = new FileOutputStream(sourceFile);
        try {
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            // Get the orientation of the image
            ExifInterface exif = new ExifInterface(sourceFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Rotate the image based on the orientation information
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            // Create a new bitmap with the rotated image
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            // Save the rotated image to the file
            FileOutputStream rotatedOutputStream = new FileOutputStream(sourceFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, rotatedOutputStream);
            rotatedOutputStream.close();

            return sourceFile;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void loadResizedImage(File sourceFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourceFile.getPath(), options);

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        int desiredWidth = 1500;
        int desiredHeight = 1500;

        float aspectRatio = (float) originalWidth / originalHeight;

        int newWidth, newHeight;

        if (aspectRatio > 1) {
            // Landscape image
            newWidth = desiredWidth;
            newHeight = Math.round(desiredWidth / aspectRatio);
        } else {
            // Portrait or square image
            newHeight = desiredHeight;
            newWidth = Math.round(desiredHeight * aspectRatio);
        }

        Bitmap sourceOriginalBitmap = BitmapFactory.decodeFile(sourceFile.getPath());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(sourceOriginalBitmap, newWidth, newHeight, true);
        originalBitmap = resizedBitmap;

        // Save the resized image to a file
        File resizeFile = new File(resizeImageDIr(), System.currentTimeMillis() + "." + fileExtension);
        saveBitmapToFile(resizedBitmap, resizeFile);

        resizePathStr = resizeFile.getPath();

        photoView.setImageBitmap(resizedBitmap);
    }

    private void loadOriginalImage(File sourceFile) {
        Glide.with(this)
                .asBitmap()
                .load(sourceFile.getPath())
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

    public class InPaint extends AsyncTask<Void, Void, Void>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mProgressBar.setVisibility(View.VISIBLE);
            progressBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (!TextUtils.isEmpty(sourcePathStr)) {
                    // Get the mask image path from the DrawingView
                    File maskImagePath = drawingView.createMask(sourcePathStr);

                    if (maskImagePath.exists()){

                        Uri maskUri = Uri.fromFile(maskImagePath);

                        startInPiating( maskUri);

                    }else {
                        Log.e("InPainting Error : ", "Mask image path does not exist");
                    }
                } else {
                    Log.e("InPainting Error : ", "Please select an image from the gallery");
                }
            }catch (Exception e){
                Log.e("InPainting Error3 : ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            //mProgressBar.setVisibility(View.INVISIBLE);
            progressBarLayout.setVisibility(View.INVISIBLE);

            drawingView.clearDrawing();
        }
    }

    private void handleImageClick(ImageView imageView, TextView textColor) {
        if (selectedImageView != null) {
            selectedImageView.setColorFilter(ContextCompat.getColor(this, R.color.icon_color), PorterDuff.Mode.SRC_IN);

            if (selectedTextColor!=null){
                selectedTextColor.setTextColor(ContextCompat.getColor(this, R.color.icon_color));

            }

        }

        imageView.setColorFilter(ContextCompat.getColor(this, R.color.sky_blue), PorterDuff.Mode.SRC_IN);
        if (textColor!=null){
            textColor.setTextColor(ContextCompat.getColor(this, R.color.sky_blue));
        }

        selectedImageView = imageView;
        selectedTextColor = textColor;
    }

    public native void MyInPaintExample(String sourceImg, String maskImg, String inpaintImg, int patchSize);

    private float mapValue(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

}