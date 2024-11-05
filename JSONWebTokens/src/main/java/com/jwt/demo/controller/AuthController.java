package com.jwt.demo.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jwt.demo.dto.LoginDto;
import com.jwt.demo.dto.TokenDto;
import com.jwt.demo.jwt.JwtFilter;
import com.jwt.demo.jwt.TokenProvider;
import com.jwt.demo.service.AuthenticationService;
import com.jwt.demo.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController // REST 컨트롤러임을 나타내는 애너테이션으로, JSON 응답을 반환합니다.
@RequestMapping("/api") // 이 컨트롤러의 기본 요청 경로를 "/api"로 설정합니다.
@RequiredArgsConstructor // final 필드를 자동으로 생성자 주입하는 Lombok 애너테이션입니다.
public class AuthController {

    private final AuthenticationService authenticationService; // 인증 관련 로직을 담당하는 서비스
    private final TokenProvider tokenProvider; // JWT 토큰 생성 및 인증을 처리하는 TokenProvider 클래스
    private final TokenBlacklistService tokenBlacklistService; // 토큰 블랙리스트 관리 서비스

    // 로그인 엔드포인트
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDto loginDto, HttpServletRequest request) {
        // User-Agent 헤더 정보를 가져와 deviceInfo로 사용합니다.
        String deviceInfo = request.getHeader("User-Agent");

        // 인증 서비스에서 토큰을 생성하고, Optional로 반환됩니다.
        Optional<TokenResponse> optTokenResponse = authenticationService.makeTokens(loginDto, deviceInfo);

        // JWT 토큰을 HTTP 응답 헤더에 추가하기 위해 헤더 객체를 생성합니다.
        HttpHeaders httpHeaders = new HttpHeaders();
        
        // 응답 헤더에 "Authorization" 항목으로 액세스 토큰을 추가합니다.
        optTokenResponse.ifPresent(tokenResponse -> httpHeaders.add(
                JwtFilter.AUTHORIZATION_HEADER, "Bearer " + tokenResponse.getAccessToken()
        ));

        // 토큰 응답을 HTTP 상태 200과 함께 반환합니다.
        return new ResponseEntity<>(optTokenResponse.orElse(null), httpHeaders, HttpStatus.OK);
    }

    // 토큰 갱신 엔드포인트
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest, 
                                          Authentication authentication) {     
        try {
            // Authentication 객체가 null일 경우 새로 생성
            if (authentication == null) {
                String refreshToken = refreshTokenRequest.getRefreshToken();
                
                // TokenProvider를 사용해 리프레시 토큰 유효성을 검증합니다.
                if (tokenProvider.validateToken(refreshToken)) {
                    // 유효한 경우, 토큰으로부터 인증 정보를 가져와 SecurityContext에 설정합니다.
                    authentication = tokenProvider.getAuthentication(refreshToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 유효하지 않으면, 오류 응답을 반환합니다.
                    return ResponseEntity.badRequest().body("Invalid or expired refresh token. Please login again.");
                }
            }

            // 인증 정보가 있는 경우 새로운 액세스 토큰을 발급합니다.
            Optional<TokenDto> tokenDto = authenticationService.makeNewAccessToken(refreshTokenRequest, authentication);
            
            if (tokenDto.isPresent()) {
                // 발급된 새로운 액세스 토큰을 응답으로 반환합니다.
                return ResponseEntity.ok(tokenDto.get());
            } else {
                // 만료된 리프레시 토큰인 경우, 오류 응답을 반환합니다.
                return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
            }            
        } catch (Exception e) {
            // 예외 발생 시, 예외 메시지를 반환합니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        }        
    }

    // 로그아웃 엔드포인트
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7); // "Bearer " 부분 제거
            
            // 토큰의 남은 만료 시간을 계산
            Duration expiration = tokenProvider.getExpiration(jwtToken); 

            // 현재 시간에 expiration(Duration)을 더해서 만료 시간을 계산
            LocalDateTime expirationTime = LocalDateTime.now().plus(expiration);

            // 블랙리스트에 토큰과 계산된 만료 시간(expirationTime)을 전달
            tokenBlacklistService.addToBlacklist(jwtToken, expirationTime);
            
            return ResponseEntity.ok("Successfully logged out.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token.");
        }
    }
}
