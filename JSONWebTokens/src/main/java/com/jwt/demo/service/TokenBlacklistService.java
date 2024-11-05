package com.jwt.demo.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * TokenBlacklistService는 블랙리스트에 등록된 JWT 토큰을 관리하는 서비스입니다.
 * 토큰을 블랙리스트에 추가하고, 특정 토큰이 블랙리스트에 있는지 확인하는 메서드를 제공합니다.
 */
@Service
public class TokenBlacklistService {

    private final JdbcTemplate jdbcTemplate; // 데이터베이스 작업을 수행하기 위한 JdbcTemplate

    // 생성자 주입으로 JdbcTemplate을 주입받습니다.
    @Autowired
    public TokenBlacklistService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 주어진 토큰을 블랙리스트에 추가합니다.
     * @param token 블랙리스트에 추가할 JWT 토큰
     * @param expiredAt 해당 토큰의 만료 시간
     */
    public void addToBlacklist(String token, LocalDateTime expiredAt) {
        // jwt_blacklist 테이블에 토큰과 만료 시간을 저장하는 SQL 쿼리
        String sql = "INSERT INTO jwt_blacklist (token, expired_at) VALUES (?, ?)";
        jdbcTemplate.update(sql, token, expiredAt); // 데이터베이스에 토큰을 추가
    }

    /**
     * 특정 토큰이 블랙리스트에 등록되어 있는지 확인합니다.
     * @param token 확인할 JWT 토큰
     * @return 블랙리스트에 있으면 true, 그렇지 않으면 false
     */
    public boolean isBlacklisted(String token) {
        // 토큰이 블랙리스트에 있는지와 만료 시간이 현재 시간보다 이후인지를 확인하는 SQL 쿼리
        String sql = "SELECT COUNT(*) FROM jwt_blacklist WHERE token = ? AND expired_at > NOW()";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token);
        return count != null && count > 0; // 블랙리스트에 있는 경우 true 반환
    }
}
