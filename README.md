# 🤖 Spring Boot 기반 FAQ & RAG AI 챗봇 서비스

Spring Boot 3.x/4.0 기반의 FAQ 관리 및 RAG(검색 증강 생성) 패턴을 적용한 AI 고객지원 챗봇 백엔드 서비스입니다.  
FAQ 조회 성능 최적화를 위한 **Redis 캐싱**과 실무 수준의 **전역 예외 처리(Exception Handling)**를 갖추고 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

- **Backend**: Java 17+, Spring Boot 3.x / 4.0, Spring Data JPA
- **Database**: H2 Database (개발용) / MySQL
- **Cache**: Redis
- **AI/LLM**: Mock Client (인터페이스 기반 RAG 파이프라인)
- **Build & Tools**: Gradle, Lombok, Git

---

## 💡 핵심 기능 (Key Features)

1. **RAG (Retrieval-Augmented Generation) 기반 챗봇 서비스**
    - 사용자 질문 수신 시 FAQ DB에서 유사 키워드 데이터 조회를 통한 Context 확보
    - 동적 System Prompt 구성 및 LLM 호출
    - LLM 지연/장애 및 FAQ 검색 실패 시 Fallback(1:1 문의 안내) 응답 자동 전환 및 대화 이력 저장

2. **Redis 기반 인기 FAQ 캐싱**
    - 조회수가 높은 상위 10개 인기 FAQ 목록을 Redis 메모리에 캐스 처리
    - 반복적인 DB I/O 호출을 줄여 조회 성능 최적화

3. **안정적인 전역 예외 처리 (Global Exception Handling)**
    - `@RestControllerAdvice`를 활용하여 API 예외 응답 규격화 (`ErrorResponse`)
    - DTO 유효성 검증(`@Valid`) 실패 및 리소스 부재에 대한 세분화된 HTTP 상태 코드 반환

---

## 🔄 RAG 시스템 대화 흐름 (Architecture)
README.md에서 텍스트 다이어그램(ASCII/Text Art)이 줄 바꿈이나 글꼴 크기 때문에 깨져 보이는 이유는 Markdown의 코드 블록(```) 처리가 빠졌거나 Mermaid 다이어그램 구문을 사용하지 않았기 때문입니다.

해결 방법은 크게 2가지가 있습니다. 프로젝트 상황에 맞는 방식을 선택해 보세요.

방법 1. Markdown 코드 블록(```)으로 감싸기 (가장 간단함)
텍스트 아트는 고정 폭 글꼴(Monospace Font)로 출력되어야 줄이 맞춰집니다. 단순히 내용 위아래를 ``` (백틱 3개)로 감싸주기만 해도 깔끔하게 고정되어 깨지지 않습니다.

Markdown
```text
[사용자 질문] 
     │
     ▼
[FaqRepository] ──(키워드 검색)──► FAQ Context 존재? ──(No)──► [Fallback 응답]
     │                                                                 │
    (Yes)                                                              │
     ▼                                                                 │
[System Prompt 동적 생성]                                              │
     │                                                                 │
     ▼                                                                 │
[LlmClient (Mock LLM)] ────(오류 발생/Timeout)─────────────────────────┤
     │                                                                 │
   (성공)                                                              │
     ▼                                                                 ▼
[SearchHistory DB 저장] ◄──────────────────────────────────────────────┘
     │
     ▼
[ChatResponse 반환]
```
---

## 📌 주요 해결 과제 및 트러블 슈팅 (Troubleshooting)

### 1. Spring Boot 4.0 / Spring Data Redis 직렬화 호환성 문제
- **문제**: 기존 `GenericJackson2JsonRedisSerializer` 적용 시 최신 Spring Data Redis / Jackson 3.x 네임스페이스 변경에 따른 Deprecated 경고 발생.
- **해결**: Spring 표준 정적 팩토리 메서드인 `RedisSerializer.json()`을 도입하여 버전에 의존적이지 않은 유연하고 깔끔한 JSON 직렬화 구조로 개선.

### 2. RAG 파이프라인 예외 및 LLM 장애 대응 (Fallback)
- **문제**: 외부 AI API 호출 실패나 적절한 FAQ 검색 결과가 없는 경우 서비스 응답 불능 위험.
- **해결**: Try-Catch 및 조건절 분기를 통해 실패 시 즉시 1:1 문의 안내 매뉴얼 응답(`isFallback = true`)을 생성하도록 설계하여 시스템 가용성 보장.

---

## 🔌 API 엔드포인트 명세 (API Endpoints)

| Method | URI | 설명 |
| :--- | :--- | :--- |
| `POST` | `/api/v1/faqs` | FAQ 신규 등록 |
| `GET` | `/api/v1/faqs` | 카테고리별 FAQ 조회 |
| `GET` | `/api/v1/faqs/popular` | 인기 FAQ 상위 10개 조회 (Redis Cache) |
| `GET` | `/api/v1/faqs/search` | 키워드 FAQ 검색 |
| `POST` | `/api/v1/chat` | RAG 기반 AI 챗봇 대화 요청 |
| `GET` | `/api/v1/search-histories` | 최근 검색 및 대화 이력 조회 |