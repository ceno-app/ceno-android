package ie.equalit.ceno.utils.unified_push_notifications

import android.Manifest
import android.R.attr.name
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings
import okhttp3.internal.notify
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.PushService
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage
import kotlin.jvm.java

class UnifiedPushServiceImpl : PushService() {
    override fun onNewEndpoint(
        endpoint: PushEndpoint,
        instance: String
    ) {
        Settings.setUnifiedPushNotificationsInstance(
            this,
            endpoint.url
        )
        createNotificationChannel(this, endpoint.url)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessage(
        message: PushMessage,
        instance: String
    ) {
        // Create an explicit intent for an Activity in your app.
        val intent = Intent(this, BrowserActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = Settings.getUnifiedPushNotificationsInstance(this)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.suggestedsites_cenomanual)
            .setContentTitle("New message")
            .setContentText(String(message.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this)
            .notify(1, builder.build())
    }

    override fun onRegistrationFailed(
        reason: FailedReason,
        instance: String
    ) {
        TODO("Not yet implemented")
    }

    override fun onUnregistered(instance: String) {
        TODO("Not yet implemented")
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        val descriptionText = "Receive push notifications from ntfy as a distributor"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelId, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}