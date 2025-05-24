package com.example.weather

import UltraSrtFcstResponse
import VilageFcstResponse
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.weather.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

class WeatherRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val serviceKey = "nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="
    private val nx = 60
    private val ny = 127

    /** 지금 시점의 초단기예보 → SharedPreferences 저장 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchUltraShortNow(onComplete: () -> Unit = {}) {
        val ref      = LocalDateTime.now().minusMinutes(45)
        val baseDate = ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = ref.format(DateTimeFormatter.ofPattern("HHmm"))

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(call: Call<UltraSrtFcstResponse>, resp: Response<UltraSrtFcstResponse>) {
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                val map   = items.associateBy { it.category }
                prefs.edit()
                    .putString("weather_temp",     map["T1H"]?.fcstValue ?: "--")
                    .putString("weather_sky",      map["SKY"]?.fcstValue ?: "1")
                    .putString("weather_precip",   map["PTY"]?.fcstValue ?: "0")
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

    /** 지금부터 5시간치 초단기예보 (시간별 종합 데이터) */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchHourlyForecastNext5Hours(onComplete: (List<HourlyForecast>) -> Unit) {
        val ref      = LocalDateTime.now().minusMinutes(45)
        val baseDate = ref.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = ref.format(DateTimeFormatter.ofPattern("HHmm"))
        val now      = LocalDateTime.now()
        // 1. 현재 시각을 시간 단위로 내림
        val startHour = now.truncatedTo(ChronoUnit.HOURS)
        val cutoff    = startHour.plusHours(5)

        RetrofitClient.apiService.getUltraShortForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<UltraSrtFcstResponse> {
            override fun onResponse(call: Call<UltraSrtFcstResponse>, resp: Response<UltraSrtFcstResponse>) {
                Log.d("WeatherRepo", "Request URL: ${call.request().url()}")
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                // fcstDate+fcstTime 별로 그룹핑
                val byDateTime = items.groupBy { it.fcstDate + it.fcstTime }
                // 그룹마다 DTO 생성 후 시간 필터링
                val list = byDateTime.mapNotNull { (dtStr, grp) ->
                    val dt = LocalDateTime.parse(
                        dtStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                    )
                    if (!dt.isBefore(startHour) && !dt.isAfter(cutoff)) {
                        val map = grp.associateBy { it.category }
                        HourlyForecast(
                            time        = dt,
                            temperature = map["T1H"]?.fcstValue ?: "--",
                            sky         = map["SKY"]?.fcstValue ?: "1",
                            precip      = map["PTY"]?.fcstValue ?: "0",
                            windDir     = map["VEC"]?.fcstValue ?: "--",
                            windSpd     = map["WSD"]?.fcstValue ?: "--"
                        )
                    } else null
                }.sortedBy { it.time }

                onComplete(list)
            }
            override fun onFailure(call: Call<UltraSrtFcstResponse>, t: Throwable) {
                Log.e("WeatherRepo", "fetchHourlyForecastNext5Hours failed", t)
                onComplete(emptyList())
            }
        })
    }

    /** 오늘의 단기예보(일 최고·최저 기온) → SharedPreferences 저장 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchDailyHighLow(onComplete: () -> Unit = {}) {
        val today    = LocalDateTime.now()
        val baseDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = "0200"

        RetrofitClient.apiService.getVillageForecast(
            serviceKey, baseDate, baseTime, nx, ny
        ).enqueue(object : Callback<VilageFcstResponse> {
            override fun onResponse(call: Call<VilageFcstResponse>, resp: Response<VilageFcstResponse>) {
                val items = resp.body()?.response?.body?.items?.item.orEmpty()
                val high  = items.find { it.category == "TMX" }?.fcstValue ?: "--"
                val low   = items.find { it.category == "TMN" }?.fcstValue ?: "--"
                prefs.edit()
                    .putString("weather_daily_high", high)
                    .putString("weather_daily_low",  low)
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
