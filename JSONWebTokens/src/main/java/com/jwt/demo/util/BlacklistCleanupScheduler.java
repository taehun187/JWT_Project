package com.jwt.demo.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BlacklistCleanupScheduler 클래스는 스케줄링을 통해 매일 자정에 
 * 만료된 JWT 토큰을 블랙리스트에서 제거하는 작업을 수행하는 컴포넌트입니다.
 */
@Component
public class BlacklistCleanupScheduler {

    private final JdbcTemplate jdbcTemplate; // 데이터베이스 작업을 위한 JdbcTemplate

    // 생성자를 통해 JdbcTemplate을 주입받습니다.
    public BlacklistCleanupScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 매일 자정에 실행되어, 만료된 토큰들을 블랙리스트 테이블에서 삭제합니다.
     * cron 표현식: "0 0 0 * * *"는 매일 자정 0시에 작업을 수행하도록 설정합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void removeExpiredTokens() {
        // 만료 시간이 현재 시간보다 이전인 토큰을 삭제하는 SQL 쿼리
        String sql = "DELETE FROM jwt_blacklist WHERE expired_at < NOW()";
        jdbcTemplate.update(sql); // SQL 쿼리를 실행하여 만료된 토큰을 삭제
    }
}
