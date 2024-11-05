package com.jwt.demo.util;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "12345"; // 새로 설정할 비밀번호
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("새로운 암호화된 비밀번호: " + encodedPassword);
    }
}
