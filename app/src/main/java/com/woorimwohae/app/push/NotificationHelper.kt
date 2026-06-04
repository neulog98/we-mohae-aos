package com.woorimwohae.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.woorimwohae.app.MainActivity
import com.woorimwohae.app.R
import com.woorimwohae.app.web.WebUrlPolicy

object NotificationHelper {
  private const val EXTRA_ROUTE = "notification_route"

  fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel =
      NotificationChannel(
        context.getString(R.string.notification_channel_id),
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = context.getString(R.string.notification_channel_description)
        enableLights(true)
        lightColor = Color.rgb(183, 93, 105)
      }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  fun showNotification(context: Context, title: String, body: String, data: Map<String, String>) {
    createChannel(context)
    val route = routeForData(data)
    val intent =
      Intent(context, MainActivity::class.java)
        .putExtra(EXTRA_ROUTE, route)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val pendingIntent =
      PendingIntent.getActivity(
        context,
        route.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    val notification =
      NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
        .setSmallIcon(R.drawable.ic_stat_notification)
        .setColor(Color.rgb(183, 93, 105))
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    runCatching { NotificationManagerCompat.from(context).notify(System.nanoTime().toInt(), notification) }
  }

  fun resolveStartUrl(intent: Intent?): String {
    intent ?: return WebUrlPolicy.HOME_URL
    intent.getStringExtra(EXTRA_ROUTE)?.let { return it }
    return routeForData(
      mapOf(
        "scheduleId" to (intent.getStringExtra("scheduleId") ?: ""),
        "budgetId" to (intent.getStringExtra("budgetId") ?: ""),
      ),
    )
  }

  private fun routeForData(data: Map<String, String>): String =
    when {
      data["scheduleId"].isNullOrBlank().not() -> WebUrlPolicy.CALENDAR_URL
      data["budgetId"].isNullOrBlank().not() -> WebUrlPolicy.BUDGET_URL
      else -> WebUrlPolicy.HOME_URL
    }
}
