package com.example.petcommunity;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MessagingManager {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String MESSAGES_NODE = "Messages";

    private DatabaseReference databaseReference;


    public MessagingManager(Context context) {
        // Khởi tạo Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Lấy savedUsername từ SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");

        // Lấy UID của người dùng hiện tại làm một phần của đường dẫn
        String uid = savedUsername;
        databaseReference = firebaseDatabase.getReference(MESSAGES_NODE).child(uid);


    }

    // Phương thức để gửi tin nhắn
    public void sendMessage(String messageContent, String recipientId) {
        // Tạo một ID ngẫu nhiên cho tin nhắn mới
        String messageId = databaseReference.push().getKey();

        // Lấy UID của người gửi
        String senderId = FirebaseAuth.getInstance().getUid();

        // Tạo một đối tượng Message
        Message newMessage = new Message(messageContent, System.currentTimeMillis(), senderId, recipientId);

        // Lưu tin nhắn vào Firebase Realtime Database
        databaseReference.child(messageId).setValue(newMessage);
        sendNotificationToRecipient(recipientId, newMessage);
    }

    private void sendNotificationToRecipient(String recipientId, Message message) {
        // Ở đây, bạn cần thêm logic để gửi thông báo đến người nhận.
        // Điều này có thể sử dụng Firebase Cloud Messaging (FCM) hoặc một dịch vụ thông báo khác.
        // Dưới đây chỉ là một ví dụ giả sử sử dụng FCM.

        // Thay thế "YOUR_FCM_SERVER_KEY" bằng khóa server FCM của bạn
        String serverKey = "YOUR_FCM_SERVER_KEY";
        FCMNotificationSender.sendNotification(serverKey, recipientId, "Bạn có một tin nhắn mới", message.getContent());
    }

    // Phương thức để lắng nghe tin nhắn mới
    public void listenForMessages(MessageListener listener) {
        databaseReference.addChildEventListener(new MessageChildEventListener(listener));
    }
}

