package com.jwt.demo.entities;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RefreshToken {
    @Id
    private String token;
    private String username;
    private LocalDateTime expiryDate; // 토큰 만료 날짜와 시간
    private boolean isExpired; // 토큰 만료 여부 필드 추가
    private String deviceInfo; // 디바이스 정보 필드 추가

    // 만료 처리 메서드
    public void expire() {
        this.isExpired = true;
    }

    // 만료 여부 확인 메서드
    public boolean isTokenExpired() {
        return this.isExpired || expiryDate.isBefore(LocalDateTime.now());
    }
}
