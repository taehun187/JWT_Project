package com.jwt.demo.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

// SecurityUtil 클래스는 현재 로그인된 사용자의 사용자 이름을 추출하는 데 사용됩니다. 
// 일반적으로 사용자 정보가 필요하거나, 현재 인증된 사용자 이름을 가져와야 할 때 호출됩니다.
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    // 기본 생성자를 private으로 선언하여 외부에서 객체를 생성하지 못하게 함
    private SecurityUtil() {}

    // 현재 인증된 사용자의 사용자 이름을 Optional로 반환하는 메서드
    public static Optional<String> getCurrentUsername() {
        
        // SecurityContext에서 현재 Authentication 객체를 가져옴
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없는 경우 빈 Optional 반환
        if (authentication == null) {
            logger.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;
        // 인증 객체의 Principal이 UserDetails 타입인 경우 사용자 이름 추출
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            username = springSecurityUser.getUsername();
        } 
        // Principal이 문자열인 경우 바로 사용 가능
        else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        // 사용자 이름을 Optional로 반환 (null일 경우 빈 Optional 반환)
        return Optional.ofNullable(username);
    }
}
