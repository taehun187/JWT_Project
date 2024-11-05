package com.jwt.demo.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jwt.demo.controller.RefreshTokenRequest;
import com.jwt.demo.controller.TokenResponse;
import com.jwt.demo.dto.LoginDto;
import com.jwt.demo.dto.TokenDto;
import com.jwt.demo.entities.RefreshToken;
import com.jwt.demo.jwt.TokenProvider;
import com.jwt.demo.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service // AuthenticationService 클래스는 사용자 인증과 토큰 관리를 위한 서비스
public class AuthenticationService {

    private final TokenProvider tokenProvider; // JWT 토큰 생성 및 검증을 담당하는 TokenProvider
    private final AuthenticationManagerBuilder authenticationManagerBuilder; // Spring Security의 AuthenticationManager

    @Autowired
    private RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰을 관리하는 리포지토리

    /**
     * 사용자가 로그인하면 액세스 토큰과 리프레시 토큰을 생성하여 반환하는 메서드
     * @param loginDto 로그인 정보 (사용자 이름, 비밀번호)
     * @return 액세스 및 리프레시 토큰을 포함한 TokenResponse 객체
     */                        // 로그인 시 토큰 생성 !
    public Optional<TokenResponse> makeTokens(LoginDto loginDto, String deviceInfo) {
        log.info("makeTokens");

        // 사용자의 인증 정보를 생성하여 Spring Security에서 처리
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // 인증을 수행하고 인증 객체를 생성
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.info("username=" + authentication.getName());

        // 인증 정보를 SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 액세스 토큰 생성
        String accessToken = tokenProvider.createToken(authentication, true);

        // 리프레시 토큰 생성 및 저장 (deviceInfo 전달)
        String refreshToken = tokenProvider.createAndPersistRefreshTokenForUser(authentication, deviceInfo);

        // 토큰들을 포함한 TokenResponse 객체를 반환
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        return Optional.ofNullable(tokenResponse);
    }

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰을 발급하는 메서드
     * @param refreshTokenRequest 클라이언트가 보낸 리프레시 토큰
     * @param authentication 현재 사용자의 인증 정보
     * @return 새로운 액세스 토큰을 포함한 TokenDto 객체
     */
    @Transactional         // 리프레시 토큰을 통한 새로운 액세스 토큰 발급 !
    public Optional<TokenDto> makeNewAccessToken(RefreshTokenRequest refreshTokenRequest, Authentication authentication) {
        String refreshTokenValue = refreshTokenRequest.getRefreshToken();
        
        // 리프레시 토큰을 조회하고 만료 상태와 디바이스 정보 일치 여부를 확인
        RefreshToken validRefreshToken = refreshTokenRepository.findById(refreshTokenValue)
                .filter(token -> !token.isTokenExpired()) // 만료되지 않은 토큰만 유효함
                .filter(token -> token.getDeviceInfo().equals(refreshTokenRequest.getDeviceInfo())) // 디바이스 정보 일치 여부 확인
                .orElseThrow(() -> new IllegalStateException("Invalid or expired refresh token"));

        // 기존 리프레시 토큰을 만료 처리
        validRefreshToken.expire();
        refreshTokenRepository.save(validRefreshToken);

        // 새로운 리프레시 토큰을 생성하고 저장
        String newRefreshToken = tokenProvider.createAndPersistRefreshTokenForUser(
                authentication, 
                refreshTokenRequest.getDeviceInfo()
        );

        // 새로운 액세스 토큰 생성
        String accessToken = tokenProvider.createToken(authentication, true);

        return Optional.of(new TokenDto(accessToken, newRefreshToken));
    }

    /**
     * 리프레시 토큰 만료 여부를 확인하는 메서드
     * @param refreshToken 만료 여부를 확인할 리프레시 토큰 객체
     * @return 만료되었는지 여부를 boolean으로 반환
     */         // 리프레시 토큰이 만료되었는지 여부를 확인 !
    public boolean isTokenExpired(RefreshToken refreshToken) {
        return refreshToken.getExpiryDate().isBefore(LocalDateTime.now());
    }
}
