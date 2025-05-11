package com.example.weather

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.content.SharedPreferences
import android.preference.PreferenceManager

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val prefs: SharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE)

        val temperature = prefs.getString("weather_temp", "--°C")
        val condition = prefs.getString("weather_cond", "정보 없음")

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            views.setTextViewText(R.id.tv_temperature, temperature)
            views.setTextViewText(R.id.tv_condition, condition)
            manager.updateAppWidget(id, views)
        }
    }
}
