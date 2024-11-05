package com.jwt.demo.jwt;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.jwt.demo.entities.RefreshToken;
import com.jwt.demo.repository.RefreshTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;

// JWT 토큰 생성, 리프레시 토큰 저장, 토큰 검증 및 인증정보 추출을 담당하는 클래스입니다.
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth"; // 권한 정보를 담을 클레임 키
    private final String secret; // JWT 생성에 사용될 비밀 키
    private final long accessTokenValidityInMilliseconds; // 액세스 토큰의 유효시간 (밀리초 단위)
    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰의 유효시간 (밀리초 단위)
    private Key key; // 암호화 키
    private final String secretKey = "your-secret-key";

    @Autowired
    private RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장소

    public TokenProvider(
            @Value("${jwt.secret}") String secret, // 비밀 키를 application.yml에서 주입
            @Value("${jwt.token-validity-in-seconds}") long accessTokenValidityInSeconds, // 액세스 토큰 유효시간
            @Value("${jwt.refreshtoken-validity-in-seconds}") long refreshTokenValidityInSeconds) { // 리프레시 토큰 유효시간
        this.secret = secret;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000; // 초를 밀리초로 변환
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000; // 초를 밀리초로 변환
    }

    /**
     * 초기화 작업 수행, secret 값을 Base64로 디코딩하여 key 변수에 설정합니다.
     */
    public void afterPropertiesSet() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes); // HMAC SHA-512 알고리즘으로 키 생성
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid JWT secret key");
        }
    }

    /**
     * JWT 토큰을 생성합니다.
     * @param authentication 인증 객체
     * @param isAccessToken 액세스 토큰인지 여부
     * @return 생성된 JWT 토큰
     */
    public String createToken(Authentication authentication, boolean isAccessToken) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // 권한 이름 추출
                .collect(Collectors.joining(",")); // 권한들을 콤마로 구분하여 문자열로 결합

        long now = (new Date()).getTime(); // 현재 시간
        Date validity = new Date(now + (isAccessToken ? accessTokenValidityInMilliseconds : refreshTokenValidityInMilliseconds));

        return Jwts.builder()
                .setSubject(authentication.getName()) // 사용자 이름을 JWT의 subject로 설정
                .claim(AUTHORITIES_KEY, authorities) // 권한 정보를 클레임에 추가
                .signWith(key, SignatureAlgorithm.HS512) // 암호화 알고리즘과 키 설정
                .setExpiration(validity) // 만료 시간 설정
                .compact(); // 토큰 생성 및 반환
    }

    /**
     * 리프레시 토큰을 생성하고 저장합니다.
     * @param authentication 인증 객체
     * @param deviceInfo 디바이스 정보
     * @return 생성된 리프레시 토큰
     */
    public String createAndPersistRefreshTokenForUser(Authentication authentication, String deviceInfo) {
        String refreshToken = this.createToken(authentication, false); // 리프레시 토큰 생성

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);
        Instant instant = validity.toInstant();
        LocalDateTime expiryDate = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        String username = authentication.getName(); 
        RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, username, expiryDate, false, deviceInfo);
        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
    }

    /**
     * JWT 토큰에서 인증 정보를 추출하여 Authentication 객체를 생성합니다.
     * @param token JWT 토큰
     * @return 인증 정보를 포함한 Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        // 클레임에서 권한 정보를 추출하여 GrantedAuthority 리스트로 변환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(",")) // 권한 문자열을 콤마로 분리
                        .map(SimpleGrantedAuthority::new) // SimpleGrantedAuthority 객체로 변환
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities); // 사용자 정보를 가진 User 객체 생성

        return new UsernamePasswordAuthenticationToken(principal, token, authorities); // 인증 객체 생성하여 반환
    }

    /**
     * JWT 토큰의 유효성을 검증하는 메서드입니다.
     * @param token JWT 토큰
     * @return 유효한 토큰이면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // 토큰을 파싱 및 검증
            return true; // 유효한 토큰일 경우 true 반환
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다."); // 서명이 잘못된 경우 로그 출력
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다."); // 토큰이 만료된 경우 로그 출력
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다."); // 지원하지 않는 형식의 토큰일 경우 로그 출력
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다."); // 잘못된 형식의 토큰일 경우 로그 출력
        }
        return false; // 유효하지 않은 토큰일 경우 false 반환
    }
    
    /**
     * JWT 토큰의 남은 만료 시간을 계산합니다.
     * @param token JWT 토큰
     * @return 남은 만료 시간을 Duration으로 반환
     */
    public Duration getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        long nowMillis = System.currentTimeMillis();
        long expirationMillis = expiration.getTime();

        return Duration.ofMillis(expirationMillis - nowMillis);
    }
}
