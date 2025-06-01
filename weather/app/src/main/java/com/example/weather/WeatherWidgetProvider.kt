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
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

        repo.fetchUltraShortNow {
            repo.fetchDailyHighLow {
                repo.fetchHourlyForecastNext5Hours { list ->
                    // “현재 맑음 & 3시간 뒤 비·눈·비눈(precip != "0")”일 때만 알림
                    if (list.size > 3 && isGood(list[0]) && isBad(list[3])) {
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

                    // 위젯 업데이트
                    ids.forEach { widgetId ->
                        val pty     = prefs.getString("weather_precip", "0")!!
                        val sky     = prefs.getString("weather_sky", "1")!!
                        val bgColor = when (pty) {
                            "1","4" -> Color.parseColor("#90CAF9")
                            "2","3" -> Color.parseColor("#B3E5FC")
                            else    -> if (sky == "1") Color.parseColor("#FFF59D")
                            else                Color.parseColor("#CFD8DC")
                        }
                        val timeStr = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))

                        // 1) 메인 화면 클릭 시: MainActivity 실행
                        val launchPI = PendingIntent.getActivity(
                            context, 0,
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // 2) 새로고침 클릭 시: 위젯 업데이트 브로드캐스트
                        val refreshPI = PendingIntent.getBroadcast(
                            context, 0,
                            Intent(context, WeatherWidgetProvider::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // RemoteViews 생성 및 바인딩
                        val views = RemoteViews(context.packageName, R.layout.widget_weather).apply {
                            setInt(R.id.widget_root, "setBackgroundColor", bgColor)
                            setTextViewText(R.id.tv_widget_time,     timeStr)
                            setTextViewText(
                                R.id.tv_widget_location,
                                prefs.getString("weather_location", "위치: 알 수 없음")
                            )
                            setTextViewText(
                                R.id.tv_widget_temp,
                                "${prefs.getString("weather_temp", "--")}°C"
                            )
                            setTextViewText(
                                R.id.tv_widget_high_low,
                                "${prefs.getString("weather_daily_high", "--")}°C/" +
                                        "${prefs.getString("weather_daily_low", "--")}°C\n" +
                                        "PM10: ${prefs.getString("weather_pm10", "--")}㎍/m³\n" +
                                        "PM2.5: ${prefs.getString("weather_pm25", "--")}㎍/m³"
                            )
                            val iconRes = when (pty) {
                                "1","4" -> R.drawable.rain
                                "2"      -> R.drawable.rainsnow
                                "3"      -> R.drawable.snow
                                else     -> if (sky == "1") R.drawable.sunny else R.drawable.cloude
                            }
                            setImageViewResource(R.id.iv_widget_weather_icon, iconRes)

                            setOnClickPendingIntent(R.id.widget_root,       launchPI)
                            setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPI)
                        }

                        mgr.updateAppWidget(widgetId, views)
                    }
                }
            }
        }
    }

    // “안 좋음” 기준: 강수(precip != "0")
    private fun isBad(f: HourlyForecast): Boolean =
        f.precip != "0"

    // “좋음” 기준: 하늘 맑음(sky == "1") & 강수 없음(precip == "0")
    private fun isGood(f: HourlyForecast): Boolean =
        f.sky == "1" && f.precip == "0"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendWeatherAlert(context: Context, forecast: HourlyForecast) {
        val whenStr = forecast.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        val body = when (forecast.precip) {
            "1","4" -> "$whenStr 에 비가 올 예정입니다."
            "2"      -> "$whenStr 에 비/눈이 올 예정입니다."
            "3"      -> "$whenStr 에 눈이 올 예정입니다."
            else     -> "$whenStr 에 날씨 변화 주의"
        }
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("3시간 후 날씨 알림")
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_refresh) // 24×24dp 흑백 아이콘으로 교체하세요
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val chan = NotificationChannel(
            CHANNEL_ID, "날씨 알림", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "3시간 뒤 비/눈 예보 알림" }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(chan)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("WeatherWidgetProvider", "onReceive() called, action=${intent.action}")
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Log.d("WeatherWidgetProvider", "ACTION_APPWIDGET_UPDATE 수신됨!")
            val mgr = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (ids != null) {
                onUpdate(context, mgr, ids)
            }
        }
    }
}
