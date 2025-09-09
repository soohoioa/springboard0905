# board0905 – JWT/Redis/Spring Security 작업 정리

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


---

## 13) 트러블슈팅 (Troubleshooting)

아래는 실제로 자주 만나는 증상 → 원인 → 해결 순서대로 정리했습니다.

### A. 빌드/컴파일 단계 오류

<a id="A-1"></a>
1. **`Cannot resolve symbol 'UserDetailsService'` / `loadUserByUsername` 인식 안 됨**
  - **원인:** spring-security 의존성/임포트 누락, `CustomUserDetailsService` 경로/이름 불일치.
  - **해결:**
    - `implementation("org.springframework.boot:spring-boot-starter-security")` 추가.
    - `CustomUserDetailsService`가 `UserDetailsService`를 구현하고 `@Service`로 스캔되는지 확인.
    - `import org.springframework.security.core.userdetails.UserDetailsService;` 확인.
      <a id="A-2"></a>

2. **JJWT 타입 충돌 (`Required type: Key / Provided: SecretKey`, `cannot cast 'Key' to 'javax.crypto.SecretKey'`)**
  - **원인:** JJWT 0.12.x에서 키 타입을 혼용.
  - **해결:**
    - **오직 `javax.crypto.SecretKey`로 통일.**
    - `JwtProperties.key()` 반환 타입을 `SecretKey`로, `Keys.hmacShaKeyFor(...)` 사용.
    - 파서/서명 시 캐스팅 없이 `props.key()` 그대로 전달.
      <a id="A-3"></a>

3. **`Cannot resolve symbol 'StringRedisTemplate'` / `opsForValue()` 등 인식 안 됨**
  - **원인:** Redis 의존성 누락.
  - **해결:** `implementation("org.springframework.boot:spring-boot-starter-data-redis")` 추가 후 리빌드.
    <a id="A-4"></a>

4. **`Cannot resolve symbol 'SecurityRequirement'`**
  - **원인:** springdoc-openapi 의존성/임포트 누락.
  - **해결:** `implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")` 추가 및
    `import io.swagger.v3.oas.models.security.SecurityRequirement;` 확인.
    <a id="A-5"></a>

5. **`jakarta.*` / `javax.*` 혼용**
  - **원인:** Spring Boot 3.x는 **jakarta** 네임스페이스 사용.
  - **해결:** `jakarta.servlet.*`, `jakarta.validation.*` 등으로 임포트 정리.

---

### B. 애플리케이션 부팅 실패

<a id="B-1"></a>
1. **Bean 충돌 (`A bean with that name has already been defined ... overriding is disabled`)**
  - **원인:** 동일 이름의 `@Bean` 중복 등록 (예: `passwordEncoder()`).
  - **해결:** 한 곳으로 **집중**하고 다른 정의 **삭제**. (임시로 `spring.main.allow-bean-definition-overriding=true`는 비권장)
    <a id="B-2"></a>

2. **`No qualifying bean of type 'StringRedisTemplate'`**
  - **원인:** 의존성 누락/오토컨피그 비활성.
  - **해결:** 의존성 추가, `spring.data.redis.host/port` 설정, 빈 주입 위치 확인.
    <a id="B-3"></a>

3. **Redis 연결 실패 (`Connection refused: /127.0.0.1:6379`)**
  - **원인:** Redis 서버 미기동/포트 다름.
  - **해결:**
    - 로컬: `redis-server` 실행.
    - Docker: `docker run -p 6379:6379 --name redis -d redis:7`
      <a id="B-4"></a>

4. **DB 스키마 오류 (`Unknown database 'board0905'`)**
  - **원인:** 스키마 미생성.
  - **해결:** DB에 스키마 생성: `CREATE DATABASE board0905 CHARACTER SET utf8mb4;`
    <a id="B-5"></a>

5. **컴포넌트 스캔 범위**
  - **원인:** 메인 클래스 위치가 루트가 아님.
  - **해결:** `@SpringBootApplication`을 `com.project.board0905` 루트에 두거나 `scanBasePackages` 지정.

---

### C. 인증/인가 이슈 (401/403)

<a id="C-1"></a>
1. **401 Unauthorized**
  - **원인:** `Authorization` 헤더 누락/오타, `Bearer ` 프리픽스 누락, 토큰 만료/블랙리스트, 서버 시간 불일치.
  - **해결:**
    - 헤더 형식: `Authorization: Bearer <access_token>`
    - 필터 순서: `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`
    - 서버 시간 동기화 (NTP).
      <a id="C-2"></a>

2. **403 Forbidden**
  - **원인:** 권한 부족 또는 `ROLE_` 프리픽스 누락.
  - **해결:**
    - `SimpleGrantedAuthority("ROLE_" + role)` 형태로 권한 부여.
    - `@PreAuthorize("hasRole('ADMIN')")` 사용 시 실제 권한은 `"ROLE_ADMIN"`이어야 함.
    - 경로 보안과 메서드 보안 규칙을 중복/충돌 없이 설계.
      <a id="C-3"></a>

3. **비밀번호 인코딩 문제**
  - **원인:** DB에 평문 비밀번호 저장.
  - **해결:** 회원 생성 시 `BCryptPasswordEncoder`로 인코딩 후 저장.

---

### D. Swagger 관련

<a id="D-1"></a>
1. **Swagger 접근 403/401**
  - **원인:** 경로 미허용.
  - **해결:** `/swagger-ui/**`, `/v3/api-docs/**`를 `permitAll`.
    <a id="D-2"></a>

2. **Swagger에서 인증이 안 먹는 경우**
  - **해결:** 우상단 **Authorize** 버튼에 `Bearer <access_token>` 입력.
    <a id="D-3"></a>

3. **CORS**
  - **원인:** 다른 오리진에서 Swagger UI 접근.
  - **해결:** CORS 설정에 `Authorization` 헤더, 허용 오리진/메서드 추가.

```java
// 예: 전역 CORS
@Bean
public WebMvcConfigurer corsConfigurer() {
  return new WebMvcConfigurer() {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
        .allowedOrigins("http://localhost:3000")
        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .allowCredentials(true);
    }
  };
}
```

---

### E. 토큰/재발급/로그아웃 이슈

<a id="E-1"></a>
1. **로그아웃 후에도 호출이 되는 것처럼 보임**
  - **원인:** 블랙리스트 TTL 계산 오류(남은 만료 시간 대신 고정 TTL), 블랙리스트 키 네임스페이스 문제.
  - **해결:** 토큰의 `exp`에서 **남은 초** 계산 → 그만큼 TTL 저장. 키 프리픽스에 환경 구분(`dev:`/`prod:`) 권장.
    <a id="E-2"></a>

2. **`Invalid signature`/`JWT signature does not match...`**
  - **원인:** 서버 인스턴스 간 시크릿 불일치, 환경변수 오타.
  - **해결:** 모든 인스턴스에 **동일한 시크릿** 사용, 철자 재확인, 재배포.
    <a id="E-3"></a>

3. **재발급 실패 (`mismatched refresh` 등)**
  - **원인:** RT 회전(Rotation) 정책에서 최신 RT가 Redis에 반영되지 않음.
  - **해결:** 재발급 시 **반드시 RT 교체 저장**. 이전 RT 재사용 감지 시 전체 세션 무효화(선택).
    <a id="E-4"></a>

4. **시크릿 길이 문제**
  - **원인:** HS256에 충분히 긴 키 미사용.
  - **해결:** 256비트(32바이트) 이상 랜덤:
    - Base64: `openssl rand -base64 48`
    - Hex: `openssl rand -hex 32`
  - Base64 문자열을 시크릿으로 사용할 땐 `Decoders.BASE64.decode(secret)`로 디코딩 후 `hmacShaKeyFor`에 전달.

---

### F. 필터/설정 이슈

<a id="F-1"></a>
1. **필터가 두 번 적용되거나 전혀 적용되지 않음**
  - **원인:** `@Component` + `FilterRegistrationBean` 중복 등록, 순서 오류.
  - **해결:** 보안 필터 체인에만 추가하고, 별도 등록은 하지 않음. 순서는 `UsernamePasswordAuthenticationFilter` 이전.
    <a id="F-2"></a>

2. **경로 매칭 오류**
  - **원인:** Spring Security 6의 matcher DSL 변경.
  - **해결:** `authorizeHttpRequests(auth -> auth.requestMatchers(...).permitAll() ...)` 형태로 최신 DSL 사용.

---

### G. 운영 팁 (키 로테이션/네임스페이스/로그)

- **시크릿 로테이션(간단 버전):** 새 시크릿으로 배포 전, 다중 시크릿을 허용하는 파서 구성 → 점진 전환(고급: `kid` 헤더/JWK 도입 검토).
- **Redis 키 네임스페이스:** `app:{env}:auth:rt:{username}`, `app:{env}:auth:bl:{jti}`처럼 환경/앱 구분자 추가 권장.
- **로그 레벨:** 초기 디버깅 시 `org.springframework.security=DEBUG`, 필터 내부 `Slf4j`로 `sub/role/exp/blacklist 여부` 로그.

```java
// 간단한 디버그 예시 (필터 내부)
log.debug("[JWT] sub={}, exp={}, blacklisted={}", username, expiration, tokenStore.isBlacklisted(token));
```

---

### H. 빠른 매핑표 (에러 → 조치)

| 증상/로그 | 가장 흔한 원인 | 빠른 조치 |
|---|---|---|
| 401 Unauthorized | Authorization 헤더 누락/만료/블랙리스트 | Bearer 헤더 확인, 만료 확인, 블랙리스트 키 확인 |
| 403 Forbidden | 권한 미부여/ROLE_ 프리픽스 누락 | `ROLE_` 접두어로 권한 매핑, `@PreAuthorize` 점검 |
| Bean overriding disabled | 중복 @Bean 등록 | 한 곳만 남기기 (`passwordEncoder`, `AuthenticationManager`) |
| Invalid signature | 시크릿 불일치 | 모든 인스턴스 시크릿 통일, 철자 확인 |
| Redis connection refused | Redis 미기동 | `redis-server` or Docker 실행 |
| `SecurityRequirement` not found | springdoc 누락 | 의존성/임포트 추가 |
| JJWT 타입 캐스팅 오류 | Key/SecretKey 혼용 | `SecretKey`로 통일, 캐스팅 제거 |


---

## 14) 내 오류 ↔ 해결 섹션 빠른 링크

| 증상/오류 메시지 | 해결 섹션 링크 |
|---|---|
| `Cannot resolve symbol 'CustomUserDetailsService'` / `loadUserByUsername(String)` | [A-1. UserDetailsService 미인식](#A-1) |
| `Required type: Key / Provided: SecretKey` | [A-2. JJWT 타입 충돌](#A-2) |
| `Inconvertible types; cannot cast 'Key' to 'javax.crypto.SecretKey'` | [A-2. JJWT 타입 충돌](#A-2) |
| `Cannot resolve symbol 'StringRedisTemplate'` / `opsForValue()` / `delete()` / `hasKey()` | [A-3. Redis 의존성/임포트 누락](#A-3) |
| `Cannot resolve symbol 'SecurityRequirement'` | [A-4. springdoc-openapi 누락](#A-4) |
| `A bean with that name has already been defined... overriding is disabled` | [B-1. 빈 중복 충돌](#B-1) |
| `Unknown database 'board0905'` | [B-4. DB 스키마 생성](#B-4) |
| 401 Unauthorized | [C-1. 401 원인/해결](#C-1) |
| 403 Forbidden | [C-2. 권한/ROLE_ 접두어](#C-2) |
| Swagger 401/403 | [D-1. Swagger 경로 허용](#D-1) |
| `Invalid signature` / `JWT signature does not match` | [E-2. 시크릿 불일치](#E-2) |
| 로그아웃 후에도 호출 가능 | [E-1. 블랙리스트 TTL/키 문제](#E-1) |
| 필터가 두 번 적용/미적용 | [F-1. 필터 등록/순서](#F-1) |
| 경로 매칭 오류 | [F-2. Security 6 DSL](#F-2) |
