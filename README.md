![budderz.JPG](docs/images/budderz.JPG)

# 🤝 벗터 (Buddy Space)

**벗터**는 친구를 뜻하는 `벗`과 자리/장소를 뜻하는 `터`의 합성어로,  
**친구를 만드는 자리**, **친구와 함께하는 공간**을 의미합니다.

> 관심사가 맞는 친구를 만나고, 관계를 자연스럽게 이어가는 것이  
> 바쁜 일상 속에서는 생각보다 쉽지 않습니다.  
> 벗터는 이런 만남과 관계가 더 쉬워지도록 돕는 커뮤니티 플랫폼입니다.

<br>

## 📌 서비스 개요

- 사용자는 관심사와 성향에 따라 **온라인, 오프라인, 온·오프라인 혼합 모임**을 직접 생성할 수 있습니다.
- **취미, 학교, 업무, 운동, 게임, 스터디 등 다양한 주제**의 모임에 가입하거나 초대 링크로 바로 참여할 수 있습니다.
- 초대 링크로 빠르게 참여하거나, 가입 요청/승인/거절 등 체계적인 멤버십 관리도 지원합니다.
- 사진첩, 일정, 미션, 투표, 채팅 등 **활발한 커뮤니티 활동**을 위한 기능이 통합되어 있습니다.

<br>

## 🚩 프로젝트 기간

- **진행 기간**: 2025.05.27 ~ 2025.07.07 (6주)
- **기획·디자인**: 약 1주
- **개발 기간**: 약 5주 (주간 스프린트 및 매일 코드 리뷰 진행)

<br>

## 🛠 기술 스택

### 🧱 Backend
- **Language**: Java 17 (Toolchain)
- **Framework**: Spring Boot 3.5.0, Spring Security, Spring Data JPA, Spring WebFlux(WebClient)
- **DBMS**: PostgreSQL, Redis (Redisson)
- **Build**: Gradle
- **Query**: QueryDSL 5 (Jakarta)
- **Testing**: JUnit, Spring Boot Test

### 🛡 인증/보안
- JWT (jjwt-api, jjwt-impl, jjwt-jackson)
- OAuth2 (Google)

### ☁ 클라우드 & 배포
- AWS EC2, S3, OpenSearch
- Docker / Docker Compose
- GitHub Actions (CI/CD)
- Nginx + Certbot (SSL 자동화)

### 🔔 실시간 통신
- SSE (Server-Sent Events) - 알림
- WebSocket - 채팅

<br>

## ✨ 주요 기능

### 🔐 인증/인가
- 이메일, 구글 소셜 로그인
- JWT 기반 인증, Refresh Token 재발급
- 환경별 CORS 설정

### 👤 사용자
- 회원가입 / 로그인 / 정보 조회 및 수정 / 탈퇴

### 🏡 동네 인증
- 현재 위치 기반 동네 인증 (좌표 → 주소 → 저장)
- 내 동네 관리 및 조회

### 👥 모임
- 모임 생성/수정/삭제
- 가입 요청, 초대 링크, 멤버 차단/강퇴/권한 관리
- 접근 권한 설정 및 예외 처리

### 📎 첨부파일
- S3 업로드, Presigned URL 다운로드
- 썸네일 생성 및 저장
- 게시글 이미지/영상 미리보기
- 프로필/커버 이미지 첨부 및 적용

### 📝 게시글
- 일반/공지/예약 게시글 작성 및 수정
- 댓글, 대댓글 기능 포함

### 📆 일정, 미션, 투표
- 일정 관리 (생성, 조회, 수정, 삭제)
- 미션 및 미션 인증 관리
- 투표 생성, 진행, 종료

### 💬 실시간 채팅
- WebSocket 기반 채팅방 관리
- 메시지 읽음 처리 및 상태 동기화

### 📡 로깅 & 모니터링
- AOP 기반 요청 로깅
- Fluent Bit → OpenSearch
- Discord 실시간 알림

<br>

## 🔗 시스템 문서 및 설계

- **Cloud Architecture**  
![architecture.png](docs/images/architecture.png)

- **ERD**  
![erd.png](docs/images/erd.png)

- **API 문서**
    - [벗터 API 명세서](https://team-budderz.github.io/buddy-space-api/)
    - [Postman 문서](https://documenter.getpostman.com/view/43185152/2sB2xEA8Fu)
    - [WebSocket 채팅 API](https://www.notion.so/WebSocket-API-21dd1de91b88801db74be46dd0d6c9af?pvs=21)
    - [SSE 알림 API](https://www.notion.so/SSE-API-21dd1de91b8880e8ae96ff3ed30bd620?pvs=21)

<br>

## ⚙ 기술적 의사 결정

- [JWT 인증 설계](https://www.notion.so/JWT-21d2dc3ef51480abb5beed16ac6b0f32?pvs=21)
- [위치 기반 동네 인증](https://www.notion.so/21d2dc3ef514800a9362d5dd8b6cf2fa?pvs=21)
- [모임 도메인 구조 및 정책](https://www.notion.so/21d2dc3ef514804189fff7e22702fbc2?pvs=21)
- [S3 첨부파일 처리 구조](https://www.notion.so/S3-21d2dc3ef514804daa5bc8dbe67cdff7?pvs=21)
- [SSE 기반 알림 시스템](https://www.notion.so/SSE-Server-Sent-Events-21d2dc3ef5148037833ed6703e144816?pvs=21)
- [도커 기반 인프라 구성](https://www.notion.so/21d2dc3ef51480a9b3adcc8f6e484b94?pvs=21)

<br>

## 🧩 트러블슈팅 및 성능 개선

- [Presigned URL 관련 이미지 유지 이슈](https://www.notion.so/Presigned-URL-21d2dc3ef514806cbbb2d944f1d9fed5?pvs=21)
- [댓글 API 모든 댓글 반환 문제](https://www.notion.so/API-21d2dc3ef514805b8945caf0d9c98716?pvs=21)
- [WebSocket 연결 오류 해결](https://www.notion.so/WebSocket-Connection-Failed-21d2dc3ef514805b8d10ea6f6cab81dc?pvs=21)
- [SSE 실시간 알림 수신 실패](https://www.notion.so/SSE-21d2dc3ef51480968ed0ff88f4dd892a?pvs=21)

<br>

## 👨‍👩‍👧‍👦 팀 소개

> 내일배움캠프 최종 프로젝트 - **벗터즈 (Budderz)** 팀

| 이름                                       | 역할        | 담당 업무               |
|------------------------------------------|-----------|---------------------|
| [@withong](https://github.com/withong)   | 팀장 / 백엔드  | 모임, 멤버십, 첨부파일       |
| [@exmrim](https://github.com/exmrim)     | 부팀장 / 백엔드 | 인증/인가, 사용자          |
| [@dawn](https://github.com/dawn0920)     | 백엔드       | 게시글 및 댓글, 실시간 알림    |
| [@somin](https://github.com/somin-jeong) | 백엔드       | 배포 및 CI/CD, 로깅/모니터링 |
| [@ssongyi](https://github.com/jrl103)    | 백엔드       | 실시간 채팅              |
| [@haneul](https://github.com/haneul02)         | 프론트엔드     | 메인 UI/UX 및 프론트엔드 전반 |

<br>

## 📚 프로젝트 정보

> 본 프로젝트는 **내일배움캠프 최종 프로젝트 (Spring 6기)** 로 진행되었습니다.

