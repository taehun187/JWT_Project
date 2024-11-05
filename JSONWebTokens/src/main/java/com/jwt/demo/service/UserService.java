package com.jwt.demo.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jwt.demo.dto.UserDto;
import com.jwt.demo.entities.Authority;
import com.jwt.demo.entities.User;
import com.jwt.demo.repository.UserRepository;
import com.jwt.demo.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class UserService {
    
	// UserService 클래스는 사용자의 회원가입과 사용자 정보를 관리하는 서비스 클래스입니다. 
	// 주로 회원가입 처리, 특정 사용자 정보 조회, 현재 로그인한 사용자 정보 조회 기능을 제공
	
    private final UserRepository userRepository; // 사용자 정보를 저장 및 조회하는 리포지토리
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화에 사용하는 인코더

    // 회원가입 메서드
    @Transactional
    public User signup(UserDto userDto) {
        // 이미 가입된 유저인지 확인
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다."); // 가입된 유저가 있으면 예외 발생
        }

        // 권한 정보 생성 (ROLE_USER)
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER") // 기본 권한을 ROLE_USER로 설정
                .build();

        // 새로운 유저 정보 생성 및 암호화된 비밀번호 저장
        User user = User.builder()
                .username(userDto.getUsername()) // 사용자 이름 설정
                .password(passwordEncoder.encode(userDto.getPassword())) // 비밀번호 암호화 후 설정
                .nickname(userDto.getNickname()) // 사용자 닉네임 설정
                .authorities(Collections.singleton(authority)) // 권한 설정
                .activated(true) // 사용자 활성화 상태 설정
                .build();

        return userRepository.save(user); // 생성된 유저를 저장하고 반환
    }

    // 특정 사용자 이름을 가진 사용자와 권한 정보 조회 메서드
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username); // 사용자 이름으로 사용자 및 권한 조회
    }

    // 현재 로그인한 사용자의 사용자 정보 및 권한 조회 메서드
    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities() {
        return SecurityUtil.getCurrentUsername() // 현재 인증된 사용자 이름 가져오기
                .flatMap(userRepository::findOneWithAuthoritiesByUsername); // 사용자 이름으로 사용자 정보와 권한 조회
    }
}
