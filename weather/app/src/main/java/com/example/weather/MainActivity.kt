package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

import com.example.weather.ui.theme.WeatherTheme
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    private lateinit var repo: WeatherRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 한국(서울) 타임존 고정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
        // 위치 권한 요청
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            100
        )

        repo = WeatherRepository(this)

        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                WeatherApp(repo)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(repo: WeatherRepository) {
    val context = LocalContext.current

    // 날씨 코드를 가져오도록 설정
    var ptyCode by remember { mutableStateOf("0") }
    var skyCode by remember { mutableStateOf("1") }
    var hourly by remember { mutableStateOf(listOf<HourlyForecast>()) }
    var highLow by remember { mutableStateOf("--°C/--°C") }
    var windDir by remember { mutableStateOf("--") }
    var windSpd by remember { mutableStateOf("--") }

    // 위치, 시간 상태
    var locationText by remember { mutableStateOf("위치: 로딩 중...") }
    var timeText by remember { mutableStateOf("") }

    // 위치 가져오기
    LaunchedEffect(Unit) {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        try {
            fused.lastLocation.await()?.let {
                locationText = "위치: ${it.latitude.format(2)}, ${it.longitude.format(2)}"
                context.getSharedPreferences("weather_prefs", 0)
                    .edit()
                    .putString("weather_location", locationText)
                    .apply()
            }
        } catch (_: Exception) {}
    }
    // 시간 갱신
    LaunchedEffect(Unit) {
        while(true) {
            timeText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            delay(1_000L)
        }
    }
    // 날씨 데이터 로드
    LaunchedEffect(Unit) {
        repo.fetchHourlyForecastNext5Hours { hourly = it }
        repo.fetchDailyHighLow {
            val p = context.getSharedPreferences("weather_prefs", 0)
            val h = p.getString("weather_daily_high", "--") ?: "--"
            val l = p.getString("weather_daily_low", "--") ?: "--"
            highLow = "$h°C/$l°C"
        }
        repo.fetchUltraShortNow {
            val p = context.getSharedPreferences("weather_prefs", 0)
            skyCode = p.getString("weather_sky", "1") ?: "1"
            ptyCode = p.getString("weather_precip", "0") ?: "0"
            windDir = p.getString("weather_wind_dir", "--") ?: "--"
            windSpd = p.getString("weather_wind_spd", "--") ?: "--"
        }
    }

    // 전체 배경 색 결정
    val backgroundColor = when (ptyCode) {
        "1", "4" -> Color(0xFF90CAF9)    // 비/소나기: 파랑
        "2", "3" -> Color(0xFFB3E5FC)    // 눈/진눈깨비: 연파랑
        else -> if (skyCode == "1") Color(0xFFFFF59D) else Color(0xFFCFD8DC)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(locationText, style = MaterialTheme.typography.bodySmall)
                            Text(timeText, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            repo.fetchHourlyForecastNext5Hours { hourly = it }
                            repo.fetchDailyHighLow {}
                            repo.fetchUltraShortNow {}
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 위젯 스타일 요약 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 날씨 아이콘
                        val iconRes = when (ptyCode) {
                            "1", "4" -> R.drawable.img_2
                            "2", "3" -> R.drawable.img_3
                            else -> if (skyCode == "1") R.drawable.img else R.drawable.img_1
                        }
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${hourly.firstOrNull()?.temperature ?: "--"}°C",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = highLow,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 테이블 헤더
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("시간","기온","날씨","하늘","풍향","풍속").forEach { header ->
                        Text(header, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))

                // 데이터 테이블
                LazyColumn(Modifier.weight(1f)) {
                    items(hourly) { item ->
                        val skyStr = if (skyCode == "1") "맑음" else "흐림"
                        val condStr = when (ptyCode) {
                            "1", "4" -> "비"
                            "2", "3" -> "눈"
                            else -> skyStr
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.time.format(DateTimeFormatter.ofPattern("HH:mm")), Modifier.weight(1f))
                            Text("${item.temperature}°C", Modifier.weight(1f))
                            Text(condStr, Modifier.weight(1f))
                            Text(skyStr, Modifier.weight(1f))
                            Text(windDir, Modifier.weight(1f))
                            Text(windSpd, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(decimals: Int): String = "%.$decimals f".format(this)
