package com.example.petcommunity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

public class ImageEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        imageView = findViewById(R.id.imageView);
        Button btnRotate = findViewById(R.id.btnRotate);
        Button btnCrop = findViewById(R.id.btnCrop);
        Button btnUpload = findViewById(R.id.btnUpload);

        btnRotate.setOnClickListener(v -> rotateImage());
        btnCrop.setOnClickListener(v -> cropImage());
        btnUpload.setOnClickListener(v -> uploadImage());

        // Open the gallery to choose an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void rotateImage() {
        if (imageUri != null) {
            UCrop.Options options = new UCrop.Options();
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
            options.setToolbarTitle("Rotate");

            // Set the desired rotation angle (in degrees)
            options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));

            UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "rotated_image.jpg")))
                    .withOptions(options)
                    .start(this);
        }
    }


    private void cropImage() {
        // Crop the image using Android Image Cropper library
        if (imageUri != null) {
            UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg")))
                    .withAspectRatio(16, 9)
                    .start(this);
        }
    }

    private void uploadImage() {
        // Implement image upload logic here
        // You can use the rotated or cropped image URI
        if (imageUri != null) {
            // TODO: Implement image upload logic
            Toast.makeText(this, "Image upload logic goes here", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                // Original image selected from the gallery
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    loadImage(imageUri);
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                // Image cropped
                final Uri croppedUri = UCrop.getOutput(data);
                if (croppedUri != null) {
                    imageUri = croppedUri;
                    loadImage(croppedUri);
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            // Handle cropping error
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .into(imageView);
    }
}

