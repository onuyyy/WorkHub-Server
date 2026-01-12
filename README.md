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

웹 개발사와 고객사 간의 프로젝트 관리 및 커뮤니케이션을 위한 웹 기반 협업 플랫폼

## 프로젝트 소개

Work Hub는 웹 개발 프로젝트의 전체 생명주기를 관리하고, 개발사와 고객사 간의 원활한 소통을 지원하는 프로젝트 관리 시스템입니다. 프로젝트 단계별 진행 상황을 체크리스트 기반으로 추적하고, 실시간 알림을 통해 모든 이해관계자가 프로젝트 현황을 파악할 수 있습니다.

## 주요 기능

### 프로젝트 관리
- **단계별 프로젝트 관리**: 요구사항 정의 → 화면설계 → 디자인 → 퍼블리싱 → 개발 → QA
- **체크리스트 기반 진행도 추적**: 각 단계별 상세 체크리스트로 진행 상황 관리
- **프로젝트 상태 관리**: 진행 중, 완료, 보류 등 상태별 프로젝트 분류

### 커뮤니케이션
- **프로젝트 게시판**: 프로젝트별 공지사항 및 소통 공간
- **CS 게시판**: 고객 지원 및 문의 관리
- **댓글 시스템**: 게시글 및 체크리스트 항목별 의견 교환
- **실시간 알림**: SSE 기반 실시간 알림 시스템

### 권한 관리
- **역할 기반 접근 제어**: 관리자, 개발자, 고객사 역할별 권한 관리
- **프로젝트별 멤버 관리**: 개발 팀원 및 고객사 담당자 지정

### 변경 이력 추적
- **통합 히스토리**: 모든 엔티티의 생성, 수정, 삭제 이력 자동 기록
- **감사 로그**: IP 주소, User Agent 등 메타데이터 포함

## 기술 스택

### Backend
- **Java 21**: 최신 LTS 버전
- **Spring Boot 3.5.7**: 프레임워크
- **Spring Security**: 세션 기반 인증/인가
- **Spring Data JPA**: ORM
- **QueryDSL**: 타입 안전한 복잡한 쿼리 작성

### Database
- **PostgreSQL 16**: 메인 데이터베이스
  - JSONB를 활용한 히스토리 데이터 저장
  - View를 활용한 통합 히스토리 조회

### Infrastructure
- **Docker & Docker Compose**: 컨테이너 기반 배포
- **AWS S3**: 파일 저장소
- **GitHub Actions**: CI/CD 파이프라인
- **Prometheus & Grafana**: 모니터링 및 메트릭 시각화

## CI 파이프라인

- `.github/workflows/ci.yml` 워크플로우가 `main`, `develop` 대상 PR 생성·업데이트 시 자동으로 실행되어 `./gradlew clean test --build-cache`를 수행합니다.
- 워크플로우 내부 `services`에서 Postgres 16, Redis 7 컨테이너를 띄우고 `SPRING_PROFILES_ACTIVE=test`로 테스트 전용 설정(`src/main/resources/application-test.yml`)을 적용합니다.
- CI 실행에 필요한 Secrets: `CI_DB_PASSWORD`(데이터베이스 비밀번호). 필요 시 `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_REDIS_*`를 추가로 등록하면 기본값 대신 사용할 수 있습니다.
- 로컬에서 CI 환경을 재현하려면 `docker compose up postgres redis` 실행 후 `SPRING_PROFILES_ACTIVE=test ./gradlew clean test`를 수행하세요.

---

## 시작하기

### 사전 요구사항
- Java 21 이상
- Docker & Docker Compose
- PostgreSQL 16 (로컬 개발 시 Docker 사용 권장)

### 환경 설정

1. 저장소 클론
```bash
git clone https://github.com/your-org/workhub.git
cd workhub
```

2. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일 편집하여 필요한 값 설정
```

3. Docker Compose로 실행
```bash
# 애플리케이션 실행 (PostgreSQL 포함)
DB_PASSWORD=your_password docker compose --profile app up -d

# 모니터링 스택 실행 (선택사항)
docker compose --profile monitor up -d
```

### 로컬 개발 (Gradle)

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.workhub.project.service.ProjectServiceTest"

# 애플리케이션 실행 (dev 프로필)
./gradlew bootRun
```

### 접속 정보

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:9090 (모니터링 스택 실행 시)
- **Grafana**: http://localhost:3000 (모니터링 스택 실행 시)
  - 기본 계정: admin/admin

## 프로젝트 구조

```
src/main/java/com/workhub/
├── WorkhubApplication.java      # 애플리케이션 진입점
├── global/                       # 전역 설정 및 공통 컴포넌트
│   ├── config/                   # Spring 설정 (Security, QueryDSL, S3, Swagger)
│   ├── error/                    # 예외 처리 및 ErrorCode
│   ├── response/                 # 공통 API 응답 포맷
│   ├── security/                 # 인증/인가 핸들러
│   ├── entity/                   # 공통 엔티티 (BaseTimeEntity, BaseHistoryEntity)
│   ├── notification/             # 알림 공통 헬퍼
│   └── util/                     # 유틸리티 클래스
│
├── userTable/                    # 사용자 및 회사 관리
├── project/                      # 프로젝트 관리
├── projectNode/                  # 프로젝트 단계 관리
├── checklist/                    # 체크리스트 관리
├── post/                         # 게시판 (공지사항, 일반 게시글)
├── cs/                          # 고객 지원 (CS 게시판, Q&A)
├── projectNotification/          # 실시간 알림 (SSE)
├── history/                      # 통합 변경 이력
├── dashboard/                    # 대시보드 및 통계
└── file/                        # 파일 업로드/다운로드 (S3)
```

각 도메인은 다음과 같은 레이어로 구성됩니다:
- `controller/` - REST API 엔드포인트
- `service/` - 비즈니스 로직 (CRUD별 분리)
- `repository/` - JPA Repository 및 QueryDSL 구현체
- `entity/` - JPA 엔티티
- `dto/request/`, `dto/response/` - Java Record 기반 DTO

## API 응답 형식

모든 API는 표준화된 응답 포맷을 사용합니다:

### 성공 응답
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "id": 1,
    "name": "프로젝트명"
  }
}
```

### 에러 응답
```json
{
  "success": false,
  "code": "PJ-001",
  "message": "프로젝트를 찾을 수 없습니다.",
  "data": null
}
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 빌드 캐시를 사용한 테스트 (CI 환경)
./gradlew clean test --build-cache

# 특정 패턴의 테스트만 실행
./gradlew test --tests "*ProjectService*"
```

### CI/CD

GitHub Actions를 통해 자동화된 테스트가 실행됩니다:
- **트리거**: `main`, `dev` 브랜치로의 Pull Request
- **환경**: PostgreSQL 16
- **실행**: `./gradlew clean test --build-cache`

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

### 1. 도메인 기반 레이어드 아키텍처
각 도메인이 독립적인 레이어(Controller-Service-Repository-Entity)를 가지며, 도메인 간 의존성을 최소화합니다.

### 2. QueryDSL 활용
복잡한 동적 쿼리는 QueryDSL을 사용하여 타입 안전성과 가독성을 확보합니다.

### 3. 통합 히스토리 시스템
- 모든 엔티티의 변경 이력을 자동으로 추적
- PostgreSQL View를 통해 통합 조회
- JSONB로 변경 전 데이터 스냅샷 저장

### 4. 실시간 알림 (In-Memory SSE)
```
[사용자 액션] → [Service] → DB 저장
                              ↓
                    [NotificationEmitterService]
                              ↓
                        [SSE 연결된 클라이언트]
```
- 단일 인스턴스 환경에서 `ConcurrentHashMap` 기반 메모리 관리
- 멀티 인스턴스 확장 시 Redis Pub/Sub 추가 가능

### 5. 세션 기반 인증
JWT 대신 Spring Security의 세션 기반 인증을 사용하여 보안성을 강화합니다.

## 모니터링

### Actuator Endpoints
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Prometheus & Grafana
```bash
# 모니터링 스택 시작
docker compose --profile monitor up -d
```

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

## 문서

- **API 문서**: [Swagger UI](http://localhost:8080/swagger-ui.html)
  
## 라이선스

이 프로젝트는 비공개 프로젝트입니다.

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
