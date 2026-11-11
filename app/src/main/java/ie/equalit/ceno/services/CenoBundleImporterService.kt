package ie.equalit.ceno.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.ui.res.pluralStringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel
import kotlinx.serialization.json.Json

class CenoBundleImporterService : Service() {

    private val viewModel = CenoBundleImporterViewModel()

    private val notificationId by lazy {
        System.currentTimeMillis()
            .toInt()
    }

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
        intent ?: return START_NOT_STICKY
        val zipExtra = intent.getStringExtra(ZIP_EXTRA) ?: throw Exception("zip uri not found")

        val notificationIntent = Intent(this, BrowserActivity::class.java).apply {
            action = ACTION_TAP
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val notification = createForegroundNotification(
            notificationIntent,
            R.drawable.ic_launcher_foreground,
            notificationId,
            inProgress = true,
            title = getString(R.string.preparing_to_import),
            description = getString(R.string.shutting_down_ouinet_client)
        )
        startForeground(notificationId, notification.notification)

        val cmd =
            Json.decodeFromString<List<CenoBundleImporterViewModel.CenoBundleMetaData>>(zipExtra)
        try {
            components.ouinet.background.shutdown(false) {
                unzip(
                    intent = notificationIntent,
                    cmd = cmd,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun unzip(
        intent: Intent,
        cmd: List<CenoBundleImporterViewModel.CenoBundleMetaData>
    ) {
        viewModel.unzip(
            context = this,
            cmd = cmd,
            progressCallback = { siteName, progress, total ->
                val notification = createForegroundNotification(
                    intent,
                    R.drawable.ic_launcher_foreground,
                    notificationId,
                    inProgress = true,
                    title = getString(R.string.importing_for, siteName, siteName),
                    description = getString(R.string.this_will_take_while),
                    progress = progress,
                    total = total
                )

                startForeground(notificationId, notification.notification)
            },
        ) {
            try {
                components.ouinet.background.start {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showNotification(
                R.drawable.ic_launcher_foreground,
                getString(R.string.finished_importing),
                resources.getQuantityString(
                    R.plurals.sites_imported,
                    cmd.size,
                    cmd.size,
                ),
                intent
            )
        }
    }

    private fun createForegroundNotification(
        intent: Intent,
        icon: Int,
        notificationId: Int,
        inProgress: Boolean,
        title: String = "",
        description: String = "",
        progress: Int = 0,
        total: Int = 0
    ): ForegroundInfo {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis()
                .toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(icon)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (inProgress) {
                    this.setProgress(0, 0, true)
                } else {
                    this.setProgress(total, progress, false)
                }
            }

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
            setSound(null, null)
            enableVibration(false)
        }
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(
        icon: Int,
        title: String,
        description: String,
        intent: Intent
    ) {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis()
                .toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            // double-check permission check here to satisfy IDE lint compiler warnings
            if (ActivityCompat.checkSelfPermission(
                    this@CenoBundleImporterService,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {

                notify(notificationId, builder.build())
            }
        }
    }

    companion object {
        const val ZIP_EXTRA = "ZIP_EXTRA"
        const val CHANNEL_ID = "ie.ceno.bundle_importer"

        const val ACTION_TAP = "ie.ceno.CenoBundleImporterService.ACTION_TAP"
    }
}