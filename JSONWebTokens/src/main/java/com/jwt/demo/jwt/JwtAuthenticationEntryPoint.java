package com.jwt.demo.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 인증되지 않은 사용자가 보호된 리소스에 접근할 때, HTTP 상태 코드 401 (Unauthorized)로 응답합니다.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
