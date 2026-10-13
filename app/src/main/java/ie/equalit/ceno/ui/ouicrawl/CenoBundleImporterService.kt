package ie.equalit.ceno.ui.ouicrawl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.work.ForegroundInfo
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel

class CenoBundleImporterService: Service() {

    private val viewModel = CenoBundleImporterViewModel()

    private val notificationId = System.currentTimeMillis().toInt()

    // Binder given to clients.
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): CenoBundleImporterService = this@CenoBundleImporterService
    }

    override fun onCreate() {
        createNotificationChannel()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: throw Exception("Intent should not be null")

        val notification = createForegroundNotification(
            intent,
            R.drawable.ic_launcher_foreground,
            100,
            0,
            true
        )
        startForeground(notificationId, notification.notification)


        val baseZipUri = intent.getStringExtra(BASE_ZIP_EXTRA) ?: throw Exception("Base zip uri not found")
        val latestZipUri = intent.getStringExtra(LATEST_ZIP_EXTRA) ?: throw Exception("Latest zip uri not found")

        viewModel.unzip(
            context = this,
            intent = intent,
            baseZipUri.toUri(),
            latestZipUri.toUri()
        ) { processing, progress ->
            val notification = createForegroundNotification(
                intent,
                R.drawable.ic_launcher_foreground,
                100,
                progress,
                processing
            )

            startForeground(notificationId, notification.notification)
        }

        return START_STICKY
    }

    private fun createForegroundNotification(
        intent: Intent,
        icon: Int,
        maxProgress: Int,
        progress: Int = 0,
        isProcessing: Boolean = false,
    ) : ForegroundInfo {
        val progress = progress + 1
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Importing progress"
        val description = "Importing base..."

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(icon)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentIntent(pendingIntent)
            .setProgress(maxProgress, progress, isProcessing)

        val notification = builder.build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                notificationId,
                notification
            )
        }
    }

    private fun createNotificationChannel() {
        val progressChannelName = getString(R.string.ouicrawl_importer_progress)
        val descriptionText = getString(R.string.view_ouicrawl_importer_progress)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, progressChannelName, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val BASE_ZIP_EXTRA = "BASE_ZIP_EXTRA"
        const val LATEST_ZIP_EXTRA = "LATEST_ZIP_EXTRA"
        const val CHANNEL_ID = "ie.ceno.bundle_importer"
    }
}