package com.example.yrmultimediaco.opencvnativeinpaint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;

public class ResultImage extends AppCompatActivity {

    private ImageView backImage, inPaintedImg;
    private Button saveToCameraRoll;
    private Toolbar mToolbar;
    private String resultImagePath;
    TextView pathText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_image);

        backImage = findViewById(R.id.backImage);
        inPaintedImg = findViewById(R.id.resultImage);
        saveToCameraRoll = findViewById(R.id.saveToCameraBtn);
        pathText = findViewById(R.id.pathText);
        mToolbar = findViewById(R.id.resultToolbar);

        setSupportActionBar(mToolbar);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InpaintActivity.class);
                startActivity(intent);
                finish();
            }
        });

        resultImagePath = getIntent().getStringExtra("inpaintedImagePath");

        Glide.with(this)
                .load(resultImagePath)
                .into(inPaintedImg);

        saveToCameraRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultImagePath != null){

                    Bitmap inPaintedBitmap = BitmapFactory.decodeFile(resultImagePath);
                    String imageName = new File(resultImagePath).getName();

                    pathText.setText(imageName);

                    saveImageToGallery(inPaintedBitmap, imageName);

                    Toast.makeText(ResultImage.this, "Image Saved To Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResultImage.this, "No Image To Save", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void saveImageToGallery(Bitmap bitmap, String imageName){

        String savedImagePath = null;

        String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File imageFolder = new File(galleryPath, "Inpainted Images");

        if (!imageFolder.exists()){
            imageFolder.mkdirs();
        }

        File imageFile = createUniqueFile(imageFolder, imageName);
        savedImagePath = imageFile.getAbsolutePath();

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

    }

    private File createUniqueFile(File directory, String baseName){

        int index = 0;
        String fileName = baseName;

        while(new File(directory, fileName).exists()) {
            index ++;
            fileName = "(" + index + ")" + "_" + baseName;
        }
        return new File(directory, fileName);
    }

}