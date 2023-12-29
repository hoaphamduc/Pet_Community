package com.example.petcommunity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcommunity.Adapter.MessageAdapter;

public class MessageActivity extends AppCompatActivity implements MessageListener {
    private EditText messageEditText;
    private Button sendButton;
    private ListView messageListView;
    private MessageAdapter messageAdapter;
    private MessagingManager messagingManager;
    private String savedUsername, logedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        savedUsername = intent.getStringExtra("username");

        // Khởi tạo các thành phần giao diện
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        // Khởi tạo adapter cho danh sách tin nhắn
        messageAdapter = new MessageAdapter(this);
        messageListView.setAdapter(messageAdapter);

        // Khởi tạo đối tượng MessagingManager
        messagingManager = new MessagingManager(this);

        // Lắng nghe tin nhắn mới
        messagingManager.listenForMessages(this);

        // Xử lý sự kiện khi nhấn nút gửi
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    // Phương thức để gửi tin nhắn
    private void sendMessage() {
        String messageContent = messageEditText.getText().toString().trim();

        // Use the recipient ID retrieved from the intent
        String recipientId = getIntent().getStringExtra("recipientId");

        if (!messageContent.isEmpty()) {
            // Gửi tin nhắn bằng MessagingManager với thông tin người nhận
            messagingManager.sendMessage(messageContent, recipientId);

            // Xóa nội dung trong EditText
            messageEditText.setText("");
        } else {
            Toast.makeText(this, "Nhập nội dung tin nhắn trước khi gửi", Toast.LENGTH_SHORT).show();
        }
    }


    // Xử lý khi nhận được tin nhắn mới
    @Override
    public void onMessageReceived(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Hiển thị tin nhắn mới trên giao diện
                messageAdapter.addMessage(message);
            }
        });
    }
}
