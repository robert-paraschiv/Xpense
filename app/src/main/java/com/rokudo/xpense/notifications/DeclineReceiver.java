package com.rokudo.xpense.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rokudo.xpense.models.Invitation;
import com.rokudo.xpense.utils.DatabaseUtils;

public class DeclineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("walletID")) {
            DatabaseUtils.invitationsRef.document(intent.getStringExtra("walletID"))
                    .update("status", Invitation.STATUS_DECLINED);

            int notificationID = intent.getIntExtra("notificationID", 0);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationID);
        }
    }
}
