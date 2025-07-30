# 🟩 NE:MO - 모임 매칭 서비스

**NE:MO**는 조건에 맞는 스터디/모임을 탐색하고 실시간으로 참여할 수 있는 매칭 플랫폼입니다.  
kafka를 통합 데이터 정합성, WebSocket + SSE 기반의 빠른 AI 모임 추천, Redis 캐싱 등 다양한 기술을 도입하여 효율적이고 반응성 높은 서비스를 제공합니다.

---

## 🛠️ Tech Stack

| Category         | Technology |
|------------------|------------|
| Language         | Java 21 |
| Framework        | Spring Boot 3.4.5 |
| Database         | MySQL, Redis |
| Messaging        | Kafka |
| Realtime         | WebSocket, SSE |
| Infrastructure   | Docker, NGINX, AWS EC2 |
| Monitoring       | Sentry, SigNoz |
| CI/CD            | GitHub Actions |

---

## 📖 주요 기능

### 1️⃣ 회원 관리
- 카카오 Oauth를 활용한 회원 가입, 로그인
- Access / Refresh Token 기반 JWT 인증

### 2️⃣ AI 기반 모임 생성
- 모임 생성 시 기본정보만 입력해도 AI가 자동으로 모임 소개, 계획 작성 및 추천
- 모임을 생성하는 시간을 획기적으로 단축

### 3️⃣ 모임 탐색 및 참여
- 조건 기반 탐색 (지역, 카테고리, 키워드, 태그, 모집중)
- 활성화 이벤트를 한 눈에 보기 위해 업데이트순 정렬, 페이징, 키워드 검색 지원

### 4️⃣ 일정 관리
- 모임별 일정 생성
- 참여자 등록 및 나의 일정 확인 가능

### 5️⃣ AI 기반 모임 추천
- Kafka 메시징으로 AI 서버에 요청
- WebSocket 기반 응답 수신 및 비동기 처리

---

## 🗂️ ERD 설계
<img width="1994" height="1053" alt="Screenshot 2025-07-27 at 3 52 44 AM 1" src="https://github.com/user-attachments/assets/8405a249-6747-4ff3-8e10-fcbaeec6b709" />

- `users`: 사용자 정보
- `groups`: 모임 정보
- `tags`: 태그
- `group_tag`: 모임 - 태그
- `group_participants`: 모임 참가자
- `schedules`: 일정
- `schedule_participants`: 일정 참여자
---

## 🧠 기술 블로그 기반 주요 고민 & 해결 전략

### 1. Kafka 도입
- **문제**: 외부 API 호출 지연에 따른 Thread 점유로 BE 에러율 70%, AI의 Chroma DB와의 정합성 깨짐
- **해결**: Kafka 기반 비동기 이벤트 구조로 전환
- **결과**: 정합성 확보, 에러율 감소

📎 [관련 블로그](https://medium.com/@oij3115/tech-which-is-a-better-choice-between-rabbitmq-and-kafka-97721abb410b)

### 2. HeapDump 분석으로 메모리 누수 해결
- **문제**: Full GC가 자주 발생하고, GC 이후에도 Heap 메모리 사용률이 회수되지 않음
- **접근**: MAT를 활용한 HeapDump 분석을 통해 `byte[]`, `char[]`, `Thread`, `AgentBuilder` 등의 비정상 객체를 추적
- **해결**: OpenTelemetry와 ByteBuddy Agent 설정 조정 → 불필요한 객체 할당 제거
- **결과**: 힙 메모리 사용률 감소, Full GC 빈도 5~7회 -> 4회 미만으로 감소

📎 [관련 블로그](https://medium.com/@oij3115/tech-%EB%A9%94%EB%AA%A8%EB%A6%AC-%EB%88%84%EC%88%98-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-heapdump-%EB%B6%84%EC%84%9D-b6c5fe093e94)


### 3. 테스트 코드 성능 최적화 및 프로파일링
- **문제**: 테스트 코드 실행 시간이 10초 이상으로 비효율적
- **접근**: IntelliJ Profiler로 테스트 성능 분석 → `Thread.sleep`, `NettyEventLoop`, 외부 API 호출이 병목
- **해결**: WebClient를 Mock으로 대체, Thread Pool 조절로 개선
- **결과**: 테스트 실행 시간 10s → 6s 512ms로 단축

📎 [관련 블로그](https://medium.com/@oij3115/tech-%EB%8A%90%EB%A6%B0-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%BD%94%EB%93%9C-%ED%94%84%EB%A1%9C%ED%8C%8C%EC%9D%BC%EB%A7%81%EA%B3%BC-%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94%ED%95%98%EA%B8%B0-12fbea6d3e8d)


### 4. Redis 캐시 TTL + Jitter 전략
- **문제**: 캐시 만료 시점에 트래픽 집중 (Cache Stampede) → Redis 부하 및 성능 저하
- **해결**: TTL 설정 시 `Jitter(Random)` 적용 및 null 캐싱 활용하여 트래픽 분산
- **결과**: TTL 충돌 시점 완화, 조회 응답 속도 평균 58ms → 1ms

📎 [관련 블로그](https://medium.com/@oij3115/tech-redis-3-redis-ttl-%EC%8A%A4%ED%83%AC%ED%94%BC%EB%93%9C-penetration-%EB%B0%A9%EC%96%B4-%EC%A0%84%EB%9E%B5-jitter%EC%99%80-null-%EC%BA%90%EC%8B%B1-%ED%99%9C%EC%9A%A9-c5ccfa202870)


### 5. Redisson 분산락으로 동시성 제어
- **문제**: 모임 신청에서 동시 요청으로 인해 데이터 정합성이 깨지는 문제 발생
- **접근**: Redisson Lock 기반의 분산락 도입, `tryLock`과 `waitTime` 조절
- **결과**: race condition 방지, 분산환경에서의 데이터 무결성 확보

📎 [관련 블로그](https://medium.com/@oij3115/tech-redis-2-redisson-%EB%B6%84%EC%82%B0%EB%9D%BD%EC%9C%BC%EB%A1%9C-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4%ED%95%98%EA%B3%A0-%EC%8A%A4%ED%83%AC%ED%94%BC%EB%93%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-with-lettuce-34553cc73bdf)


### 6. Redis 성능 향상 및 병목 해결
- **문제**: MySQL에 불필요하게 연결이 발생하며 병목과 커넥션 낭비가 빈번
- **해결**: Redis 커넥션 풀 설정 최적화, Lettuce 비동기 처리 전략 적용
- **결과**: 동시 처리 성능 **2배 증가**, 메모리 사용률 감소로 시스템 안정화

📎 [관련 블로그](https://medium.com/@oij3115/tech-redis%EB%A1%9C-%EC%84%B1%EB%8A%A5-%ED%96%A5%EC%83%81%EA%B3%BC-%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-9c3f01aea5e7)


### 7. API 병목 개선을 위한 전략
- **문제**: AI 서버 및 DB 호출 지연으로 인해 전체 API 응답 속도 저하
- **해결**: Index 최적화 (불필요한 Index 제거, 컬럼 재정렬로 Range Scan 개선)
- **결과**: 응답 시간 단축, 서버 처리량 증가 및 트래픽 대응력 향상

📎 [관련 블로그](https://medium.com/@oij3115/tech-api-%EB%B3%91%EB%AA%A9-%EA%B0%9C%EC%84%A0%ED%95%98%EA%B8%B0-59ba3d60f065)


---

## 🔧 시스템 아키텍처 (V3 기준)
<img width="4988" height="5180" alt="sdfdfsdfsfsdfdsfsdfsdfs drawio (1)" src="https://github.com/user-attachments/assets/e17ad70a-7ab9-4949-8bab-c603d89c93cf" />

- Kafka 기반 비동기 메시징 (AI 그룹 생성 요청/응답)
- Redis 기반 캐싱 + 분산락
- WebSocket 기반 BE ↔ AI 서버 통신
- SSE 챗봇 Streaming 구현

---

## ✅ 기술 포인트 요약

| 기술 요소        | 설명 |
|------------------|------|
| Kafka            | AI 연동 / 메시지 큐 / DLQ 구성 |
| Redis Cache      | `@Cacheable` + TTL + Jitter → 평균 1ms 응답 |
| Redis Lock       | Redisson 분산락 기반 모임 신청 중복 방지 |
| WebSocket + SSE  | AI 응답 Streaming 처리 → UX 개선|
| Virtual Thread   | Timed Waiting Thread 감소, 응답 속도 향상 |
| Heap 분석         | GC 튜닝 + Agent 비활성화로 메모리 사용률 감소 |
