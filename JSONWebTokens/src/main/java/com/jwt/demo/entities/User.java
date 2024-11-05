package com.jwt.demo.entities;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users") // users 테이블에 매핑
public class User {

    @JsonIgnore // JSON 직렬화 시 이 필드는 무시
    @Id 
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가하는 ID
    private Long userId;

    @Column(name = "username", length = 50, unique = true) // 사용자 이름, 고유 값
    private String username;

    @JsonIgnore // JSON 직렬화 시 이 필드는 무시
    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "nickname", length = 50) // 닉네임 저장
    private String nickname;

    @JsonIgnore // JSON 직렬화 시 이 필드는 무시
    @Column(name = "activated")
    private boolean activated; // 계정 활성화 여부

    @ManyToMany // 다대다 관계를 설정
    @JoinTable(
            name = "user_authority", // 다대다 관계를 위한 연결 테이블의 이름
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")}, // `User` 엔티티의 외래키를 지정
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")}) // `Authority` 엔티티의 외래키를 지정
    private Set<Authority> authorities; // `User`와 연결된 `Authority` 목록
    
    /* User와 Authority 간의 다대다 관계는 사용자와 권한(역할) 시스템을 구현하기 위해 필요합니다. 
     * 다대다 관계를 사용하면 하나의 사용자가 여러 권한을 가질 수 있고, 반대로 특정 권한이 여러 사용자에게 부여될 수 있습니다. 
     * 이렇게 함으로써 사용자와 권한 간의 유연한 매핑이 가능해지며, 
     * 각각의 사용자가 개별적으로 여러 역할을 가질 수 있게 됩니다.
     */
}
