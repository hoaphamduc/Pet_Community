package com.example.petcommunity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcommunity.Post;
import com.example.petcommunity.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private OnItemClickListener listener;

    // Constructor to initialize the list of posts
    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    // Inner ViewHolder class
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        // Other views for post details
        TextView usernameTextView;
        TextView postContentTextView;
        Button editButton;
        Button deleteButton;

        public PostViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            usernameTextView = itemView.findViewById(R.id.username);
            postContentTextView = itemView.findViewById(R.id.postContentEditText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            // Set click listeners for the edit and delete buttons
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Load the post image using Picasso or your preferred image loading library
        Picasso.get().load(post.getImageUrl()).into(holder.postImageView);

        // Bind other post details to views
        holder.usernameTextView.setText(post.getSavedUsername());
        holder.postContentTextView.setText(post.getStatus());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Set the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Interface for click listeners
    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onItemClick(int position);
    }
}
