# JWT/Redis/Spring Security 작업 정리

## 1) 목표 정의

**목표:** Spring Security 기반의 **Stateless JWT 인증/인가 파이프라인**을 프로젝트에 붙이고, **Redis**로 Refresh 토큰 보관 + Access 토큰 **블랙리스트(로그아웃 무효화)** 까지 지원.

**왜:** 서버 세션을 안 쓰면(Stateless) **수평 확장**이 쉽고, Redis를 곁들이면 JWT의 단점(강제 무효화 어려움)을 **실무적으로 보완**할 수 있음.

---

## 2) 의존성 및 기본 설정

### 무엇을 했나
- `spring-boot-starter-security`, `jjwt-api/impl/jackson (0.12.x)`, `spring-boot-starter-data-redis`, `springdoc-openapi-starter-webmvc-ui` 추가.
- `application.yml`에 `jwt`(issuer/secret/만료)와 `redis`(host/port) 설정.

### 왜 이렇게 했나
- **Security:** 필터 체인 구성 및 메서드 보안.
- **JJWT 0.12.x:** 최신 빌더/검증 API(`Jwts.SIG.HS256`, `parser().verifyWith(...)`) 사용.
- **Redis:** RT/블랙리스트 저장소.
- **springdoc:** Swagger UI에서 Bearer 인증으로 바로 테스트하기 위함.

---

## 3) 패키지 구조 정리

### 무엇을 했나
- 기능단위로 분리:  
  `auth/{controller, service, jwt, config, dto}`, `domain.user/{entity, repository}`, `common/{error, response}`, `config/*`
- `SecurityConfig`와 보안 관련 빈은 `auth.config`로, 전역 설정은 `common.config/config`로.

### 왜 이렇게 했나
- **찾기 쉬움 + 관심사 분리.** 보안, 도메인, 공통이 섞여 있으면 유지보수 난이도가 커짐.

---

## 4) UserDetailsService 구현 및 필터 의존성 정리

### 무엇을 했나
- `CustomUserDetailsService` 구현(Repository로 사용자 조회 → `UserDetails` 반환).
- `JwtAuthFilter`가 구현체 이름이 아닌 `UserDetailsService` **인터페이스**에 의존하도록 수정.

### 왜 이렇게 했나
- 스프링 보안 표준(SRP) 준수, **구현체 교체 용이성↑, 테스트/주입 안정성↑.**
- “Cannot resolve symbol `CustomUserDetailsService`” 문제도 **패키지/이름 의존 제거**로 자연스럽게 해소.

---

## 5) JWT 구성 (JJWT 0.12.x 대응)

### 무엇을 했나
- `JwtProperties`에서 **`javax.crypto.SecretKey` 반환으로 통일**(`Keys.hmacShaKeyFor(...)`).
- `JwtProvider`에서 Access/Refresh 생성, 파싱/검증, 클레임 접근 구현.

### 왜 이렇게 했나
- JJWT 0.12.x는 키 검증/서명에서 `SecretKey`를 명확히 기대함.  
  → `Key`/`SecretKey` 혼용으로 생기던
    - “Required type: Key / Provided: SecretKey”
    - “cannot cast ‘Key’ to ‘javax.crypto.SecretKey’”  
      같은 **타입 충돌을 근본 해결.**

---

## 6) Redis 토큰 저장소 (TokenStore)

### 무엇을 했나
- `StringRedisTemplate` 기반으로
    - `auth:rt:{username}` 키에 **Refresh 저장/조회/삭제**
    - `auth:bl:{token}` 키에 **Access 블랙리스트 + TTL 저장**
- 누락된 `spring-boot-starter-data-redis` 의존성과 `StringRedisTemplate` import 추가.

### 왜 이렇게 했나
- **Refresh:** 재발급 시 서버 단 상태 점검(탈취 방지, 단일 RT 정책).
- **Blacklist:** Access 토큰을 만료까지 기다리지 않고 **즉시 무효화**(로그아웃, 강퇴 등).

---

## 7) 인증 서비스 / 시큐리티 체인

### 무엇을 했나
- **AuthService:** 로그인(AT/RT 발급+저장), 재발급(RT 검증→AT/RT 재발급), 로그아웃(AT 블랙리스트 + RT 삭제).
- **SecurityConfig:**
    - `STATELESS` 세션,
    - `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` `permitAll`, 나머지 보호,
    - `JwtAuthFilter`를 `UsernamePasswordAuthenticationFilter` **이전에** 추가,
    - `DaoAuthenticationProvider`로 `UserDetailsService` + `PasswordEncoder` 연결.

### 왜 이렇게 했나
- 필터 순서/정책 설정으로 **요청마다 토큰 검증** → 컨트롤러에서는 `@PreAuthorize` 등으로 **인가만** 신경 쓰면 됨.
- Provider 분리로 **인증 흐름 가시성↑, 테스트 용이.**

---

## 8) Swagger(OpenAPI) Bearer 보안 설정

### 무엇을 했나
- `OpenApiConfig`에서 `SecurityRequirement("bearerAuth")` + `SecurityScheme` 등록.
- 누락된 `SecurityRequirement` import/의존성 문제 해결.

### 왜 이렇게 했나
- Swagger UI에서 **Authorize 버튼**으로 **Bearer 토큰 입력 후 API 테스트** 가능 → 개발/디버그 속도↑.

---

## 9) 중복 빈 충돌 해결

### 무엇을 했나
- `passwordEncoder()`가 `SecurityConfig`와 `SecurityBeans`에 **중복 등록**되어 “bean overriding disabled” 에러.
- 한 곳(`SecurityBeans`)으로 **집중**하고 다른 한 곳에서 **제거**.  
  (혹은 이름 변경/`@Primary`/오버라이딩 허용은 비권장)

### 왜 이렇게 했나
- **빈 이름 충돌 원천 차단**, 설정 **단일화**로 혼란 방지.

---

## 10) 현재 상태 요약

- **JWT 파이프라인:** 구현 완료 (발급/검증/재발급/로그아웃)
- **Redis 연동:** RT 저장 + AT 블랙리스트 동작
- **시큐리티 체인:** Stateless, 경로 보안, 필터 순서 OK
- **Swagger 연동:** Bearer 인증 테스트 가능
- **타입/빈 충돌:** JJWT `SecretKey`/`SecurityRequirement`/중복 Bean 문제 해결

---

## 11) 스모크 테스트 순서(빠르게 검증)

1. **사용자 생성:** 비밀번호는 반드시 `BCrypt`로 인코딩되어 저장.
2. **로그인:** `POST /api/v1/auth/login` → `accessToken`/`refreshToken` 확인.
3. **보호 API 호출:** `Authorization: Bearer <AT>`로 접근되는지 확인.
4. **재발급:** `POST /api/v1/auth/refresh` → 새 AT/RT 발급 + RT 교체 저장 확인.
5. **로그아웃:** `POST /api/v1/auth/logout` → AT 블랙리스트 저장 및 RT 삭제.
6. **재호출 차단 확인:** 로그아웃 직후 같은 AT로 호출 시 **거부**되는지 확인.

---

## 12) 남은 점검 포인트(있다면)

- `User`/`UserRepository`의 경로·시그니처가 `CustomUserDetailsService`와 정확히 **매칭**되는지.
- 메인 애플리케이션의 **컴포넌트 스캔 범위**가 `com.project.board0905` 루트를 확실히 커버하는지.
- 로컬 Redis가 **실행 중**인지(미실행 시 런타임 연결 오류 발생).
- 컨트롤러에 필요한 곳에 `@PreAuthorize("hasRole('ADMIN')")` 등 **인가 규칙** 추가.
