package com.example.petcommunity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextView usernameTextView;
    private EditText verificationCodeEditText;
    private Button verifyButton, signInButton;
    private String correctCode, enteredusername;
    private TextView timerTextView;
    private Button buttonResend;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần trong layout
        usernameTextView = findViewById(R.id.tvEmail);
        verificationCodeEditText = findViewById(R.id.verifyEdittext);
        verifyButton = findViewById(R.id.verifyButton);
        signInButton = findViewById(R.id.signin);
        timerTextView = findViewById(R.id.timerTextView);
        buttonResend = findViewById(R.id.buttonResend);
        // Start the countdown timer (e.g., in the onCreate method)
        startCountdownTimer();
        buttonResend.setEnabled(false);
        buttonResend.setBackgroundResource(R.drawable.custom_button_3); // Set the disabled background
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        correctCode = intent.getStringExtra("verificationCode");
        enteredusername = intent.getStringExtra("user");
        // Set a click listener for the "Resend" button
        buttonResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resend logic here (generate a new verification code, send email, etc.)
                sendVerificationCode(email);

                // Restart the countdown timer
                startCountdownTimer();
            }
        });
        String notification = "Your verification code has been sent to email: " + email + ". The code will expire in 5 minutes, please check it soon!";
        if (email != null) {
            usernameTextView.setText(notification);
        }

        // Xử lý sự kiện khi người dùng nhấn nút Verify
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý logic kiểm tra mã xác thực
                String enteredCode = verificationCodeEditText.getText().toString().trim();
                if (checkVerificationCode(enteredCode)) {
                    showPasswordInputDialog();
                } else {
                    // Code is invalid or expired
                    Toast.makeText(ResetPasswordActivity.this, "Invalid or expired verification code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút Sign In
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang trang đăng nhập
                Intent signInIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(signInIntent);
                finish();
            }
        });
    }

    private void showPasswordInputDialog() {
        // Tạo AlertDialog để người dùng nhập mật khẩu mới
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter New Password");

        // Sử dụng LayoutInflater để tạo layout cho AlertDialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password_input, null);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmNewPasswordEditText = dialogView.findViewById(R.id.confirmNewPasswordEditText);

        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

                if (newPassword.equals(confirmNewPassword) && isValidPassword(newPassword)) {
                    // Đổi mật khẩu khi 2 mật khẩu khớp nhau và mật khẩu hợp lệ
                    // Kiểm tra xem người dùng có tồn tại trong Firebase Realtime Database không
                    mDatabase.orderByChild("username").equalTo(enteredusername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    // Lấy UID của người dùng từ Firebase Realtime Database (nếu có)
                                    String uid = userSnapshot.getKey();
                                    String newPasswordHashed = PasswordManager.hashPassword(newPassword);
                                    // Cập nhật mật khẩu mới vào Firebase Realtime Database
                                    mDatabase.child(uid).child("password").setValue(newPasswordHashed)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> dbUpdateTask) {
                                                    if (dbUpdateTask.isSuccessful()) {
                                                        // Show a toast message when the password is successfully changed
                                                        Toast.makeText(ResetPasswordActivity.this, "Password changed successfully. Welcome back to Pet Community!!!", Toast.LENGTH_SHORT).show();

                                                        // Chuyển về trang đăng nhập
                                                        Intent signInIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                        startActivity(signInIntent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ResetPasswordActivity.this, "Failed to update password in Firebase Realtime Database", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                // Tên người dùng không tồn tại
                                Toast.makeText(ResetPasswordActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Xử lý lỗi truy vấn Firebase Realtime Database
                            Toast.makeText(ResetPasswordActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Hiển thị thông báo nếu 2 mật khẩu không khớp hoặc mật khẩu không hợp lệ
                    if (!newPassword.equals(confirmNewPassword)) {
                        Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Password must be at least 8 characters long and contain at least one uppercase letter and one special character", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        // Xử lý sự kiện khi người dùng nhấn nút Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }



    private boolean checkVerificationCode(String enteredCode) {
        // Retrieve verification code and timestamp from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("VerificationPrefs", Context.MODE_PRIVATE);
        String savedCode = sharedPreferences.getString("verificationCode", "");
        long savedTimestamp = sharedPreferences.getLong("timestamp", 0);

        // Check if the entered code matches and is within the 5-minute window
        return enteredCode.equals(savedCode) && isWithinFiveMinutes(savedTimestamp);
    }

    private boolean isWithinFiveMinutes(long timestamp) {
        long currentTimeMillis = System.currentTimeMillis();
        long fiveMinutesInMillis = 5 * 60 * 1000; // 5 minutes in milliseconds

        return currentTimeMillis - timestamp <= fiveMinutesInMillis;
    }

    private void startCountdownTimer() {
        // Set the time limit to 5 minutes (300,000 milliseconds)
        timeLeftInMillis = 5 * 60 * 1000;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                // Handle the timer finish event (e.g., disable UI elements)
                // You can add logic here, such as showing a message or enabling the "Resend" button
                timeLeftInMillis = 0;
                updateCountdownText();
                buttonResend.setEnabled(true); // Enable the "Resend" button when the timer finishes
                buttonResend.setBackgroundResource(R.drawable.custom_button); // Set the normal background
            }
        }.start();
    }

    private void updateCountdownText() {
        // Format the remaining time and set it to the timerTextView
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
        if (timeLeftInMillis != 0) {
            // Disable the "Resend" button if the timer has finished
            buttonResend.setEnabled(false);
            buttonResend.setBackgroundResource(R.drawable.custom_button_3); // Set the normal background
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private String generateVerificationCode() {
        // Length of the verification code
        int codeLength = 8;

        // Characters for the verification code
        String characters = "0123456789QWERTYUIOPASDFGHJKLZXCVBNM";

        StringBuilder verificationCode = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < codeLength; i++) {
            // Randomly select a character from the characters string
            char randomChar = characters.charAt(random.nextInt(characters.length()));

            // Add the selected character to the verification code
            verificationCode.append(randomChar);
        }

        return verificationCode.toString();
    }

    private void sendVerificationCode(String email) {
        // Config Gmail SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Gmail account credentials
        final String username = "petcommunityofficial@gmail.com";
        final String password = "jqkpsqsfomlbmout";

        // Session to authenticate with Gmail
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header
            message.setFrom(new InternetAddress(username));

            // Set To: header field of the header
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            // Set Subject: header field
            message.setSubject("Password Reset Code");

            // Generate a random verification code
            String verificationCode = generateVerificationCode();

            // Get the current timestamp in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            // Save the verification code and timestamp to SharedPreferences
            saveVerificationCodeAndTimestamp(verificationCode, currentTimeMillis);

            // Set the actual message
            message.setText("Your verification code is: " + verificationCode);

            // Send the message
            new ResetPasswordActivity.SendMailTask().execute(message, username, password, email, verificationCode);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void saveVerificationCodeAndTimestamp(String verificationCode, long timestamp) {
        SharedPreferences sharedPreferences = getSharedPreferences("VerificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("verificationCode", verificationCode);
        editor.putLong("timestamp", timestamp);
        editor.apply();
    }
    // AsyncTask to send email in the background
    private static class SendMailTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                MimeMessage message = (MimeMessage) params[0];
                String username = (String) params[1];
                String password = (String) params[2];
                String email = (String) params[3];
                String verificationCode = (String) params[4];
                Transport.send(message);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        // Kiểm tra có ít nhất một chữ viết hoa và một ký tự đặc biệt
        String regex = "^(?=.*[A-Z])(?=.*[@#$%^&+=]).*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}