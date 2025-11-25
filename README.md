# Work Hub 개발 가이드라인

## 목차
1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [공통 응답 형식](#4-공통-응답-형식)
5. [예외 처리](#5-예외-처리)
6. [API 설계 규칙](#6-api-설계-규칙)
7. [코드 컨벤션](#7-코드-컨벤션)
8. [Git 전략](#8-git-전략)
9. [PR/코드 리뷰 프로세스](#9-pr코드-리뷰-프로세스)
10. [기타 참고사항](#10-기타-참고사항)
11. [로그 관리](#11-로그-관리)

---

## 1. 프로젝트 개요

Work Hub는 웹 개발사와 고객사 간의 프로젝트 관리 및 커뮤니케이션을 위한 웹 서비스입니다.

### 주요 기능
- 프로젝트 단계별 관리 (요구사항 정의 → 화면설계 → 디자인 → 퍼블리싱 → 개발 → QA)
- 체크리스트 기반 진행 상황 관리
- 고객사/개발사 간 커뮤니케이션
- 알림 시스템
- 권한 기반 접근 제어

---

## 2. 기술 스택

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot, Spring Security
- **Library**: JPA, QueryDSL
- **모니터링/에러 트래킹**: Sentry

### Database
- **Main DB**: PostgreSQL
- **Cache/Pub-Sub**: Redis

### Infrastructure
- **CI/CD**: GitHub Actions
- **배포 환경**: Docker
- **문서화**: Swagger

## CI 파이프라인

- `.github/workflows/ci.yml` 워크플로우가 `main`, `develop` 대상 PR 생성·업데이트 시 자동으로 실행되어 `./gradlew clean test --build-cache`를 수행합니다.
- 워크플로우 내부 `services`에서 Postgres 16, Redis 7 컨테이너를 띄우고 `SPRING_PROFILES_ACTIVE=test`로 테스트 전용 설정(`src/main/resources/application-test.yml`)을 적용합니다.
- CI 실행에 필요한 Secrets: `CI_DB_PASSWORD`(데이터베이스 비밀번호). 필요 시 `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_REDIS_*`를 추가로 등록하면 기본값 대신 사용할 수 있습니다.
- 로컬에서 CI 환경을 재현하려면 `docker compose up postgres redis` 실행 후 `SPRING_PROFILES_ACTIVE=test ./gradlew clean test`를 수행하세요.

---

### PostgreSQL 채택 이유

#### PostgreSQL vs MySQL 비교

| 구분 | PostgreSQL | MySQL |
|------|------------|-------|
| **특징** | 고급 기능, 확장성, 정합성 | 빠른 읽기, 단순함, 웹 친화적 |
| **읽기 성능** | 보통 | 우수 (캐시 활용) |
| **복잡한 쿼리** | 우수 | 보통 |
| **데이터 타입** | JSONB, 배열, hstore 등 풍부 | 기본 타입 중심 |
| **트랜잭션** | ACID 준수, 안정성 우수 | InnoDB로 지원 |

#### Work Hub에서 PostgreSQL을 선택한 이유
- 복잡한 비즈니스 로직 처리
- 데이터 무결성과 안정성 중요
- 분석/통계 작업 필요
- JSONB를 활용한 유연한 데이터 저장

---

### 알림 기능 아키텍처 (Redis + SSE)
```
[사용자 액션] 
    → [API 서버] 
        → DB 저장 
        → Redis Publish (알림 이벤트)
            → [Redis Pub/Sub]
                → [알림/SSE 서버]
                    → 각 사용자 SSE 커넥션에 Push
                        → [브라우저 EventSource]
```

---

## 3. 프로젝트 구조

### Backend (도메인형 레이어드 아키텍처)
```
src/main/java/com/workhub
│
├── WorkHubApplication.java
│
├── global/                          # 전역 설정
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── util/
│
├── user/                            # 사용자 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│       ├── request/
│       └── response/
│
├── company/                         # 회사 도메인
├── project/                         # 프로젝트 도메인
├── projectnode/                     # 프로젝트 단계 도메인
├── checklist/                       # 체크리스트 도메인
├── post/                            # 게시판 도메인
├── cspost/                          # CS 게시판 도메인
├── notification/                    # 알림 도메인
└── changelog/                       # 변경 로그 도메인
```

---

## 4. 공통 응답 형식

모든 API 응답은 아래 형식을 따릅니다.

### 성공 응답
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    // 응답 데이터
  }
}
```

### 생성 응답
```json
{
  "success": true,
  "code": "CREATED",
  "message": "생성이 완료되었습니다.",
  "data": {
    // 생성된 데이터
  }
}
```

### 실패 응답
```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "data": null
}
```

### ApiResponse 클래스
```java
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, "CREATED", message, data);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
```

---

## 5. 예외 처리

프로젝트는 전역 예외 처리 시스템을 사용합니다. 각 레이어에 맞는 Exception을 사용하세요.

### 예외 클래스 사용 규칙

| 레이어 | Exception 클래스 |
|--------|------------------|
| Service | `BusinessException` |
| Controller | `ControllerException` |

### ErrorCode 추가 방법

새로운 에러가 필요한 경우 `ErrorCode.java`에 상황에 맞는 코드를 추가하세요.
```java
// ErrorCode.java 예시

// 사용자 관련
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-001", "사용자를 찾을 수 없습니다."),
USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "U-002", "이미 존재하는 사용자입니다."),

// 프로젝트 관련
PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PJ-001", "프로젝트를 찾을 수 없습니다."),
PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PJ-002", "프로젝트 접근 권한이 없습니다."),

// 게시글 관련
POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P-001", "게시글을 찾을 수 없습니다."),
POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "P-002", "이미 삭제된 게시글입니다."),

// 댓글 관련
COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C-001", "댓글을 찾을 수 없습니다."),

// 체크리스트 관련
CHECK_LIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CL-001", "체크리스트 항목을 찾을 수 없습니다."),
CONFIRM_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "CL-002", "동의 권한이 없습니다."),
```

### Service 레이어 사용 예시
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
```

---

## 6. API 설계 규칙

### URL 규칙
- **camelCase** 사용
- 복수형 명사 사용
- 동사 대신 HTTP Method로 행위 표현
```
✅ 올바른 예시
GET    /api/checkListItems
POST   /api/checkListItems
PATCH  /api/checkListItems/{id}
DELETE /api/checkListItems/{id}
GET    /api/checkListItems/{id}/comments
PATCH  /api/checkListComments/{id}
GET    /api/projectNodes/{id}
```

### Request/Response 규칙
- **camelCase** 사용
- 선택적 필드는 생략 (null 전달 지양)
```json
// Request
{
  "itemTitle": "반응형 웹 필요",
  "description": "PC/태블릿/모바일 대응",
  "hashtag": "REQ_DEF"
}

// Response
{
  "templateId": 1,
  "itemTitle": "반응형 웹 필요",
  "createdAt": "2025-01-15T14:30:00"
}
```

### HTTP Status Code

| 상황 | Status Code |
|------|-------------|
| 조회 성공 | 200 OK |
| 생성 성공 | 201 Created |
| 수정/삭제 성공 | 200 OK |
| 잘못된 요청 | 400 Bad Request |
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 서버 오류 | 500 Internal Server Error |

---

## 7. 코드 컨벤션

### 7-1. 네이밍 규칙

| 구분 | 규칙 | 예시 |
|------|------|------|
| 변수/함수 | camelCase | `getUserData`, `userInfo` |
| 클래스 | PascalCase | `UserProfile`, `ProjectService` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | lowercase | `com.workhub.user` |
| URL | camelCase | `/api/checkListItems` |

### 7-2. 들여쓰기 및 공백

- **탭 크기**: Tabs
- **라인 길이**: 80자 제한 권장
- **함수 간 공백**: 함수와 함수 사이 한 줄 공백 유지
- **중괄호 위치**: 같은 줄에 시작
```java
// 올바른 예시
if (condition) {
    // code
}

public void method() {
    // code
}
```

### 7-3. 주석 규칙

- **함수 주석**: 함수 상단에 해당 함수의 목적, 입력값, 반환값 명시
- **코드 설명 주석**: 코드가 복잡하거나 중요한 부분에 설명 추가
- 단, 무분별한 주석은 제한
```java
/**
 * 사용자 ID로 사용자를 조회합니다.
 *
 * @param userId 사용자 ID
 * @return User 엔티티
 * @throws BusinessException 사용자를 찾을 수 없는 경우
 */
public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
}
```

### 7-4. 코드 구조

- **모듈화**: 관련 기능별로 코드를 모듈화
- **파일 구조 통일**: BE/FE 각각의 구조 준수
- **단일 책임 원칙**: 하나의 클래스/함수는 하나의 역할만 수행

### 7-5. DTO/Entity 규칙

#### Record 사용
- **DTO는 Java Record를 사용**하여 불변 객체로 작성합니다.
- Record는 간결한 문법과 불변성을 제공하여 안전한 데이터 전달이 가능합니다.

#### Static 메서드 네이밍 규칙
- **Record**: `from` static 메서드 사용 (Entity → DTO 변환)
- **Entity**: `of` static 메서드 사용 (DTO → Entity 변환)

```java
// Record 예시 (DTO)
public record UserResponse(
    Long userId,
    String username,
    String email
) {
    // Entity → DTO 변환
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
}

// Entity 예시
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;

    // DTO → Entity 변환
    public static User of(UserCreateRequest request) {
        User user = new User();
        user.username = request.username();
        user.email = request.email();
        return user;
    }
}
```

---

## 8. Git 전략

### 8-1. 브랜치 네이밍
```
main       → 운영 배포용
develop    → 개발 통합
feature/*  → 기능 단위
fix/*      → 버그 수정
hotfix/*   → 긴급 수정
refactor/* → 코드 리팩토링
```

### 8-2. 커밋 메시지
```
feat:     새로운 기능 추가
fix:      버그 수정
docs:     문서 수정
style:    코드 포맷팅, 세미콜론 누락 등
refactor: 코드 리팩토링
test:     테스트 코드 추가
chore:    빌드 업무 수정, 패키지 매니저 수정
```

### 커밋 메시지 예시
```
feat: 사용자 로그인 기능 구현
fix: 체크리스트 동의 시 권한 검증 오류 수정
docs: README 설치 가이드 추가
refactor: UserService 코드 정리
```

---

## 9. PR/코드 리뷰 프로세스

### 9-1. PR 생성 절차

1. `feature/*` 브랜치에서 작업
2. 작업 완료 후 `develop` 브랜치로 PR 생성
3. PR 템플릿에 맞춰 작성

### 9-2. PR 템플릿
```markdown
## 작업 내용
- 구현한 기능 또는 수정 사항 설명

## 변경 사항
- 변경된 파일 및 주요 로직 설명

## 테스트
- [ ] 로컬 테스트 완료
- [ ] 단위 테스트 작성

## 스크린샷 (선택)
- UI 변경 시 스크린샷 첨부
```

### 9-3. 리뷰 규칙

- 최소 **2명 이상**의 리뷰어 승인 필요
- 모든 **CI 테스트 통과** 확인
- 리뷰어는 24시간 이내 리뷰 진행
- Approve 전 모든 코멘트 해결

### 9-4. 머지 규칙

- Squash Merge 사용 권장
- 머지 후 feature 브랜치 삭제

---

## 10. 기타 참고사항

### 환경별 설정
- `application.yml`: 공통 설정
- `application-dev.yml`: 개발 환경
- `application-prod.yml`: 운영 환경

### 문서화
- API 문서: Swagger UI (`/swagger-ui.html`)
- ERD: `/docs/erd.png`

### 모니터링
- 에러 트래킹: Sentry 대시보드에서 확인

---

## 11. 로그 관리

### 11-1. Docker Compose 실행

```bash
# 전체 서비스 실행 (Redis + Spring Boot)
DB_PASSWORD=your_password docker compose up -d

# 빌드 포함 실행
DB_PASSWORD=your_password docker compose up --build -d

# 로그 확인
docker logs -f workhub-app

# 서비스 중지
docker compose down
```

### 11-2. 로그 확인 방법

| 방법 | 명령어/위치 | 용도 |
|------|------------|------|
| **Docker Logs** | `docker logs -f workhub-app` | 실시간 디버깅 |
| **로컬 파일** | `docker exec workhub-app tail -f /app/logs/workhub.log` | 로컬 백업 |
| **CloudWatch Console** | AWS Console → CloudWatch → Logs → /ecs/workhub | 검색, 필터링, 분석 |
| **CloudWatch CLI** | `aws logs tail /ecs/workhub --follow` | CLI에서 실시간 확인 |

### 11-3. 로그 활용 예시

#### 개발 환경
```bash
# 실시간 로그 확인
docker logs -f workhub-app

# ERROR만 필터링
docker logs workhub-app 2>&1 | grep ERROR

# 최근 100줄만 보기
docker logs --tail 100 workhub-app
```

#### 운영 환경 (EC2)
```bash
# CloudWatch Logs 실시간 확인
aws logs tail /ecs/workhub --follow

# 특정 키워드 검색
aws logs filter-log-events \
  --log-group-name /ecs/workhub \
  --filter-pattern "ERROR"
```

### 11-4. CloudWatch Logs 설정

#### EC2 IAM 역할 설정

1. **IAM → Roles → Create role**
2. **Trusted entity**: AWS service → EC2
3. **Permissions**: `CloudWatchAgentServerPolicy` 선택
4. **Role name**: `EC2-CloudWatch-Logs-Role`
5. **EC2 인스턴스에 역할 부여**: EC2 Console → Actions → Security → Modify IAM role

#### 로그 확인
- **Console**: CloudWatch → Logs → Log groups → `/ecs/workhub`
- **CLI**: `aws logs tail /ecs/workhub --follow`

### 11-5. 로그 파일 위치

- **컨테이너 내부**: `/app/logs/workhub.log`
- **호스트 볼륨**: Docker volume `work-hub_app_logs`
- **CloudWatch**: Log group `/ecs/workhub`, Stream `workhub-app`

---

**작성자**: Work Hub 개발팀
