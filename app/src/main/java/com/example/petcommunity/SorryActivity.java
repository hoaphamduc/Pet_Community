package com.example.petcommunity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SorryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sorry);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle button click, for example, navigate back to the homepage
                navigateToHomepage();
            }
        });
    }

    private void navigateToHomepage() {
        // You can replace this with the appropriate code to go back to the homepage
        Intent intent = new Intent(SorryActivity.this, NewFeedsActivity.class);
        startActivity(intent);
        finish();
    }
}
