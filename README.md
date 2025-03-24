# MAKAR
<img width="1920" alt="1" src="https://github.com/MAKAR-Andriod/MAKAR/assets/84546438/354bf19c-a022-419a-b3fa-49c0ff15fe7f">


## 프로젝트 개요

- **목적**: 사용자가 지하철을 이용할 때, **정확한 막차 시간과 실제 도착 가능 경로를 안내**하여 막차를 놓치지 않도록 도와주는 앱
- **배경**: 단순 시간표만으로는 '지금 탑승하면 목적지까지 갈 수 있는지' 판단하기 어려움  
  - 대학생 대상 사용자 조사 결과, 정확한 막차 시간 정보 부족과 시간 확인 미흡이 주요 원인
- **해결하고자 한 문제**:
  - 분기역, 환승, 종착역 등 **노선 구조를 고려한 경로 판단**
  - **최근/즐겨찾는 경로 설정** 기능 부재
  - **막차 알림 기능**의 필요성
- **주요 기능**:
  - 간편한 출발-도착 설정으로 막차 시간 확인
  - 자주 가는 경로 저장 및 빠른 재사용
  - 실시간 막차 알림 기능 제공
- **기간**: 2023.11.01 ~ 2023.12.14 (약 6주)
- **팀 구성**: 3명

---


## 팀원 역할 분담

- **박지윤**: UI/UX 설계 및 구현, 사용자 관련 DB 설계, 즐겨찾기 및 최근 경로 기능
- **김다은**: 사용자 인증, 알림 시스템, 막차 알림 기능
- **안채연**: 데이터 모델링, API 연동, 막차 시간 계산 알고리즘 개발, 데이터 통합 및 검증
 
---

## 기술 스택

- **언어**: Java
- **플랫폼**: Android
- **데이터베이스**: Firebase Firestore
- **API**: ODsay API
- **비동기 처리**: CompletableFuture

---

### 기술 선택 이유

#### 📌 ODsay API
- 공공데이터포털 API보다 더 완전하고 정확한 지하철 데이터 제공
- 누락된 역/호선 정보 없음
- 각 구간별 소요 시간 제공 → 경로 판단에 유리
- 정확한 환승 경로, 실시간 데이터 지원
- 일부 환승 정보는 공공데이터포털 데이터를 병합하여 보완

#### 📌 Firebase Firestore
- 유연한 NoSQL 기반 데이터 구조
- 다양한 형태의 데이터(배열, 객체 등) 저장 용이
- 복잡한 쿼리 지원 (Realtime Database보다 유리)
- 비동기 작업이 많은 구조에 적합 → `CompletableFuture`와 함께 사용

---

## 협업 방식

- 프로젝트 초기, 전체 작업을 **간트 차트(Gantt Chart)** 형식으로 작성하여 주차별 일정과 역할을 시각화
- 기능을 세분화하고 **UI / Server 역할 및 담당자**를 명확히 배정
- 이후 매주 팀 미팅을 통해 다음을 진행함:
  - 진행 상황 점검
  - 이슈 및 오류 공유
  - 일정 조정 및 우선순위 재정립
  - 기능 개선 논의
- 일정표는 실제 작업 상황에 따라 지속적으로 업데이트하며 반영

📌 **초기 간트 차트**  ![image](https://github.com/user-attachments/assets/f2079d5e-d528-4345-b715-d5ae94ed9ee7)

📌 **최종 간트 차트** ![image](https://github.com/user-attachments/assets/74f6af8b-005e-46c2-ae61-b2c3f5b10a6d)


---


🧭 **IA 다이어그램**  
![image](https://github.com/user-attachments/assets/9dbe3754-4a9f-4a80-8ad4-cf45374cd2fe)


**클래스 다이어그램**  
![image](https://github.com/user-attachments/assets/f4f5ffbd-89bc-432b-a601-7112c981b245)


⏱️ **막차 계산 전체 플로우**  
>  출발역/도착역 및 현재 시간 기반으로, 각 환승 구간을 분석하여 막차 탑승 가능 경로를 도출하는 흐름
![image](https://github.com/user-attachments/assets/a74e1fe8-ce80-40b2-bd8e-883e21ec7cc7)

   
📂 **Firebase 구조 설명**  
![image](https://github.com/user-attachments/assets/0cde7a26-b85b-40b5-8dd5-ef65037598cd)


---

## 사용한 외부 라이브러리

1. **Apache POI (`poi-ooxml:3.9`)**  
   - Microsoft Office 파일(엑셀 등) 읽기/쓰기용

2. **Jackson Databind (`jackson-databind:2.12.4`)**  
   - JSON 파싱 및 객체 매핑

3. **ODsay 대중교통 OpenAPI**
   - [대중교통 길찾기](https://lab.odsay.com/guide/releaseReference#searchPubTransPathT)  
   - [지하철역 전체 시간표 조회](https://lab.odsay.com/guide/releaseReference#subwayTimeTable)  
   - [정류장 검색](https://lab.odsay.com/guide/releaseReference#searchStation)


