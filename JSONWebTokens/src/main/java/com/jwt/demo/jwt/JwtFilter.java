package com.jwt.demo.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.jwt.demo.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtFilter 클래스는 HTTP 요청에서 JWT 토큰을 추출하고 검증하여, 
 * 유효한 토큰일 경우 사용자 인증 정보를 Security Context에 저장합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization"; // HTTP 헤더에서 JWT 토큰을 찾기 위한 키
    private final TokenProvider tokenProvider; // 토큰 생성 및 검증을 위한 객체
    private final TokenBlacklistService tokenBlacklistService; // 블랙리스트에 등록된 토큰을 관리하는 서비스

    /**
     * HTTP 요청이 필터를 통과할 때 호출되는 메서드로, 요청에서 JWT 토큰을 추출하고 검증하여
     * 인증 정보를 Security Context에 설정하는 역할을 수행합니다.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        // 요청에서 JWT 토큰을 추출
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        // 토큰이 없는 경우 다음 필터로 넘어갑니다.
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 토큰이 존재하고, 블랙리스트에 포함되지 않았으며 유효할 경우
        if (StringUtils.hasText(jwt) && !tokenBlacklistService.isBlacklisted(jwt) && tokenProvider.validateToken(jwt)) {
            // 토큰으로부터 인증 정보를 추출하고, Security Context에 설정
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            // 토큰이 유효하지 않거나 블랙리스트에 있는 경우 로그 기록
            log.debug("유효하지 않거나 블랙리스트에 등록된 JWT 토큰입니다, uri: {}", requestURI);
        }

        // 필터 체인에서 다음 필터로 요청을 전달
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출하는 메서드입니다.
     * Bearer 토큰의 경우 "Bearer " 접두어를 제거한 토큰 문자열을 반환합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 또는 null (토큰이 없을 경우)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // "Authorization" 헤더에서 토큰을 가져옴
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 접두어를 제거하고 토큰 값만 반환
        }
        return null; // 토큰이 없거나 올바르지 않은 형식일 경우 null 반환
    }
}
