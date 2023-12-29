package com.example.petcommunity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // Lấy đường dẫn ảnh từ Intent
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Load ảnh và hiển thị nó với PhotoView
        PhotoView photoView = findViewById(R.id.photoView);
        Picasso.get().load(imageUrl).into(photoView);

        // Lấy tham chiếu đến closeButton
        ImageButton closeButton = findViewById(R.id.closeButton);

        // Xử lý sự kiện khi closeButton được nhấn
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đóng Activity khi closeButton được nhấn
                finish();
            }
        });
    }
}
