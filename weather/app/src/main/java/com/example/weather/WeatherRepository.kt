package com.example.weather

import UltraSrtFcstResponse
import VilageFcstResponse
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.weather.model.RealTimeDustResponse
import com.example.weather.model.RealTimeStationListResponse
import com.example.weather.network.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 시간별 예보를 담는 DTO
 */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: String,
    val sky: String,      // SKY 코드
    val precip: String,   // PTY 코드
    val windDir: String,  // VEC
    val windSpd: String   // WSD
)

class WeatherRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    // var로 변경: fetchLocation 성공 시 실제 위치 격자로 덮어쓰기
    private var nx: Int = 60
    private var ny: Int = 127

    private val serviceKey = "nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="
    private val misekey="nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="
    /**
     * 현재 디바이스 위치(시도·동)를 Geocoder로 조회하고
     * GridConverter로 격자좌표(nx,ny)를 업데이트
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun fetchLocation(onResult: (locationStr: String) -> Unit) {
        // 1) 권한 체크
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("WeatherRepo", "fetchLocation: 권한 없음")
            onResult("위치: 권한 없음")
            return
        }

        // 2) 위치 서비스 활성화 체크
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        if (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            && !lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        ) {
            Log.d("WeatherRepo", "fetchLocation: 위치 서비스 꺼짐")
            onResult("위치: GPS 꺼짐")
            return
        }

        // 3) 최신 위치 요청
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val tokenSource = CancellationTokenSource()
        fused.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            tokenSource.token
        )
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Log.d("WeatherRepo", "getCurrentLocation: 위치 정보가 null")
                    onResult("위치: 알 수 없음")
                    return@addOnSuccessListener
                }

                // --- 격자좌표 변환 ---
                val grid = GridConverter.convert(loc.latitude, loc.longitude)
                nx = grid.x
                ny = grid.y
                Log.d("WeatherRepo", "격자변환 → nx=$nx, ny=$ny")

                // --- Geocoder로 시·동 이름 조회 ---
                try {
                    Geocoder(context, Locale.KOREA).getFromLocation(
                        loc.latitude,
                        loc.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: List<Address>) {
                                if (addresses.isNotEmpty()) {
                                    val addr = addresses[0]
                                    val city = addr.adminArea.orEmpty()
                                    val dong = addr.subLocality ?: addr.locality.orEmpty()
                                    val locStr = "$city $dong"
                                    Log.d("WeatherRepo", "onGeocode: $locStr")
                                    prefs.edit().putString("weather_location", locStr).apply()
                                    onResult(locStr)
                                } else {
                                    Log.d("WeatherRepo", "onGeocode: 주소 없음")
                                    onResult("위치: 알 수 없음")
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("WeatherRepo", "Geocoder 오류", e)
                    onResult("위치: 알 수 없음")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherRepo", "getCurrentLocation 실패", e)
                onResult("위치: 알 수 없음")
            }
    }

    /**
     * 초단기예보: 현재 시점 기준 → SharedPreferences 저장
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchUltraShortNow(onComplete: () -> Unit = {}) {
        val ref = LocalDateTime.now().minusMinutes(45)
        val baseDate = ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = ref.format(DateTimeFormatter.ofPattern("HHmm"))

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(
                call: Call<UltraSrtFcstResponse>,
                resp: Response<UltraSrtFcstResponse>
            ) {
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                val map = items.associateBy { it.category }
                prefs.edit()
                    .putString("weather_temp", map["T1H"]?.fcstValue ?: "--")
                    .putString("weather_sky", map["SKY"]?.fcstValue ?: "1")
                    .putString("weather_precip", map["PTY"]?.fcstValue ?: "0")
                    .putString("weather_wind_dir", map["VEC"]?.fcstValue ?: "--")
                    .putString("weather_wind_spd", map["WSD"]?.fcstValue ?: "--")
                    .apply()
                onComplete()
            }

            override fun onFailure(call: Call<UltraSrtFcstResponse>, t: Throwable) {
                Log.e("WeatherRepo", "fetchUltraShortNow failed", t)
                onComplete()
            }
        })
    }

    /**
     * 시간별 초단기예보 5시간치 → DTO 리스트 반환
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchHourlyForecastNext5Hours(onComplete: (List<HourlyForecast>) -> Unit) {
        val ref = LocalDateTime.now().minusMinutes(45)
        val baseDate = ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = ref.format(DateTimeFormatter.ofPattern("HHmm"))
        val now = LocalDateTime.now()
        val startHour = now.truncatedTo(ChronoUnit.HOURS)
        val cutoff = startHour.plusHours(5)

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(
                call: Call<UltraSrtFcstResponse>,
                resp: Response<UltraSrtFcstResponse>
            ) {
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                val list = items.groupBy { it.fcstDate + it.fcstTime }
                    .mapNotNull { (dtStr, grp) ->
                        val dt = LocalDateTime.parse(
                            dtStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                        )
                        if (!dt.isBefore(startHour) && !dt.isAfter(cutoff)) {
                            val map = grp.associateBy { it.category }
                            HourlyForecast(
                                time = dt,
                                temperature = map["T1H"]?.fcstValue ?: "--",
                                sky = map["SKY"]?.fcstValue ?: "1",
                                precip = map["PTY"]?.fcstValue ?: "0",
                                windDir = map["VEC"]?.fcstValue ?: "--",
                                windSpd = map["WSD"]?.fcstValue ?: "--"
                            )
                        } else null
                    }
                    .sortedBy { it.time }
                onComplete(list)
            }

            override fun onFailure(call: Call<UltraSrtFcstResponse>, t: Throwable) {
                Log.e("WeatherRepo", "fetchHourlyForecastNext5Hours failed", t)
                onComplete(emptyList())
            }
        })
    }

    /**
     * 일일 단기예보: 일 최고/최저 기온 → SharedPreferences 저장
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDailyHighLow(onComplete: () -> Unit = {}) {
        val today = LocalDateTime.now()
        val baseDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = "0200"

        RetrofitClient.apiService.getVillageForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<VilageFcstResponse> {
            override fun onResponse(
                call: Call<VilageFcstResponse>,
                resp: Response<VilageFcstResponse>
            ) {
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                val high = items.find { it.category == "TMX" }?.fcstValue ?: "--"
                val low = items.find { it.category == "TMN" }?.fcstValue ?: "--"
                prefs.edit()
                    .putString("weather_daily_high", high)
                    .putString("weather_daily_low", low)
                    .apply()
                onComplete()
            }

            override fun onFailure(call: Call<VilageFcstResponse>, t: Throwable) {
                Log.e("WeatherRepo", "fetchDailyHighLow failed", t)
                onComplete()
            }
        })
    }

    /**
     * 내 현재 위경도 → TM 좌표로 변환 → 인근 측정소 1순위 → 미세먼지 값 조회
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchNearestDust(onComplete: (pm10: String, pm25: String) -> Unit) {
        // 1) 권한, 위치 서비스 체크는 이미 구현된 fetchLocation 흐름 재활용
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val token = CancellationTokenSource()

        fused.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            token.token
        ).addOnSuccessListener { loc ->
            if (loc == null) {
                onComplete("--", "--")
                return@addOnSuccessListener
            }

            // 2) → TM 좌표 변환 (환경공단 API용)
            val tm = TMConverter.convert(loc.latitude, loc.longitude)
            Log.d("WeatherRepo", "TM 변환 → x=${tm.x}, y=${tm.y}")

            // 3) 인근 측정소 조회
            RetrofitClient.airQualityService
                .getNearbyStationList(misekey, tm.x, tm.y)
                .enqueue(object : Callback<RealTimeStationListResponse> {
                    override fun onResponse(
                        call: Call<RealTimeStationListResponse>,
                        resp: Response<RealTimeStationListResponse>
                    ) {
                        val station = resp.body()
                            ?.response
                            ?.body
                            ?.items
                            ?.firstOrNull()
                            ?.stationName

                        if (station.isNullOrEmpty()) {
                            onComplete("--", "--")
                            return
                        }

                        // 4) 해당 측정소로 미세먼지 값 조회
                        RetrofitClient.airQualityService
                            .getRealTimeDust(
                                serviceKey = misekey,
                                stationName = station
                            ).enqueue(object : Callback<RealTimeDustResponse> {
                                override fun onResponse(
                                    call: Call<RealTimeDustResponse>,
                                    resp2: Response<RealTimeDustResponse>
                                ) {
                                    val item = resp2.body()
                                        ?.response
                                        ?.body
                                        ?.items
                                        ?.firstOrNull()
                                    val pm10 = item?.pm10 ?: "--"
                                    val pm25 = item?.pm25 ?: "--"
                                    prefs.edit()
                                        .putString("weather_pm10", pm10)
                                        .putString("weather_pm25", pm25)
                                        .apply()
                                    onComplete(pm10, pm25)
                                }
                                override fun onFailure(call: Call<RealTimeDustResponse>, t: Throwable) {
                                    onComplete("--", "--")
                                }
                            })
                    }
                    override fun onFailure(call: Call<RealTimeStationListResponse>, t: Throwable) {
                        onComplete("--", "--")
                    }
                })
        }.addOnFailureListener {
            onComplete("--", "--")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDustData(
        stationName: String,
        onComplete: (pm10: String, pm25: String) -> Unit
    ) {
        RetrofitClient.airQualityService.getRealTimeDust(
            serviceKey = misekey,
            stationName = stationName
        ).enqueue(object : Callback<RealTimeDustResponse> {
            override fun onResponse(
                call: Call<RealTimeDustResponse>,
                resp: Response<RealTimeDustResponse>
            ) {
                val item = resp.body()
                    ?.response
                    ?.body
                    ?.items  // 바로 리스트
                    ?.firstOrNull()

                val pm10 = item?.pm10 ?: "--"
                val pm25 = item?.pm25 ?: "--"
                Log.d("WeatherRepo", "Dust API URL: ${call.request().url()}")
                // SharedPreferences 에도 저장해두면 나중에 Widget 등에서 편리합니다.
                prefs.edit()
                    .putString("weather_pm10", pm10)
                    .putString("weather_pm25", pm25)
                    .apply()

                onComplete(pm10, pm25)
            }

            override fun onFailure(call: Call<RealTimeDustResponse>, t: Throwable) {
                Log.e("WeatherRepo", "fetchDustData failed", t)
                onComplete("--", "--")
            }
        })}
}
