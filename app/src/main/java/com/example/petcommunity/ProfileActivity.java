package com.example.petcommunity;

import static com.example.petcommunity.NewFeedsActivity.PICK_IMAGE_REQUEST;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcommunity.Adapter.PostAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private Button home;
    private Button profile;
    private Button search;
    private Button post;
    private Button logout;
    private Button message;
    private Button notification, changeinfo, showmore;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String savedUsername, avatarUrl;
    private Bitmap rotatedBitmap;
    private ImageView demoImage, showavt;
    private Uri selectedImageUri = null;
    private TextView showage, showname, showlocation;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        showavt = findViewById(R.id.showavt);
        home = findViewById(R.id.home);
        profile = findViewById(R.id.profile);
        search = findViewById(R.id.search);
        post = findViewById(R.id.post);
        logout = findViewById(R.id.logout);
        message = findViewById(R.id.message);
        changeinfo = findViewById(R.id.changeinfo);
        notification = findViewById(R.id.notification);
        showage = findViewById(R.id.showage);
        showname = findViewById(R.id.showname);
        showlocation = findViewById(R.id.showlocation);
        showmore = findViewById(R.id.showmore);

        changeinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Lấy username từ trang trước
        SharedPreferences sharedPreferences = this.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        savedUsername = sharedPreferences.getString("username", "");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        showmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ShowMoreInfomationActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // Initialize RecyclerView and adapter
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        // Set the layout manager and adapter for the RecyclerView
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);

        // Load and display posts for the logged-in username
        loadUserPosts(savedUsername);
        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                onEditButtonClick(position);
            }

            @Override
            public void onDeleteClick(int position) {
                onDeleteButtonClick(position);
            }
            @Override
            public void onItemClick(int position) {
                openFullscreenActivity(position);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,NewFeedsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy UID của người dùng hiện tại
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Truy cập vào node Users của Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                // Xóa giá trị token của người dùng
                userRef.child("token").removeValue();

                // Chuyển hướng đến màn hình MainActivity (hoặc màn hình đăng nhập, tùy thuộc vào logic của bạn)
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("New post");

                // Inflate layout cho AlertDialog
                View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
                builder.setView(view);

                // Trường nhập status và nút chọn ảnh
                EditText statusEditText = view.findViewById(R.id.statusEditText);
                Button chooseImageButton = view.findViewById(R.id.chooseImageButton);
                demoImage = view.findViewById(R.id.demoImage);
                chooseImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Mở activity để chọn hình ảnh từ thư viện
                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                    }
                });
                Button rollLeftButton = view.findViewById(R.id.rollleft);
                rollLeftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Gọi phương thức xoay ảnh sang trái
                        demoImage.setRotation(demoImage.getRotation() - 90);
                        demoImage.setDrawingCacheEnabled(true);
                        rotatedBitmap = rotateBitmap(demoImage.getDrawingCache(), -90);
                        demoImage.setDrawingCacheEnabled(false);
                    }
                });

                Button rollRightButton = view.findViewById(R.id.rollright);
                rollRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Gọi phương thức xoay ảnh sang phải
                        demoImage.setRotation(demoImage.getRotation() + 90);
                        demoImage.setDrawingCacheEnabled(true);
                        rotatedBitmap = rotateBitmap(demoImage.getDrawingCache(), 90);
                        demoImage.setDrawingCacheEnabled(false);
                    }
                });

                // Xử lý sự kiện khi nhấn nút "Lưu" trong AlertDialog
                builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String status = statusEditText.getText().toString();

                        // Kiểm tra xem status và ảnh có giá trị hay không
                        if (!TextUtils.isEmpty(status) && rotatedBitmap != null) {
                            // Lưu hình ảnh vào Firebase Storage
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            StorageReference imagesRef = storageRef.child("images");
                            String uniqueImageName = "image_" + System.currentTimeMillis() + ".jpg";
                            StorageReference imageRef = imagesRef.child(uniqueImageName);

                            // Convert Bitmap to byte array
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            // Upload byte array to Firebase Storage
                            UploadTask uploadTask = imageRef.putBytes(data);

                            uploadTask.addOnSuccessListener(taskSnapshot -> {
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    // Lưu status và URL hình ảnh vào Firebase Realtime Database
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");

                                    // Tạo một ID ngẫu nhiên cho bài đăng
                                    String postId = databaseReference.push().getKey();

                                    // Tạo một đối tượng Post để lưu dữ liệu
                                    Post post = new Post(postId, status, imageUrl, savedUsername);

                                    // Đưa dữ liệu vào Firebase
                                    databaseReference.child(postId).setValue(post);

                                    // Đóng AlertDialog
                                    dialog.dismiss();
                                    loadUserPosts(savedUsername);

                                    // Hiển thị thông báo hoặc chuyển đến màn hình chính sau khi đăng bài
                                    Toast.makeText(ProfileActivity.this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                // Xử lý lỗi khi tải lên Firebase Storage.
                                e.printStackTrace();
                                Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Thông báo lỗi nếu status hoặc ảnh trống
                            Toast.makeText(ProfileActivity.this, "Please enter status and select image!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




                // Xử lý sự kiện khi nhấn nút "Hủy" trong AlertDialog
                builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Đóng AlertDialog
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
    private void loadUserPosts(String targetUsername) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // Query posts where savedUsername is equal to the targetUsername
        postsRef.orderByChild("savedUsername").equalTo(targetUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Clear the existing postList
                    postList.clear();

                    // Iterate through the dataSnapshot to get posts
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            postList.add(post);
                        }
                    }
                    // Notify the adapter of the data change
                    postAdapter.notifyDataSetChanged();
                } else {
                    // No posts found for the specified username
                    Toast.makeText(ProfileActivity.this, "No posts found for " + targetUsername, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur during the data fetch
                Toast.makeText(ProfileActivity.this, "Error fetching posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost(String postId) {
        // Implement the logic to delete the post from the database
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        // Retrieve the post data to get the image URL
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Delete the image from Firebase Storage
                        deleteImageFromStorage(post.getImageUrl());

                        // Delete the post data from the database
                        postRef.removeValue();

                        // Notify the user that the post has been deleted
                        Toast.makeText(ProfileActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();

                        // Refresh the posts after deletion
                        loadUserPosts(savedUsername);

                        // Check if there are no more posts, and navigate to NewFeedsActivity if needed
                        if (postList.isEmpty()) {
                            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void deleteImageFromStorage(String imageUrl) {
        // Get an instance of Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from the image URL
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);

        // Delete the image from Firebase Storage
        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully
                // You can add additional actions here if needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors if image deletion fails
                // You can log the error or show a message to the user
                // For example, you can log the error to Logcat
                Log.e("DeleteImage", "Error deleting image: " + e.getMessage());
            }
        });
    }

    private void editPost(String postId) {
        // Implement the logic to navigate to the edit screen with the postId
        // You can use an Intent to pass data to the editing activity
        // For example:
        Intent intent = new Intent(ProfileActivity.this, EditPostActivity.class);
        intent.putExtra("postId", postId);
        startActivity(intent);
    }

    // Add this method to handle edit button click
    public void onEditButtonClick(int position) {
        String postId = postList.get(position).getPostId();
        editPost(postId);
    }

    // Add this method to handle delete button click
    public void onDeleteButtonClick(int position) {
        String postId = postList.get(position).getPostId();
        deletePost(postId);
    }
    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            demoImage.setImageURI(selectedImageUri);

            // Convert the selected image URI to Bitmap and set it to rotatedBitmap
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                rotatedBitmap = originalBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void openFullscreenActivity(int position) {
        // Get the clicked post
        Post post = postList.get(position);

        // Start FullscreenImageActivity and pass the image URL
        Intent intent = new Intent(ProfileActivity.this, FullScreenImageActivity.class);
        intent.putExtra("imageUrl", post.getImageUrl());
        startActivity(intent);
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
                                    String currentCity = dataSnapshot.child("currentCity").getValue(String.class);
                                    avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                                    if (dateOfBirth != null && dateOfBirth.length() > 0) {
                                        // Parse the date
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                        try {
                                            // Tính toán tuổi
                                            String calculateAge = "Age " + calculateAge(dateFormat.parse(dateOfBirth));
                                            showage.setText(calculateAge);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        String calculateAge = "Unknown age";
                                        showage.setText(calculateAge);
                                    }
                                    // Định dạng của chuỗi ngày tháng năm sinh
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    if (currentCity != null){
                                    String location = "Live in " + currentCity;
                                    showlocation.setText(location);
                                    } else {
                                        showlocation.setText("Unknown location");
                                    }

                                    showname.setText(savedUsername);
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
    private static int calculateAge(Date dateOfBirth) {
        Date currentDate = new Date();

        // Lấy năm từ ngày tháng năm sinh và ngày hiện tại
        int birthYear = dateOfBirth.getYear();
        int currentYear = currentDate.getYear();

        // Tính toán tuổi
        int age = currentYear - birthYear;

        // Kiểm tra nếu ngày sinh trong tương lai thì giảm tuổi đi 1
        if (currentDate.before(new Date(currentDate.getYear(), dateOfBirth.getMonth(), dateOfBirth.getDate()))) {
            age--;
        }

        return age;
    }
}


