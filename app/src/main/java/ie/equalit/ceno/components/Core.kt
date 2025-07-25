/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.components

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import mozilla.components.browser.engine.gecko.permission.GeckoSitePermissionsStorage
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.thumbnails.ThumbnailsMiddleware
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.amo.AMOAddonsProvider
import mozilla.components.feature.addons.migration.DefaultSupportedAddonsChecker
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.feature.customtabs.store.CustomTabsServiceStore
import mozilla.components.feature.downloads.DownloadMiddleware
import mozilla.components.feature.media.MediaSessionFeature
import mozilla.components.feature.media.middleware.RecordingDevicesMiddleware
import mozilla.components.feature.prompts.file.FileUploadsDirCleaner
import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.WebAppShortcutManager
import mozilla.components.feature.readerview.ReaderViewMiddleware
import mozilla.components.feature.search.middleware.SearchMiddleware
import mozilla.components.feature.search.region.RegionMiddleware
import mozilla.components.feature.session.HistoryDelegate
import mozilla.components.feature.sitepermissions.OnDiskSitePermissionsStorage
import mozilla.components.feature.top.sites.DefaultTopSitesStorage
import mozilla.components.feature.top.sites.PinnedSiteStorage
import mozilla.components.feature.webnotifications.WebNotificationFeature
import mozilla.components.lib.dataprotect.SecureAbove22Preferences
import mozilla.components.service.location.LocationService
import mozilla.components.support.base.worker.Frequency
import ie.equalit.ceno.AppRequestInterceptor
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.EngineProvider
import ie.equalit.ceno.R
import ie.equalit.ceno.R.string.pref_key_remote_debugging
import ie.equalit.ceno.R.string.pref_key_tracking_protection_normal
import ie.equalit.ceno.R.string.pref_key_tracking_protection_private
import ie.equalit.ceno.downloads.DownloadService
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.media.MediaSessionService
import ie.equalit.ceno.share.SaveToPDFMiddleware
import mozilla.components.browser.storage.sync.PlacesBookmarksStorage
import mozilla.components.feature.downloads.DateTimeProvider
import mozilla.components.feature.downloads.DefaultDateTimeProvider
import mozilla.components.feature.downloads.DefaultFileSizeFormatter
import mozilla.components.feature.downloads.DownloadEstimator
import mozilla.components.feature.downloads.FileSizeFormatter
import java.util.concurrent.TimeUnit

private const val DAY_IN_MINUTES = 24 * 60L

/**
 * Component group for all core browser functionality.
 */
class Core(private val context: Context) {

    /**
     * The browser engine component initialized based on the build
     * configuration (see build variants).
     */
    val engine: Engine by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(context),
            remoteDebuggingEnabled = prefs.getBoolean(context.getPreferenceKey(pref_key_remote_debugging), false),
            testingModeEnabled = prefs.getBoolean(context.getPreferenceKey(R.string.pref_key_testing_mode), false),
            trackingProtectionPolicy = createTrackingProtectionPolicy(prefs),
            historyTrackingDelegate = HistoryDelegate(lazyHistoryStorage),
        )
        EngineProvider.createEngine(context, defaultSettings)
    }

    /**
     * The [Client] implementation (`concept-fetch`) used for HTTP requests.
     */
    val client: Client by lazy {
        EngineProvider.createClient(context)
    }

    /**
     * The [BrowserStore] holds the global [BrowserState].
     */
    val store by lazy {
        BrowserStore(
            middleware = listOf(
                DownloadMiddleware(context, DownloadService::class.java),
                ThumbnailsMiddleware(thumbnailStorage),
                ReaderViewMiddleware(),
                RegionMiddleware(
                    context,
                    LocationService.default(),
                ),
                SearchMiddleware(context),
                RecordingDevicesMiddleware(context, context.components.notificationsDelegate),
                SaveToPDFMiddleware(context)
            ) + EngineMiddleware.create(engine),
        ).apply {
            icons.install(engine, this)

            WebNotificationFeature(
                context,
                engine,
                icons,
                R.drawable.ic_notification,
                geckoSitePermissionsStorage,
                BrowserActivity::class.java,
                notificationsDelegate = context.components.notificationsDelegate,
            )

            MediaSessionFeature(context, MediaSessionService::class.java, this).start()
        }
    }

    /**
     * The [CustomTabsServiceStore] holds global custom tabs related data.
     */
    val customTabsStore by lazy { CustomTabsServiceStore() }

    /**
     * The storage component for persisting browser tab sessions.
     */
    val sessionStorage: SessionStorage by lazy {
        SessionStorage(context, engine)
    }

    /**
     * The storage component to persist browsing history (with the exception of
     * private sessions).
     */
    val lazyHistoryStorage = lazy { PlacesHistoryStorage(context) }

    /**
     * A convenience accessor to the [PlacesHistoryStorage].
     */
    val historyStorage by lazy { lazyHistoryStorage.value }

    val lazyBookmarksStorage = lazy { PlacesBookmarksStorage(context) }
    val bookmarksStorage by lazy { lazyBookmarksStorage.value}

    /**
     * A storage component for persisting thumbnail images of tabs.
     */
    val thumbnailStorage by lazy { ThumbnailStorage(context) }

    /**
     * Component for managing shortcuts (both regular and PWA).
     */
    /* CENO: supportWebApps was breaking AddToHomescreen for some websites, set to false */
    val shortcutManager by lazy { WebAppShortcutManager(context, client, ManifestStorage(context), false) }

    /**
     * A storage component for site permissions.
     */
    val geckoSitePermissionsStorage by lazy {
        val geckoRuntime = EngineProvider.getOrCreateRuntime(context)
        GeckoSitePermissionsStorage(geckoRuntime, OnDiskSitePermissionsStorage(context))
    }

    /**
     * Icons component for loading, caching and processing website icons.
     */
    val icons by lazy { BrowserIcons(context, client) }

    // Addons
    val addonManager by lazy {
        AddonManager(store, engine, addonProvider, addonUpdater)
    }

    val addonUpdater by lazy {
        DefaultAddonUpdater(
            context,
            Frequency(1, TimeUnit.DAYS),
            notificationsDelegate = context.components.notificationsDelegate,
        )
    }

    val addonProvider by lazy {
        provideDefaultAddonProvider()
    }

    /* CENO: add storage locations for pinned/suggested sites */
    val cenoPinnedSiteStorage by lazy { PinnedSiteStorage(context) }

    val cenoTopSitesStorage by lazy {
        val defaultTopSites = mutableListOf<Pair<String, String>>()

        if (!context.cenoPreferences().defaultTopSitesAdded) {
            defaultTopSites.add(
                Pair(
                    context.getString(R.string.default_top_site_1_title),
                    context.getString(R.string.default_top_site_1_url)
                )
            )
            defaultTopSites.add(
                Pair(
                    context.getString(R.string.default_top_site_2_title),
                    context.getString(R.string.default_top_site_2_url)
                )
            )
            defaultTopSites.add(
                Pair(
                    context.getString(R.string.default_top_site_3_title),
                    context.getString(R.string.default_top_site_3_url)
                )
            )
            defaultTopSites.add(
                Pair(
                    context.getString(R.string.default_top_site_4_title),
                    context.getString(R.string.default_top_site_4_url)
                )
            )
        context.cenoPreferences().defaultTopSitesAdded = true
        }

        DefaultTopSitesStorage(
            pinnedSitesStorage = cenoPinnedSiteStorage,
            historyStorage = historyStorage,
            topSitesProvider = null, //contileTopSitesProvider,
            defaultTopSites = defaultTopSites
        )
    }

    @Suppress("MagicNumber")
    val supportedAddonsChecker by lazy {
        DefaultSupportedAddonsChecker(
            context,
            Frequency(12, TimeUnit.HOURS),
        )
    }

    val fileUploadsDirCleaner: FileUploadsDirCleaner by lazy {
        FileUploadsDirCleaner { context.cacheDir }
    }

    private fun provideDefaultAddonProvider(): AMOAddonsProvider {
        return AMOAddonsProvider(
            context = context,
            client = client,
            collectionName = "7dfae8669acc4312a65e8ba5553036",
            maxCacheAgeInMinutes = DAY_IN_MINUTES,
        )
    }

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param prefs the shared preferences to use when reading tracking
     * protection settings.
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    fun createTrackingProtectionPolicy(
        prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        normalMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_normal), true),
        privateMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_private), true),
    ): TrackingProtectionPolicy {
        val trackingPolicy = TrackingProtectionPolicy.recommended()
        return when {
            normalMode && privateMode -> trackingPolicy
            normalMode && !privateMode -> trackingPolicy.forRegularSessionsOnly()
            !normalMode && privateMode -> trackingPolicy.forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.none()
        }
    }

    fun  setRootCertificate( rootCertificate: String) {
        EngineProvider.rootCertificate = rootCertificate
    }

    private val lazySecurePrefs = lazy { SecureAbove22Preferences(context, KEY_STORAGE_NAME) }

    val fileSizeFormatter: FileSizeFormatter by lazy { DefaultFileSizeFormatter(context.applicationContext) }

    private val dateTimeProvider: DateTimeProvider by lazy { DefaultDateTimeProvider() }
    val downloadEstimator: DownloadEstimator by lazy { DownloadEstimator(dateTimeProvider = dateTimeProvider) }

    companion object {
        private const val KEY_STORAGE_NAME = "core_prefs"
    }
}
