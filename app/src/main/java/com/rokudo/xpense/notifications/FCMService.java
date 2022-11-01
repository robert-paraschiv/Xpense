package com.rokudo.xpense.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.LocusId;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rokudo.xpense.R;
import com.rokudo.xpense.activities.MainActivity;
import com.rokudo.xpense.utils.DatabaseUtils;

public class FCMService extends FirebaseMessagingService implements DefaultLifecycleObserver {
    private static final String TAG = "FCMService";

    private boolean isAppInForeground;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        prepareNotification(remoteMessage);
    }

    private void prepareNotification(RemoteMessage remoteMessage) {
        if (isAppInForeground) {
            Log.d(TAG, "prepareNotification: was in foreground");
        } else {
            if (remoteMessage.getData().size() > 0) {
                String senderPictureUrl = remoteMessage.getData().get("senderPictureUrl");

                if (senderPictureUrl == null || senderPictureUrl.equals("null")) {
                    Bitmap bitmap = drawableToBitmap(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_person_24));
                    sendNotification(bitmap, remoteMessage);
                } else {
                    Glide.with(getApplicationContext()).asBitmap().load(senderPictureUrl)
                            .circleCrop()
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                    sendNotification(bitmap, remoteMessage);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                }
            }
        }
    }

    private void sendNotification(Bitmap bitmap, RemoteMessage remoteMessage) {
        String senderPhoneNumber = remoteMessage.getData().get("senderPhoneNumber");

        int notificationID = getNotificationID(senderPhoneNumber);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = getString(R.string.default_notification_channel_id);
        createNotificationChannel(notificationManager, channelId);


        sendNewNotification(bitmap, remoteMessage, notificationID, channelId, notificationManager);

    }

    private void sendNewNotification(Bitmap bitmap, RemoteMessage remoteMessage, int notificationID,
                                     String channelId, NotificationManager notificationManager) {

        String senderName = remoteMessage.getData().get("senderName");
        String walletTitle = remoteMessage.getData().get("walletTitle");
        String walletID = remoteMessage.getData().get("walletID");

        Bundle bundle = new Bundle();
        bundle.putString("walletID", walletID);
        bundle.putInt("notificationID", notificationID);

        Intent acceptIntent = new Intent(this, AcceptReceiver.class).putExtras(bundle);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, notificationID, acceptIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Action acceptAction = new Notification.Action.Builder(null, "Accept", acceptPendingIntent).build();

        Intent declineIntent = new Intent(this, DeclineReceiver.class).putExtras(bundle);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(this, notificationID, declineIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Action declineAction = new Notification.Action.Builder(null, "Decline", declinePendingIntent).build();

        Intent intent = createIntentForNotification(FCMService.this, walletID);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder notificationBuilder = new Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(getResources().getColor(R.color.message_received_backgroundColor, null))
                .setLargeIcon(bitmap)
                .setContentTitle(senderName)
                .setContentText("has invited you to join his wallet: " + walletTitle)
                .addAction(acceptAction)
                .addAction(declineAction)
                .setShortcutId("" + notificationID)
                .setLocusId(new LocusId("" + notificationID))
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @NonNull
    public static Intent createIntentForNotification(Context context, String walletID) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("walletID", walletID);
        return intent;
    }


    public static void createNotificationChannel(NotificationManager notificationManager, String channelId) {
        // Since android Oreo notification channel is needed.
        NotificationChannel channel = new NotificationChannel(channelId,
                "Message notifications",
                NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 200});
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build());

        notificationManager.createNotificationChannel(channel);
    }


    private int getNotificationID(String senderPhoneNumber) {
        int notificationID = 0;
        if (senderPhoneNumber != null && !senderPhoneNumber.equals("null")) {
            String rectifiedString = senderPhoneNumber.replaceAll("[^\\d]", "");
            notificationID = Integer.parseInt(rectifiedString.substring(rectifiedString.length() - 9));
        }
        return notificationID;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null) {
            DatabaseUtils.usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).update("token", token);
            Log.d(TAG, "onNewToken: " + token);
        }
    }

    @Override
    public void onCreate() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        isAppInForeground = true;
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        isAppInForeground = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

}
