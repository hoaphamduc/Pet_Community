package com.example.petcommunity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcommunity.Adapter.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearchUsername;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edtSearchUsername = findViewById(R.id.edtSearchUsername);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerView = findViewById(R.id.recyclerView);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUsers();
            }
        });
    }

    private void searchUsers() {
        String searchUsername = edtSearchUsername.getText().toString().trim();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        if (!TextUtils.isEmpty(searchUsername)) {
            // Tìm kiếm theo trường "username" trong mỗi nút con của "users"
            mDatabase.orderByChild("username")
                    .startAt(searchUsername)
                    .endAt(searchUsername + "\uf8ff")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userList.clear(); // Clear previous results
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String uid = userSnapshot.getKey();
                                    String username = userSnapshot.child("username").getValue(String.class);
                                    User user = new User(uid, username);

                                    userList.add(user);
                                }
                                userAdapter.notifyDataSetChanged();

                                // Thêm mã để chuyển đến ProfileActivity khi bấm vào kết quả tìm kiếm
                                userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int position) {
                                        // Lấy thông tin user từ vị trí trong danh sách
                                        User clickedUser = userList.get(position);

                                        // Chuyển đến ProfileActivity và truyền dữ liệu
                                        Intent intent = new Intent(SearchActivity.this, ProfileFriendActivity.class);
                                        intent.putExtra("username", clickedUser.getUsername());
                                        startActivity(intent);
                                    }
                                });
                            } else {
                                Toast.makeText(SearchActivity.this, "No matching users found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(SearchActivity.this, "Please enter a username to search", Toast.LENGTH_SHORT).show();
        }
    }




}

