package com.jwt.demo.controller;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;    
}


/*TokenResponse 클래스는 JWT 기반 인증 시스템에서 액세스 토큰과 리프레시 토큰을 
  클라이언트에 응답으로 제공할 때 사용하는 데이터 전송 객체(DTO)*/
 