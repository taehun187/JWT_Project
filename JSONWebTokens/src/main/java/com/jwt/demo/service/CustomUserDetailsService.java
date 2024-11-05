package com.jwt.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;

import com.jwt.demo.entities.User;
import com.jwt.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("userDetailsService") // Spring Security에서 사용할 사용자 서비스로 등록 // 
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class CustomUserDetailsService implements UserDetailsService {
    // CustomUserDetailsService 클래스는 Spring Security의 사용자 인증을 지원하기 위한 서비스로, 
	// UserDetailsService 인터페이스를 구현하여 사용자 정보를 데이터베이스에서 조회하고 인증을 수행하는 역할을 합니다. 
	// 이 클래스는 특히 JWT 기반 인증 시스템에서 사용자 정보를 조회하고 인증 객체를 반환하는 데 사용됩니다.
	
    private final UserRepository userRepository; // 사용자 정보 조회를 위한 리포지토리
  
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화에 사용하는 인코더

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        // 주어진 사용자 이름으로 데이터베이스에서 사용자 정보를 조회하여 인증을 수행하는 메서드
        log.info("+loadUserByname");
        
        // 데이터베이스에서 사용자 정보 조회 및 UserDetails 객체 생성
        UserDetails userDetails = userRepository.findOneWithAuthoritiesByUsername(username)
                .map(user -> createUser(username, user)) // 사용자를 찾으면 createUser 메서드 호출
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.")); // 사용자가 없으면 예외 발생
        log.info("-loadUserByname");
        return userDetails; // UserDetails 객체 반환
    }

    private org.springframework.security.core.userdetails.User createUser(String username, User user) {
        // 사용자 객체로부터 UserDetails 객체를 생성하는 메서드
        if (!user.isActivated()) { // 사용자가 활성화되지 않았으면 예외 발생
            throw new RuntimeException(username + " -> 활성화되어 있지 않습니다.");
        }
        
        log.info("createUser: username=" + username);

        // 사용자 권한 목록을 SimpleGrantedAuthority 객체로 변환
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName())) // 권한 이름을 기반으로 SimpleGrantedAuthority 생성
                .collect(Collectors.toList());
        
        // UserDetails 객체 생성 및 반환
        org.springframework.security.core.userdetails.User uds = 
        		(org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.
        		User.withUsername(username) // 사용자 이름 설정
        		    .password(user.getPassword()) // 비밀번호 설정
        		    .authorities(grantedAuthorities) // 권한 설정
        		    .build();
        
        log.info("-createUser");
        return uds; // 생성된 UserDetails 객체 반환
    }
}
