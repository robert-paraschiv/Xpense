package com.rokudo.xpense.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.utils.DatabaseUtils

class DeclineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra("walletID")) {
            DatabaseUtils.invitationsRef.document(intent.getStringExtra("walletID")!!)
                .update("status", Invitation.STATUS_DECLINED)
            val notificationID = intent.getIntExtra("notificationID", 0)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationID)
        }
    }
}

