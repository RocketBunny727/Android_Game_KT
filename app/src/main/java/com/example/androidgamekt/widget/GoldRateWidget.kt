package com.example.androidgamekt.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.androidgamekt.R

class GoldRateWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold_rate)
            val intent = Intent(context, GoldRateWidgetUpdateService::class.java)
            val pending = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetRoot, pending)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        context.startService(Intent(context, GoldRateWidgetUpdateService::class.java))
    }

    companion object {
        fun updateAll(context: Context, text: String) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, GoldRateWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_gold_rate)
                views.setTextViewText(R.id.tvGoldRate, text)
                manager.updateAppWidget(id, views)
            }
        }
    }
}


