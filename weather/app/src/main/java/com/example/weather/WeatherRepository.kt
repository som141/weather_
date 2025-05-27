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
import com.example.weather.network.RetrofitClient

import com.google.android.gms.location.LocationServices
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

    private val serviceKey = "nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="
    private val nx = 60
    private val ny = 127

    /**
     * 현재 디바이스 위치(시도·동)를 Geocoder로 동기 조회
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun fetchLocation(onResult: (locationStr: String) -> Unit) {
        // 1) 권한 체크
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onResult("위치: 권한 없음")
            return
        }

        // 2) 마지막 위치 요청
        val fused = LocationServices.getFusedLocationProviderClient(context)
        fused.lastLocation
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    onResult("위치: 알 수 없음")
                    return@addOnSuccessListener
                }
                try {
                    val geo = Geocoder(context, Locale.KOREA)
                    // API 33+ 에서 추가된 비동기 오버로드
                    geo.getFromLocation(
                        loc.latitude,
                        loc.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: List<Address>) {
                                // 받은 주소 리스트를 여기서만 처리
                                if (addresses.isNotEmpty()) {
                                    val addr = addresses[0]
                                    val city = addr.adminArea.orEmpty()
                                    val dong = addr.subLocality ?: addr.locality.orEmpty()
                                    val locStr = "$city $dong"
                                    prefs.edit()
                                        .putString("weather_location", locStr)
                                        .apply()
                                    onResult(locStr)
                                } else {
                                    onResult("위치: 알 수 없음")
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("WeatherRepo", "Geocoder error", e)
                    onResult("위치: 알 수 없음")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherRepo", "Location fetch failed", e)
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

}
