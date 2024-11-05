package com.jwt.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.Repository;
//import org.springframework.stereotype.Repository;


import com.jwt.demo.entities.User;

//@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id); // ID로 사용자 검색

    User save(User user); // 사용자 저장

    Optional<User> findOneWithAuthoritiesByUsername(String username); // 사용자명으로 사용자 및 권한 정보를 함께 조회
    
    void delete(User user); // 사용자 삭제
}

// 사용자(User) 엔티티의 데이터베이스 작업을 처리하는 JPA 리포지토리입니다. 
// JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며, 추가적인 사용자 조회 메서드를 정의해 놓았습니다.