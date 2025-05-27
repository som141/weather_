package com.example.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
class WeatherWidgetLargeProvider : AppWidgetProvider() {
    companion object {
        private const val CHANNEL_ID = "weather_alerts"
        private const val NOTIF_ID   = 1001
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        createNotificationChannel(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        mgr: AppWidgetManager,
        ids: IntArray
    ) {
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val repo  = WeatherRepository(context)

        // 1) 단기·일별 예보 저장 → 5시간치 예보 가져오기
        repo.fetchUltraShortNow {
            repo.fetchDailyHighLow {
                repo.fetchHourlyForecastNext5Hours { list ->
                    // 2) “현재 맑음 → 3시간 뒤 나쁨”이면 알림
                    if (list.size > 3 && isGood(list[0]) && isBad(list[3])) {
                        // Android 13+ 권한 체크
                        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true

                        if (canNotify) {
                            sendWeatherAlert(context, list[3])
                        }
                    }

                    // 3) 위젯 화면 업데이트
                    updateAllWidgets(context, mgr, ids, list)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAllWidgets(
        context: Context,
        mgr: AppWidgetManager,
        ids: IntArray,
        data: List<HourlyForecast>
    ) {
        val prefs   = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val timeStr = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        val launchPI = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val refreshPI = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, WeatherWidgetLargeProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        ids.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_large)

            // background
            val pty = prefs.getString("weather_precip","0")!!
            val sky = prefs.getString("weather_sky","1")!!
            val bgColor = when (pty) {
                "1","4" -> Color.parseColor("#90CAF9")
                "2","3" -> Color.parseColor("#B3E5FC")
                else    -> if (sky=="1") Color.parseColor("#FFF59D") else Color.parseColor("#CFD8DC")
            }
            views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

            // text
            views.setTextViewText(R.id.tv_widget_location,
                prefs.getString("weather_location","위치: 알 수 없음")!!)
            views.setTextViewText(R.id.tv_widget_time,     "시간: $timeStr")
            views.setTextViewText(R.id.tv_widget_temp,
                "${prefs.getString("weather_temp","--")}°C")
            views.setTextViewText(R.id.tv_widget_high_low,
                "${prefs.getString("weather_daily_high","--")}°C/" +
                        "${prefs.getString("weather_daily_low","--")}°C")

            // icon
            val iconRes = when (pty) {
                "1","4" -> R.drawable.img_2
                "2"     -> R.drawable.img_3
                "3"     -> R.drawable.img_2
                else    -> if (sky=="1") R.drawable.img else R.drawable.img_1
            }
            views.setImageViewResource(R.id.iv_widget_weather_icon, iconRes)

            // graph
            val bmp = createTempGraph(data)
            views.setImageViewBitmap(R.id.iv_widget_temp_graph, bmp)

            // click handlers
            views.setOnClickPendingIntent(R.id.widget_root,    launchPI)
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPI)

            mgr.updateAppWidget(widgetId, views)
        }
    }

    private fun isGood(f: HourlyForecast): Boolean =
        f.sky == "1" && f.precip == "0"

    private fun isBad(f: HourlyForecast): Boolean =
        f.precip != "0" || f.sky != "1"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendWeatherAlert(context: Context, forecast: HourlyForecast) {
        val whenStr = forecast.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        val body = when {
            forecast.precip != "0" -> "$whenStr 에 비가 올 예정입니다."
            forecast.sky    != "1" -> "$whenStr 에 흐릴 예정입니다."
            else                   -> "$whenStr 에 날씨 변화 주의"
        }

        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("3시간 후 날씨 알림")
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val chan = NotificationChannel(
            CHANNEL_ID, "날씨 알림", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "3시간 뒤 날씨 변화 알림" }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(chan)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTempGraph(data: List<HourlyForecast>): Bitmap {
        // 1) 더 크게: 폭 300 × 높이 120
        val width  = 170
        val height = 120

        // 2) 여백
        val marginLeft   = 24
        val marginTop    = 12
        val marginRight  = 16
        val marginBottom = 24

        // 3) 비트맵 + 캔버스 (흰 배경)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp).apply { drawColor(Color.WHITE) }

        if (data.size < 2) return bmp

        // 4) 데이터 준비
        val temps = data.mapNotNull { it.temperature.toFloatOrNull() }
        val times = data.map { it.time.format(DateTimeFormatter.ofPattern("HH:mm")) }
        val maxT  = temps.maxOrNull()!!
        val minT  = temps.minOrNull()!!
        val range = (maxT - minT).takeIf { it > 0 } ?: 1f

        // 5) 그릴 영역 & 간격
        val graphW = width  - marginLeft - marginRight
        val graphH = height - marginTop  - marginBottom
        val stepX  = graphW.toFloat() / (temps.size - 1)

        // 6) 페인트: 선명하게, 글자 작게
        val paintAxis = Paint().apply {
            style       = Paint.Style.STROKE
            strokeWidth = 2f
            color       = Color.BLACK
            isAntiAlias = true
        }
        val paintText = Paint().apply {
            style       = Paint.Style.FILL
            textSize    = 8f
            color       = Color.BLACK
            isAntiAlias = true
        }
        val paintLine = Paint().apply {
            style       = Paint.Style.STROKE
            strokeWidth = 3f
            color       = Color.parseColor("#1565C0")
            isAntiAlias = true
        }
        val paintDot = Paint().apply {
            style       = Paint.Style.FILL
            color       = Color.parseColor("#0D47A1")
            isAntiAlias = true
        }

        // 7) 축 그리기
        val x0 = marginLeft.toFloat()
        val y0 = (height - marginBottom).toFloat()
        // Y축
        canvas.drawLine(x0, marginTop.toFloat(), x0, y0, paintAxis)
        // X축
        canvas.drawLine(x0, y0, (width - marginRight).toFloat(), y0, paintAxis)

        // 8) Y축 레이블 (최고/최저 온도)
        canvas.drawText("${maxT.toInt()}°", 0f, marginTop + paintText.textSize, paintText)
        canvas.drawText("${minT.toInt()}°", 0f, y0 + paintText.textSize, paintText)

        // 9) 포인트 계산 및 그리기
        val pts = temps.mapIndexed { i, t ->
            val x = marginLeft + i * stepX
            val y = marginTop + graphH - ((t - minT) / range) * graphH
            PointF(x, y)
        }
        pts.forEachIndexed { i, p ->
            if (i < pts.lastIndex) {
                val n = pts[i + 1]
                canvas.drawLine(p.x, p.y, n.x, n.y, paintLine)
            }
            canvas.drawCircle(p.x, p.y, 4f, paintDot)
            // 점 위 온도 값
            val txt = "${temps[i].toInt()}°"
            val tw  = paintText.measureText(txt)
            canvas.drawText(txt, p.x - tw / 2, p.y - 6f, paintText)
        }

        // 10) X축 레이블 (시간)
        pts.forEachIndexed { i, p ->
            val lbl = times[i]
            val tw  = paintText.measureText(lbl)
            canvas.drawText(lbl, p.x - tw / 2, height.toFloat(), paintText)
        }

        return bmp
    }







}
