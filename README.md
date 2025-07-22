# Android-LinkBandDemoApp

LooxidLabs LinkBand 디바이스와의 Bluetooth 연결 및 센서 데이터 수집을 시연하는 Android 데모 앱입니다.

## 주요 기능

### 📡 Bluetooth 연결
- LinkBand 디바이스 자동 스캔 및 연결
- 자동 재연결 기능
- 연결 상태 실시간 모니터링

### 📊 센서 데이터 수집

#### EEG (뇌전도)
- 2채널 원시 신호(raw data)
- 전압 변환값 (µV 단위)
- 전극 접촉 상태 정보

#### PPG (광전 용적 맥파)
- 적외선(IR) 및 적색(RED) 신호

#### ACC (가속도계)
- 3축(x, y, z) 원시값
- 움직임 모드 전환 기능 지원

#### 배터리
- 잔량 모니터링 기능

### 📈 배치 데이터 수집
- 샘플 수 기반 수집
- 시간 기반 수집 (초/분)
- 센서별 개별 설정
- 실시간 모니터링

### 💾 데이터 관리
- CSV 형식으로 센서 데이터 저장

## 기술 스택

- **언어**: Kotlin
- **최소 지원 버전**: Android API 34 (Android 14.0)+
- **Java 버전**: 17

## 프로젝트 구조

```
AndroidLinkBandDemoApp/
├── app/                          # 메인 앱 모듈
│   ├── src/main/
│   │   ├── java/                 # Kotlin 소스 코드
│   │   │   ├── MainActivity.kt   # 메인 액티비티
│   │   │   ├── adapters/         # SDK 어댑터 레이어
│   │   │   ├── viewmodels/       # MVVM 뷰모델
│   │   │   ├── repositories/     # 데이터 레포지토리
│   │   │   └── utils/            # 유틸리티 클래스
│   │   ├── res/                  # 리소스 파일
│   │   └── AndroidManifest.xml   # 앱 매니페스트
│   └── build.gradle              # 앱 모듈 빌드 설정
├── gradle.properties             # 프로젝트 설정
├── build.gradle                  # 루트 빌드 설정
└── settings.gradle               # 프로젝트 설정
```

## 설치 및 실행

1. Android Studio Arctic Fox 이상 필요
2. Java 17 설치
3. Android 14.0 이상의 실제 디바이스에서 실행 (Bluetooth 기능 필요)

안드로이드 스튜디오 설정
1. File -> Sync Project with Gradle Files
2. 앱 빌드: run 'app'

## 사용법

1. 앱 실행 후 "스캔 시작" 버튼 터치
2. 발견된 LinkBand 디바이스에 "연결" 버튼 터치
3. 연결 완료 후 실시간 센서 데이터 확인
4. 필요시 배치 데이터 수집 설정 후 기록 시작
