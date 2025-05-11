package com.example.weather

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.weather.ui.theme.WeatherTheme
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            100
        )

        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "som",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        Text(text = "Hello $name!")

        Button(
            onClick = {
                LocationUtil.getCurrentLocation(context) { location ->
                    val lat = location?.latitude ?: 37.5665  // 서울 위도
                    val lon = location?.longitude ?: 126.9780 // 서울 경도

                    val grid = GridConverter.convert(lat, lon)
                    Log.d("GRID", "격자 좌표: x=${grid.x}, y=${grid.y}")

                    val now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(45)
                    val baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    val baseTime = "0000" // 실시간 계산 필요 시 추가

                    val encodedKey =
                        "nVI4pgoe68ebaYhYMSSCyBFeldG0NThgzKEsA6mfpCNJ7jNxG0qbRzeUvUtjN6S42+Ca+Vnp6+Md/NbOJ9Z5Ag=="

                    val call = RetrofitClient.apiService.getUltraShortForecast(
                        serviceKey = encodedKey,
                        baseDate = baseDate,
                        baseTime = baseTime,
                        nx = 60,
                        ny = 127
                    )

                    Log.d("REAL_URL", call.request().url().toString())
                    call.enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>,
                            response: Response<WeatherResponse>
                        ) {
                            if (response.isSuccessful) {
                                val items = response.body()?.response?.body?.items?.item
                                val categoryMap = items?.associateBy { it.category }

                                val temp = categoryMap?.get("T1H")?.fcstValue ?: "-"
                                val humi = categoryMap?.get("REH")?.fcstValue ?: "-"
                                val pty = categoryMap?.get("PTY")?.fcstValue ?: "-"
                                val rn1 = categoryMap?.get("RN1")?.fcstValue ?: "-"
                                val sky = categoryMap?.get("SKY")?.fcstValue ?: "-"
                                val vec = categoryMap?.get("VEC")?.fcstValue ?: "-"
                                val wsd = categoryMap?.get("WSD")?.fcstValue ?: "-"
                                val lgt = categoryMap?.get("LGT")?.fcstValue ?: "-"

                                val ptyStr = when (pty) {
                                    "0" -> "없음"
                                    "1" -> "비"
                                    "2" -> "비/눈"
                                    "3" -> "눈"
                                    "4" -> "소나기"
                                    else -> "알 수 없음"
                                }

                                val skyStr = when (sky) {
                                    "1" -> "맑음"
                                    "3" -> "구름많음"
                                    "4" -> "흐림"
                                    else -> "알 수 없음"
                                }

                                val lgtStr = when (lgt) {
                                    "0" -> "없음"
                                    "1" -> "있음"
                                    else -> "알 수 없음"
                                }

                                val weatherSummary = """
                                    🌡 기온: ${temp}°C
                                    💧 습도: ${humi}%
                                    🌧 강수형태: $ptyStr
                                    🌂 1시간 강수량: ${rn1}mm
                                    ☁️ 하늘상태: $skyStr
                                    🌬 풍향: ${vec}°
                                    💨 풍속: ${wsd} m/s
                                    ⚡ 낙뢰: $lgtStr
                                """.trimIndent()

                                Log.d("날씨요약", weatherSummary)

                                context
                                    .getSharedPreferences("weather", Context.MODE_PRIVATE)
                                    .edit()
                                    .apply {
                                        putString("weather_temp", "$temp°C")
                                        putString("weather_cond", ptyStr)
                                        apply()
                                    }

                                // ✅ 2. 위젯 강제 갱신
                                val manager = AppWidgetManager.getInstance(context)
                                val ids = manager.getAppWidgetIds(
                                    ComponentName(context, WeatherWidgetProvider::class.java)
                                )
                                val intent = Intent(context, WeatherWidgetProvider::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                }
                                context.sendBroadcast(intent)
                            } else {
                                Log.e("초단기예보", "응답 실패: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("초단기예보", "요청 실패: ${t.message}")
                        }
                    })
                }
            }
        ) {
            Text("초단기예보 요청")
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {
        Greeting("Android")
    }
}
