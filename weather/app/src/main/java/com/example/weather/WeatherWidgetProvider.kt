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
        // 테스트용 알림 브로드캐스트 액션
        private const val ACTION_TEST_ALARM = "com.example.weather.ACTION_TEST_WEATHER_ALERT"
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
                    // (기존 알림 로직… 짧게 생략)

                    ids.forEach { widgetId ->
                        val pty  = prefs.getString("weather_precip", "0")!!
                        val sky  = prefs.getString("weather_sky", "1")!!
                        val bgColor = when (pty) {
                            "1","4" -> Color.parseColor("#90CAF9")
                            "2","3" -> Color.parseColor("#B3E5FC")
                            else    -> if (sky=="1") Color.parseColor("#FFF59D")
                            else Color.parseColor("#CFD8DC")
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

                        // 2) 새로고침 클릭 시: ACTION_APPWIDGET_UPDATE 브로드캐스트
                        val refreshPI = PendingIntent.getBroadcast(
                            context, 0,
                            Intent(context, WeatherWidgetProvider::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // 3) 테스트 알림 클릭 시: ACTION_TEST_ALARM 브로드캐스트
                        val testAlarmIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                            action = ACTION_TEST_ALARM
                        }
                        val testAlarmPI = PendingIntent.getBroadcast(
                            context,
                            widgetId, // 위젯별 requestCode를 widgetId로 주면 겹치지 않습니다.
                            testAlarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // RemoteViews 생성 및 뷰 바인딩
                        val views = RemoteViews(context.packageName, R.layout.widget_weather).apply {
                            setInt(R.id.widget_root, "setBackgroundColor", bgColor)
                            setTextViewText(R.id.tv_widget_time,     timeStr)
                            setTextViewText(R.id.tv_widget_location, prefs.getString("weather_location", "위치: 알 수 없음"))
                            setTextViewText(R.id.tv_widget_temp,     "${prefs.getString("weather_temp","--")}°C")
                            setTextViewText(
                                R.id.tv_widget_high_low,
                                "${prefs.getString("weather_daily_high","--")}°C/" +
                                        "${prefs.getString("weather_daily_low","--")}°C\n" +
                                        "PM10: ${prefs.getString("weather_pm10","--")}㎍/m³\n" +
                                        "PM2.5: ${prefs.getString("weather_pm25","--")}㎍/m³"
                            )
                            val iconRes = when (pty) {
                                "1","4" -> R.drawable.rain
                                "2"      -> R.drawable.rainsnow
                                "3"      -> R.drawable.snow
                                else     -> if (sky=="1") R.drawable.sunny else R.drawable.cloude
                            }
                            setImageViewResource(R.id.iv_widget_weather_icon, iconRes)

                            // 클릭 이벤트 연결
                            setOnClickPendingIntent(R.id.widget_root,        launchPI)
                            setOnClickPendingIntent(R.id.btn_widget_refresh,  refreshPI)
                            setOnClickPendingIntent(R.id.tv_widget_test_alarm, testAlarmPI)
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

        // 반드시 small icon을 지정해야 합니다.
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("3시간 후 날씨 알림")
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_refresh) // ← 반드시 존재하는 drawable 리소스로 바꿔 주세요
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("WeatherWidgetProvider", "onReceive() called, action=${intent.action}")

        // 테스트용 알림 액션
        if (intent.action == ACTION_TEST_ALARM) {
            Log.d("WeatherWidgetProvider", "ACTION_TEST_ALARM 수신됨!")
            createNotificationChannel(context)

            val dummyTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now().plusHours(3)
            } else {
                return
            }
            val dummyForecast = HourlyForecast(
                time        = dummyTime,
                temperature = "25",
                sky         = "3",
                precip      = "1",
                windDir     = "0",
                windSpd     = "0"
            )
            sendWeatherAlert(context, dummyForecast)
            return
        }

        // 위젯 업데이트 액션
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
