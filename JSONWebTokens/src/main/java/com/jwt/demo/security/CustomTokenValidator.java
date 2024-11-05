package com.jwt.demo.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.jwt.demo.jwt.TokenProvider;
import com.jwt.demo.service.TokenBlacklistService;

/**
 * CustomTokenValidator는 토큰의 유효성을 검사하고,
 * 블랙리스트에 등록된 토큰인지 확인하는 역할을 합니다.
 */
@Component
public class CustomTokenValidator {

    private final TokenProvider tokenProvider; // JWT 토큰 생성 및 유효성 검증을 담당하는 TokenProvider
    private final TokenBlacklistService tokenBlacklistService; // 블랙리스트 관리 서비스를 통해 토큰이 블랙리스트에 있는지 확인

    // 생성자 주입 방식으로 TokenProvider와 TokenBlacklistService를 주입받습니다.
    public CustomTokenValidator(TokenProvider tokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * 주어진 토큰이 유효한지와 블랙리스트에 등록되어 있는지를 검사합니다.
     * @param token 검사할 토큰
     */
    public void validateToken(String token) {
        // 블랙리스트에 있는지 확인
        if (tokenBlacklistService.isBlacklisted(token)) {
            // 블랙리스트에 있으면 예외를 던집니다.
            throw new BadCredentialsException("This token is blacklisted.");
        }

        // 토큰 유효성 검증
        if (!tokenProvider.validateToken(token)) {
            // 유효하지 않거나 만료된 토큰일 경우 예외를 던집니다.
            throw new BadCredentialsException("Invalid or expired token.");
        }
    }
}
