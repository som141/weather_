package com.example.weather

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.weather.R
import com.example.weather.WeatherRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherWidgetProvider : AppWidgetProvider() {

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

        // 1) 먼저 현재·일 최고저 저장
        repo.fetchUltraShortNow {
            repo.fetchDailyHighLow {
                // 2) 5시간치 예보 및 알림 처리
                repo.fetchHourlyForecastNext5Hours { list ->
                    if (list.size > 3 && isGood(list[0]) && isBad(list[3])) {
                        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true

                        if (canNotify) sendWeatherAlert(context, list[3])
                    }
                    // 3) 위젯 UI 갱신
                    val location = prefs.getString("weather_location", "위치: 알 수 없음")!!
                    val temp     = prefs.getString("weather_temp", "--")!!
                    val high     = prefs.getString("weather_daily_high", "--")!!
                    val low      = prefs.getString("weather_daily_low", "--")!!
                    val pty      = prefs.getString("weather_precip", "0")!!
                    val sky      = prefs.getString("weather_sky",    "1")!!
                    // 미세먼지/초미세먼지
                    val pm10     = prefs.getString("weather_pm10",   "--")!!
                    val pm25     = prefs.getString("weather_pm25",   "--")!!

                    ids.forEach { widgetId ->
                        val bgColor = when (pty) {
                            "1","4" -> Color.parseColor("#90CAF9")
                            "2","3" -> Color.parseColor("#B3E5FC")
                            else       -> if (sky=="1") Color.parseColor("#FFF59D") else Color.parseColor("#CFD8DC")
                        }
                        val timeStr = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))

                        val launchPI = PendingIntent.getActivity(
                            context, 0,
                            Intent(context, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val refreshPI = PendingIntent.getBroadcast(
                            context, 0,
                            Intent(context, WeatherWidgetProvider::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val views = RemoteViews(context.packageName, R.layout.widget_weather).apply {
                            setInt(R.id.widget_root, "setBackgroundColor", bgColor)
                            setTextViewText(R.id.tv_widget_location, location)
                            setTextViewText(R.id.tv_widget_time,     timeStr)
                            setTextViewText(R.id.tv_widget_temp,     "${temp}°C")
                            // 고정된 한 TextView 에 온도 범위와 미세먼지 모두 표시
                            setTextViewText(
                                R.id.tv_widget_high_low,
                                "${prefs.getString("weather_daily_high","--")}°C/" +
                                        "${prefs.getString("weather_daily_low","--")}°C\n" +
                                        "PM10: ${pm10}㎍/m³\n" +
                                        "PM2.5: ${pm25}㎍/m³"
                            )

                            val iconRes = when (pty) {
                                "1","4" -> R.drawable.img_2
                                "2"      -> R.drawable.img_3
                                "3"      -> R.drawable.img_2
                                else      -> if (sky=="1") R.drawable.img else R.drawable.img_1
                            }
                            setImageViewResource(R.id.iv_widget_weather_icon, iconRes)

                            setOnClickPendingIntent(R.id.widget_root, launchPI)
                            setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPI)
                        }
                        mgr.updateAppWidget(widgetId, views)
                    }
                }
            }
        }
    }

    private fun isGood(f: HourlyForecast) = f.sky == "1" && f.precip == "0"
    private fun isBad(f: HourlyForecast)  = f.precip != "0" || f.sky != "1"

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendWeatherAlert(context: Context, forecast: HourlyForecast) {
        val whenStr = forecast.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        val body = when {
            forecast.precip != "0" -> "${whenStr} 에 비가 올 예정입니다."
            forecast.sky    != "1" -> "${whenStr} 에 흐릴 예정입니다."
            else                   -> "${whenStr} 에 날씨 주의"
        }
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
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
            CHANNEL_ID,
            "날씨 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "3시간 뒤 날씨 변화 알림" }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(chan)
    }
}