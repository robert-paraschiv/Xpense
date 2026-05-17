package com.rokudo.xpense.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rokudo.xpense.R
import com.rokudo.xpense.activities.MainActivity
import com.rokudo.xpense.utils.DatabaseUtils

class FCMService : FirebaseMessagingService(), DefaultLifecycleObserver {
    companion object {
        private const val TAG = "FCMService"

        fun createIntentForNotification(context: Context, walletID: String): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra("walletID", walletID)
            }
        }

        fun createNotificationChannel(notificationManager: NotificationManager, channelId: String) {
            val channel = NotificationChannel(
                channelId, "Message notifications", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        fun drawableToBitmap(drawable: Drawable?): Bitmap {
            if (drawable is BitmapDrawable) return drawable.bitmap
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }

    private var isAppInForeground = false

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        prepareNotification(remoteMessage)
    }

    private fun prepareNotification(remoteMessage: RemoteMessage) {
        if (isAppInForeground) {
            Log.d(TAG, "prepareNotification: was in foreground")
        } else {
            if (remoteMessage.data.isNotEmpty()) {
                val senderPictureUrl = remoteMessage.data["senderPictureUrl"]
                if (senderPictureUrl == null || senderPictureUrl == "null") {
                    val bitmap = drawableToBitmap(
                        AppCompatResources.getDrawable(applicationContext, R.drawable.ic_baseline_person_24)
                    )
                    sendNotification(bitmap, remoteMessage)
                } else {
                    Glide.with(applicationContext).asBitmap().load(senderPictureUrl)
                        .circleCrop()
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                sendNotification(resource, remoteMessage)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            }
        }
    }

    private fun sendNotification(bitmap: Bitmap, remoteMessage: RemoteMessage) {
        val senderPhoneNumber = remoteMessage.data["senderPhoneNumber"]
        val notificationID = getNotificationID(senderPhoneNumber)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.default_notification_channel_id)
        createNotificationChannel(notificationManager, channelId)
        sendNewNotification(bitmap, remoteMessage, notificationID, channelId, notificationManager)
    }

    private fun sendNewNotification(
        bitmap: Bitmap, remoteMessage: RemoteMessage, notificationID: Int,
        channelId: String, notificationManager: NotificationManager
    ) {
        val senderName = remoteMessage.data["senderName"]
        val walletTitle = remoteMessage.data["walletTitle"]
        val walletID = remoteMessage.data["walletID"] ?: ""

        val bundle = android.os.Bundle().apply {
            putString("walletID", walletID)
            putInt("notificationID", notificationID)
        }

        val acceptIntent = Intent(this, AcceptReceiver::class.java).putExtras(bundle)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, notificationID, acceptIntent, PendingIntent.FLAG_IMMUTABLE)
        val acceptAction = Notification.Action.Builder(null, "Accept", acceptPendingIntent).build()

        val declineIntent = Intent(this, DeclineReceiver::class.java).putExtras(bundle)
        val declinePendingIntent = PendingIntent.getBroadcast(this, notificationID, declineIntent, PendingIntent.FLAG_IMMUTABLE)
        val declineAction = Notification.Action.Builder(null, "Decline", declinePendingIntent).build()

        val intent = createIntentForNotification(this, walletID)
        val pendingIntent = PendingIntent.getActivity(this, notificationID, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(resources.getColor(R.color.message_received_backgroundColor, null))
            .setLargeIcon(bitmap)
            .setContentTitle(senderName)
            .setContentText("has invited you to join his wallet: $walletTitle")
            .addAction(acceptAction)
            .addAction(declineAction)
            .setShortcutId("$notificationID")
            .setLocusId(LocusId("$notificationID"))
            .setCategory(Notification.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun getNotificationID(senderPhoneNumber: String?): Int {
        if (senderPhoneNumber != null && senderPhoneNumber != "null") {
            val rectified = senderPhoneNumber.replace(Regex("[^\\d]"), "")
            return rectified.substring(rectified.length - 9).toInt()
        }
        return 0
    }

    override fun onNewToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.phoneNumber != null) {
            DatabaseUtils.usersRef.document(user.phoneNumber!!).update("token", token)
            Log.d(TAG, "onNewToken: $token")
        }
    }

    override fun onCreate() {
        super<FirebaseMessagingService>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
    }

    override fun onDestroy() {
        super<FirebaseMessagingService>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}

