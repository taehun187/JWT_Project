package com.jwt.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 이 코드는 단순히 비밀번호를 암호화하는 방법을 보여주는 예제일 뿐이며, 
// 실제 애플리케이션에서는 회원가입 로직에서 이런 암호화 과정을 포함하여 처리합니다.
public class BCryptPasswordEncoderExample {

    public static void main(String[] args) {
        // BCryptPasswordEncoder 객체를 생성하여 encoder 변수에 할당합니다.
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // '12345'라는 문자열을 BCrypt 방식으로 암호화하여 result 변수에 저장합니다.
        String result = encoder.encode("12345");
        
        // 암호화된 결과를 출력합니다.
        System.out.println(result);
    }
}