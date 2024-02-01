package com.example.petcommunity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.petcommunity.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private Button btn;
    private String appToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.myButton);
        checkUserToken();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestNotificationPermission();
        }
    }

    private void checkUserToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        appToken = task.getResult();
                    }
                });
        SharedPreferences sharedPreferences = this.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(savedUsername);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("token")) {
                    String token = dataSnapshot.child("token").getValue(String.class);

                    if (token != null && appToken != null && appToken.equals(token)) {
                        // Token retrieved successfully and matches, start NewFeedsActivity
                        Intent intent = new Intent(MainActivity.this, NewFeedsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle cases where either token or appToken is null
                        // or they do not match
                    }
                } else {
                    // DataSnapshot does not exist or does not have "token" child
                    // Handle as needed, e.g., user not found in the database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi truy vấn Firebase
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationPermissionGranted()) {
                requestNotificationPermission();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean isNotificationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED && areNotificationsEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // TODO: Display an explanation to the user about why you need the permission.
            // After the user sees the explanation, try again to request the permission.
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean areNotificationsEnabled() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with notifications.
            } else {
                // Permission denied, inform the user or handle accordingly.
            }
        }
    }
}
