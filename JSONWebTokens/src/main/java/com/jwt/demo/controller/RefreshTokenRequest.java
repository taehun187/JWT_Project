package com.jwt.demo.controller;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    private String refreshToken;
    private String deviceInfo; // 디바이스 정보 필드 추가, User-Agent 값과 같은 정보
}

/*클라이언트가 리프레시 토큰을 서버에 보내어 새로운 액세스 토큰을 요청할 때 사용*/
