# Weather App 사용자 및 개발자 가이드

## 📌 프로젝트 README

### 1. 프로젝트 소개

Weather 프로젝트는 다음 세 가지 구성 요소로 이루어진 Android 애플리케이션입니다:

1. **Weather App**: 사용자 인터페이스를 통해 현재 날씨 정보와 예보를 확인할 수 있는 메인 애플리케이션
2. **Widget (소형 위젯)**: 홈 화면에 배치하여 간단한 날씨 정보를 자동으로 표시하는 소형 위젯
3. **Large Widget (라지 위젯)**:  2x4 크기에 맞게 확장된 형태로, 더 상세한 날씨 정보와 기온 그래프를 제공하는 대형 위젯

이 세 요소는 모두 Kotlin으로 작성되었으며, 기상청(KMA) OpenAPI 및 미세먼지 API를 사용해 데이터를 가져옵니다.

### 2. 주요 기능

* **Weather App**

  * 현재 위치 기반 실시간 날씨 정보 (온도, 풍향/풍속, 습도, 하늘 상태 등)
  * 5시간 기온 변화 예측 그래프
  * 일 최저·최고 기온 정보
  * 미세먼지, 초미세먼지 정보
  * 알림(Notification)을 통한 기상 경고 표시

* **Widget (소형 위젯)**

  * 홈 화면에 시간별 간단 기온 및 미세먼지, 하늘 상태 표시
  * 주기적 자동 업데이트 (예: 1시간마다 갱신)
  * ‘새로고침’ 버튼을 통한 즉시 갱신 기능
  * 알림을 통한 기상 경고
  * 알림 버튼이나 링크를 통해 날씨 앱으로 이동

* **Large Widget (대형 위젯)**

  * 2x4 크기에 맞춘 확장형 위젯
  * 5시간 기온 변화 그래프를 이미지로 렌더링하여 표시
  * 일 최저·최고 기온 및 간략한 하늘 상태 메시지
  * 알림 버튼이나 링크를 통해 날씨 앱으로 이동
  * 알림을 통한 기상 경고

### 3. 요구사항

* Android Studio (최신 안정화 버전 권장)
* Gradle 7.x 이상
* Kotlin 1.5.x 이상
* Android SDK 21(API 레벨 21) 이상
* AndroidX 라이브러리 (AppCompat, ConstraintLayout, Material Components 등)
* 인터넷(날씨 API 호출용)

### 4. 설치 및 빌드

1. 터미널에서 저장소를 클론합니다.

   ```bash
   git clone https://github.com/som141/weather_.git
   cd weather_
   ```
2. Android Studio를 열고, `Open an existing Android Studio project`에서 `weather_` 루트 폴더를 선택합니다.
3. Gradle 동기화가 자동으로 진행됩니다. 필요한 SDK와 라이브러리가 설치될 때까지 기다립니다.
4. 에뮬레이터 또는 실제 Android 기기를 연결한 후, 상단의 Run 버튼을 눌러 `app` 모듈을 실행합니다.

### 5. 패키지 및 배포

* **APK 생성**: Build > Build Bundle(s) / APK(s) > Build APK(s)
* **릴리즈 모드 서명**: `app/build.gradle`의 `signingConfigs`를 작성하고, `buildTypes`에서 `release` 섹션을 설정한 후, `Generate Signed Bundle / APK`를 사용합니다.

### 6. 라이선스

* 본 프로젝트의 라이선스는 MIT License를 따릅니다. 자세한 사항은 저장소의 `LICENSE` 파일을 참고하세요.

---

## 🌟 사용자 가이드

이 섹션에서는 최종 사용자 입장에서 각 구성 요소(앱, 소형 위젯, 대형 위젯)를 어떻게 설치하고 사용하는지 설명합니다.

### 1. Weather App 사용자 가이드

#### 1-1. 설치 및 초기 설정

1. Play 스토어 혹은 직접 설치된 APK 파일을 통해 앱을 설치합니다.
2. 앱을 최초로 실행하면 위치 권한(Location Permission) 요청 팝업이 나타납니다.

   * **위치 권한 허용**: 현재 위치 기반 날씨를 제공하기 위해 반드시 허용해야 합니다.
3. 위치 권한이 허용되면, 앱은 자동으로 현재 위치를 가져와 날씨 데이터를 표시합니다.

#### 1-2. 주요 화면 설명

* **홈 화면 (MainActivity)**

  * 상단: 현재 지역 이름과 현재 날씨 상태(아이콘, 설명)
  * 중앙: 현재 온도, 체감 온도, 습도, 미세먼지 수치, 풍속/풍향 정보
  * 하단: 5시간 기온 변화 예측 그래프
  * 새로고침 버튼: 최신 데이터로 갱신
  * 알림 설정: 기상 경고 알림 세부 설정

#### 1-3. 알림(Notification) 설정

* 알림을 통해 기상 특보(예: 폭우, 폭설 등)가 발생하면 기기 상태 표시줄에 알림이 표시됩니다.
* 알림 채널 해제/재설정은 설정 > 앱 > Weather > 알림에서 조절합니다.

#### 1-4. 문제 해결

* **위치 정보 미수집**: 설정 > 애플리케이션 > Weather > 권한에서 위치 권한이 허용되어 있는지 확인합니다.
* **데이터 미표시**: 네트워크 연결 상태를 확인하고, 앱 내 새로고침 버튼을 눌러 다시 시도합니다.

---

### 2. 소형 위젯(User Widget) 사용자 가이드

#### 2-1. 위젯 추가 방법

1. 홈 화면 빈 공간을 길게 터치하여 위젯 목록을 엽니다.
2. `Weather Widget (소형)`을 찾아 길게 터치하여 원하는 위치에 드롭합니다.

#### 2-2. 위젯 구성 및 사용법

* 위젯에는 현재 시간, 기온, 미세먼지, 하늘 상태 아이콘이 표시됩니다.
* 주기적으로(기본 1시간) 날씨 정보를 자동으로 갱신합니다.
* 위젯 내 새로고침 아이콘을 탭하면 즉시 업데이트 됩니다.

#### 2-3. 크기 및 레이아웃

* 2x2 크기로 고정되어 있으며, 최소 너비와 높이는 `minWidth`, `minHeight` 속성으로 지정되어 있습니다.
* 레이아웃 파일: `res/layout/widget_weather.xml`

#### 2-4. 트러블슈팅

* **위젯이 빈 화면으로 표시됨**: 앱을 삭제 후 재설치하거나, 위젯 업데이트 주기(`updatePeriodMillis`)가 올바르게 설정되어 있는지 `AndroidManifest.xml`을 확인합니다.
* **새로고침 버튼 작동 안함**: 위젯 `PendingIntent` 설정이 제대로 되었는지 레포지토리 코드를 확인합니다.

---

### 3. 대형 위젯(Large Widget) 사용자 가이드

#### 3-1. 위젯 추가 방법

1. 홈 화면 빈 공간을 길게 터치하여 위젯 목록을 엽니다.
2. `Weather Widget (대형)`을 찾아 2x3 또는 2x4 크기로 원하는 위치에 드롭합니다。

#### 3-2. 위젯 구성 및 사용법

* **상단 섹션**: 현재 위치, 현재 기온, 하늘 상태 아이콘 및 설명이 표시됩니다。
* **그래프 섹션**: 5시간 기온 변화 그래프가 이미지 형태로 표시됩니다。
* **하단 섹션**: 일 최저·최고 기온 정보와 간략한 날씨 메시지(예: “맑음”, “비”) 표시。
* 위젯 내 새로고침 버튼을 탭하면 즉시 정보가 갱신됩니다。
* 홈 화면 위젯 배경을 투명하게 설정할 수 있습니다。

#### 3-3. 크기 및 레이아웃

* 기온 그래프 이미지를 더 넓게 보여줄 수 있도록 조정되어 있습니다。
* 레이아웃별 `minWidth`, `minHeight`, `minResizeWidth`, `minResizeHeight` 속성을 AndroidManifest.xml에서 지정해야 합니다。

#### 3-4. 알림 및 인터랙션

* 알림 아이콘을 클릭하면 날씨 앱의 메인 화면으로 이동합니다。
* 그래프 영역을 탭하면 더 상세한 날씨 예보 화면(앱 내)을 실행하도록 설정할 수 있습니다。

#### 3-5. 트러블슈팅

* **그래프가 보이지 않음**: `RemoteViews.setImageViewBitmap` 호출 확인, 메모리 부족 여부 확인。
* **위젯 크기가 맞지 않음**: AndroidManifest.xml의 `<appwidget-provider>` 태그 내 `android:initialLayout` 및 `android:minWidth`/`android:minHeight` 설정을 검토하여 정확한 dp 값인지 확인합니다。

---

## 🛠️ 개발자 가이드

### 1. 프로젝트 디렉토리 구조

```
weather_
├── .idea/                  # Android Studio IDE 설정 파일
├── weather/                # 최상위 모듈 폴더
│   ├── app/                # Android 애플리케이션 모듈
│   │   ├── manifests/
│   │   │   └── AndroidManifest.xml
│   │   ├── kotlin+java/
│   │   │   └── com.example.weather/
│   │   │       ├── MainActivity.kt
│   │   │       ├── RetrofitClient.kt
│   │   │       ├── WeatherApiService.kt
│   │   │       ├── WeatherRepository.kt
│   │   │       ├── WeatherWidgetProvider.kt
│   │   │       ├── WeatherWidgetLargeProvider.kt
│   │   │       ├── model/               # 데이터 모델 클래스
│   │   │       │   ├── RealTimeDustResponse.kt
│   │   │       │   ├── RealTimeStationListResponse.kt
│   │   │       │   ├── UltraSrtFcstResponse.kt
│   │   │       │   └── VilageFcstResponse.kt
│   │   │       ├── network/             # 네트워크 인터페이스 및 클라이언트
│   │   │       │   ├── AirQualityService.kt
│   │   │       │   └── ApiService.kt
│   │   │       ├── ui.theme/            # UI 테마 관련 클래스
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Theme.kt
│   │   │       │   └── Type.kt
│   │   │       ├── util/                # 유틸리티 클래스 (GridConverter, TMConverter)
│   │   │       │   ├── GridConverter.kt
│   │   │       │   └── TMConverter.kt
│   │   └── res/                        # 리소스 디렉토리
│   │       ├── drawable/              # 이미지 및 드로어블 리소스
│   │       │   ├── cloude.gif
│   │       │   ├── ic_launcher_background.xml
│   │       │   ├── ic_launcher_foreground.xml
│   │       │   ├── ic_refresh.xml
│   │       │   ├── rain.gif
│   │       │   ├── rainsnow.gif
│   │       │   ├── snow.gif
│   │       │   ├── sunny.gif
│   │       │   └── widget_bg.xml
│   │       ├── layout/                # 레이아웃 XML
│   │       │   ├── activity_main.xml
│   │       │   ├── widget_small.xml
│   │       │   ├── widget_large.xml
│   │       │   └── widget_weather.xml
│   │       ├── mipmap/                # 런처 아이콘 등
│   │       ├── values/                # 색상, 문자열, 스타일 리소스
│   │       │   ├── colors.xml
│   │       │   ├── strings.xml
│   │       │   └── styles.xml
│   │       └── xml/                   # 위젯 설정 및 백업/복원 규칙
│   │           ├── backup_rules.xml
│   │           ├── data_extraction_rules.xml
│   │           ├── weather_widget_info.xml
│   │           └── weather_widget_large_info.xml
│   └── build.gradle                  # 모듈별 Gradle 설정
├── build.gradle                      # 루트 Gradle 설정
├── settings.gradle                   # 포함된 모듈 정의
├── .gitignore
└── README.md                         # 프로젝트 개요 및 설치 안내
```

### 2. 개발 환경 설정

1. **Android Studio 설치**: Android Studio Arctic Fox(R) 이상 버전을 권장합니다.
2. **SDK 설정**: Android SDK Platform 21 이상 설치
3. **Gradle 버전**: Gradle 7.x 이상 사용
4. **Kotlin 버전**: Kotlin 1.6.x 이상 지원
5. **JDK**: JDK 11 이상 권장

### 3. 주요 라이브러리

* **AndroidX AppCompat**: 호환성 지원
* **AndroidX ConstraintLayout**: 레이아웃 구성
* **Material Components**: UI 컴포넌트 디자인
* **Retrofit2 + Gson**: 네트워크 통신 및 JSON 파싱
* **Kotlin Coroutines**: 비동기 처리를 위한 코루틴
* **Coil**: 이미지 로딩 라이브러리 (옵션)

### 4. 주요 구성 요소 및 코드 설명

#### 4-1. MainActivity.kt

* 앱 실행 시 가장 먼저 호출되는 액티비티
* **onCreate**:&#x20;
* 1\. 위치 권한(Permission)을 확인하고, 허용되어 있지 않으면 요청&#x20;
* 2\. `WeatherRepository` 인스턴스를 생성하여 `fetchCurrentWeather()`와 `fetchHourlyForecast()` 호출&#x20;
* 3\. 받아온 데이터를 UI(View)에 바인딩
* **위치 권한 처리**:

  * Android 6.0 이상부터 권한 요청을 런타임에 처리
  * `ActivityCompat.requestPermissions`를 사용

#### 4-2. WeatherRepository.kt

* 날씨 API 호출을 담당하는 클래스
* **Retrofit Client 설정** (`RetrofitClient.kt`)

  * Base URL: KMA OpenAPI 엔드포인트
  * GsonConverterFactory 등록
  * OkHttpClient에 타임아웃 설정 및 로깅 인터셉터(선택) 추가
* **ApiService.kt**

  * `@GET` 어노테이션을 활용하여 현재 날씨, 예보, 미세먼지 등 각종 엔드포인트 정의
* **fetchCurrentWeather(params)**

  * 위치(위도, 경도) 파라미터를 바탕으로 현재 날씨 API 호출
  * 응답을 `CurrentWeatherResponse` 모델로 파싱
* **fetchHourlyForecast(params)**

  * 5시간 예보 API 호출 후 `List<Forecast>` 형태로 반환
* **CoroutineScope 사용**

  * `viewModelScope` 혹은 `GlobalScope.launch`를 통해 비동기 호출
  * 예외 처리와 응답 유효성 검사를 수행

#### 4-3. WeatherWidgetProvider.kt (소형 위젯)

* **AppWidgetProvider**를 상속받아 구현
* **onUpdate**:

  * `RemoteViews`를 생성하여 `widget_small.xml` 레이아웃을 바인딩
  * `PendingIntent`를 설정하여 새로고침 버튼 클릭 시 브로드캐스트를 수신하도록 함
* **onReceive**:

  * 브로드캐스트(`ACTION_REFRESH`, `ACTION_AUTO_UPDATE`)를 처리하여 `WeatherRepository`로 데이터 갱신
* **updateAppWidget**:

  * 위젯 내 `TextView`(온도, 미세먼지, 하늘 상태) 및 `ImageView`(아이콘) 설정
  * `appWidgetManager.updateAppWidget` 호출로 뷰 갱신

#### 4-4. WeatherWidgetLargeProvider.kt (대형 위젯)

* **AppWidgetProvider**를 상속받아 구현
* **onUpdate**:

  * `RemoteViews`로 `widget_large.xml` 불러오기
  * 기온 그래프를 그려 `ImageView`에 설정
  * 알림 및 앱 이동을 위한 `PendingIntent` 설정
* **그래프 렌더링**:

  * `Canvas`와 `Paint`로 5시간 기온 데이터 기반 라인 그래프를 `Bitmap`으로 생성
  * `RemoteViews.setImageViewBitmap` 호출하여 위젯에 표시
* **onReceive**:

  * 새로고침 브로드캐스트 처리 및 그래프 갱신

#### 4-5. AndroidManifest.xml 설정

* **권한(permission)**:

  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  ```
* **위젯 Provider 등록**:

  ```xml
  <receiver android:name=".WeatherWidgetProvider" >
      <intent-filter>
          <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
      </intent-filter>
      <meta-data
          android:name="android.appwidget.provider"
          android:resource="@xml/weather_widget_info" />
  </receiver>
  <receiver android:name=".WeatherWidgetLargeProvider" >
      <intent-filter>
          <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
      </intent-filter>
      <meta-data
          android:name="android.appwidget.provider"
          android:resource="@xml/weather_widget_large_info" />
  </receiver>
  ```
* **Widget 업데이트 주기**:

  * `weather_widget_info.xml` 및 `weather_widget_large_info.xml`에서 `android:updatePeriodMillis` 설정 (예: 3600000ms = 1시간)

### 5. 레이아웃 및 리소스 파일 설명

#### 5-1. Layout

* **widget\_weather.xml**

  * 2x2 크기의 소형 위젯 레이아웃
  * `TextView`(시간, 온도, 미세먼지), `ImageView`(하늘 상태 아이콘), 새로고침 버튼 배치
* **widget\_large.xml**

  * 2x3 및 2x4 크기에 모두 대응하는 레이아웃
  * `ImageView`(기온 그래프), `TextView`(현재 온도, 미세먼지, 최소/최대 온도), 버튼(앱 실행, 새로고침)

#### 5-2. Values

* **strings.xml**

  * 앱 이름, 버튼 텍스트, 위젯 설명 등 문자열 리소스 설정
* **colors.xml**

  * 앱 전역 색상 팔레트 정의 (기본 배경, 텍스트 색상, 강조 색상 등)
* **styles.xml**

  * `Theme.WeatherApp` 테마 설정 (MaterialComponents 기반)

#### 5-3. Drawable

* **cloude.gif, rain.gif, rainsnow\.gif, snow\.gif, sunny.gif** 등 애니메이션 GIF
* **ic\_refresh.xml** 및 기타 아이콘 리소스
* **widget\_bg.xml** 등 커스텀 드로어블 (위젯 배경)

### 6. API 및 네트워크

#### 6-1. API 엔드포인트

* **Weather API (기상청 OpenAPI)**

  * Base URL: `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/`
  * **ApiService.kt**에 정의된 메서드를 사용

    * `getUltraSrtFcst(serviceKey, pageNo, numOfRows, dataType, base_date, base_time, nx, ny)` (초단기예보 조회)
    * `getVilageFcst(serviceKey, pageNo, numOfRows, dataType, base_date, base_time, nx, ny)` (단기예보 조회)

* **Air Quality API (미세먼지)**

  * Base URL: `https://apis.data.go.kr/`
  * **AirQualityService.kt**에 정의된 상대 경로 사용

    * `getCtprvnRltmMesureDnsty(serviceKey, returnType, sidoName, ver)` (실시간 미세먼지 조회)

**※ 인증키(Service Key)는 ********************************************************************************************************************`RetrofitClient.kt`******************************************************************************************************************** 또는 별도의 보안 파일에서 상수로 관리합니다.**

#### 6-2. Retrofit 및 네트워크 구성

```kotlin
object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    // 날씨 API 호출용 Retrofit 인스턴스
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // 미세먼지 API 호출 전용 Retrofit 인스턴스
    private val retrofitAir: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/")  // 상대 경로로 호출
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val airQualityService: AirQualityService by lazy {
        retrofitAir.create(AirQualityService::class.java)
    }
}
```

* `apiService`는 기상청(OpenAPI)의 초단기예보 및 단기예보 엔드포인트를 기본 URL로 사용합니다.
* `airQualityService`는 미세먼지 API 상대 경로 호출을 위해 `https://apis.data.go.kr/`를 기본 URL로 사용합니다.

### 7. 동작 흐름

1. **앱 실행** → `MainActivity.onCreate` 호출
2. **위치 권한 확인** → 권한 허용 시 `fetchCurrentWeather`, `fetchHourlyForecast`, `fetchDustInfo` 호출
3. **UI 업데이트** → 받아온 데이터를 화면에 바인딩
4. **위젯 등록** → 홈 화면에 추가된 위젯이 `AppWidgetManager`로부터 `onUpdate` 호출
5. **위젯 데이터 요청** → `onUpdate` 또는 `onReceive`에서 `WeatherRepository`를 통해 데이터 갱신
6. **위젯 UI 업데이트** → `RemoteViews`로 레이아웃을 구성하고 `appWidgetManager.updateAppWidget` 호출
7. **알림 발생** → 특정 조건(3시간 이후 기상악화 ) 시 `NotificationManagerCompat`를 통해 알림 생성

### 8. 빌드 및 테스트

* **디버그 빌드**: Android Studio Run 버튼 사용
* **위젯 테스트**: 홈 화면에서 위젯을 추가하고, 로그캣(Logcat)에 디버그 메시지 확인
* **단위 테스트**: `app/src/test/java/`에 코루틴 기반 유닛 테스트 추가 가능

### 9. 코드 스타일 가이드

* Kotlin 공식 [coding conventions](https://kotlinlang.org/docs/coding-conventions.html) 준수
* 함수 및 클래스에 주석(KDoc) 추가 권장
* 네트워크 호출 시 CoroutineScope(예: `viewModelScope`) 사용 권장
* `ViewBinding` 또는 `DataBinding` 사용 시 메모리 관리 주의

### 10. 기여 방법

1. 저장소를 포크하고 로컬 머신에 클론합니다.

   ```bash
   git clone https://github.com/<your-username>/weather_.git
   cd weather_
   ```
2. 새로운 브랜치를 생성하여 기능 추가 또는 버그 수정합니다.

   ```bash
   git checkout -b feature/새로운기능
   ```
3. 변경 사항을 커밋하고 푸시합니다.

   ```bash
   git add .
   git commit -m "[Feature] 새로운 기능 설명"
   git push origin feature/새로운기능
   ```
4. GitHub에서 Pull Request를 생성하여 리뷰를 요청합니다.
5. 리뷰가 완료되면 병합(Merge)됩니다.

### 11. 이슈 관리

* 버그 리포트, 기능 요청 등은 GitHub 이슈 트래커에 등록하세요.
* 이슈 제목에는 `[BUG]` 또는 `[FEATURE]` 태그를 붙여주세요.

### 12. 라이선스 및 저작권

* Weather 프로젝트는 **MIT License**를 따릅니다.
* 프로젝트 내 사용된 모든 리소스(아이콘, 이미지 등)의 저작권은 해당 저작권자에게 있습니다.
