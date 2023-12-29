package com.example.petcommunity;

import static com.example.petcommunity.NewFeedsActivity.PICK_IMAGE_REQUEST;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ChangeInfoActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;

    private EditText edtDateOfBirth, edtAddress, edtCurrentCity, edtWorkplace;
    private Calendar calendar;
    private String savedUsername, avatarUrl;
    private TextView tvEmail, tvUsername;
    private Spinner spinnerGender;
    private Button btnSaveInfo, btnChooserNewAvt, logout, notification, message;
    private ImageView showavt, demoImage;
    private Uri selectedImageUri = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeinfo);
        logout = findViewById(R.id.logout);
        notification = findViewById(R.id.notification);
        message = findViewById(R.id.message);
        edtDateOfBirth = findViewById(R.id.edtDateOfBirth);
        calendar = Calendar.getInstance();
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        edtAddress = findViewById(R.id.edtAddress);
        edtCurrentCity = findViewById(R.id.edtCurrentCity);
        spinnerGender = findViewById(R.id.spinnerGender);
        edtWorkplace = findViewById(R.id.edtWorkplace);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        showavt = findViewById(R.id.showavt);
        btnChooserNewAvt = findViewById(R.id.btnChooserNewAvt);

        // Lấy username từ trang trước
        SharedPreferences sharedPreferences = this.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        savedUsername = sharedPreferences.getString("username", "");
        tvUsername.setText(savedUsername);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        // Lấy mảng string từ resources
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);

        // Tạo adapter và thiết lập cho Spinner
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeInfoActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeInfoActivity.this, SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeInfoActivity.this, SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });



        // Chọn ảnh làm avt
        btnChooserNewAvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Được gọi khi một mục được chọn
                String selectedGender = parentView.getItemAtPosition(position).toString();
                // Làm gì đó với thông tin đã chọn, ví dụ: hiển thị trong Log
                Log.d("SelectedGender", selectedGender);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Được gọi khi không có mục nào được chọn
            }
        });

        // Ngày mặc định khi mở DatePickerDialog
        updateEditText();

        edtDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
                Intent intent = new Intent(ChangeInfoActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Lưu ngày được chọn và cập nhật EditText
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateEditText();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateEditText() {
        // Format ngày theo định dạng mong muốn (ví dụ: "dd/MM/yyyy")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDateOfBirth.setText(dateFormat.format(calendar.getTime()));
    }
    private void saveUserInfo() {
        // Lấy thông tin từ các EditText và Spinner
        String username = savedUsername;
        String dateOfBirth = edtDateOfBirth.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String currentCity = edtCurrentCity.getText().toString().trim();
        String workplace = edtWorkplace.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Lưu thông tin người dùng vào Firebase Realtime Database
        mDatabase.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Lấy UID của người dùng từ Firebase Realtime Database (nếu có)
                        String uid = userSnapshot.getKey();
                        DatabaseReference currentUserDB = mDatabase.child(uid);
                        currentUserDB.child("dateOfBirth").setValue(dateOfBirth);
                        currentUserDB.child("address").setValue(address);
                        currentUserDB.child("currentCity").setValue(currentCity);
                        currentUserDB.child("workplace").setValue(workplace);
                        currentUserDB.child("gender").setValue(gender);
//                        currentUserDB.child("avatarUrl").setValue(avatarUrl);
                        Toast.makeText(ChangeInfoActivity.this, "Information changed successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi truy vấn Firebase Realtime Database bị hủy bỏ
                Toast.makeText(ChangeInfoActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the image URI from the intent
            imageUri = data.getData();
            // Set the image URI to the ImageView
            showavt.setImageURI(imageUri);
            // Gọi hàm để cắt ảnh thành hình vuông và hiển thị (optional)
            cropAndDisplayImage();
        }
    }



    private void cropAndDisplayImage() {
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .override(200, 200) // Kích thước vuông mong muốn (300x300)
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Hiển thị ảnh đã cắt
                        showavt.setImageBitmap(resource);

                        // Gọi hàm để lưu ảnh vào Firebase Storage và nhận URL
                        uploadImage();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Code này được gọi khi ảnh được xóa khỏi target
                    }
                });
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("avatars/" + savedUsername);

            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .override(200, 200)
                    .centerCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            storageReference.putBytes(data)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Upload successful, get the download URL
                                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString();
                                            updateAvatarUrl(imageUrl);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure during image upload
                                        Toast.makeText(ChangeInfoActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                    });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Code executed when the image is removed from the target
                        }
                    });
        }
    }

    private void updateAvatarUrl(String imageUrl) {
        mDatabase.orderByChild(savedUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference currentUserDB = mDatabase.child(savedUsername).child("avatarUrl");
                        currentUserDB.setValue(imageUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Handle successful update
                                    } else {
                                        // Handle unsuccessful update
                                        Toast.makeText(ChangeInfoActivity.this, "Failed to update avatar URL.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancellation of the database query
                Toast.makeText(ChangeInfoActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void loadUserInfo() {
        mDatabase.orderByChild("username").equalTo(savedUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String uid = userSnapshot.getKey();
                        DatabaseReference currentUserDB = mDatabase.child(uid);

                        currentUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Load user information into respective EditText fields
                                    String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                                    String address = dataSnapshot.child("address").getValue(String.class);
                                    String currentCity = dataSnapshot.child("currentCity").getValue(String.class);
                                    String workplace = dataSnapshot.child("workplace").getValue(String.class);
                                    String gender = dataSnapshot.child("gender").getValue(String.class);
                                    avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                                    String email = dataSnapshot.child("email").getValue(String.class);

                                    // Set values to EditText fields
                                    edtDateOfBirth.setText(dateOfBirth);
                                    edtAddress.setText(address);
                                    edtCurrentCity.setText(currentCity);
                                    edtWorkplace.setText(workplace);
                                    tvEmail.setText(email);

                                    // Set the selected item in the spinner
                                    if (gender != null && !TextUtils.isEmpty(gender)) {
                                        int spinnerPosition = adapter.getPosition(gender);
                                        spinnerGender.setSelection(spinnerPosition);
                                    }

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
