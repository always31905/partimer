# 💼 **알바 대타 구인 앱 프로젝트(ALT)**

## 🧩 1. 프로젝트 개요

- 근무 전 컨디션 불량·갑작스러운 일정 변경 등으로 알바를 가지 못하는 상황 발생
- 기존에는 단체 채팅방에 문의하거나 지인에게 부탁해야 했고, 원하는 시점까지 구인을 못 할 수 있는 번거로움 존재
- 단기 알바를 통해 알바비를 벌고 싶은 사람과 알바 대타를 수하는 사람을 연결하는 플랫폼 개발

---

## 🎯 2. 프로젝트 목표

- ALT앱을 통해 빠르고 간편하게 대타(Alternative)를 구할 수 있는 서비스를 제공
- 갑작스러운 결근 상황에도 최소한의 손실로 근무 일정을 원활히 유지

---

## 🔄 3. 앱 작동 구조 (End-to-End Flow)

### 🔐 1) 앱 진입 & 인증

- 앱 실행 → `MainActivity` → Firebase Auth 로그인 (Google 계정)
- **첫 로그인** 시:
    
    → `SetNicknameActivity`에서 닉네임 입력 → Firestore `users` 컬렉션에 저장
    
- 로그인 완료 → `HomeActivity` 진입

### 📝 2) 대타 요청 글쓰기

- FAB 클릭 → `WriteFragment` 진입
- 사용자 입력: 날짜, 장소, 시급, 키워드
- 등록 클릭 시 → Firestore `job_posts`에 문서 저장
- `JobPost` 객체는 작성자 정보(`writerUid`, `writerName`)와 근무 조건(`date`, `pay`, `location`, `keywords` 등)을 포함

![Image](https://github.com/user-attachments/assets/0c6ea4ee-6861-4ed2-8f92-b7a578516068)



### 📄 3) 요청 조회 & 필터

- `HomeFragment.loadPosts(구, 날짜, 시급, 키워드)`를 호출해 `job_posts` 컬렉션에서 게시글 데이터를 조회
- 사용자는 `SearchFragment`를 통해 원하는 조건(구/날짜/시급/키워드 등)으로 검색 가능
- 쿼리 결과를 `RecyclerView`에 바인딩, `JobPostAdapter`를 이용해 각 게시글의 제목, 위치, 시급 등의 정보를 출력

---

### 💬 4) 상세조회 & 채팅

- 게시글 클릭 → `DetailFragment` 진입
- 상세 페이지에서는 댓글(Comment) 기능을 통해 요청자에게 지원 의사를 밝히거나 간단하게 문의
- 댓글은 실시간으로 갱신되며, 작성자의 닉네임은 UID 기반으로 캐싱 처리되어 효율적으로 표시
- 사용자가 “채팅하기” 버튼을 클릭하면 Firebase Realtime Database를 통해 1:1 채팅방이 생성
- `ChatFragment` or `ChatRoomActivity`에서 1:1 채팅

---

### 🙋‍♂️ 5) 내 계정 관리

- `AccountFragment`에서 내 프로필 / 지원 글 목록 조회, 수정, 삭제

---

## 🧭 4. 앱 작동 흐름 (How it works)

- **홈(Home)**
    - 전체 대타 요청을 최신순으로 정리하여 recyclerview로 생성
- **글쓰기(Write)**
    - 제목, 내용, 근무 날짜, 장소, 시급, 키워드를 입력 후 “등록하기” → Firestore에 문서 생성
- **검색/필터(Search)**
    - 장소·날짜·시급·키워드별 조건 검색 → 각 조건에 맞는 글만 보여주고 해당 조건이 없을 경우 모든 글을 표시
- **요청 상세 & 지원(Chat)**
    - 게시글 상세 확인 후 “채팅하기” 클릭 → 요청자↔지원자 간 실시간 1:1 대화 → 구인후에는 사용자가 글 삭제
- **마이페이지(Account)**
    - 내가 올린 글 조회, 생성, 삭제.

---

## 🛠️ 5. 기술 스택 & 아키텍처

- **프론트엔드**
    - **Android (Java)**: 네이티브 앱 개발 언어로 사용자 인터페이스 및 로직 구현
    - **MVVM**: UI와 비즈니스 로직 분리를 통한 유지보수성과 테스트 용이성 향상
    - **Jetpack Fragment**: UI 화면을 fragment로 모듈화해 재사용성과 화면 전환 관리 최적화
- **백엔드 & BaaS**
    - Firebase Auth (이메일/구글 로그인)
    - Firebase Firestore (요청·지원·유저 데이터)
    - Firebase Realtime Database(유저간의 1대1 대화)
- **라이브러리 & 툴**
    - RecyclerView: 다수의 데이터를 리스트 형태로 효율적이고 유연하게 표시
    - FragmentTransaction: 화면 간 이동을 안전하고 구조적으로 관리

---

## 🔗 6. Firebase 연동 세부사항

1. **Authentication**
    - 이메일/구글 로그인 (`FirebaseAuth`)
    - 로그인 상태에 따른 화면 접근 제어
2. **Firestore**
    - 컬렉션: `users`, `job_posts`, `comments`
3. Realtime Database
    - 사용자 간 1:1 실시간 채팅 기능 구현
    - 메시지 송수신을 위한 경량 데이터 처리에 사용
    
![Image](https://github.com/user-attachments/assets/b45c0df8-8dcb-4205-8f7d-eac47de20931)

## 🗓️ 7. 향후 계획 & To-Do

:

- 위치 기반 Google Maps SDK 연동, 지도로 위치 선택·시각화 기능
- UI/UX 개선 및 사용자 피드백 반영
- 사용자 평점 기능
- 채팅, 댓글 알람 기능

---

## 👥 8. 팀원 및 역할
