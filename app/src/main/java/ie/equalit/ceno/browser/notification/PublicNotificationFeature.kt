package ie.equalit.ceno.browser.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import ie.equalit.ceno.ext.ifChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import kotlin.reflect.KClass

class PublicNotificationFeature<T:AbstractPublicNotificationService>(
    context: Context,
    private val store: BrowserStore,
    private val notificationServiceClass: KClass<T>
) :LifecycleAwareFeature{

    private val applicationContext = context.applicationContext
    private var scope: CoroutineScope? = null

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { state -> state.normalTabs.isNotEmpty() }
                .ifChanged()
                .collect { hasPublicTabs ->
                    if (hasPublicTabs) {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@collect
                        }
                        applicationContext.startService(Intent(applicationContext, notificationServiceClass.java))
                    }
                }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}