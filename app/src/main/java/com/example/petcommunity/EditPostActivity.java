package com.example.petcommunity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditPostActivity extends AppCompatActivity {

    private EditText editPostContent;
    private ImageView editedPostImageView;
    private Button saveEditedPostButton;
    // Add other views as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Initialize views
        editPostContent = findViewById(R.id.editPostContent);
        editedPostImageView = findViewById(R.id.editedPostImageView);
        saveEditedPostButton = findViewById(R.id.saveEditedPostButton);
        // Initialize other views

        // Retrieve postId from Intent
        String postId = getIntent().getStringExtra("postId");

        // Implement logic to load post details and display in the views
        loadPostDetails(postId);

        // Set click listener for editedPostImageView
        editedPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open FullScreenImageActivity when the image is clicked
                openFullScreenImageActivity(postId);
            }
        });

        // Set click listener for saveEditedPostButton
        saveEditedPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to save the edited post content
                saveEditedPostContent(postId);
                Intent intent = new Intent(EditPostActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadPostDetails(String postId) {

         DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
         postRef.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if (dataSnapshot.exists()) {
                     Post post = dataSnapshot.getValue(Post.class);
                     if (post != null) {
                         // Populate views with post details
                         editPostContent.setText(post.getStatus());
                         Picasso.get().load(post.getImageUrl()).into(editedPostImageView);
                         // Update other views
                     }
                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {
                 // Handle errors if needed
             }
         });
    }
    private void openFullScreenImageActivity(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Open FullScreenImageActivity with the image URL
                        Intent fullScreenIntent = new Intent(EditPostActivity.this, FullScreenImageActivity.class);
                        fullScreenIntent.putExtra("imageUrl", post.getImageUrl());
                        startActivity(fullScreenIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }
    private void saveEditedPostContent(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);

        // Retrieve the new post content from the EditText
        String editedContent = editPostContent.getText().toString();

        // Update the post content in the database
        postRef.child("status").setValue(editedContent);

        // Notify the user that the post has been updated
        Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
    }

}
