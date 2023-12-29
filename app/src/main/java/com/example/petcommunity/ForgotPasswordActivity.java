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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Properties;
import java.util.Random;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button signin, buttonFindpw;
    private EditText username, email;
    private String entereduser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize UI elements
        signin = findViewById(R.id.signin);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        buttonFindpw = findViewById(R.id.buttonFindpw);


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the LoginActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonFindpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from EditText
                String enteredUsername = username.getText().toString().trim();
                String enteredEmail = email.getText().toString().trim();
                entereduser = username.getText().toString().trim();

                // Check if all information is entered
                if (enteredUsername.isEmpty() || enteredEmail.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter all fields!!!", Toast.LENGTH_SHORT).show();
                }
                else { checkUsernameAndEmailMatch(enteredUsername, enteredEmail);}
            }
        });
    }
    private void checkUsernameAndEmailMatch(String enteredUsername, String enteredEmail) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabase.orderByChild("username").equalTo(enteredUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean emailMatched = false;
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String savedEmail = userSnapshot.child("email").getValue(String.class);
                        if (savedEmail.equals(enteredEmail)) {
                            // Email matches
                            emailMatched = true;
                            // Call the function to send verification code
                            sendVerificationCode(enteredEmail);
                        }
                    }
                    if (!emailMatched) {
                        // Email doesn't match any records
                        Toast.makeText(ForgotPasswordActivity.this, "Incorrect email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Username not found
                    Toast.makeText(ForgotPasswordActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle Firebase query error
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            new SendMailTask().execute(message, username, password, email, verificationCode);

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

    // AsyncTask to send mail in the background
    private class SendMailTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                // Extract parameters
                MimeMessage message = (MimeMessage) params[0];
                String username = (String) params[1];
                String password = (String) params[2];
                String email = (String) params[3];
                String verificationCode = (String) params[4];

                // Send the message
                Transport.send(message);

                // After sending, move to the ResetPasswordActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("verificationCode", verificationCode);
                intent.putExtra("user", entereduser);

                // Start the ResetPasswordActivity
                startActivity(intent);

            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
