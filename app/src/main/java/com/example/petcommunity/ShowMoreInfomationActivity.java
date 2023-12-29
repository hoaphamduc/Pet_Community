package com.example.petcommunity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ShowMoreInfomationActivity extends AppCompatActivity {
    private TextView edtDateOfBirth, edtAddress, edtCurrentCity, edtWorkplace;
    private String savedUsername, avatarUrl;
    private TextView tvUsername, gender;
    private ImageView showavt;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private Bitmap rotatedBitmap;
    private Button goBack;

    @Override
    protected void  onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_show_more_infomation);
        edtDateOfBirth = findViewById(R.id.edtDateOfBirth);
        tvUsername = findViewById(R.id.tvUsername);
        edtAddress = findViewById(R.id.edtAddress);
        edtCurrentCity = findViewById(R.id.edtCurrentCity);
        gender = findViewById(R.id.gender);
        edtWorkplace = findViewById(R.id.edtWorkplace);
        goBack = findViewById(R.id.goBack);
        showavt = findViewById(R.id.showavt);
        SharedPreferences sharedPreferences = this.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        savedUsername = sharedPreferences.getString("username", "");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowMoreInfomationActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void loadUserInfo() {
        mDatabase.orderByChild("username").equalTo(savedUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference currentUserDB = mDatabase.child(savedUsername);

                        currentUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Load user information into respective EditText fields
                                    String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                                    String address = dataSnapshot.child("address").getValue(String.class);
                                    String currentCity = dataSnapshot.child("currentCity").getValue(String.class);
                                    String workplace = dataSnapshot.child("workplace").getValue(String.class);
                                    String genderSaved = dataSnapshot.child("gender").getValue(String.class);
                                    avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);

                                    // Set values to EditText fields
                                    edtDateOfBirth.setText(dateOfBirth);
                                    edtAddress.setText(address);
                                    edtCurrentCity.setText(currentCity);
                                    edtWorkplace.setText(workplace);
                                    gender.setText(genderSaved);
                                    tvUsername.setText(savedUsername);

                                    // Load avatar using Picasso or Glide
                                    if (avatarUrl != null && !TextUtils.isEmpty(avatarUrl)) {
                                        // Load avatar using Picasso
                                        Picasso.get()
                                                .load(avatarUrl)
                                                .error(R.drawable.demoavt) // Set a placeholder or error image
                                                .into(showavt);

                                        // Alternatively, you can use Glide
                                        // Glide.with(ChangeInfoActivity.this).load(avatarUrl).error(R.drawable.demoavt).into(showavt);
                                    } else {
                                        // Handle the case where avatarUrl is null or empty
                                        showavt.setImageResource(R.drawable.demoavt); // Set a default avatar image
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
