/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import ie.equalit.ceno.addons.WebExtensionActionPopupActivity
import ie.equalit.ceno.base.BaseActivity
import ie.equalit.ceno.browser.BrowserFragment
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.browser.DefaultBrowsingManager
import ie.equalit.ceno.browser.dialogs.LoadExternalUrlDialog
import ie.equalit.ceno.browser.notification.AbstractPublicNotificationService
import ie.equalit.ceno.browser.notification.CenoNotificationBroadcastReceiver
import ie.equalit.ceno.browser.notification.PublicNotificationFeature
import ie.equalit.ceno.browser.notification.PublicNotificationService
import ie.equalit.ceno.components.ceno.TopSitesStorageObserver
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.setSecureScreen
import ie.equalit.ceno.home.HomeFragment.Companion.BEGIN_TOUR_TOOLTIP
import ie.equalit.ceno.metrics.NetworkMetrics
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.SettingsFragment
import ie.equalit.ceno.ui.theme.DefaultThemeManager
import ie.equalit.ceno.ui.theme.ThemeManager
import ie.equalit.ceno.utils.XMLParser
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import ie.equalit.ouinet.Ouinet.RunningState
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.SearchAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.WebExtensionState
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.search.ext.waitForSelectedOrDefaultSearchEngine
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.webextensions.WebExtensionPopupObserver
import java.io.IOException
import kotlin.system.exitProcess

/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : BaseActivity(),
    CenoNotificationBroadcastReceiver.NotificationListener {

    lateinit var themeManager: ThemeManager
    lateinit var browsingModeManager: BrowsingModeManager
    private val screenStartTime = System.currentTimeMillis()
    private var ouinetStartupTime = 0.0
    private var hasOuinetStarted = false
    private var hasRanChecksAndPermissions = false

    private var publicNotificationObserver: PublicNotificationFeature<PublicNotificationService>? =
        null
    private lateinit var cenoNotificationBroadcastReceiver: CenoNotificationBroadcastReceiver

    private val webExtensionPopupObserver by lazy {
        WebExtensionPopupObserver(components.core.store, ::openPopup)
    }

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private var isActivityResumed = false
    private var lastCall: (() -> Unit)? = null

    private lateinit var reminderNotificationIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeAndBrowsingMode(getModeFromIntentOrLastKnown())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (intent.action == Intent.ACTION_VIEW) {
            loadExternalUrl(SafeIntent(intent))
        }

        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            if ((destination.id == R.id.homeFragment || destination.id == R.id.browserFragment) && !hasRanChecksAndPermissions) {
                hasRanChecksAndPermissions = true

                if (Settings.showCrashReportingPermissionNudge(this)) {
                    showCrashReportingPermission()
                }
            }
        }

        components.useCases.customLoadUrlUseCase.onNoSelectedTab = { url ->
            openToBrowser(url, newTab = true, private = themeManager.currentMode.isPersonal)
        }

        Logger.info(" --------- Starting ouinet service")
        components.ouinet.setBackground(this)

        components.ouinet.background.startup()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Settings.setAllowNotifications(
            this, components.permissionHandler.isAllowingPostNotifications()
        )

        /* CENO: Set default behavior for AppBar */
        supportActionBar!!.apply {
            hide()
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(
                    this@BrowserActivity, R.color.ceno_action_bar
                )
                    .toDrawable()
            )
        }

        publicNotificationObserver = PublicNotificationFeature(
            applicationContext,
            components.core.store,
            PublicNotificationService::class,
        )

        cenoNotificationBroadcastReceiver = CenoNotificationBroadcastReceiver(this)
        val notificationIntentFilter = IntentFilter()
        notificationIntentFilter.addAction(AbstractPublicNotificationService.ACTION_CLEAR)
        notificationIntentFilter.addAction(AbstractPublicNotificationService.ACTION_STOP)
        notificationIntentFilter.addAction(ACTION_FOREGROUND_REMIND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(
                cenoNotificationBroadcastReceiver, notificationIntentFilter, RECEIVER_NOT_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                this,
                cenoNotificationBroadcastReceiver,
                notificationIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        if (Settings.shouldShowOnboarding(this)) {
            components.cenoPreferences.nextTooltip = BEGIN_TOUR_TOOLTIP
        }

        navHost.navController.popBackStack() // Remove startupFragment from backstack

        when {
            components.core.store.state.selectedTab == null -> navHost.navController.navigate(R.id.action_global_home)
            else -> navHost.navController.navigate(R.id.action_global_browser)
        }

        /* CENO: need to initialize top sites to be displayed in CenoHomeFragment */
        initializeTopSites()

        initializeSearchEngines()

        components.webExtensionPort.createPort()

        /* Do not notify user of data policy because we are not collecting telemetry data
        *  and we already have a notification for stopping/pausing/purging local CENO data
        * NotificationManager.checkAndNotifyPolicy(this)
         */
        lifecycle.addObserver(webExtensionPopupObserver)

        // check if a crash happened in the last session
        if (Settings.wasCrashSuccessfullyLogged(this@BrowserActivity)) {
            Settings.logSuccessfulCrashEvent(this@BrowserActivity, false)
            Toast.makeText(
                this@BrowserActivity, getString(R.string.crash_report_sent), Toast.LENGTH_SHORT
            )
                .show()
        }

        // reset the value of lastCrash if permission nudge won't be shown
        if (!Settings.showCrashReportingPermissionNudge(this)) {
            Settings.setCrashHappened(this@BrowserActivity, false) // reset the value of lastCrash
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

            reminderNotificationIntent = Intent(ACTION_FOREGROUND_REMIND).let {
                it.setPackage(packageName)
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
        updateOuinetStatus()

        if (Settings.isOuinetMetricsEnabled(this)) NetworkMetrics(
            this, CoroutineScope(Dispatchers.IO)
        ).collectNetworkMetrics()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

    }

    /* This function displays the popup that asks users if they want to opt in for
    the crash reporting feature
     */
    private fun showCrashReportingPermission() {
        // launch Sentry activation dialog
        val dialogView =
            View.inflate(this@BrowserActivity, R.layout.crash_reporting_nudge_dialog, null)
        val radio0 = dialogView.findViewById<RadioButton>(R.id.radio0)
        val radio1 = dialogView.findViewById<RadioButton>(R.id.radio1)

        val sentryActionDialog by lazy {
            AlertDialog.Builder(this)
                .apply {
                    setPositiveButton(getString(R.string.onboarding_warning_button)) { _, _ -> }
                }
        }

        AlertDialog.Builder(this)
            .apply {
                setView(dialogView)
                setPositiveButton(getString(R.string.onboarding_battery_button)) { _, _ ->
                    when {
                        radio0.isChecked -> {
                            Settings.alwaysAllowCrashReporting(this@BrowserActivity)
                            SentryAndroid.init(
                                this@BrowserActivity,
                                SentryOptionsConfiguration.getConfig(this@BrowserActivity)
                            )

                            sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_in))
                                .show()
                        }

                        radio1.isChecked -> {
                            Settings.neverAllowCrashReporting(this@BrowserActivity)
                            sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_out))
                                .show()
                        }
                    }
                }
                setOnDismissListener {
                    Settings.setCrashHappened(
                        this@BrowserActivity, false
                    ) // reset the value of lastCrash
                }
                setNegativeButton(getString(R.string.mozac_feature_prompt_not_now)) { _, _ ->
                    Settings.setCrashHappened(
                        this@BrowserActivity, false
                    ) // reset the value of lastCrash
                }
                create()
            }
            .show()
    }

    private fun getModeFromIntentOrLastKnown(): BrowsingMode {
        return if (components.core.store.state.selectedTab == null) BrowsingMode.Normal
        else cenoPreferences().lastKnownBrowsingMode
    }

    private fun setupThemeAndBrowsingMode(mode: BrowsingMode) {
        cenoPreferences().lastKnownBrowsingMode = mode
        themeManager = DefaultThemeManager(mode, this)
        browsingModeManager = DefaultBrowsingManager(mode, cenoPreferences()) { newMode ->
            themeManager.currentMode = newMode
            if (Settings.secureScreen(this)) {
                if (cenoPreferences().isSecureScreenPersonalOnly) {
                    window?.setSecureScreen(newMode.isPersonal)
                } else {
                    window?.setSecureScreen(true)
                }
            }
            components.appStore.dispatch(AppAction.ModeChange(newMode))
        }
        //components.appStore.dispatch(AppAction.ModeChange(mode))
    }

    private fun updateOuinetStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    val status = RunningState.valueOf(components.ouinet.background.getState())
                    if (components.appStore.state.ouinetStatus != status) {
                        components.appStore.dispatch(AppAction.OuinetStatusChange(status))
                        if (!hasOuinetStarted && status == RunningState.Started) {
                            ouinetStartupTime =
                                (System.currentTimeMillis() - screenStartTime) / MILLISECOND
                            hasOuinetStarted = true
                        }
                        if (status == RunningState.Started || status == RunningState.Degraded) {
                            components.ouinet.updateEndpoints()
                        }
                    }
                    delay(DELAY_TWO_SECONDS)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + FOREGROUND_TIMEOUT_REMINDER_DURATION,
            reminderNotificationIntent
        )
    }

    override fun onStart() {
        super.onStart()
        components.notificationsDelegate.bindToActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        components.notificationsDelegate.unBindActivity(this)
        publicNotificationObserver?.stop()
        this.unregisterReceiver(cenoNotificationBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()

        /* CENO: in Android 9 or later, it is possible that the
         * service may have stopped while app was in background
         * try sending an intent to restart the service
         */
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) && components.ouinet.background.getState() != RunningState.Started.toString()) {
            Logger.info(" --------- Starting ouinet service onResume")
            components.ouinet.background.start()
        }
        isActivityResumed = true
        //If we have some fragment to show do it now then clear the queue
        if (lastCall != null) {
            updateView(lastCall!!)
            lastCall = null
        }

        /*
        CENO: Update behavior for AppBar
        This needs to be optimized to reduce the need to update this part of the codebase when a new fragment is created
        */
        supportActionBar!!.apply {
            when (navHost.navController.currentDestination?.id) {
                R.id.settingsFragment,
                R.id.networkSettingsFragment,
                R.id.privacySettingsFragment,
                R.id.customizationSettingsFragment,
                R.id.installedSearchEnginesSettingsFragment,
                R.id.deleteBrowsingDataFragment,
                R.id.aboutFragment,
                R.id.websiteSourceSettingsFragment
                -> show()

                else -> hide()
            }
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(
                    this@BrowserActivity, R.color.ceno_action_bar
                )
                    .toDrawable()
            )
        }
        publicNotificationObserver?.start()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) alarmManager.cancel(
            reminderNotificationIntent
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Settings.setAllowNotifications(this, isGranted)
            components.permissionHandler.requestBatteryOptimizationsOff(this)
        }

    val getLogfileLocation =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri?.let {
                try {
                    // get logs from internal storage
                    this.openFileInput("${getString(R.string.ceno_android_logs_file_name)}.txt")
                        .bufferedReader()
                        .useLines { lines ->
                            val fileContent = lines.toMutableList()
                                .joinToString("\n")
                            val file = contentResolver.openOutputStream(it)
                            file?.write(fileContent.toByteArray())
                            file?.close()
                        }
                } catch (e: IOException) {
                    Log.e(TAG, e.message.toString())
                }
            }
        }

    /* CENO: Handle intent sent to BrowserActivity to open to Homepage or open a homescreen shortcut link */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val safeIntent = SafeIntent(intent)
        if (safeIntent.action == AbstractPublicNotificationService.ACTION_TAP) {
            val bundle = Bundle().apply {
                putBoolean(SettingsFragment.SCROLL_TO_CACHE, true)
            }
            navHost.navController.navigate(R.id.action_global_settings, bundle)
        }
        if (safeIntent.action == Intent.ACTION_VIEW) {
            loadExternalUrl(safeIntent)
        }

    }

    override fun onUserLeaveHint() {
        val fragment: Fragment? =
            navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (fragment is UserInteractionHandler && fragment.onHomePressed()) {
            return
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(
        parent: View?, name: String, context: Context, attrs: AttributeSet
    ): View? = when (name) {
        EngineView::class.java.name -> components.core.engine.createView(context, attrs)
            .asView()

        else -> super.onCreateView(parent, name, context, attrs)
    }

    private fun openPopup(webExtensionState: WebExtensionState) {
        val intent = Intent(this, WebExtensionActionPopupActivity::class.java)
        intent.putExtra("web_extension_id", webExtensionState.id)
        intent.putExtra("web_extension_name", webExtensionState.name)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    /* CENO: Add function to open requested site in BrowserFragment */
    fun openToBrowser(url: String? = null, newTab: Boolean = false, private: Boolean = false) {
        if (url != null) {
            if (newTab) {
                //set browsingMode
                browsingModeManager.mode = BrowsingMode.fromBoolean(private)
                components.useCases.tabsUseCases.addTab(
                    url = url,
                    selectTab = true,
                    private = private,
                )
            } else {
                components.useCases.sessionUseCases.loadUrl(
                    url = url
                )
            }
        }
        showBrowser()
    }

    private fun showBrowser() {

        if (navHost.navController.currentDestination?.id == R.id.browserFragment) {
            return
        }

        navHost.navController.navigate(R.id.action_global_browser)
    }

    fun switchBrowsingModeHome(currentMode: BrowsingMode) {
        browsingModeManager.mode = BrowsingMode.fromBoolean(!currentMode.isPersonal)

        components.appStore.dispatch(AppAction.ModeChange(browsingModeManager.mode))
    }

    fun updateView(action: () -> Unit) {
        //If the activity is in background we register the transaction
        if (!isActivityResumed) {
            lastCall = action
        } else {
            //Else we just invoke it
            action.invoke()
        }
    }

    private fun shutdownCallback(doClear: Boolean): Runnable {
        return Runnable {
            if (doClear) {
                val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                am.clearApplicationUserData()
            }
            exitProcess(0)
        }
    }

    fun beginShutdown(
        doClear: Boolean,
        stalledDuration: Long = resources.getInteger(R.integer.shutdown_fragment_stalled_duration)
            .toLong()
    ) {
        val handler = Handler(Looper.myLooper()!!)
        val callback = shutdownCallback(doClear)
        handler.postDelayed(
            callback, stalledDuration
        )
        components.ouinet.background.shutdown(doClear) {
            handler.removeCallbacks(callback)
            callback.run()
        }
    }

    /* CENO: Function to initialize top site storage and observer */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeTopSites() {/*  Launch a coroutine to initialize top site storage cache and update it in the store */
        val defaultTopSites: List<Pair<String, String>>? =
            if (!components.cenoPreferences.defaultTopSitesAdded) {
                XMLParser.parseTopsitesXml(
                    applicationContext.resources.getXml(R.xml.default_topsites),
                    this
                ) as List<Pair<String, String>>
            } else {
                null
            }
        GlobalScope.launch(Dispatchers.IO) {
            if (!components.cenoPreferences.defaultTopSitesAdded && defaultTopSites != null) {
                components.core.cenoTopSitesStorage.addTopSites(defaultTopSites)
                components.cenoPreferences.defaultTopSitesAdded = true
            }
            components.core.cenoTopSitesStorage.getTopSites(
                totalSites = components.cenoPreferences.topSitesMaxLimit
            )
            components.appStore.dispatch(
                AppAction.Change(
                    topSites = components.core.cenoTopSitesStorage.cachedTopSites.sort()
                )
            )
        }

        /* Register TopSitesStorageObserver, which will update AppStore when top sites are changed/added/removed */
        components.core.cenoTopSitesStorage.apply {
            register(
                observer = TopSitesStorageObserver(
                    this, components.cenoPreferences, components.appStore
                )
            )
        }
    }

    private fun initializeSearchEngines() {
        components.core.store.dispatch(SearchAction.RefreshSearchEnginesAction)
        if (Settings.shouldUpdateSearchEngines(this)) {
            components.core.store.state.search.searchEngines.filter { searchEngine ->
                searchEngine.id in listOf(
                    getString(R.string.remove_search_engine_id_1),
                    getString(R.string.remove_search_engine_id_2)
                )
            }
                .forEach { searchEngine ->
                    components.useCases.searchUseCases.removeSearchEngine(searchEngine)
                }
            components.core.store.waitForSelectedOrDefaultSearchEngine {
                components.core.store.state.search.searchEngines.forEach { searchEngine ->
                    if (searchEngine.id == getString(R.string.default_search_engine_id)) {
                        components.useCases.searchUseCases.selectSearchEngine(searchEngine)
                    }
                }
            }
            Logger.debug("${components.core.store.state.search.searchEngines}")
            Logger.debug("${components.core.store.state.search.selectedOrDefaultSearchEngine}")
            Settings.setUpdateSearchEngines(this, false)
        }
    }

    fun openSettings() {
        val bundle = Bundle().apply {
            putBoolean(SettingsFragment.SCROLL_TO_BRIDGE, true)
        }
        navHost.navController.navigate(R.id.action_global_settings, bundle)
    }

    companion object {
        private const val TAG = "BrowserActivity"
        const val DELAY_TWO_SECONDS = 2000L
        const val ACTION_FOREGROUND_REMIND = "ie.equalit.ceno.browser.notification.action.REMIND"
        const val FOREGROUND_TIMEOUT_REMINDER_DURATION: Long = 18000000L

        const val DEFAULT_SHUTDOWN_DURATION: Long = 500L
        const val MILLISECOND: Double = 1000.0
    }

    override fun onStopTapped() {
        publicNotificationObserver?.stop()
        val duration = if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resources.getInteger(R.integer.shutdown_fragment_stalled_duration)
                .toLong()
        } else {
            DEFAULT_SHUTDOWN_DURATION
        }
        beginShutdown(doClear = false, stalledDuration = duration)
    }

    override fun onClearTapped() {
        publicNotificationObserver?.stop()
        //if the app is in foreground, set the duration to show standby fragment until ouinet is closed to 15seconds
        val duration = if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resources.getInteger(R.integer.shutdown_fragment_stalled_duration)
                .toLong()
        } else {
            DEFAULT_SHUTDOWN_DURATION
        }
        beginShutdown(doClear = true, stalledDuration = duration)
    }

    private fun loadExternalUrl(safeIntent: SafeIntent) {
        val url = safeIntent.dataString
        if (url.isNullOrBlank()) {
            Logger.debug("ACTION_VIEW without dataString; ignoring.")
            return
        }
        //show dialog
        if (Settings.shouldVerifyExternalUrl(this)) {
            val dialog = LoadExternalUrlDialog(this, url) {
                components.utils.intentProcessor.process(safeIntent.unsafe)
                navHost.navController.navigate(R.id.action_global_browser)
            }.getDialog()
            if (isFinishing || isDestroyed) return
            dialog.show()
        } else {
            components.utils.intentProcessor.process(safeIntent.unsafe)
            navHost.navController.navigate(R.id.action_global_browser)
        }
    }
}
