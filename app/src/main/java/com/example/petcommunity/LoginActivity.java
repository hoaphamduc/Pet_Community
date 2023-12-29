package com.example.petcommunity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    private Button signup;
    private Button forgotpw;
    private Button login,unhidepw;
    private EditText username,password;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private CheckBox rememberPassword;
    private static final String SHARED_PREFS_NAME = "MySharedPrefs"; // Tên của SharedPreferences
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private String appToken;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        signup = findViewById(R.id.signup);
        rememberPassword = findViewById(R.id.rememberPassword);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        forgotpw = findViewById(R.id.forgotpw);
        unhidepw = findViewById(R.id.unhidepw);
        login = findViewById(R.id.login);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        unhidepw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nhấn vào nút "unhidepw"
                isPasswordVisible = !isPasswordVisible;

                if (isPasswordVisible) {
                    // Hiển thị mật khẩu dưới dạng văn bản thô
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    // Ẩn mật khẩu dưới dạng mật khẩu
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                }
                // Để thấy sự thay đổi, bạn cần cập nhật lại EditText
                password.setSelection(password.getText().length());
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String usernameText = username.getText().toString();
                final String passwordText = password.getText().toString();

                mDatabase.orderByChild("username").equalTo(usernameText).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            boolean passwordMatched = false;
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String savedPassword = userSnapshot.child("password").getValue(String.class);
                                boolean valid = BCrypt.checkpw(passwordText, savedPassword);

                                if (savedPassword != null && valid) {
                                    // Mật khẩu đúng
                                    passwordMatched = true;

                                    if (rememberPassword.isChecked()) {
                                        // Nếu checkbox "Remember Password" được chọn, lưu mật khẩu vào SharedPreferences
                                        editor.putString(KEY_PASSWORD, passwordText);
                                        editor.apply();
                                    } else {
                                        // Nếu checkbox không được chọn, xóa toàn bộ thông tin mật khẩu đã lưu
                                        editor.remove(KEY_PASSWORD);
                                        editor.apply();
                                    }

                                    // Lấy token từ Firebase Messaging
                                    FirebaseMessaging.getInstance().getToken()
                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    if (task.isSuccessful()) {
                                                        // Lưu token vào Firebase Realtime Database
                                                        String token = task.getResult();
                                                        userSnapshot.getRef().child("token").setValue(token);
                                                    } else {
                                                        // Xử lý khi không lấy được token
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(LoginActivity.this, "Password is null", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (passwordMatched) {
                                // Nếu mật khẩu khớp với bất kỳ bản ghi nào, lưu tên người dùng vào SharedPreferences
                                editor.putString(KEY_USERNAME, usernameText);
                                editor.apply();
                                // Đăng nhập thành công, điều hướng đến màn hình khác (ví dụ: NewFeedsActivity)
                                Intent intent = new Intent(LoginActivity.this, NewFeedsActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Mật khẩu không đúng
                                Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Tên người dùng không tồn tại
                            Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi truy vấn Firebase
                        Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // Kiểm tra xem có thông tin tên người dùng và mật khẩu đã được lưu hay không
        if (sharedPreferences.contains(KEY_USERNAME) || sharedPreferences.contains(KEY_PASSWORD)) {
            String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            // Điền thông tin vào EditText tương ứng
            username.setText(savedUsername);
            password.setText(savedPassword);
            rememberPassword.setChecked(true);
        }






        forgotpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void navigateToHome() {
        // Chuyển hướng đến màn hình chính (ví dụ: NewFeedsActivity)
        Intent intent = new Intent(LoginActivity.this, NewFeedsActivity.class);
        startActivity(intent);
        finish();
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
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue(String.class);
                    // Token đã được lấy thành công, sử dụng token cho các chức năng cần thiết
                    if (appToken.equals(token)){
                        navigateToHome();
                    }
                } else {
                    // Người dùng không tồn tại trong database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi truy vấn Firebase
            }
        });
    }

    private boolean isPasswordVisible = false;

}
