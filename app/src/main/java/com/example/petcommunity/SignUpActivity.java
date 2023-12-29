package com.example.petcommunity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SignUpActivity extends AppCompatActivity {
    private Button signin, signup, smallButton;
    private EditText username, password, repassword, email, verifyEmail;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        signup = findViewById(R.id.signup);
        signin = findViewById(R.id.signin);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        repassword = findViewById(R.id.repassword);
        email = findViewById(R.id.email);
        smallButton = findViewById(R.id.smallButton);
        verifyEmail = findViewById(R.id.verifyEmail);

        smallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailText = email.getText().toString();
                sendVerificationCode(emailText);
                Toast.makeText(SignUpActivity.this, "The confirmation code has been sent to your email. Please check your email within 5 minutes!!!", Toast.LENGTH_LONG).show();
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String usernameText = username.getText().toString();
                final String emailText = email.getText().toString();
                final String passwordText = password.getText().toString();
                final String veryfyMail = verifyEmail.getText().toString();

                String repasswordText = repassword.getText().toString();
                if (!passwordText.equals(repasswordText)) {
                    // Xử lý lỗi xác nhận mật khẩu không khớp ở đây (ví dụ: hiển thị thông báo)
                    Toast.makeText(SignUpActivity.this, "Password does not match!", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword(passwordText)) {
                    // Xử lý lỗi mật khẩu không đủ mạnh ở đây (ví dụ: hiển thị thông báo)
                    Toast.makeText(SignUpActivity.this, "Password must have at least 1 capital letter and 1 special character!", Toast.LENGTH_SHORT).show();
                } else {
                    // Kiểm tra tên người dùng đã tồn tại
                    mDatabase.orderByChild("username").equalTo(usernameText).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usernameDataSnapshot) {
                            if (usernameDataSnapshot.exists()) {
                                // Tên người dùng đã tồn tại
                                // Xử lý lỗi (ví dụ: hiển thị thông báo)
                                Toast.makeText(SignUpActivity.this, "Username already exists!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Tên người dùng chưa tồn tại, tiến hành kiểm tra email
                                checkEmailExist(emailText, new EmailCheckCallback() {
                                    @Override
                                    public void onEmailCheckResult(boolean emailExists) {
                                        if (emailExists) {
                                            // Email đã tồn tại
                                            // Xử lý lỗi (ví dụ: hiển thị thông báo)
                                            Toast.makeText(SignUpActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Tiến hành mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
                                            int WORK_FACTOR = 12;
                                            String salt = BCrypt.gensalt(WORK_FACTOR);
                                            String hash = BCrypt.hashpw(passwordText, salt);
                                            // Cả tên người dùng và email đều chưa tồn tại, tiến hành kiểm tra mã gửi về mail
//                                            String userId = mDatabase.push().getKey(); // Tạo một ID ngẫu nhiên cho người dùng
                                            if (checkVerificationCode(veryfyMail)) {
                                            DatabaseReference currentUserDB = mDatabase.child(usernameText);
                                            currentUserDB.child("username").setValue(usernameText);
                                            currentUserDB.child("password").setValue(hash);
                                            currentUserDB.child("email").setValue(emailText);
                                            // Điều hướng đến màn hình khác (ví dụ: trang chính)
                                            Toast.makeText(SignUpActivity.this, "Registered successfully. Please log in!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                            }
                                            else {
                                                Toast.makeText(SignUpActivity.this,"Confirmation code is incorrect", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Xử lý lỗi truy vấn Firebase cho tên người dùng ở đây (ví dụ: hiển thị thông báo)
                            Toast.makeText(SignUpActivity.this, "Error checking username existence: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    // Hàm kiểm tra mật khẩu có ít nhất 8 ký tự, bao gồm ít nhất một chữ viết hoa và một ký tự đặc biệt
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
    public interface EmailCheckCallback {
        void onEmailCheckResult(boolean emailExists);
    }
    private void checkEmailExist(final String email, final EmailCheckCallback callback) {
        mDatabase.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean emailExists = dataSnapshot.exists();
                callback.onEmailCheckResult(emailExists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi truy vấn Firebase cho email ở đây (ví dụ: hiển thị thông báo)
                callback.onEmailCheckResult(false); // Mặc định là email không tồn tại nếu có lỗi
            }
        });
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
            message.setSubject("Verification Code");

            // Generate a random verification code
            String verificationCode = generateVerificationCode();

            // Get the current timestamp in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            // Save the verification code and timestamp to SharedPreferences
            saveVerificationCodeAndTimestamp(verificationCode, currentTimeMillis);

            // Set the actual message
            message.setText("Your verification code is: " + verificationCode);

            // Send the message
            new SignUpActivity.SendMailTask().execute(message, username, password, email, verificationCode);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
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
}