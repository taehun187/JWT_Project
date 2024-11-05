package com.jwt.demo.jwt;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.jwt.demo.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig 클래스는 Spring Security 설정을 정의합니다.
 * JWT 기반 인증을 적용하고, 세션 설정을 Stateless로 변경하여 API 서버의 요구에 맞는 보안 구성을 제공합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider; // JWT 생성 및 검증을 담당하는 TokenProvider
    private final TokenBlacklistService tokenBlacklistService; // 블랙리스트 검증을 위한 서비스
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 인증 실패 시 처리하는 EntryPoint
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler; // 접근 거부 시 처리하는 핸들러

    /**
     * PasswordEncoder 빈을 생성하여 Spring Security에서 비밀번호 암호화를 처리할 수 있게 합니다.
     *
     * @return BCryptPasswordEncoder를 사용하여 암호화된 비밀번호를 생성하는 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain 빈을 등록하여 HTTP 보안 설정을 정의합니다.
     * CORS와 CSRF를 비활성화하고, JWT 기반 인증을 위해 세션을 사용하지 않도록 설정합니다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception 설정 중 발생하는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 비활성화
        http.cors(cors -> cors.disable());
        
        // CSRF 보호 비활성화
        http.csrf(csrf -> csrf.disable());

        // X-Frame-Options 헤더 비활성화
        http.headers(headers -> headers.frameOptions().disable());

        // 인증 실패 및 접근 거부 처리 설정
        http.exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                     .accessDeniedHandler(jwtAccessDeniedHandler));

        // 세션을 사용하지 않는 Stateless 모드로 설정
        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 접근 제어 설정: 특정 경로는 모든 사용자가 접근 가능하며, 그 외 경로는 인증이 필요
        http.authorizeHttpRequests(c -> c.requestMatchers("/api/login", "/api/refresh-token", "/api/signup", "/favicon.ico").permitAll()
                                          .anyRequest().authenticated());

        // JWT 보안 설정을 적용하여 JwtFilter를 Security Filter Chain에 추가
        http.apply(new JwtSecurityConfig(tokenProvider, tokenBlacklistService));

        return http.build();
    }
}
