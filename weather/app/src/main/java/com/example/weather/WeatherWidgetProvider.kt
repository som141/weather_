import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.weather.R

class WeatherWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        mgr: AppWidgetManager,
        ids: IntArray
    ) {
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val temp    = prefs.getString("weather_temp", "--") ?: "--"
        val high    = prefs.getString("weather_daily_high", "--") ?: "--"
        val low     = prefs.getString("weather_daily_low",  "--") ?: "--"
        val cond    = prefs.getString("weather_precip",    "0")  ?: "0"
        val sky     = prefs.getString("weather_sky",       "1")  ?: "1"
        val skyStr  = when (sky) { "1"->"맑음";"3"->"구름많음";"4"->"흐림";else->"?" }
        val condStr = if (cond == "0") skyStr else when(cond) {
            "1"->"비";"2"->"비/눈";"3"->"눈";"4"->"소나기";else->"?"
        }

        ids.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            views.setTextViewText(R.id.tv_widget_temp,     "$temp°C")
            views.setTextViewText(R.id.tv_widget_high_low, "$high°C/$low°C")
            views.setTextViewText(R.id.tv_widget_cond,     condStr)

            // 새로고침 클릭 처리
            val intent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, pi)

            mgr.updateAppWidget(widgetId, views)
        }
    }}