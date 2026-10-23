package ie.equalit.ceno.ui.ouicrawl

import android.R.attr.description
import android.R.attr.label
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import androidx.work.ForegroundInfo
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

        val notification = createForegroundNotification(
            intent,
            R.drawable.ic_launcher_foreground,
            notificationId,
            inProgress = true,
            title = getString(R.string.preparing_to_import),
            description = getString(R.string.shutting_down_ouinet_client)
        )
        startForeground(notificationId, notification.notification)

        val cmd = Json.decodeFromString<List<CenoBundleImporterViewModel.CenoBundleMetaData>>(zipExtra)
        try {
            components.ouinet.background.shutdown(false) {
                unzip(
                    context = this,
                    intent = intent,
                    cmd = cmd,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun unzip(
        context: Context,
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
            val notification = createForegroundNotification(
                intent,
                R.drawable.ic_launcher_foreground,
                notificationId,
                inProgress = false,
                title = context.getString(R.string.finished_importing),
                description = getString(R.string.sites_imported, cmd.size)
            )
            startForeground(notificationId, notification.notification)
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
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(icon)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentIntent(pendingIntent)
            .apply {
                if(inProgress) {
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

    companion object {
        const val ZIP_EXTRA = "ZIP_EXTRA"
        const val CHANNEL_ID = "ie.ceno.bundle_importer"
    }
}