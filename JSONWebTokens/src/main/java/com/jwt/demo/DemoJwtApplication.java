package com.jwt.demo;

// 컬렉션을 사용하기 위해 Collections 클래스 임포트
import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jwt.demo.dto.UserDto;
import com.jwt.demo.entities.Authority;
import com.jwt.demo.entities.User;
import com.jwt.demo.repository.UserRepository;

// Spring Boot 애플리케이션을 나타내는 애너테이션으로 해당 클래스는 애플리케이션 진입점이 됩니다.
@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화 - 만료된 토큰 주기적으로 삭제 시키기위해 
public class DemoJwtApplication {
	
    // CommandLineRunner 빈을 생성하는 메서드. 애플리케이션이 시작될 때 주어진 데이터를 데이터베이스에 저장합니다.
	@Bean
	public CommandLineRunner dataLoader(
			UserRepository userRepository, // 사용자 데이터를 저장하기 위한 UserRepository
			PasswordEncoder passwordEncoder // 비밀번호 암호화를 위한 PasswordEncoder
			) {
		
        // CommandLineRunner를 반환하여, 애플리케이션이 시작되면 사용자 데이터를 생성하고 데이터베이스에 저장합니다.
		return new CommandLineRunner() {
		      @Override
		      public void run(String... args) throws Exception {
                  
                  // 권한 설정: ROLE_USER 권한을 가진 Authority 객체를 생성합니다.
		    	  Authority authority = Authority.builder()
		                  .authorityName("ROLE_USER")
		                  .build();
		    	  
                  // 사용자 DTO(UserDto) 생성: 사용자 정보를 담고 있는 객체입니다.
		    	  UserDto userDto = UserDto.builder()
		    			  .username("lth1518@gmail.com")
		    			  .password("12345")
		    			  .nickname("TAEHUN")
		    			  .build();
		    	  
                  // User 객체 생성: DTO 정보를 기반으로, User 엔티티 객체를 빌드합니다.
		    	  User user = User.builder()
		                  .username(userDto.getUsername()) // 이메일을 사용자 이름으로 설정
		                  .password(passwordEncoder.encode(userDto.getPassword())) // 비밀번호 암호화
		                  .nickname(userDto.getNickname()) // 사용자 닉네임 설정
		                  .authorities(Collections.singleton(authority)) // 권한 설정
		                  .activated(true) // 계정 활성화 상태 설정
		                  .build();

		          // 데이터베이스에 User 객체를 저장합니다.
		          userRepository.save(user);
		      }
		};
	
	}

    // 메인 메서드: Spring Boot 애플리케이션을 실행하는 진입점입니다.
	public static void main(String[] args) {
		SpringApplication.run(DemoJwtApplication.class, args);
	}
}
