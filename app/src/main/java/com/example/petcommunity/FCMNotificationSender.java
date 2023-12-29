package com.example.petcommunity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FCMNotificationSender {
    public static void sendNotification(String serverKey, String recipientId, String title, String message) {
        try {
            // Địa chỉ endpoint FCM
            URL url = new URL("https://fcm.googleapis.com/fcm/send");

            // Mở kết nối HTTP
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + serverKey);
            conn.setDoOutput(true);

            // Tạo JSON object để định dạng thông báo
            String jsonInputString = "{"
                    + "\"to\": \"/topics/" + recipientId + "\","
                    + "\"notification\": {"
                    + "\"title\": \"" + title + "\","
                    + "\"body\": \"" + message + "\""
                    + "}"
                    + "}";

            // Gửi dữ liệu JSON đến FCM server
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Đọc kết quả trả về từ FCM server (đọc response)
            try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
                String responseBody = scanner.useDelimiter("\\A").next();
                System.out.println(responseBody);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

