package com.jwt.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jwt.demo.entities.RefreshToken;

/**
 * RefreshTokenRepository는 RefreshToken 엔티티에 대한 데이터베이스 연산을 수행하는 리포지토리입니다.
 * JpaRepository 인터페이스를 상속하여 기본적인 CRUD 연산을 지원합니다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    // JpaRepository를 상속하면 기본 CRUD 메서드가 제공되며, RefreshToken의 ID 타입은 String입니다.
    // 필요에 따라 커스텀 쿼리를 추가하여 리프레시 토큰의 조회 및 삭제 로직을 구현할 수 있습니다.
}
