# Weather App 사용자 및 개발자 가이드

## 📄 개요

이 문서는 Kotlin으로 개발된 Weather 애플리케이션, 위젯(소형), 라지 위젯(대형)에 대한 사용자 가이드, 개발자 가이드, 그리고 프로젝트 전체의 README 내용을 포함합니다. 이 가이드를 통해 최종 사용자는 기능을 이해하고 사용할 수 있으며, 개발자는 프로젝트 구조를 파악하고 개발 환경에서 빌드 및 실행하는 방법을 숙지할 수 있습니다.

---

## 📌 프로젝트 README

### 1. 프로젝트 소개

Weather 프로젝트는 다음 세 가지 구성 요소로 이루어진 Android 애플리케이션입니다:

1. **Weather App**: 사용자 인터페이스를 통해 현재 날씨 정보와 예보를 확인할 수 있는 메인 애플리케이션
2. **Widget (위젯)**: 홈 화면에 배치하여 간단한 날씨 정보를 자동으로 표시하는 소형 위젯
3. **Large Widget (라지 위젯)**: 홈 화면의 2x2 또는 2x4 크기에 맞게 확장된 형태로, 더 상세한 날씨 정보와 기온 그래프를 제공하는 대형 위젯

이 세 요소는 모두 Kotlin으로 작성되었으며, Korea Meteorological Administration (KMA) API를 사용해 날씨 데이터를 가져옵니다.

### 2. 주요 기능

* **Weather App**

    * 현재 위치 기반 실시간 날씨 정보 (온도, 풍향/풍속, 습도, 하늘상태 등)
    * 5시간 기온 변화 예측 그래프
    * 일 최저·최고 기온 정보
    * 알림(Notification)을 통한 기상 경고 표시

* **Widget (소형 위젯)**

    * 홈 화면에 시간별 간단 기온 및 하늘 상태 표시
    * 주기적 자동 업데이트 (예: 1시간마다 갱신)
    * ‘새로고침’ 버튼을 통한 즉시 갱신 기능

* **Large Widget (대형 위젯)**

    * 2x2 또는 2x4 크기에 맞춘 확장형 위젯
    * 5시간 기온 변화 그래프를 이미지로 렌더링하여 표시
    * 일 최저·최고 기온 및 간략한 하늘 상태 메시지
    * 알림 버튼이나 링크를 통해 날씨 앱으로 이동

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
2. Android Studio를 열고, `Open an existing Android Studio project`에서 클론한 `weather_` 루트 폴더를 선택합니다.
3. Gradle 동기화가 자동으로 진행됩니다. 필요한 SDK와 라이브러리가 설치될 때까지 기다립니다.
4. 에뮬레이터 또는 실제 Android 기기를 연결한 후, 상단의 Run 버튼을 눌러 `app` 모듈을 실행합니다.

### 5. 패키지 및 배포

* **APK 생성**: Build > Build Bundle(s) / APK(s) > Build APK(s)를 통해 APK를 생성할 수 있습니다.
* **릴리즈 모드 서명**: `app/build.gradle`의 `signingConfigs`를 작성하고, `Build Types`에서 `release` 섹션을 설정한 후, `Generate Signed Bundle / APK` 를 사용합니다.

### 6. 라이선스

* Weather 프로젝트는 MIT License를 따릅니다. 자세한 사항은 저장소의 `LICENSE` 파일을 참고하세요.

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

    * 상단에는 현재 지역 이름과 현재 날씨 상태(아이콘, 설명)가 표시됩니다.
    * 중앙 영역에는 현재 온도, 체감 온도, 습도, 풍속/풍향 정보가 나열됩니다.
    * 하단에는 5시간 기온 변화 예측 그래프가 표시됩니다.
    * 상단 우측 알림 아이콘을 누르면 기상 경고 알림 설정 화면으로 이동합니다.
    * 새로고침 버튼을 눌러 최신 데이터로 갱신할 수 있습니다.

#### 1-3. 알림(Notification) 설정

* 알림을 통해 기상 특보(예: 폭우, 폭설 등)가 발생하면 기기 상태 표시줄에 알림이 표시됩니다.
* 알림 채널 해제/재설정은 설정 > 앱 > Weather > 알림에서 조절할 수 있습니다.

#### 1-4. 문제 해결

* **위치 정보 미수집**: 설정 > 애플리케이션 > Weather > 권한에서 위치 권한이 허용되어 있는지 확인합니다.
* **데이터 미표시**: 네트워크 연결 상태를 확인하고, 앱 내 새로고침 버튼을 눌러 다시 시도합니다.

---

### 2. 소형 위젯(User Widget) 사용자 가이드

#### 2-1. 위젯 추가 방법

1. 홈 화면 빈 공간을 길게 터치하여 위젯 목록을 엽니다.
2. `Weather Widget (소형)`을 찾아 길게 터치하여 원하는 위치에 드롭합니다.

#### 2-2. 위젯 구성 및 사용법

* 위젯에는 현재 시간, 기온, 하늘상태 아이콘이 표시됩니다.
* 주기적으로(기본 1시간) 날씨 정보를 자동으로 갱신합니다.
* 위젯 내 새로고침 아이콘을 탭하면 즉시 업데이트 됩니다.

#### 2-3. 크기 및 레이아웃

* 2x1 크기로 고정되어 있으며, 최소 너비와 높이는 `minWidth`, `minHeight` 속성으로 지정되어 있습니다.
* 레이아웃 파일: `res/layout/widget_small.xml`

#### 2-4. 트러블슈팅

* **위젯이 빈 화면으로 표시됨**: 앱을 삭제 후 재설치하거나, 위젯 업데이트 주기(`updatePeriodMillis`)가 올바르게 설정되어 있는지 `AndroidManifest.xml`을 확인합니다.
* **새로고침 버튼 작동 안함**: 위젯 `PendingIntent` 설정이 제대로 되었는지 레포지토리 코드를 확인합니다.

---

### 3. 대형 위젯(Large Widget) 사용자 가이드

#### 3-1. 위젯 추가 방법

1. 홈 화면 빈 공간을 길게 터치하여 위젯 목록을 엽니다.
2. `Weather Widget (대형)`을 찾아 2x2 또는 2x4 크기로 원하는 위치에 드롭합니다.

#### 3-2. 위젯 구성 및 사용법

* **상단 섹션**: 현재 위치, 현재 기온, 하늘 상태 아이콘 및 설명이 표시됩니다.
* **그래프 섹션**: 5시간 기온 변화 그래프가 이미지 형태로 표시됩니다.
* **하단 섹션**: 일 최저·최고 기온 정보와 간략한 날씨 메시지(예: “맑음”, “비”) 표시.
* 위젯 내 새로고침 버튼을 탭하면 즉시 정보가 갱신됩니다.
* 홈 화면 위젯 배경을 투명하게 설정할 수 있습니다.

#### 3-3. 크기 및 레이아웃

* **2x2 레이아웃**: `res/layout/widget_large_2x2.xml`
* **2x4 레이아웃**: `res/layout/widget_large_2x4.xml`

    * 기온 그래프 이미지를 더 넓게 보여줄 수 있도록 조정되어 있습니다.
* 레이아웃별 `minWidth`, `minHeight`, `minResizeWidth`, `minResizeHeight` 속성을 AndroidManifest.xml에서 지정해야 합니다.

#### 3-4. 알림 및 인터랙션

* 알림 아이콘을 클릭하면 날씨 앱의 메인 화면으로 이동합니다.
* 그래프 영역을 탭하면 더 상세한 날씨 예보 화면(앱 내)을 실행하도록 설정할 수 있습니다.

#### 3-5. 트러블슈팅

* **그래프가 보이지 않음**: `android:adjustViewBounds`, `android:resizeMode` 등 이미지 관련 설정 확인. `Bitmap`으로 그려진 경우 권한 문제 또는 메모리 부족 여부를 확인합니다.
* **위젯 크기가 맞지 않음**: AndroidManifest.xml의 `<appwidget-provider>` 태그 내 `android:initialLayout` 및 `android:minWidth`/`android:minHeight` 설정을 검토하여 정확한 dp 값인지 확인합니다.

---

## 🛠️ 개발자 가이드

이 섹션은 개발자 관점에서 프로젝트를 전체적으로 이해하고, 개발 환경을 구성하며, 주요 코드를 설명합니다.

### 1. 프로젝트 디렉토리 구조

```
weather_
├── .idea/                  # Android Studio IDE 설정 파일
├── weather/                # 최상위 모듈 폴더
│   ├── app/                # Android 애플리케이션 모듈
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/example/weather/
│   │   │       │   ├── MainActivity.kt
│   │   │       │   ├── WeatherRepository.kt
│   │   │       │   ├── WeatherWidgetProvider.kt
│   │   │       │   ├── WeatherWidgetLargeProvider.kt
│   │   │       │   ├── models/               # 데이터 모델 클래스
│   │   │       │   │   ├── CurrentWeather.kt
│   │   │       │   │   ├── Forecast.kt
│   │   │       │   │   └── ...
│   │   │       │   ├── network/              # Retrofit 인터페이스 및 클라이언트
│   │   │       │   │   ├── ApiService.kt
│   │   │       │   │   └── RetrofitClient.kt
│   │   │       │   └── utils/                # 유틸리티 클래스 (예: LocationUtil)
│   │   │       └── res/
│   │   │           ├── layout/
│   │   │           │   ├── activity_main.xml
│   │   │           │   ├── widget_small.xml
│   │   │           │   ├── widget_large_2x2.xml
│   │   │           │   └── widget_large_2x4.xml
│   │   │           ├── values/
│   │   │           │   ├── colors.xml
│   │   │           │   ├── strings.xml
│   │   │           │   └── styles.xml
│   │   │           └── drawable/
│   │   │               ├── ic_weather.png
│   │   │               └── ic_refresh.png
│   │   └── AndroidManifest.xml
│   └── build.gradle         # 모듈별 Gradle 설정
├── build.gradle             # 루트 Gradle 설정
├── settings.gradle          # 포함된 모듈 정의
├── .gitignore
└── README.md                # 프로젝트 개요 및 설치 안내
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
* **onCreate**:

    1. 레이아웃 `activity_main.xml` 로드
    2. 위치 권한(Permission)을 확인하고, 허용되어 있지 않으면 요청
    3. `WeatherRepository` 인스턴스를 생성하여 `fetchCurrentWeather()`와 `fetchHourlyForecast()` 호출
    4. 받아온 데이터를 UI(View)에 바인딩
* **위치 권한 처리**:

    * Android 6.0 이상부터 권한 요청을 런타임에 처리
    * `ActivityCompat.requestPermissions`를 사용

#### 4-2. WeatherRepository.kt

* 날씨 API 호출을 담당하는 클래스
* **Retrofit Client 설정** (`RetrofitClient.kt`)

    * Base URL: KMA OpenAPI 엔드포인트
    * GsonConverterFactory 등록
    * OkHttpClient에 로깅 인터셉터(선택) 추가
* **ApiService.kt**

    * `@GET` 어노테이션을 활용하여 현재 날씨, 예보, 미세먼지 등 각종 엔드포인트 정의
* **fetchCurrentWeather(params)**

    * 위치(위도, 경도) 파라미터를 바탕으로 현재 날씨 API 호출
    * 응답을 `CurrentWeather` 모델로 파싱
* **fetchHourlyForecast(params)**

    * 5시간 예보 API 호출 후 `List<Forecast>` 형태로 반환
* **CoroutineScope**

    * `viewModelScope` 혹은 `GlobalScope.launch`를 통해 비동기 호출
    * 예외 처리와 응답 유효성 검사를 수행

#### 4-3. WeatherWidgetProvider.kt (소형 위젯)

* **AppWidgetProvider**를 상속받아 구현
* **onUpdate**:

    * `RemoteViews`를 생성하여 `widget_small.xml` 레이아웃을 바인딩
    * `PendingIntent`를 설정하여 새로고침 버튼 클릭 시 `ACTION_REFRESH` 브로드캐스트를 수신하도록 함
* **onReceive**:

    * 브로드캐스트(`ACTION_REFRESH`, `ACTION_AUTO_UPDATE`)를 처리
    * `WeatherRepository`를 사용해 최신 데이터를 가져온 후, 위젯 레이아웃을 업데이트
* **updateAppWidget**:

    * 텍스트뷰(온도, 하늘 상태) 및 이미지뷰(아이콘)를 `setTextViewText`, `setImageViewResource` 등으로 설정
    * `appWidgetManager.updateAppWidget` 호출로 뷰 갱신

#### 4-4. WeatherWidgetLargeProvider.kt (대형 위젯)

* **AppWidgetProvider**를 상속받아 구현
* **onUpdate**:

    * `widget_large_2x2.xml` 또는 `widget_large_2x4.xml`을 `RemoteViews`로 불러옴
    * 그래프 이미지를 생성하여 위젯의 `ImageView`에 설정
    * 알림 또는 상세화면 이동을 위한 버튼 `PendingIntent` 설정
* **그래프 렌더링**:

    * `Canvas`와 `Paint`를 사용하여 5시간 기온 데이터 기반 라인 그래프를 `Bitmap`으로 그린 다음, `RemoteViews.setImageViewBitmap` 호출
    * 그래프 스타일(선 굵기, 폰트 크기 등)을 `Paint` 객체에서 정의
* **onReceive**:

    * 새로고침 브로드캐스트 처리
    * `updateAppWidget` 호출로 레이아웃 및 그래프 재생성

#### 4-5. AndroidManifest.xml 설정

* **앱 권한(permission)**:

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

    * `weather_widget_info.xml`와 `weather_widget_large_info.xml`에서 `android:updatePeriodMillis`를 지정하여 자동 업데이트 주기 설정 (예: 3600000ms = 1시간)

### 5. 레이아웃 및 리소스 파일 설명

#### 5-1. Layout

* **activity\_main.xml**

    * 전체 화면을 ConstraintLayout 기반으로 구성
    * 현재 날씨 텍스트뷰, 아이콘, 그래프 영역을 포함
    * `SwipeRefreshLayout` 추가하여 당겨서 새로고침 기능 제공

* **widget\_small.xml**

    * 2x1 크기의 소형 위젯 레イ아웃
    * `TextView`(시간, 온도), `ImageView`(하늘 상태 아이콘), 새로고침 버튼 배치

* **widget\_large\_2x2.xml**, **widget\_large\_2x4.xml**

    * 각각 2x2, 2x4 크기에 맞춰 디자인
    * `ImageView`(기온 그래프), `TextView`(현재 온도, 최소/최대 온도), 버튼(앱 실행, 새로고침)

#### 5-2. Values

* **strings.xml**

    * 앱 이름, 버튼 텍스트, 위젯 설명 등 문자열 리소스 설정
* **colors.xml**

    * 앱 전역 색상 팔레트 정의 (기본 배경, 텍스트 색상, 강조 색상 등)
* **styles.xml**

    * 테마 설정 (`Theme.WeatherApp`), `MaterialComponents` 기반 스타일 정의

#### 5-3. Drawable

* **ic\_weather.png**, **ic\_refresh.png** 등 아이콘 리소스
* **graph\_background.xml** 등의 커스텀 드로어블 (옵션)

### 6. API 및 네트워크

#### 6-1. API 엔드포인트

* **Current Weather**: `https://api.kma.go.kr/...]` (초단기 실황 조회)
* **Hourly Forecast (5H)**: `https://api.kma.go.kr/...]` (초단기 예보 조회)
* **Daily Min/Max**: `https://api.kma.go.kr/...]` (단기 예보 조회)
* **Air Quality (옵션)**: 미세먼지 및 초미세먼지 API 엔드포인트

**※ 실제 엔드포인트 URL과 인증키는 `ApiService.kt` 내 상수로 관리**

#### 6-2. Retrofit 및 네트워크 구성

* **RetrofitClient.kt**:

  ```kotlin
  object RetrofitClient {
      private const val BASE_URL = "https://api.kma.go.kr/..."  
      private val retrofit by lazy {
          Retrofit.Builder()
              .baseUrl(BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .client(provideOkHttpClient())
              .build()
      }
      val apiService: ApiService by lazy {
          retrofit.create(ApiService::class.java)
      }

      private fun provideOkHttpClient(): OkHttpClient {
          return OkHttpClient.Builder()
              .addInterceptor(HttpLoggingInterceptor().apply {
                  level = HttpLoggingInterceptor.Level.BODY
              })
              .build()
      }
  }
  ```
* **ApiService.kt**:

  ```kotlin
  interface ApiService {
      @GET("getCurrentWeather")
      suspend fun getCurrentWeather(
          @Query("lat") lat: Double,
          @Query("lon") lon: Double,
          @Query("serviceKey") serviceKey: String
      ): Response<CurrentWeatherResponse>

      @GET("getHourlyForecast")
      suspend fun getHourlyForecast(
          @Query("lat") lat: Double,
          @Query("lon") lon: Double,
          @Query("serviceKey") serviceKey: String
      ): Response<HourlyForecastResponse>

      @GET("getDailyMinMax")
      suspend fun getDailyMinMax(
          @Query("lat") lat: Double,
          @Query("lon") lon: Double,
          @Query("serviceKey") serviceKey: String
      ): Response<DailyMinMaxResponse>
  }
  ```

### 7. 동작 흐름

1. **앱 실행** → `MainActivity.onCreate` 호출
2. **위치 권한 확인** → 권한 허용 시 `fetchCurrentWeather`, `fetchHourlyForecast`, `fetchDailyMinMax` 호출
3. **UI 업데이트** → 받아온 데이터를 화면에 바인딩
4. **위젯 등록** → 홈 화면에 추가된 위젯이 `AppWidgetManager`로부터 `onUpdate` 호출
5. **위젯 데이터 요청** → `onUpdate` 또는 `onReceive`에서 `WeatherRepository`를 통해 데이터 갱신
6. **위젯 UI 업데이트** → `RemoteViews`로 레이아웃을 구성하고 `appWidgetManager.updateAppWidget` 호출
7. **알림 발생** → 특정 조건(예: 강수 확률 > 80%) 시 `NotificationManagerCompat`를 통해 알림 생성

### 8. 빌드 및 테스트

* **디버그 빌드**: Android Studio Run 버튼 사용
* **위젯 테스트**: 홈 화면에서 위젯을 추가하고, 교차 확인을 위해 로그캣(Logcat)에 디버그 메시지 출력
* **단위 테스트**: `app/src/test/java/` 디렉토리에 코루틴 기반 유닛 테스트 추가 가능

### 9. 코드 스타일 가이드

* Kotlin 공식 coding conventions 준수
* 함수 및 클래스에 주석(KDoc) 추가 권장
* 네트워크 호출 시 CoroutineScope(예: `viewModelScope`) 사용 권장
* `ViewBinding` 또는 `DataBinding` 사용 시 메모리 관리 주의

### 10. 기여 방법

1. 본 저장소를 포크하여 로컬 머신에 클론합니다.
2. 새로운 브랜치를 생성하여 기능을 추가하거나 버그를 수정합니다.

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

* 버그 리포트, 기능 요청 등을 GitHub 이슈 트래커에 등록하세요.
* 이슈 제목에는 `[BUG]` 또는 `[FEATURE]` 태그를 붙여주세요.

### 12. 라이선스 및 저작권

* Weather 프로젝트는 **MIT License** 를 따릅니다.
* 프로젝트 내 사용된 모든 리소스(아이콘, 이미지 등)의 저작권은 해당 저작권자에게 있습니다.

---

## 🎯 요약

이 가이드는 Weather 애플리케이션(코틀린 기반)과 위젯(소형 및 대형)에 대한 사용 방법과 개발 방법을 상세히 설명합니다.

* 사용자 가이드를 통해 기능 사용법, 설치, 문제 해결 방안을 제공합니다.
* 개발자 가이드를 통해 프로젝트 구조, 주요 코드, 네트워크 통신 방식 등을 설명하여 확장 및 유지보수를 용이하게 합니다.
* README 섹션에서는 프로젝트 개요, 설치 방법, 빌드 및 배포, 기여 방법, 라이선스 정보를 요약합니다.

이 문서를 참고하여 Weather 프로젝트를 원활하게 이해하고, 사용자로서 또는 개발자로서 필요한 작업을 수행할 수 있습니다.
