package com.andarb.movietinder.model.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.nearby.Nearby

class ConnectionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action.equals(NOTIFICATION_INTENT_FILTER) && context != null) {
            Nearby.getConnectionsClient(context).stopAllEndpoints()

            val notificationManagement = NotificationManagement(context)
            notificationManagement.cancel()
        }
    }
}