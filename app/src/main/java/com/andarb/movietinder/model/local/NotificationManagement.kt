package com.andarb.movietinder.model.local

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.andarb.movietinder.R
import com.andarb.movietinder.model.remote.RemoteEndpoint

const val NOTIFICATION_ID = 753159
const val NOTIFICATION_INTENT_FILTER = "com.andarb.movietinder.DISCONNECT_ENDPOINTS"

/**
 * Configures, sends and cancels notifications
 */
class NotificationManagement(val context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** Sets up the notification channel */
    fun setupChannel() {
        val name = context.getString(R.string.notification_name)
        val descriptionText = context.getString(R.string.notification_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(context.packageName, name, importance)
        mChannel.description = descriptionText

        notificationManager.createNotificationChannel(mChannel)
    }

    /** Creates and sends a notification about the connected device */
    fun send() {
        val intent = Intent(context, ConnectionBroadcastReceiver::class.java).apply {
            action = NOTIFICATION_INTENT_FILTER
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val action: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.broken_link_icon),
            context.getString(R.string.notification_button_disconnect),
            pendingIntent
        ).build()

        val notification: Notification = Notification.Builder(context, context.packageName)
            .setContentTitle(
                context.getString(
                    R.string.toast_connected_endpoint,
                    RemoteEndpoint.deviceName
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .addAction(action)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /** Dismisses the ongoing notification */
    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

}