package com.example.yrmultimediaco.opencvnativeinpaint;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yrmultimediaco.opencvnativeinpaint.databinding.ActivityMainBinding;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    Bitmap srcBitmap = null;
    Bitmap dstBitmap = null;
    SeekBar sldSigma;
    Bitmap sourceMaskBitmap;
    Bitmap targetMaskBitmap;
    ProgressBar mProgressBar;
    Button inPaint;
    Context context;

    // Used to load the 'opencvnativeinpaint' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        srcBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pexels_photo_man);

//        srcBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.elephant);
       // Bitmap maskBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.elephant_mask);

        inPaint = binding.inPaint;

        mProgressBar = binding.progressBar;
        context = this;

        if (srcBitmap != null) {
            dstBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
        }
        ImageView imageView = binding.imageView;
        sldSigma = binding.sldSigma;
        imageView.setImageBitmap(dstBitmap);

        /*imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        isDrawing = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isDrawing) {
                            // Implement your drawing logic here (if needed)
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isDrawing) {
                            isDrawing = false;
                            mProgressBar.setVisibility(View.VISIBLE);
                            myInpaint(srcBitmap, dstBitmap);
                            imageView.setImageBitmap(dstBitmap); // Update the image after inpainting
                            mProgressBar.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });*/


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Initialize the source mask bitmap
                        if (srcBitmap != null) {
                            sourceMaskBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
                            targetMaskBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Update the source mask bitmap based on user interaction
                        if (sourceMaskBitmap != null) {
                            Canvas canvas = new Canvas(sourceMaskBitmap);
                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(event.getX(), event.getY(), 20, paint); // Adjust size as needed
                            imageView.setImageBitmap(sourceMaskBitmap); // Show the source mask on imageView
                        }
                        break;
                    // Handle other touch actions if needed
                }
                return true;
            }
        });

        inPaint.setOnClickListener(v -> {
//            if (srcBitmap != null && sourceMaskBitmap != null && targetMaskBitmap != null) {
//
//                mProgressBar.setVisibility(View.VISIBLE);
//
//                // Check if mask bitmaps match the input image size
//                if (srcBitmap.getWidth() == sourceMaskBitmap.getWidth() &&
//                        srcBitmap.getHeight() == sourceMaskBitmap.getHeight() &&
//                        srcBitmap.getWidth() == targetMaskBitmap.getWidth() &&
//                        srcBitmap.getHeight() == targetMaskBitmap.getHeight()) {
//
//                    int patchSize = Math.max(sourceMaskBitmap.getWidth(), sourceMaskBitmap.getHeight());
//
//                    // Ensure patch size is valid
//                    if (patchSize > 0) {
//
//                        // Before calling the JNI function
//                        Log.d("BitmapProperties", "Source Bitmap - Width: " + srcBitmap.getWidth() + ", Height: " + srcBitmap.getHeight() + ", Config: " + srcBitmap.getConfig());
//                        Log.d("BitmapProperties", "Source Mask Bitmap - Width: " + sourceMaskBitmap.getWidth() + ", Height: " + sourceMaskBitmap.getHeight() + ", Config: " + sourceMaskBitmap.getConfig());
//                        Log.d("BitmapProperties", "Target Mask Bitmap - Width: " + targetMaskBitmap.getWidth() + ", Height: " + targetMaskBitmap.getHeight() + ", Config: " + targetMaskBitmap.getConfig());
//
//                        // Perform inpainting, passing source and target masks
////                        myInpaint(srcBitmap, sourceMaskBitmap, targetMaskBitmap, patchSize);
//                        mProgressBar.setVisibility(View.GONE);
//                    } else {
//                        // Handle invalid patch size
//                        Log.e("Inpaint", "Invalid patch size");
//                    }
//                } else {
//                    // Handle mask bitmap size mismatch error
//                    Log.e("Inpaint", "Mask bitmap size does not match the input image size");
//                }
//
//                //int srcChannels = getChannelsCount(srcBitmap.getConfig());
//
//                /*// Check for ARGB_8888 format and 4 channels
//                if (srcBitmap.getConfig() == Bitmap.Config.ARGB_8888 && srcChannels == 4) {
//                    mProgressBar.setVisibility(View.VISIBLE);
//
//                    // Check if mask bitmaps match the input image size
//                    if (srcBitmap.getWidth() == sourceMaskBitmap.getWidth() &&
//                            srcBitmap.getHeight() == sourceMaskBitmap.getHeight() &&
//                            srcBitmap.getWidth() == targetMaskBitmap.getWidth() &&
//                            srcBitmap.getHeight() == targetMaskBitmap.getHeight()) {
//
//                        int patchSize = Math.max(sourceMaskBitmap.getWidth(), sourceMaskBitmap.getHeight());
//
//                        // Ensure patch size is valid
//                        if (patchSize > 0) {
//                            // Perform inpainting, passing source and target masks
//                            myInpaint(srcBitmap, sourceMaskBitmap, targetMaskBitmap, patchSize);
//                            mProgressBar.setVisibility(View.GONE);
//                        } else {
//                            // Handle invalid patch size
//                            Log.e("Inpaint", "Invalid patch size");
//                        }
//                    } else {
//                        // Handle mask bitmap size mismatch error
//                        Log.e("Inpaint", "Mask bitmap size does not match the input image size");
//                    }
//                } else {
//                    // Handle error: Image doesn't have the required channels and depth
//                    Log.e("Inpaint", "Image does not meet the required conditions");
//                }*/
//            } else {
//                // Handle error: Null bitmaps
//                Log.e("Inpaint", "Null bitmaps detected");
//            }


            File privateDir = new File(context.getFilesDir(), "RemoveObj");
            if(!privateDir.exists()) {
                privateDir.mkdirs();
            }
            File inpaintFile = new File(privateDir, System.currentTimeMillis() + ".jpeg");


            try {
                Uri sourceURI = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.elephant);
                Uri maskURI = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.elephant_mask);

                InputStream inputStream = context.getContentResolver().openInputStream(sourceURI);
                File sourceFile = new File(privateDir, System.currentTimeMillis() + ".png");
                FileOutputStream outputStream = new FileOutputStream(sourceFile);
                IOUtils.copy(inputStream, outputStream);


                InputStream inputStream2 = context.getContentResolver().openInputStream(maskURI);
                File maskFile = new File(privateDir, System.currentTimeMillis() + ".png");
                FileOutputStream outStream2 = new FileOutputStream(maskFile);
                IOUtils.copy(inputStream2, outStream2);

                long time = System.currentTimeMillis();
                //myInpaint(sourceFile.getPath(), maskFile.getPath(), inpaintFile.getPath(), 3);
                Log.e("Remove_Time", String.valueOf(System.currentTimeMillis() - time));
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if(inpaintFile.exists()){
                    Toast.makeText(context, "DOne", Toast.LENGTH_SHORT).show();
                    Bitmap bmp = BitmapFactory.decodeFile(inpaintFile.getPath());
                    imageView.setImageBitmap(bmp);
                }
            }

        });

        sldSigma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                doBlur();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        /*// Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());*/

        Button flipBtn = binding.flipBtn;
        flipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (srcBitmap != null && dstBitmap != null) {
//                    btnFlip_click(v);
                    myFlip(srcBitmap, dstBitmap);
                    doBlur();
                }
            }
        });

    }

    private void doBlur() {
        // Convert "progress" from 0-100 to 0.1-10
        float sigma = Math.max(0.1F, sldSigma.getProgress() / 10F);
        if (srcBitmap != null && dstBitmap != null) {
            this.myBlur(srcBitmap, dstBitmap, sigma);
        }
    }

    public void btnFlip_click(View view) {
        myFlip(srcBitmap, dstBitmap);
        doBlur();
    }

    public void doInPaint(){

    }

    /*private int getChannelsCount(Bitmap.Config config) {
        // Calculate number of channels based on Bitmap.Config
        switch (config) {
            case ARGB_8888:
                return 4;
            case RGB_565:
                return 3;
            // Add more cases if needed for other formats
            default:
                return -1; // Unknown format
        }
    }*/

    /**
     * A native method that is implemented by the 'opencvnativeinpaint' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
    public native void myFlip(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void myBlur(Bitmap bitmapIn, Bitmap bitmapOut, float sigma);

//    public native void myInpaint(String sourceImg, String maskImg, String inapintImg, int patchSize);

}