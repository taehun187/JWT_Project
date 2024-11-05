package com.jwt.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jwt.demo.dto.UserDto;
import com.jwt.demo.entities.User;
import com.jwt.demo.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/api") 
public class UserController {
    
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(
            @Valid @RequestBody UserDto userDto // 유효성 검사와 함께 요청 바디의 UserDto를 받습니다.
    ) {
        // userService의 signup 메서드를 호출하여 새 사용자를 등록하고, 결과를 HTTP 응답으로 반환합니다.
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @GetMapping("/user") 
    @PreAuthorize("hasAnyRole('USER','ADMIN')") // "USER" 또는 "ADMIN" 권한을 가진 사용자만 접근할 수 있습니다.
    public ResponseEntity<User> getMyUserInfo() {
        // 현재 인증된 사용자의 정보를 가져와서 반환합니다.
        return ResponseEntity.ok(userService.getMyUserWithAuthorities().get());
    }

    @GetMapping("/user/{username}") 
    @PreAuthorize("hasAnyRole('ADMIN')") // "ADMIN" 권한을 가진 사용자만 접근할 수 있습니다.
    public ResponseEntity<User> getUserInfo(@PathVariable String username) {
        // 특정 사용자의 정보를 가져와서 반환합니다.
        return ResponseEntity.ok(userService.getUserWithAuthorities(username).get());
    }
}
