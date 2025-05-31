package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.example.weather.ui.theme.WeatherTheme
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    private lateinit var repo: WeatherRepository

    companion object {
        private const val REQUEST_LOCATION = 100
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION
        )

        repo = WeatherRepository(this)
        enableEdgeToEdge()

        setContent {
            WeatherTheme {
                WeatherApp(repo)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                repo.fetchLocation { /* Composable 에서 처리 */ }
            } else {
                Toast.makeText(
                    this,
                    "위치 권한이 거부되어 현재 위치를 표시할 수 없습니다.",
                    Toast.LENGTH_LONG
                ).show()
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
    val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    var nowTemp by remember { mutableStateOf(prefs.getString("weather_temp", "--") ?: "--") }
    var ptyCode by remember { mutableStateOf(prefs.getString("weather_precip", "0") ?: "0") }
    var skyCode by remember { mutableStateOf(prefs.getString("weather_sky", "1") ?: "1") }
    var windDir by remember { mutableStateOf(prefs.getString("weather_wind_dir", "--") ?: "--") }
    var windSpd by remember { mutableStateOf(prefs.getString("weather_wind_spd", "--") ?: "--") }
    var highLow by remember { mutableStateOf("--°C/--°C") }
    var hourly by remember { mutableStateOf(listOf<HourlyForecast>()) }
    var locationText by remember { mutableStateOf("위치: 로딩 중...") }
    var timeText by remember { mutableStateOf("") }
    var pm10 by remember { mutableStateOf("--") }
    var pm25 by remember { mutableStateOf("--") }

    // 날씨 데이터 초기 로드
    LaunchedEffect(Unit) {
        repo.fetchUltraShortNow {
            nowTemp = prefs.getString("weather_temp", "--") ?: "--"
            ptyCode = prefs.getString("weather_precip", "0") ?: "0"
            skyCode = prefs.getString("weather_sky", "1") ?: "1"
            windDir = prefs.getString("weather_wind_dir", "--") ?: "--"
            windSpd = prefs.getString("weather_wind_spd", "--") ?: "--"
        }
        repo.fetchHourlyForecastNext5Hours { hourly = it }
        repo.fetchDailyHighLow {
            val h = prefs.getString("weather_daily_high", "--") ?: "--"
            val l = prefs.getString("weather_daily_low", "--") ?: "--"
            highLow = "${h}°C/${l}°C"
        }
    }

    // 위치 및 미세먼지 초기 로드
    LaunchedEffect(Unit) {
        repo.fetchNearestDust { a, b ->
            pm10 = a
            pm25 = b
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            repo.fetchLocation { loc ->
                locationText = loc
            }
        }
    }

    // 시계 업데이트
    LaunchedEffect(Unit) {
        while (true) {
            timeText = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            delay(1000L)
        }
    }

    val bg = when (ptyCode) {
        "1", "4" -> Color(0xFF90CAF9)
        "2", "3" -> Color(0xFFB3E5FC)
        else -> if (skyCode == "1") Color(0xFFFFF59D) else Color(0xFFCFD8DC)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                repo.fetchLocation { locationText = it }
                            }
                            repo.fetchUltraShortNow {}
                            repo.fetchHourlyForecastNext5Hours { hourly = it }
                            repo.fetchDailyHighLow {}
                            repo.fetchNearestDust { a, b -> pm10 = a; pm25 = b }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = bg)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val iconRes = when (ptyCode) {
                                "1", "4" -> R.drawable.rain
                                "2" -> R.drawable.rainsnow
                                "3" ->R.drawable.snow
                                else -> if (skyCode == "1") R.drawable.sunny else R.drawable.cloude
                            }
                            AsyncImage(
                                model = iconRes,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("${nowTemp}°C", style = MaterialTheme.typography.titleLarge)
                                Text(highLow, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Text(
                            "미세먼지 PM10: $pm10 ㎍/m³, PM2.5: $pm25 ㎍/m³",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                TemperatureChart(data = hourly, modifier = Modifier.fillMaxWidth().height(200.dp))

                Spacer(Modifier.height(16.dp))

                // 시간, 기온, 날씨 등 테이블
                val times = hourly.map { it.time.format(DateTimeFormatter.ofPattern("HH:mm")) }
                val temps = hourly.map { "${it.temperature}°C" }
                val conds = hourly.map {
                    when (it.precip) {
                        "1","4" -> "비"
                        "2"     -> "비/눈"
                        "3"     -> "눈"
                        else    -> when (it.sky) {
                            "1" -> "맑음"
                            "3" -> "구름많음"
                            "4" -> "흐림"
                            else-> "?"
                        }
                    }
                }
                val skies = hourly.map {
                    when (it.sky) {
                        "1" -> "맑음"
                        "3" -> "구름많음"
                        "4" -> "흐림"
                        else-> "?"
                    }
                }
                val dirs = hourly.map { it.windDir.toFloatOrNull()?.let { deg ->
                    when {
                        deg in 337.5f..360f || deg in   0f..22.5f  -> "N"
                        deg in  22.5f.. 67.5f                       -> "NE"
                        deg in  67.5f..112.5f                       -> "E"
                        deg in 112.5f..157.5f                       -> "SE"
                        deg in 157.5f..202.5f                       -> "S"
                        deg in 202.5f..247.5f                       -> "SW"
                        deg in 247.5f..292.5f                       -> "W"
                        deg in 292.5f..337.5f                       -> "NW"
                        else                                        -> "--"
                    }
                } ?: "--" }
                val spds = hourly.map { it.windSpd }

                listOf(
                    "시간" to times,
                    "기온" to temps,
                    "날씨" to conds,
                    "하늘" to skies,
                    "풍향" to dirs,
                    "풍속" to spds
                ).forEach { (label, rowVals) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        rowVals.forEach { v ->
                            Text(v, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TemperatureChart(
    data: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    // 데이터 파싱
    val temps = data.mapNotNull { it.temperature.toFloatOrNull() }
    val times = data.map { it.time.format(DateTimeFormatter.ofPattern("HH:mm")) }
    val minT  = temps.minOrNull() ?: return
    val maxT  = temps.maxOrNull() ?: return

    Canvas(modifier = modifier) {
        val fullW = size.width
        val fullH = size.height
        val marginLeft   = 50f
        val marginRight  = 20f
        val marginTop    = 16f
        val marginBottom = 40f
        val chartW = fullW - marginLeft - marginRight
        val chartH = fullH - marginTop - marginBottom

        // 그리드
        repeat(4) { i ->
            val y = marginTop + chartH * i / 4f
            drawLine(
                color = Color.LightGray,
                start = Offset(marginLeft, y),
                end   = Offset(marginLeft + chartW, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f), 0f)
            )
        }

        // 곡선
        val dx = chartW / (temps.size - 1)
        val path = Path().apply {
            temps.forEachIndexed { i, t ->
                val x = marginLeft + dx * i
                val y = marginTop + (chartH - (t - minT) / (maxT - minT) * chartH)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, Color(0xFF1976D2), style = Stroke(width = 4f, cap = StrokeCap.Round))

        // 포인트
        temps.forEachIndexed { i, t ->
            val x = marginLeft + dx * i
            val y = marginTop + (chartH - (t - minT) / (maxT - minT) * chartH)
            drawCircle(Color.White, radius = 6f, center = Offset(x, y))
            drawCircle(Color(0xFF1976D2), radius = 4f, center = Offset(x, y))
        }

        // 축
        drawLine(Color.Black,
            start = Offset(marginLeft, marginTop),
            end   = Offset(marginLeft, marginTop + chartH),
            strokeWidth = 2f
        )
        drawLine(Color.Black,
            start = Offset(marginLeft, marginTop + chartH),
            end   = Offset(marginLeft + chartW, marginTop + chartH),
            strokeWidth = 2f
        )

        // 레이블
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        listOf(minT, (minT + maxT) / 2f, maxT).forEach { t ->
            val y = marginTop + (chartH - (t - minT) / (maxT - minT) * chartH)
            drawLine(Color.Black,
                start = Offset(marginLeft - 10f, y),
                end   = Offset(marginLeft, y),
                strokeWidth = 2f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${t.toInt()}°",
                marginLeft - 30f,
                y + textPaint.textSize / 2f,
                textPaint
            )
        }
        times.forEachIndexed { i, label ->
            val x = marginLeft + dx * i
            drawLine(Color.Black,
                start = Offset(x, marginTop + chartH),
                end   = Offset(x, marginTop + chartH + 10f),
                strokeWidth = 2f
            )
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                marginTop + chartH + textPaint.textSize + 8f,
                textPaint
            )
        }
    }
}