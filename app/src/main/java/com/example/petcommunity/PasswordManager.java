package com.example.petcommunity;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {

    // Hàm mã hóa mật khẩu
    public static String hashPassword(String passwordText) {
        int WORK_FACTOR = 12;
        String salt = BCrypt.gensalt(WORK_FACTOR);
        String hash = BCrypt.hashpw(passwordText, salt);
        return hash;
    }

    // Hàm kiểm tra mật khẩu
    public static boolean checkPassword(String passwordText, String savedPassword) {
        boolean valid = BCrypt.checkpw(passwordText, savedPassword);
        return valid;
    }

}