```markdown
# JWT 인증 데모

이 프로젝트는 Spring Boot를 사용하여 JWT 기반 인증을 구현하는 기본 예제를 보여줍니다.
애플리케이션은 로그인, 토큰 갱신, JWT 토큰 검증 및 블랙리스트 기능을 지원합니다.

## 목차
- [요구 사항](#요구-사항)
- [설치](#설치)
- [프로젝트 구조](#프로젝트-구조)
- [엔드포인트](#엔드포인트)
- [Postman 사용법](#postman-사용법)
- [설정](#설정)
- [추가 정보](#추가-정보)

## 요구 사항
- Java 17
- Maven
- MySQL

## 설치

### 데이터베이스 설정
1. **데이터베이스 생성**:
   - MySQL에 `testdb` 이름의 데이터베이스를 생성합니다.

2. **MySQL 자격 정보 설정**:
   - `application.yml` 파일에서 데이터베이스 사용자 이름과 비밀번호를 본인의 MySQL 설정에 맞게 변경합니다.

### 프로젝트 설정
1. **리포지토리 클론**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```

2. **의존성 설치**:
   ```bash
   mvn install
   ```

3. **애플리케이션 실행**:
   ```bash
   mvn spring-boot:run
   ```

## 프로젝트 구조
- **`com.jwt.demo`**: 애플리케이션 메인 패키지로, 진입점과 설정이 포함되어 있습니다.
- **`controller`**: 로그인, 토큰 갱신 및 사용자 정보 처리를 위한 REST 컨트롤러가 포함되어 있습니다.
- **`dto`**: 로그인, 사용자, 토큰 등을 위한 DTO (데이터 전송 객체)가 포함되어 있습니다.
- **`entities`**: 데이터베이스 구조를 나타내는 엔티티 클래스들이 포함되어 있습니다.
- **`jwt`**: JWT 생성, 검증 및 Spring Security에 대한 설정을 처리합니다.
- **`repository`**: 데이터 접근을 위한 인터페이스가 포함되어 있습니다.
- **`service`**: 인증 및 사용자 관리를 위한 비즈니스 로직을 처리합니다.
- **`util`**: 토큰 블랙리스트 및 정리 작업과 같은 유틸리티 클래스들이 포함되어 있습니다.

## 엔드포인트

| HTTP 메서드 | 엔드포인트              | 설명                                          |
|-------------|------------------------|------------------------------------------------|
| POST        | `/api/login`           | 사용자 인증 후 JWT 토큰을 반환합니다.           |
| POST        | `/api/refresh-token`   | 리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다. |
| GET         | `/api/user`            | 인증된 사용자의 정보를 반환합니다.             |
| POST        | `/api/signup`          | 새로운 사용자를 등록합니다.                    |
| GET         | `/api/test/check-authentication` | 인증 상태를 확인합니다.                |

## Postman 사용법

### 1. 로그인 (POST /api/login)
- **설명**: 사용자 정보를 사용해 로그인하고 JWT 액세스 토큰과 리프레시 토큰을 반환받습니다.
- **설정 방법**:
  - **URL**: `http://localhost:8080/api/login`
  - **Method**: POST
  - **Body**: JSON 형식으로 아래와 같이 설정
    ```json
    {
      "username": "lth1518@gmail.com",
      "password": "12345"
    }
    ```
- **응답 예시**:
  ```json
  {
    "accessToken": "your_access_token",
    "refreshToken": "your_refresh_token"
  }
  ```
- **주의사항**: 반환된 `accessToken`과 `refreshToken`을 이후 요청에 사용할 수 있도록 저장해둡니다.

### 2. 토큰 갱신 (POST /api/refresh-token)
- **설명**: 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급받습니다.
- **설정 방법**:
  - **URL**: `http://localhost:8080/api/refresh-token`
  - **Method**: POST
  - **Headers**: 
    - Content-Type: application/json
  - **Body**: JSON 형식으로 아래와 같이 설정
    ```json
    {
      "refreshToken": "your_refresh_token",
      "deviceInfo": "user-agent"
    }
    ```
- **응답 예시**:
  ```json
  {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token"
  }
  ```
- **주의사항**: 응답에서 받은 `new_access_token`을 이후 요청의 `Authorization` 헤더에 사용합니다.

### 3. 사용자 정보 조회 (GET /api/user)
- **설명**: 현재 로그인된 사용자의 정보를 반환합니다.
- **설정 방법**:
  - **URL**: `http://localhost:8080/api/user`
  - **Method**: GET
  - **Headers**:
    - Authorization: `Bearer your_access_token` (로그인 또는 토큰 갱신 응답에서 받은 `accessToken` 사용)
- **응답 예시**:
  ```json
  {
    "userId": 1,
    "username": "lth1518@gmail.com",
    "nickname": "TAEHUN",
    "authorities": [
      {
        "authorityName": "ROLE_USER"
      }
    ]
  }
  ```
- **주의사항**: 인증이 필요하므로 `Authorization` 헤더에 액세스 토큰을 추가해야 합니다.

### 4. 회원가입 (POST /api/signup)
- **설명**: 새로운 사용자를 등록합니다.
- **설정 방법**:
  - **URL**: `http://localhost:8080/api/signup`
  - **Method**: POST
  - **Body**: JSON 형식으로 아래와 같이 설정
    ```json
    {
      "username": "newuser@example.com",
      "password": "newpassword",
      "nickname": "newuser"
    }
    ```
- **응답 예시**:
  ```json
  {
    "userId": 2,
    "username": "newuser@example.com",
    "nickname": "newuser",
    "activated": true,
    "authorities": [
      {
        "authorityName": "ROLE_USER"
      }
    ]
  }
  ```

### 5. 인증 상태 확인 (GET /api/test/check-authentication)
- **설명**: 현재 사용자의 인증 상태와 권한을 확인합니다.
- **설정 방법**:
  - **URL**: `http://localhost:8080/api/test/check-authentication`
  - **Method**: GET
  - **Headers**:
    - Authorization: `Bearer your_access_token` (로그인 또는 토큰 갱신 응답에서 받은 `accessToken` 사용)
- **응답 예시**:
  ```json
  {
    "message": "Authenticated Username: lth1518@gmail.com, Authorities: ROLE_USER ROLE_ADMIN"
  }
  ```

## 설정

### `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/testdb?useLegacyDatetimeCode=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 12341234

  sql:    
    init:
      mode: always
      data-locations: classpath:data.sql

  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: create
    defer-datasource-initialization: true 

    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        show-sql: true

logging:
  level:
    org:
      hibernate:
        SQL: DEBUG

jwt:
  header: Authorization
  secret: <base64로 인코딩된 secret>
  token-validity-in-seconds: 120
  refreshtoken-validity-in-seconds: 1800
```

- `jwt.secret`: JWT 서명을 위한 비밀 키(Base64로 인코딩).
- `token-validity-in-seconds`: 액세스 토큰의 만료 시간(초 단위).
- `refreshtoken-validity-in-seconds`: 리프레시 토큰의 만료 시간(초 단위).

### `data.sql`

```sql
insert into users (USER_ID, USERNAME, PASSWORD, NICKNAME, ACTIVATED) values (1, 'admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', 1);
insert into AUTHORITY (AUTHORITY_NAME) values ('ROLE_USER');
insert into AUTHORITY (AUTHORITY_NAME) values ('ROLE_ADMIN');
insert into USER_AUTHORITY (USER_ID, AUTHORITY_NAME) values (1, 'ROLE_USER');
insert into USER_AUTHORITY (USER_ID, AUTHORITY_NAME) values (1, 'ROLE_ADMIN');
```

---

# 로그아웃 구현 방식 설명

이 프로젝트에서 **로그아웃** 기능은 단순히 클라이언트 측의 토큰을 무효화하는 방식이 아닌, **JWT 토큰을 서버 측에서 블랙리스트에 등록**하는 방식으로 구현되었습니다. 이 방법을 선택한 이유는 JWT 기반 인증 시스템의 특성 때문입니다. 다음은 로그아웃 방식과 블랙리스트 활용에 대한 설명입니다.

## 로그아웃 구현 방식

### 1. 블랙리스트 방식 선택 이유
JWT 기반 인증 시스템에서는 서버가 토큰을 직접 관리하지 않으므로, 일반적인 세션 기반 인증처럼 서버에서 토큰을 직접 무효화할 수 없습니다. 이를 해결하기 위해, 이 프로젝트에서는 **로그아웃 시 서버에 요청한 토큰을 블랙리스트에 등록**하여 이후 해당 토큰이 사용되지 않도록 차단하는 방식을 사용했습니다.

### 2. 블랙리스트 등록 과정
- 사용자가 `/logout` 엔드포인트에 로그아웃 요청을 보내면 서버는 해당 요청의 `Authorization` 헤더에서 **JWT 토큰**을 추출합니다.
- 추출한 토큰을 **만료 시간**과 함께 블랙리스트에 등록하여 이후 해당 토큰이 접근 요청에 사용되지 않도록 차단합니다.
- 블랙리스트에 등록된 토큰은 각 요청 시 필터에서 확인되며, 블랙리스트에 포함된 토큰의 경우 **인증이 실패**하도록 처리됩니다.

### 3. 블랙리스트 정리 작업
- 주기적으로 블랙리스트에 등록된 **만료된 토큰을 정리**하는 작업이 필요합니다. 이를 위해 프로젝트에는 `BlacklistCleanupScheduler`라는 스케줄러를 설정하여 **매일 자정에 만료된 토큰**을 삭제하도록 했습니다.

## 로그아웃 과정 요약
1. **클라이언트에서 로그아웃 요청** 전송 (`/logout` 엔드포인트).
2. **서버에서 JWT 토큰을 추출**하고 블랙리스트에 만료 시간과 함께 등록.
3. **이후 모든 요청에서 블랙리스트 조회**를 통해 해당 토큰이 사용되지 않도록 차단.
4. 주기적으로 **만료된 블랙리스트 토큰을 정리**하여 불필요한 데이터가 남지 않도록 유지.

이 방식은 토큰 기반 인증 시스템의 한계를 보완하면서 로그아웃 시 사용자에게 안전한 환경을 제공합니다.

---

## 엔드포인트 예시: 로그아웃 요청
- **URL**: `http://localhost:8080/api/logout`
- **Method**: POST
- **Headers**: 
  - Authorization: `Bearer <your_access_token>`
- **설명**: 서버에서 제공한 `access_token`을 요청 헤더에 추가하여 로그아웃을 요청합니다. 서버는 해당 토큰을 블랙리스트에 등록하고 이후 접근을 차단합니다.
  

