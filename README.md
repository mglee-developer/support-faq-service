# 🤖 FAQ & RAG AI 고객지원 챗봇

Spring Boot 기반의 **FAQ 관리 및 RAG(Retrieval-Augmented Generation) 고객지원 챗봇 백엔드 서비스**입니다.

사용자의 질문과 관련된 FAQ를 검색하여 LLM의 Context로 활용하고, Redis 기반 인기 FAQ 캐싱과 Resilience4j Circuit Breaker를 적용하여 **조회 성능과 외부 AI 서비스 장애 대응**을 고려했습니다.

---

## 🛠 Tech Stack

| 구분 | 기술 |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot, Spring MVC |
| ORM | Spring Data JPA |
| Database | MySQL |
| Cache | Redis |
| Fault Tolerance | Resilience4j Circuit Breaker |
| AI / LLM | LlmClient 인터페이스 기반 Mock LLM |
| Build | Gradle |
| Etc | Lombok, Bean Validation |

---

## 💡 주요 기능

### 1. RAG 기반 고객지원 챗봇

사용자 질문과 관련된 FAQ를 검색하고 해당 데이터를 LLM의 Context로 전달하여 답변을 생성합니다.

```text
사용자 질문
     │
     ▼
FAQ 키워드 검색
     │
     ├── FAQ 없음
     │      │
     │      ▼
     │   Fallback 응답
     │
     └── FAQ 존재
            │
            ▼
     System Prompt 생성
            │
            ▼
         LlmClient
            │
            ▼
     Circuit Breaker
        │         │
      성공       장애
        │         │
        ▼         ▼
    AI 응답    Fallback
        │         │
        └────┬────┘
             ▼
      SearchHistory 저장
             │
             ▼
       ChatResponse 반환
```

검색된 FAQ가 없는 경우와 LLM 서비스에 장애가 발생한 경우를 구분하여 Fallback 응답을 제공합니다.

---

### 2. Resilience4j Circuit Breaker 기반 LLM 장애 대응

외부 LLM 서비스의 장애가 전체 서비스로 전파되는 것을 방지하기 위해 `LlmClient`에 Circuit Breaker를 적용했습니다.

```text
ChatService
     │
     ▼
LlmClient
     │
     ▼
Circuit Breaker
     │
     ├── CLOSED
     │     └── LLM 정상 호출
     │
     ├── OPEN
     │     └── LLM 호출 차단
     │
     └── HALF_OPEN
           └── 제한된 요청으로 복구 여부 확인
```

Circuit Breaker는 외부 시스템과의 통신 경계인 `LlmClient`에 적용하고, 사용자에게 어떤 Fallback 응답을 제공할지는 비즈니스 계층인 `ChatService`에서 처리하도록 책임을 분리했습니다.

주요 설정은 다음과 같습니다.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      llmClient:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 5
        minimumNumberOfCalls: 3
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 2
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

---

### 3. Redis 기반 인기 FAQ 캐싱

조회수가 높은 FAQ 상위 10개를 Redis에 캐싱하여 반복적인 DB 조회를 줄였습니다.

```java
@Cacheable(
    value = "popularFaqs",
    key = "'top10'"
)
```

Redis Cache의 TTL은 **10분**으로 설정했습니다.

```text
인기 FAQ 요청
     │
     ▼
Redis Cache 확인
     │
     ├── HIT ──────► Cache 데이터 반환
     │
     └── MISS
           │
           ▼
        DB 조회
           │
           ▼
      Top 10 반환
           │
           ▼
       Redis 저장
```

---

### 4. 인기 FAQ Cache 무효화 정책

캐시의 성능과 데이터 정합성을 함께 고려하여 데이터 특성에 따라 무효화 정책을 구분했습니다.

#### FAQ 콘텐츠 변경

FAQ 등록과 같이 콘텐츠 자체가 변경되는 경우 `@CacheEvict`를 사용하여 기존 인기 FAQ Cache를 즉시 제거합니다.

```java
@CacheEvict(
    value = "popularFaqs",
    key = "'top10'"
)
```

#### 조회수 변경

FAQ 상세 조회 시 `viewCnt`를 증가시키지만, 조회마다 Cache를 제거하지 않습니다.

조회가 발생할 때마다 Cache를 무효화하면 Cache Hit Ratio가 크게 낮아질 수 있기 때문에 인기 순위는 **Redis TTL 만료 후 최신 조회수를 기준으로 갱신**하도록 구성했습니다.

```text
FAQ 상세 조회
      │
      ▼
 viewCnt + 1
      │
      └── Cache 유지
              │
              ▼
         TTL 10분 만료
              │
              ▼
     다음 인기 FAQ 요청
              │
              ▼
        최신 Top 10 조회
```

즉, 인기 FAQ 데이터에는 일정 수준의 **Eventual Consistency**를 허용하고 캐시 효율을 확보했습니다.

---

### 5. FAQ 검색과 조회수 분리

키워드 검색 결과에 포함되었다는 이유만으로 조회수를 증가시키지 않고, 사용자가 실제 FAQ 상세 내용을 조회했을 때만 조회수를 증가시키도록 분리했습니다.

```text
GET /api/v1/faqs/search?keyword=배송
→ FAQ 검색
→ viewCnt 증가 X

GET /api/v1/faqs/{faqId}
→ FAQ 상세 조회
→ viewCnt +1
```

이를 통해 인기 FAQ 순위가 단순 검색 노출 횟수가 아닌 **실제 FAQ 조회 횟수**를 기준으로 계산되도록 했습니다.

---

### 6. Session 기반 대화 이력 관리

사용자의 질문과 AI 응답을 `SearchHistory`에 저장하고 `sessionId`를 함께 관리합니다.

```text
sessionId
    │
    ├── 사용자 질문
    ├── AI 응답
    └── 생성 시간
```

동일한 `sessionId`를 기준으로 대화 이력을 조회할 수 있도록 구성하여 향후 멀티턴 대화 Context 구성으로 확장할 수 있도록 설계했습니다.

---

### 7. 전역 예외 처리

`@RestControllerAdvice`를 이용하여 API에서 발생하는 예외를 공통으로 처리합니다.

- Bean Validation 실패
- 잘못된 요청
- 리소스 조회 실패
- 서버 내부 오류

공통 `ErrorResponse` 형태로 응답하여 API의 예외 응답 형식을 일관되게 유지합니다.

---

## 🔄 주요 요청 흐름

### Chat

```text
POST /api/v1/chat
       │
       ▼
   ChatService
       │
       ▼
FAQ Repository
       │
       ├── 검색 결과 없음 ──► FAQ_NOT_FOUND Fallback
       │
       ▼
System Prompt 생성
       │
       ▼
   LlmClient
       │
       ▼
Circuit Breaker
       │
       ├── 성공 ──► AI Response
       │
       └── 장애 ──► LLM_UNAVAILABLE Fallback
                         │
       ┌─────────────────┘
       ▼
SearchHistory
(sessionId 포함)
       │
       ▼
 ChatResponse
```

---

## 🔌 API Endpoints

| Method | URI | 설명 |
|---|---|---|
| `POST` | `/api/v1/faqs` | FAQ 신규 등록 |
| `GET` | `/api/v1/faqs?category={category}` | 카테고리별 FAQ 조회 |
| `GET` | `/api/v1/faqs/{faqId}` | FAQ 상세 조회 및 조회수 증가 |
| `GET` | `/api/v1/faqs/popular` | 조회수 기준 인기 FAQ 상위 10개 조회 |
| `GET` | `/api/v1/faqs/search?keyword={keyword}` | 키워드 FAQ 검색 |
| `POST` | `/api/v1/chat` | RAG 기반 챗봇 질문 |
| `GET` | `/api/v1/search-histories` | 검색 및 대화 이력 조회 |

---

## 📌 Troubleshooting & Design Decisions

### 1. LLM 장애가 전체 서비스로 전파되는 문제

**문제**

외부 LLM API가 지연되거나 장애가 발생할 경우 고객지원 서비스까지 영향을 받을 수 있습니다.

**해결**

Resilience4j Circuit Breaker를 외부 시스템 통신 경계인 `LlmClient`에 적용했습니다.

일정 실패율 이상에서는 Circuit을 OPEN하여 LLM 호출을 차단하고 `ChatService`에서 Fallback 응답을 제공하도록 구성했습니다.

---

### 2. 인기 FAQ Cache와 DB 데이터 불일치

**문제**

인기 FAQ를 Redis에 캐싱하면 DB의 조회수 또는 FAQ 데이터가 변경되어도 기존 Cache가 반환될 수 있습니다.

**해결**

FAQ 콘텐츠 변경 시에는 `@CacheEvict`로 Cache를 즉시 무효화하고, 빈번하게 발생할 수 있는 조회수 변경은 매번 Cache를 제거하지 않고 10분 TTL을 통해 갱신하도록 설계했습니다.

이를 통해 데이터 정합성과 Cache Hit Ratio 사이의 Trade-off를 고려했습니다.

---

### 3. 검색 결과 노출과 실제 FAQ 조회 구분

**문제**

검색 결과에 포함된 모든 FAQ의 조회수를 증가시키면 실제 사용자가 읽지 않은 FAQ까지 인기 FAQ 순위에 영향을 미칩니다.

**해결**

키워드 검색과 FAQ 상세 조회를 분리하고 상세 조회 시에만 `viewCnt`를 증가시키도록 변경했습니다.

---

### 4. 대화 이력의 세션 식별 문제

**문제**

질문과 AI 응답만 저장할 경우 서로 다른 세션의 대화 이력을 구분하기 어렵습니다.

**해결**

`SearchHistory`에 `sessionId`를 함께 저장하고 세션별 조회가 가능하도록 Repository를 구성했습니다.

이를 통해 향후 이전 대화 내용을 LLM Context로 전달하는 멀티턴 대화 기능으로 확장할 수 있습니다.

---

### 5. Spring Data Redis JSON 직렬화

Redis Value를 JSON 형태로 저장하기 위해 JSON Serializer를 적용했습니다.

```java
RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json())
        );
```

---

## ⚙️ Configuration

DB 접속 정보, Redis 설정, API Key 등의 민감정보가 포함된 `application.yml`은 Git Repository에 포함하지 않습니다.

프로젝트 실행 시 로컬 환경에 맞는 설정이 필요합니다.

Circuit Breaker 설정 예시는 다음과 같습니다.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      llmClient:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 5
        minimumNumberOfCalls: 3
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 2
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

> 실제 DB 계정, 비밀번호 및 API Key는 Git Repository에 커밋하지 않습니다.

---

## 🚀 향후 개선 사항

- 실제 OpenAI API 연동
- Vector DB / Embedding 기반 FAQ 유사도 검색
- Session 기반 멀티턴 대화 Context 구성
- FAQ 수정/삭제 API 및 Cache Eviction 확대
- Circuit Breaker 상태 및 Cache Hit Ratio 모니터링
- 통합 테스트 및 장애 상황 테스트 보강