/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import ie.equalit.ceno.ext.isCrashReportActive
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.NotificationConfig
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.SystemAction
import mozilla.components.concept.engine.webextension.isUnsupported
import mozilla.components.feature.addons.update.GlobalAddonDependencyProvider
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.ktx.android.content.runOnlyInMainProcess
import mozilla.components.support.rusthttp.RustHttpConfig
import mozilla.components.support.rustlog.RustLog
import mozilla.components.support.webextensions.WebExtensionSupport
import org.cleaninsights.sdk.CleanInsights
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

open class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    private var _tracker: Tracker? = null
    @Synchronized
    fun getTracker(): Tracker? {
        if (_tracker == null) {
            _tracker = TrackerBuilder.createDefault("https://matomo.ouinet.work/matomo.php", 4).build(Matomo.getInstance(this))
        }
        return _tracker
    }


    override fun onCreate() {
        super.onCreate()

        context = this

        /* CENO: Read default preferences and set the default theme immediately at startup */
        PreferenceManager.setDefaultValues(this, R.xml.default_preferences, false)
        AppCompatDelegate.setDefaultNightMode(
                Settings.getAppTheme(this)
        )

        // Record exceptions as well as app crashes
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            if(!Settings.isCrashReportingPermissionGranted(this)) {
                Settings.setCrashHappenedCommit(this, true)
            }
            exitProcess(0)
        }

        setupCrashReporting(this)

        RustHttpConfig.setClient(lazy { components.core.client })
        setupLogging()

        // Initialize Sentry-Android
        if(Settings.isCrashReportingPermissionGranted(this)) {
            SentryAndroid.init(
                this,
                SentryOptionsConfiguration.getConfig(this)
            )
        }

        if (!isMainProcess()) {
            // If this is not the main process then do not continue with the initialization here. Everything that
            // follows only needs to be done in our app's main process and should not be done in other processes like
            // a GeckoView child process or the crash handling process. Most importantly we never want to end up in a
            // situation where we create a GeckoRuntime from the Gecko child process (
            return
        }

        /* CENO: Must add root cert prior to startup of Gecko Engine, so it is installed during GeckoViewStartup */
        components.ouinet.setConfig()
        components.core.setRootCertificate(components.ouinet.config.caRootCertPath)

        components.core.engine.warmUp()

        restoreBrowserState()

        GlobalAddonDependencyProvider.initialize(
            components.core.addonManager,
            components.core.addonUpdater,
        )
        WebExtensionSupport.initialize(
            runtime = components.core.engine,
            store = components.core.store,
            onNewTabOverride = { _, engineSession, url ->
                val tabId = components.useCases.tabsUseCases.addTab(
                    url = url,
                    selectTab = true,
                    engineSession = engineSession,
                )
                tabId
            },
            onCloseTabOverride = { _, sessionId ->
                components.useCases.tabsUseCases.removeTab(sessionId)
            },
            onSelectTabOverride = { _, sessionId ->
                components.useCases.tabsUseCases.selectTab(sessionId)
            },
            onExtensionsLoaded = { extensions ->
                components.core.addonUpdater.registerForFutureUpdates(extensions)

                val checker = components.core.supportedAddonsChecker
                val hasUnsupportedAddons = extensions.any { it.isUnsupported() }
                if (hasUnsupportedAddons) {
                    checker.registerForChecks()
                } else {
                    // As checks are a persistent subscriptions, we have to make sure
                    // we remove any previous subscriptions.
                    checker.unregisterForChecks()
                }
            },
            onUpdatePermissionRequest = components.core.addonUpdater::onUpdatePermissionRequest,
        )

        /* CENO F-Droid: Not using firefox accounts, firebase push breaks f-droid build */
        /*
        components.push.feature?.let {
            Logger.info("AutoPushFeature is configured, initializing it...")

            PushProcessor.install(it)

            // WebPush integration to observe and deliver push messages to engine.
            WebPushEngineIntegration(components.core.engine, it).start()

            // Initialize the push feature and service.
            it.initialize()
        }
        */
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        // Since there's no good callback before the app gets killed, we work around this problem
        // by persisting, when the OS gets annoyed about memory consumption, which is an indicator,
        // that we're soon going to get killed.
        cleanInsights.persist()

        runOnlyInMainProcess {
            components.core.store.dispatch(SystemAction.LowMemoryAction(level))
            components.core.icons.onTrimMemory(level)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun restoreBrowserState() = GlobalScope.launch(Dispatchers.Main) {
        val store = components.core.store
        val sessionStorage = components.core.sessionStorage

        components.useCases.tabsUseCases.restore(sessionStorage)

        // Now that we have restored our previous state (if there's one) let's setup auto saving the state while
        // the app is used.
        sessionStorage.autoSave(store)
            .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
            .whenGoingToBackground()
            .whenSessionsChange()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        // Since there's no good callback before the app gets killed, we work around this problem
        // by persisting, when the OS gets annoyed about memory consumption, which is an indicator,
        // that we're soon going to get killed.
        cleanInsights.persist()
    }

    override fun onTerminate() {
        super.onTerminate()

        // This only works in emulators, but nevertheless, for completeness sakes.
        cleanInsights.persist()
    }


    companion object {
        var mOuinetConfig: Config? = null
        var mNotificationConfig: NotificationConfig? = null
        const val NON_FATAL_CRASH_BROADCAST = "ie.equalit.ceno"

        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        @JvmStatic
        val cleanInsights: CleanInsights by lazy {
            CleanInsights(
                context!!.assets.open("cleaninsights.json").reader().readText(),
                context!!.filesDir)
        }

    }
}

private fun setupLogging() {
    // We want the log messages of all builds to go to Android logcat
    Log.addSink(AndroidLogSink())
    RustLog.enable()
}

private fun setupCrashReporting(application: BrowserApplication) {
    if (isCrashReportActive) {
        application
            .components
            .analytics
            .crashReporter.install(application)
    }
}
