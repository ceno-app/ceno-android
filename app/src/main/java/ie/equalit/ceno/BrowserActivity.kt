/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.lib.crash.Crash
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.webextensions.WebExtensionPopupFeature
import ie.equalit.ceno.addons.WebExtensionActionPopupActivity
import ie.equalit.ceno.base.BaseActivity
import ie.equalit.ceno.browser.BaseBrowserFragment
import ie.equalit.ceno.browser.BrowserFragment
import ie.equalit.ceno.browser.CrashIntegration
import ie.equalit.ceno.browser.ExternalAppBrowserFragment
import ie.equalit.ceno.components.ceno.CenoWebExt.CENO_EXTENSION_ID
import ie.equalit.ceno.components.ceno.TopSitesStorageObserver
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.isCrashReportActive
import ie.equalit.ceno.settings.Settings
import ie.equalit.ouinet.OuinetNotification
import ie.equalit.ceno.components.PermissionHandler
import ie.equalit.ceno.ext.ceno.onboardingToHome
import ie.equalit.ceno.utils.SentryOptionsConfiguration
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.*
import mozilla.components.concept.engine.manifest.WebAppManifest
import mozilla.components.feature.addons.ui.translateName
import mozilla.components.feature.pwa.ext.putWebAppManifest
import kotlin.system.exitProcess

/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : BaseActivity() {

    private lateinit var crashIntegration: CrashIntegration

    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    private val tab: SessionState?
        get() = components.core.store.state.findCustomTabOrSelectedTab(sessionId)

    private val webExtensionPopupFeature by lazy {
        WebExtensionPopupFeature(components.core.store, ::openPopup)
    }

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private var isActivityResumed = false
    private var lastCall: (() -> Unit)? = null

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?) {
        navHost.navController.navigate(R.id.action_global_browser)
    }

    /**
     * Returns a new instance of [ExternalAppBrowserFragment] to display.
     */
    open fun createExternalAppBrowserFragment(
        sessionId: String,
        manifest: WebAppManifest?,
        trustedScopes: List<Uri>
    ) {
        navHost.navController.navigate(R.id.action_global_external_browser, Bundle().apply {
            "session_id" to sessionId
            putWebAppManifest(manifest)
            putParcelableArrayList("org.mozilla.samples.browser.TRUSTED_SCOPES", ArrayList(trustedScopes))
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        components.useCases.customLoadUrlUseCase.onNoSelectedTab = { url ->
            openToBrowser(url, newTab = true)
        }

        Logger.info(" --------- Starting ouinet service")
        components.ouinet.let {
            it.setOnNotificationTapped {
                beginShutdown(false)
            }
            it.setOnConfirmTapped {
                beginShutdown(true)
            }
            it.setBackground(this)
        }

        components.ouinet.background.startup()

        /* CENO: Set default behavior for AppBar */
        supportActionBar!!.apply {
            hide()
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@BrowserActivity, R.color.ceno_action_bar)))
        }

        val safeIntent = SafeIntent(intent)

        navHost.navController.popBackStack() // Remove startupFragment from backstack
        navHost.navController.navigate(
            when {
                Settings.shouldShowOnboarding(this) && savedInstanceState == null -> R.id.action_global_onboarding
                components.core.store.state.selectedTab == null -> R.id.action_global_home
                else -> R.id.action_global_browser
            }
        )

        /* CENO: need to initialize top sites to be displayed in CenoHomeFragment */
        initializeTopSites()

        initializeSearchEngines()

        if (isCrashReportActive) {
            crashIntegration = CrashIntegration(this, components.analytics.crashReporter) { crash ->
                onNonFatalCrash(crash)
            }
            lifecycle.addObserver(crashIntegration)
        }

        /* Do not notify user of data policy because we are not collecting telemetry data
        *  and we already have a notification for stopping/pausing/purging local CENO data
        * NotificationManager.checkAndNotifyPolicy(this)
         */
        lifecycle.addObserver(webExtensionPopupFeature)

        // check if a crash happened in the last session
        if(Settings.wasCrashSuccessfullyLogged(this@BrowserActivity)) {
            Settings.logSuccessfulCrashEvent(this@BrowserActivity, false)
            Toast.makeText(this@BrowserActivity, getString(R.string.crash_report_sent), Toast.LENGTH_SHORT).show()
        }

        // Check for previous crashes
        if(Settings.showCrashReportingPermissionNudge(this)) {

            val lastCrash = Gson().fromJson(Settings.getLastCrash(this@BrowserActivity), SentryEvent::class.java)

            // launch Sentry activation dialog
            val dialogView = View.inflate(this, R.layout.crash_reporting_nudge_dialog, null)
            val radio0 = dialogView.findViewById<RadioButton>(R.id.radio0)
            val radio1 = dialogView.findViewById<RadioButton>(R.id.radio1)
            val radio2 = dialogView.findViewById<RadioButton>(R.id.radio2)

            val sentryActionDialog by lazy { AlertDialog.Builder(this).apply {
                setPositiveButton(getString(R.string.onboarding_warning_button)) { _, _ -> }
            } }

            AlertDialog.Builder(this@BrowserActivity).apply {
                setView(dialogView)
                setPositiveButton(getString(R.string.onboarding_battery_button)) { _, _ ->
                    when {
                        radio0.isChecked -> {
                            Sentry.captureEvent(lastCrash)

                            Settings.alwaysAllowCrashReporting(this@BrowserActivity)
                            SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))

                            sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_in)).show()
                        }
                        radio1.isChecked -> {
                            Sentry.captureEvent(lastCrash)

                            Settings.allowCrashReportingJustOnce(this@BrowserActivity)
                            SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))
                            Toast.makeText(this@BrowserActivity, getString(R.string.crash_report_sent), Toast.LENGTH_SHORT).show()
                        }
                        radio2.isChecked -> {
                            Settings.neverAllowCrashReporting(this@BrowserActivity)

                            SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))
                            sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_out)).show()
                        }
                    }
                }
                setOnDismissListener {
                    Settings.setLastCrash(this@BrowserActivity, "") // reset the value of lastCrash
                    SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))
                }
                setNegativeButton(getString(R.string.mozac_feature_prompt_not_now)) { _, _ ->
                    Settings.setLastCrash(this@BrowserActivity, "") // reset the value of lastCrash
                    SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))
                }
                create()
            }.show()
        }
        /*
        else {
            Settings.setLastCrash(this@BrowserActivity, "") // reset the value of lastCrash
            SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))
        }
        */
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            /* CENO: in Android 9 or later, it is possible that the
             * service may have stopped while app was in background
             * try sending an intent to restart the service
             */
            Logger.info(" --------- Starting ouinet service onResume")
            components.ouinet.background.start()
        }
        isActivityResumed = true
        //If we have some fragment to show do it now then clear the queue
        if(lastCall != null){
            updateView(lastCall!!)
            lastCall = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        /* If coming from settings fragment, always clear back stack and go back to root fragment */
        if (navHost.navController.currentDestination?.id == R.id.settingsFragment) {
            if (components.core.store.state.selectedTabId == "" ||
                components.core.store.state.selectedTabId == null
            ) {
                navHost.navController.popBackStack(R.id.homeFragment, true)
                navHost.navController.navigate(R.id.action_global_home)
            }
            else {
                navHost.navController.navigate(R.id.action_global_browser)
            }
            return
        }
        if (navHost.navController.currentDestination?.id == R.id.aboutFragment) {
            navHost.navController.navigate(R.id.action_global_settings)
            return
        }

        val fragment: Fragment? = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if ((fragment is UserInteractionHandler) && fragment.onBackPressed()) {
            return
        }

        super.onBackPressed()

        removeSessionIfNeeded()
    }

    @Suppress("DEPRECATION") // ComponentActivity wants us to use registerForActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.info(
            "Activity onActivityResult received with " +
                "requestCode: $requestCode, resultCode: $resultCode, data: $data"
        )

        val fragment = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (fragment is ActivityResultHandler && fragment.onActivityResult(requestCode, data, resultCode)) {
            return
        }

        if (requestCode == PermissionHandler.PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS) {
            if (components.permissionHandler.onActivityResult(requestCode, data, resultCode)) {
                navHost.navController.onboardingToHome()
            } else {
                updateView {
                    navHost.navController.navigate(R.id.action_onboardingBatteryFragment_to_onboardingWarningFragment)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /* CENO: Handle intent sent to BrowserActivity to open to Homepage or open a homescreen shortcut link */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val safeIntent = intent?.let { SafeIntent(it) }
        if(safeIntent?.action == Intent.ACTION_MAIN &&
            safeIntent.hasExtra(OuinetNotification.FROM_NOTIFICATION_EXTRA)
        ){
            navHost.navController.navigate(R.id.action_global_home)
        }
        if(safeIntent?.action == Intent.ACTION_VIEW) {
            navHost.navController.navigate(R.id.action_global_browser)
        }
    }

    /**
     * If needed remove the current session.
     *
     * If a session is a custom tab or was opened from an external app then the session gets removed once you go back
     * to the third-party app.
     *
     * Eventually we may want to move this functionality into one of our feature components.
     */
    private fun removeSessionIfNeeded(): Boolean {
        val session = tab ?: return false

        return if (session.source is SessionState.Source.External && !session.restored) {
            finish()
            components.useCases.tabsUseCases.removeTab(session.id)
            true
        } else {
            val hasParentSession = session is TabSessionState && session.parentId != null
            if (hasParentSession) {
                components.useCases.tabsUseCases.removeTab(session.id, selectParentIfExists = true)
            }
            // We want to return to home if this session didn't have a parent session to select.
            val goToOverview = !hasParentSession
            !goToOverview
        }
    }

    override fun onUserLeaveHint() {
        val fragment: Fragment? = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (fragment is UserInteractionHandler && fragment.onHomePressed()) {
            return
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            else -> super.onCreateView(parent, name, context, attrs)
        }

    private fun onNonFatalCrash(crash: Crash) {
        Snackbar.make(findViewById(android.R.id.content),
            R.string.crash_report_non_fatal_message, LENGTH_LONG)
            .setAction(R.string.crash_report_non_fatal_action) {
                crashIntegration.sendCrashReport(crash)
            }.show()
    }

    private fun openPopup(webExtensionState: WebExtensionState) {
        if (webExtensionState.id == CENO_EXTENSION_ID && navHost.navController.currentDestination?.id == R.id.browserFragment) {
            val fragment = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment) as BaseBrowserFragment?
            fragment?.showWebExtensionPopupPanel(webExtensionState.id)
        }
        else {
            val intent = Intent(this, WebExtensionActionPopupActivity::class.java)
            intent.putExtra("web_extension_id", webExtensionState.id)
            intent.putExtra("web_extension_name", webExtensionState.name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    /* CENO: Add function to open requested site in BrowserFragment */
    fun openToBrowser(url : String? = null, newTab : Boolean = false, private: Boolean = false){
        if (url != null) {
            if (newTab) {
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

        if(navHost.navController.currentDestination?.id == R.id.browserFragment) {
            return
        }

        navHost.navController.navigate(R.id.action_global_browser)
    }

    fun updateView(action: () -> Unit){
        //If the activity is in background we register the transaction
        if(!isActivityResumed){
            lastCall = action
        } else {
            //Else we just invoke it
            action.invoke()
        }
    }

    private fun shutdownCallback(doClear: Boolean) : Runnable {
        return Runnable {
            if (doClear) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.clearApplicationUserData()
            }
            exitOuinetServiceProcess()
            exitProcess(0)
        }
    }

    fun beginShutdown(doClear : Boolean) {
        val handler = Handler(Looper.myLooper()!!)
        val callback = shutdownCallback(doClear)
        handler.postDelayed(
            callback,
            resources.getInteger(R.integer.shutdown_fragment_stalled_duration).toLong()
        )
        components.ouinet.background.shutdown(doClear) {
            handler.removeCallbacks(callback)
            callback.run()
        }
        updateView {
            navHost.navController.navigate(R.id.action_global_shutDown, bundleOf(
                "do_clear" to doClear
            ))
        }
    }

    fun exitOuinetServiceProcess() {
        getSystemService(Context.ACTIVITY_SERVICE).let { am ->
            (am as ActivityManager).runningAppProcesses?.let { processes ->
                for (process in processes) {
                    if (process.processName.contains("ouinetService")){
                        Process.killProcess(process.pid)
                    }
                }
            }
        }
    }

    /* CENO: Function to initialize top site storage and observer */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeTopSites() {
        /*  Launch a coroutine to initialize top site storage cache and update it in the store */
        GlobalScope.launch(Dispatchers.IO) {
            components.core.cenoTopSitesStorage.getTopSites(
                totalSites = components.cenoPreferences.topSitesMaxLimit
            )
            components.appStore.dispatch(
                AppAction.Change(
                    topSites = components.core.cenoTopSitesStorage.cachedTopSites.sort(),
                    showCenoModeItem = components.cenoPreferences.showCenoModeItem,
                    showThanksCard = components.cenoPreferences.showThanksCard
                )
            )
        }

        /* Register TopSitesStorageObserver, which will update AppStore when top sites are changed/added/removed */
        components.core.cenoTopSitesStorage.apply{
            register(
                observer = TopSitesStorageObserver(
                    this,
                    components.cenoPreferences,
                    components.appStore)
            )
        }
    }

    private fun initializeSearchEngines() {
        if (Settings.shouldUpdateSearchEngines(this)) {
            components.core.store.state.search.searchEngines.filter { searchEngine ->
                searchEngine.id in listOf(
                        getString(R.string.remove_search_engine_id_1),
                        getString(R.string.remove_search_engine_id_2))
            }.forEach { searchEngine ->
                components.useCases.searchUseCases.removeSearchEngine(searchEngine)
            }
            components.core.store.state.search.searchEngines.forEach { searchEngine ->
                if (searchEngine.id == getString(R.string.default_search_engine_id)) {
                    components.useCases.searchUseCases.selectSearchEngine(searchEngine)
                }
            }
            Logger.debug("${components.core.store.state.search.searchEngines}")
            Logger.debug("${components.core.store.state.search.selectedOrDefaultSearchEngine}")
            Settings.setUpdateSearchEngines(this, false)
        }
    }
}
