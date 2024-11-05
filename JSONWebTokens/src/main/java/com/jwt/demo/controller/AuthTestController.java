package com.jwt.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class AuthTestController {

    @GetMapping("/check-authentication")
    public String checkAuthentication() {
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName(); // 사용자 이름
            StringBuilder authorities = new StringBuilder();
            
            // 사용자 권한 목록 확인
            authentication.getAuthorities().forEach(authority -> 
                authorities.append(authority.getAuthority()).append(" ")
            );
            
            // 인증된 사용자 정보 반환
            return "Authenticated Username: " + username + ", Authorities: " + authorities.toString();
        } else {
            return "No authenticated user found in SecurityContext";
        }
    }
}
