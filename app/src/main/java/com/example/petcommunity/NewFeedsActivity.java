package com.example.petcommunity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.example.petcommunity.Adapter.CommentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NewFeedsActivity extends AppCompatActivity {
    private Button profile, home, notification, message, post, search, logout;
    public static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private ImageView demoImage,postImageView,likeButton;
    private TextView username,postContentEditText,numbercomment,numberlike, tvStart;
    private Button commentButton,shareButton;
    private FirebaseDatabase database;
    private ArrayList<String> commentList = new ArrayList<>();
    private String randomPostId = "666";
    private CommentAdapter commentAdapter;
    private Bitmap rotatedBitmap;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfeeds);
        likeButton = findViewById(R.id.likeButton);
        commentButton = findViewById(R.id.commentButton);
        shareButton = findViewById(R.id.shareButton);
        numbercomment = findViewById(R.id.numbercomment);
        numberlike = findViewById(R.id.numberlike);
        postImageView = findViewById(R.id.postImageView);
        username = findViewById(R.id.username);
        postContentEditText = findViewById(R.id.postContentEditText);
        profile = findViewById(R.id.profile);
        home = findViewById(R.id.home);
        notification = findViewById(R.id.notification);
        message = findViewById(R.id.message);
        post = findViewById(R.id.post);
        search = findViewById(R.id.search);
        logout = findViewById(R.id.logout);
        tvStart = findViewById(R.id.tvStart);
        database = FirebaseDatabase.getInstance();

        SharedPreferences sharedPreferences = this.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(savedUsername);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear(); // Xóa danh sách comment hiện tại
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    String comment = commentSnapshot.getValue(String.class);
                    commentList.add(comment); // Thêm comment vào danh sách
                }
                // Đếm số lượng item trong danh sách comments
                int commentCount = commentList.size();
                // Hiển thị số lượng comment trong numbercomment TextView
                numbercomment.setText(String.valueOf(commentCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });


        // 1. Thêm sự kiện cho nút hoặc chức năng "Hiển thị Bài Đăng Ngẫu Nhiên"
        Button showRandomPostButton = findViewById(R.id.showRandomPostButton);
        showRandomPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStart.setVisibility(View.GONE);

                // 2. Truy vấn một bài đăng ngẫu nhiên từ Firebase Realtime Database
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

                // Sử dụng ValueEventListener để lấy dữ liệu
                postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Kiểm tra xem có bài đăng nào hay không
                        if (dataSnapshot.exists()) {
                            // Lấy danh sách các bài đăng
                            List<Post> postList = new ArrayList<>();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Post post = postSnapshot.getValue(Post.class);
                                postList.add(post);
                            }

                            // Chọn một bài đăng ngẫu nhiên từ danh sách
                            int randomIndex = new Random().nextInt(postList.size());
                            Post randomPost = postList.get(randomIndex);

                            // 3. Hiển thị thông tin của bài đăng trong giao diện của bạn
                            Picasso.get()
                                    .load(randomPost.getImageUrl()) // Replace with the correct URL
                                    .error(R.drawable.demoavt) // Set a placeholder or error image
                                    .into(postImageView);
                            username.setText(randomPost.getSavedUsername());
                            postContentEditText.setText(randomPost.getStatus());
                            // Tham chiếu đến bài đăng cụ thể trong cơ sở dữ liệu (postId là ID của bài đăng)
                            String postId = randomPost.getPostId(); // Lấy postId của bài đăng ngẫu nhiên
                            randomPostId = randomPost.getPostId();
                            DatabaseReference postRef = database.getReference("Posts").child(postId);


                            // Đọc giá trị likesCount từ cơ sở dữ liệu
                            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Post post = dataSnapshot.getValue(Post.class);
                                        if (post != null && post.getLikesMap() != null) {
                                            int likesCount = post.getLikesMap().size();
                                            // Sử dụng likesCount ở đây để hiển thị số lượt thích
                                            numberlike.setText(String.valueOf(likesCount));
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Xử lý lỗi nếu có
                                }
                            });
                            String userId = savedUsername; // Lấy ID của người dùng hiện đang đăng nhập

                            // Kiểm tra xem người dùng đã "like" bài đăng chưa
                            if (randomPost != null && randomPost.getLikesMap() != null && randomPost.getLikesMap().containsKey(userId) && randomPost.getLikesMap().get(userId)) {
                                // Người dùng đã "like" bài đăng, hiển thị hình ảnh "like" đã thích
                                likeButton.setImageResource(R.drawable.baseline_favorite_24);
                            } else {
                                // Người dùng chưa "like" bài đăng, hiển thị hình ảnh "like" chưa thích
                                likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                            }
                            postImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Mở màn hình xem ảnh toàn màn hình khi bấm vào postImageView
                                    Intent intent = new Intent(NewFeedsActivity.this, FullScreenImageActivity.class);
                                    intent.putExtra("imageUrl", randomPost.getImageUrl());
                                    startActivity(intent);
                                }
                            });
                            likeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String userId = savedUsername;
                                    AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.anim_scale);
                                    animatorSet.setTarget(likeButton);
                                    animatorSet.start();
                                    // Đảm bảo bài đăng có trường likesMap
                                    if (randomPost.getLikesMap() == null) {
                                        randomPost.setLikesMap(new HashMap<>());
                                    }

                                    // Kiểm tra xem người dùng đã "like" bài đăng chưa
                                    if (randomPost.getLikesMap().containsKey(userId)) {
                                        // Người dùng đã "like" bài đăng
                                        // Có thể thực hiện xử lý "dislike" tại đây
                                        randomPost.getLikesMap().remove(userId);
                                        likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
                                    } else {
                                        // Người dùng chưa "like" bài đăng
                                        // Có thể thực hiện xử lý "like" tại đây
                                        randomPost.getLikesMap().put(userId, true);
                                        likeButton.setImageResource(R.drawable.baseline_favorite_24);
                                    }

                                    // Cập nhật danh sách "likes" vào cơ sở dữ liệu
                                    postRef.child("likesMap").setValue(randomPost.getLikesMap());

                                    // Cập nhật số lượt "like" hiển thị
                                    long likesCount = randomPost.getLikesMap().size();
                                    numberlike.setText(String.valueOf(likesCount));
                                }
                            });
                            DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
                            commentsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    commentList.clear(); // Xóa danh sách comment hiện tại
                                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                                        String comment = commentSnapshot.getValue(String.class);
                                        commentList.add(comment); // Thêm comment vào danh sách
                                    }
                                    // Đếm số lượng item trong danh sách comments
                                    int commentCount = commentList.size();
                                    // Hiển thị số lượng comment trong numbercomment TextView
                                    numbercomment.setText(String.valueOf(commentCount));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Xử lý lỗi nếu có
                                }
                            });



                        } else {
                            // Không tìm thấy bài đăng nào
                            Toast.makeText(NewFeedsActivity.this, "No posts found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                        Toast.makeText(NewFeedsActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                numberlike.setVisibility(View.VISIBLE);
                likeButton.setVisibility(View.VISIBLE);
                numbercomment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);
            }

        });


        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFeedsActivity.this, NewFeedsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFeedsActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFeedsActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(NewFeedsActivity.this);
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

                                    // Hiển thị thông báo hoặc chuyển đến màn hình chính sau khi đăng bài
                                    Toast.makeText(NewFeedsActivity.this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                // Xử lý lỗi khi tải lên Firebase Storage.
                                e.printStackTrace();
                                Toast.makeText(NewFeedsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Thông báo lỗi nếu status hoặc ảnh trống
                            Toast.makeText(NewFeedsActivity.this, "Please enter status and select image!", Toast.LENGTH_SHORT).show();
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

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewFeedsActivity.this);
                builder.setTitle("Comments");

                // Inflate layout cho AlertDialog
                View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout_2, null);
                builder.setView(view);

                // Khởi tạo RecyclerView
                RecyclerView commentRecyclerView = view.findViewById(R.id.commentRecyclerView);
                commentRecyclerView.setLayoutManager(new LinearLayoutManager(NewFeedsActivity.this));
                commentAdapter = new CommentAdapter(commentList);
                commentRecyclerView.setAdapter(commentAdapter);

                // Thêm một EditText để nhập comment
                EditText commentEditText = view.findViewById(R.id.commentEditText);

                // Thêm nút "Add Comment" để thêm comment mới
                Button addCommentButton = view.findViewById(R.id.addCommentButton);
                addCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newComment = commentEditText.getText().toString();
                        if (!TextUtils.isEmpty(newComment)) {
                            String userId = savedUsername; // Lấy username của người đăng nhập hiện tại

                            DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(randomPostId);
                            String commentId = commentsRef.push().getKey(); // Tạo một ID ngẫu nhiên cho comment
                            commentsRef.child(commentId).setValue(userId + ": " + newComment);

                            commentEditText.setText(""); // Xóa nội dung EditText sau khi thêm comment
                        }
                    }
                });



                // Tải danh sách comment từ Firebase Realtime Database
                String postId = randomPostId; // Thay thế bằng ID của bài đăng được chọn
                DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
                commentsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        commentList.clear(); // Xóa danh sách comment hiện tại
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            String comment = commentSnapshot.getValue(String.class);
                            commentList.add(comment); // Thêm comment vào danh sách
                        }
                        // Đếm số lượng item trong danh sách comments
                        int commentCount = commentList.size();
                        // Hiển thị số lượng comment trong numbercomment TextView
                        numbercomment.setText(String.valueOf(commentCount));
                        commentAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView để hiển thị danh sách comment mới
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                    }
                });


                // Hiển thị danh sách comment
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });



        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy UID của người dùng hiện tại
                String uid = savedUsername;

                // Truy cập vào node Users của Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                // Xóa giá trị token của người dùng
                userRef.child("token").removeValue();

                // Chuyển hướng đến màn hình MainActivity (hoặc màn hình đăng nhập, tùy thuộc vào logic của bạn)
                Intent intent = new Intent(NewFeedsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFeedsActivity.this, SorryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFeedsActivity.this, NotificationActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
    private Bitmap rotateBitmap(Bitmap originalBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }
}