package com.jwt.demo.jwt;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jwt.demo.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

// HttpSecurity 설정에서 JWT를 사용하는 필터를 추가하여, Spring Security가 JWT 기반 인증을 수행할 수 있도록 구성하는 설정 클래스입니다.
@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    
    private final TokenProvider tokenProvider; // JWT 생성 및 검증을 담당하는 TokenProvider 객체
    private final TokenBlacklistService tokenBlacklistService; // 블랙리스트 검증을 위한 서비스 추가

    @Override
    public void configure(HttpSecurity http) {
        // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가합니다.
        http.addFilterBefore(
                new JwtFilter(tokenProvider, tokenBlacklistService), // JwtFilter에 TokenBlacklistService 추가
                UsernamePasswordAuthenticationFilter.class
        );
    }
}
