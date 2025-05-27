package com.example.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherWidgetProvider : AppWidgetProvider() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        mgr: AppWidgetManager,
        ids: IntArray
    ) {
        // SharedPreferences 에 저장된 값 읽기
        val prefs    = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val location = prefs.getString("weather_location", "위치: 알 수 없음") ?: "위치: 알 수 없음"
        val temp     = prefs.getString("weather_temp", "--") ?: "--"
        val high     = prefs.getString("weather_daily_high", "--") ?: "--"
        val low      = prefs.getString("weather_daily_low", "--") ?: "--"
        val pty      = prefs.getString("weather_precip", "0") ?: "0"
        val sky      = prefs.getString("weather_sky",    "1") ?: "1"

        // 1) 배경 색 결정 (PTY 우선, 없으면 SKY)
        val bgColor = when (pty) {
            "1", "4" -> Color.parseColor("#90CAF9")  // 비/소나기: 파랑
            "2", "3" -> Color.parseColor("#B3E5FC")  // 눈/진눈깨비: 연파랑
            else -> if (sky == "1")
                Color.parseColor("#FFF59D")              // 맑음: 노랑
            else
                Color.parseColor("#CFD8DC")              // 흐림: 회색
        }

        // 시간은 위젯 갱신 시점 기준으로 표시
        val timeStr = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        // 인텐트: 앱 실행
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val launchPI = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 인텐트: 위젯 갱신
        val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        val refreshPI = PendingIntent.getBroadcast(
            context, 0, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        ids.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            // 2) 루트 레이아웃 배경색 적용
            views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

            // 3) 위치, 시간
            views.setTextViewText(R.id.tv_widget_location, location)
            views.setTextViewText(R.id.tv_widget_time,     "시간: $timeStr")

            // 4) 날씨 아이콘, 기온, 최고/최저
            val iconRes = when (pty) {
                "1", "4" -> R.drawable.img_2       // 비, 소나기
                "2"        -> R.drawable.img_3       // 비/눈
                "3"        -> R.drawable.img_2       // 눈
                else        -> if (sky == "1")
                    R.drawable.img    // 맑음
                else
                    R.drawable.img_1  // 구름많음/흐림
            }
            views.setImageViewResource(R.id.iv_widget_weather_icon, iconRes)
            views.setTextViewText(R.id.tv_widget_temp,     "$temp°C")
            views.setTextViewText(R.id.tv_widget_high_low, "$high°C/$low°C")

            // 5) 클릭: 전체 위젯 → 앱 실행
            views.setOnClickPendingIntent(R.id.widget_root, launchPI)
            // 6) 클릭: 새로고침 버튼 → 위젯 업데이트
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPI)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
