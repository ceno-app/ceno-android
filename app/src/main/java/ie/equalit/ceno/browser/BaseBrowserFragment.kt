/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.browser

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.findinpage.view.FindInPageView
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_APP_PERMISSIONS
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_PROMPT_PERMISSIONS
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.components.ceno.ClearButtonFeature
import ie.equalit.ceno.components.ceno.ClearToolbarAction
import ie.equalit.ceno.databinding.FragmentBrowserBinding
import ie.equalit.ceno.downloads.DownloadService
import ie.equalit.ceno.ext.*
import ie.equalit.ceno.pip.PictureInPictureIntegration
import ie.equalit.ceno.addons.WebExtensionActionPopupPanel
import ie.equalit.ceno.search.AwesomeBarWrapper
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tabs.TabsTrayFragment
import mozilla.components.browser.state.action.WebExtensionAction
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.syncedtabs.SyncedTabsStorageSuggestionProvider
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.lib.state.ext.consumeFrom
import java.lang.Exception

/**
 * Base fragment extended by [BrowserFragment] and [ExternalAppBrowserFragment].
 * This class only contains shared code focused on the main browsing content.
 * UI code specific to the app or to custom tabs can be found in the subclasses.
 */
@Suppress("TooManyFunctions")
abstract class BaseBrowserFragment : Fragment(), UserInteractionHandler, ActivityResultHandler {
    var _binding: FragmentBrowserBinding? = null
    val binding get() = _binding!!

    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val contextMenuIntegration = ViewBoundFeatureWrapper<ContextMenuIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val shareDownloadsFeature = ViewBoundFeatureWrapper<ShareDownloadFeature>()
    private val appLinksFeature = ViewBoundFeatureWrapper<AppLinksFeature>()
    private val promptsFeature = ViewBoundFeatureWrapper<PromptFeature>()
    private val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    private val findInPageIntegration = ViewBoundFeatureWrapper<FindInPageIntegration>()
    private val sitePermissionFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    private val pictureInPictureIntegration = ViewBoundFeatureWrapper<PictureInPictureIntegration>()
    private val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    private val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()
    private val webAuthnFeature = ViewBoundFeatureWrapper<WebAuthnFeature>()
    private var engineSession: EngineSession? = null
    private var webExtensionActionPopupPanel: WebExtensionActionPopupPanel? = null


    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature,
    )

    private val activityResultHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        webAuthnFeature,
        promptsFeature,
    )

    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()

    private val awesomeBar: AwesomeBarWrapper
        get() = requireView().findViewById(R.id.awesomeBar)
    private val toolbar: BrowserToolbar
        get() = requireView().findViewById(R.id.toolbar)
    private val engineView: EngineView
        get() = requireView().findViewById<View>(R.id.engineView) as EngineView

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    protected var webAppToolbarShouldBeVisible = true

    /* CENO: do not make onCreateView "final", needs to be overridden by CenoHomeFragment */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.blank_background)
        (activity as AppCompatActivity).supportActionBar!!.hide()
        return binding.root
    }

    /* CENO: not using Jetpack ComposeUI anywhere yet
    *  option was removed from Settings, will be added back if needed */
    //abstract val shouldUseComposeUI: Boolean

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        /* If all tabs were removed (e.g. browsing data was cleared),
         * auto-create new home tab
         */
        if (requireComponents.core.store.state.tabs.isEmpty()) {
            requireComponents.useCases.tabsUseCases.addTab(
                url=CenoHomeFragment.ABOUT_HOME,
                selectTab = true
            )
        }

        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases.goBack,
                binding.engineView,
                sessionId
            ),
            owner = this,
            view = view,
        )

        /* CENO: Add onTabUrlChanged listener to toolbar, to handle fragment transactions */
        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                requireActivity(),
                binding.toolbar,
                requireComponents.core.historyStorage,
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.webAppUseCases,
                sessionId,
                ::onTabUrlChanged
            ),
            owner = this,
            view = view,
        )

        contextMenuIntegration.set(
            feature = ContextMenuIntegration(
                requireContext(),
                parentFragmentManager,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.contextMenuUseCases,
                binding.engineView,
                view,
                sessionId,
            ),
            owner = this,
            view = view,
        )

        shareDownloadsFeature.set(
            ShareDownloadFeature(
                context = requireContext().applicationContext,
                httpClient = requireComponents.core.client,
                store = requireComponents.core.store,
                tabId = sessionId,
            ),
            owner = this,
            view = view,
        )

        downloadsFeature.set(
            feature = DownloadsFeature(
                requireContext(),
                store = requireComponents.core.store,
                useCases = requireComponents.useCases.downloadsUseCases,
                fragmentManager = childFragmentManager,
                downloadManager = FetchDownloadManager(
                    requireContext().applicationContext,
                    requireComponents.core.store,
                    DownloadService::class,
                ),
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                },
            ),
            owner = this,
            view = view,
        )

        appLinksFeature.set(
            feature = AppLinksFeature(
                requireContext(),
                store = requireComponents.core.store,
                sessionId = sessionId,
                fragmentManager = parentFragmentManager,
                launchInApp = {
                    prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_launch_external_app), false)
                },
            ),
            owner = this,
            view = view,
        )

        promptsFeature.set(
            feature = PromptFeature(
                fragment = this,
                store = requireComponents.core.store,
                customTabId = sessionId,
                fragmentManager = parentFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                },
            ),
            owner = this,
            view = view,
        )

        windowFeature.set(
            feature = WindowFeature(requireComponents.core.store, requireComponents.useCases.tabsUseCases),
            owner = this,
            view = view,
        )

        fullScreenFeature.set(
            feature = FullScreenFeature(
                store = requireComponents.core.store,
                sessionUseCases = requireComponents.useCases.sessionUseCases,
                tabId = sessionId,
                viewportFitChanged = ::viewportFitChanged,
                fullScreenChanged = ::fullScreenChanged,
            ),
            owner = this,
            view = view,
        )

        findInPageIntegration.set(
            feature = FindInPageIntegration(
                requireComponents.core.store,
                sessionId,
                binding.findInPageBar as FindInPageView,
                binding.engineView
            ),
            owner = this,
            view = view,
        )

        sitePermissionFeature.set(
            feature = SitePermissionsFeature(
                context = requireContext(),
                fragmentManager = parentFragmentManager,
                sessionId = sessionId,
                storage = requireComponents.core.geckoSitePermissionsStorage,
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_APP_PERMISSIONS)
                },
                onShouldShowRequestPermissionRationale = { shouldShowRequestPermissionRationale(it) },
                store = requireComponents.core.store,
            ),
            owner = this,
            view = view,
        )

        pictureInPictureIntegration.set(
            feature = PictureInPictureIntegration(
                requireComponents.core.store,
                requireActivity(),
                sessionId,
            ),
            owner = this,
            view = view,
        )

        swipeRefreshFeature.set(
            feature = SwipeRefreshFeature(
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases.reload,
                binding.swipeRefresh,
            ),
            owner = this,
            view = view,
        )

        if (BuildConfig.MOZILLA_OFFICIAL) {
            webAuthnFeature.set(
                feature = WebAuthnFeature(
                    requireComponents.core.engine,
                    requireActivity(),
                ),
                owner = this,
                view = view,
            )
        }

        /* CENO: Add purge button to toolbar */
        if (prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_clear_in_toolbar), true)) {
            val clearButtonFeature = ClearButtonFeature(
                requireContext(),
                prefs.getString(
                    requireContext().getPreferenceKey(R.string.pref_key_clear_behavior), "0")!!
                    .toInt()
            )
            binding.toolbar.addBrowserAction(
                ClearToolbarAction(
                    listener = {
                        clearButtonFeature.onClick()
                    }
                )
            )
        }

        if (prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_toolbar_hide), false)) {
            binding.toolbar.enableDynamicBehavior(
                requireContext(),
                binding.swipeRefresh,
                binding.engineView,
                prefs.getBoolean(
                    requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                    false
                )
            )
        }
        else {
            binding.toolbar.disableDynamicBehavior(
                binding.engineView,
                prefs.getBoolean(
                    requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                    false
                )
            )
        }


        AwesomeBarFeature(awesomeBar, toolbar, engineView).let {
            if (Settings.shouldShowSearchSuggestions(requireContext())) {
                it.addSearchProvider(
                    requireContext(),
                    requireComponents.core.store,
                    requireComponents.useCases.searchUseCases.defaultSearch,
                    fetchClient = requireComponents.core.client,
                    mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                    engine = requireComponents.core.engine,
                    limit = 5,
                    filterExactMatch = true
                )
            }
            it.addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab
            )
            it.addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl
            )
            it.addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)
        }

        // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
        // a dependency on feature-syncedtabs (which depends on Sync).
        awesomeBar.addProviders(
            SyncedTabsStorageSuggestionProvider(
                requireComponents.backgroundServices.syncedTabsStorage,
                requireComponents.useCases.tabsUseCases.addTab,
                requireComponents.core.icons
            )
        )

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            store = requireComponents.core.store,
            showTabs = ::showTabs,
            lifecycleOwner = this
        )

        thumbnailsFeature.set(
            feature = BrowserThumbnails(
                requireContext(),
                engineView,
                requireComponents.core.store
            ),
            owner = this,
            view = view
        )


        /* CENO: not using Jetpack ComposeUI anywhere yet */
        /*
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        if (shouldUseComposeUI) {
            composeView.visibility = View.VISIBLE
            composeView.setContent { BrowserToolbar() }

            val params = swipeRefresh.layoutParams as CoordinatorLayout.LayoutParams
            params.topMargin = resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)
            swipeRefresh.layoutParams = params
        }
        */
    }

    override fun onStart() {
        super.onStart()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        /* CENO: Set toolbar appearance based on whether current tab is private or not
         * Doing this in onStart so it does not depend onViewCreated, which isn't run on returning to activity
         */
        requireComponents.core.store.state.selectedTab?.content?.private?.let{ private ->
            binding.toolbar.private = private

            /* TODO: this is still a little messy, should create ThemeManager class */
            var textPrimary = ContextCompat.getColor(requireContext(), R.color.fx_mobile_text_color_primary)
            var textSecondary = ContextCompat.getColor(requireContext(), R.color.fx_mobile_text_color_secondary)
            var urlBackground = ContextCompat.getDrawable(requireContext(), R.drawable.url_background)
            var toolbarBackground = ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_dark_background)
            var statusIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_status)!!

            if (private) {
                textPrimary = ContextCompat.getColor(requireContext(), R.color.fx_mobile_private_text_color_primary)
                textSecondary = ContextCompat.getColor(requireContext(), R.color.fx_mobile_private_text_color_secondary)
                urlBackground = ContextCompat.getDrawable(requireContext(), R.drawable.url_private_background)
                toolbarBackground = ContextCompat.getDrawable(requireContext(), R.drawable.toolbar_background)
                statusIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_status_white)!!
            }

            binding.toolbar.display.setUrlBackground(urlBackground)
            binding.toolbar.background = toolbarBackground
            binding.toolbar.edit.colors = binding.toolbar.edit.colors.copy(
                    text = textPrimary,
                    hint = textSecondary
            )
            binding.toolbar.display.colors = binding.toolbar.display.colors.copy(
                    text = textPrimary,
                    hint = textSecondary,
                    securityIconSecure = textPrimary,
                    securityIconInsecure = textPrimary,
                    menu = textPrimary
            )

            /* CENO: this is replaces the shield icon in the address bar
             * with the ceno logo, regardless of tracking protection state
             */
            binding.toolbar.display.icons = DisplayToolbar.Icons(
                emptyIcon = null,
                trackingProtectionTrackersBlocked = statusIcon,
                trackingProtectionNothingBlocked = statusIcon,
                trackingProtectionException = statusIcon,
                highlight = ContextCompat.getDrawable(requireContext(), R.drawable.mozac_dot_notification)!!,
            )
            val isToolbarPositionTop = prefs.getBoolean(
                requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                false
            )
            binding.toolbar.display.progressGravity = if(isToolbarPositionTop) {
                DisplayToolbar.Gravity.BOTTOM
            }
            else {
                DisplayToolbar.Gravity.TOP
            }
        }
    }

    /* CENO: Functions to handle hiding/showing the CenoHomeFragment when "about:home" url is requested */
    private fun showHome() {
        activity?.supportFragmentManager?.findFragmentByTag(CenoHomeFragment.TAG)?.let {
            if (it.isVisible) {
                /* CENO: BrowserFragment is already being displayed, don't do another transaction */
                return
            }
        }

        try {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.container, CenoHomeFragment.create(sessionId), CenoHomeFragment.TAG)
                commit()
            }
        } catch (ex : Exception) {
            /* Workaround for opening shortcut from homescreen, try again allowing for state loss */
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.container, CenoHomeFragment.create(sessionId), CenoHomeFragment.TAG)
                commitAllowingStateLoss()
            }
        }
    }

    private fun showBrowser() {
        activity?.supportFragmentManager?.findFragmentByTag(BrowserFragment.TAG)?.let {
            if (it.isVisible) {
                /* CENO: HomeFragment is already being displayed, don't do another transaction */
                return
            }
        }
        try {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.container, BrowserFragment.create(sessionId), BrowserFragment.TAG)
                commit()
            }
        }
        catch (ex: Exception){
            /* Workaround for opening shortcut from homescreen, try again allowing for state loss */
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.container, BrowserFragment.create(sessionId), BrowserFragment.TAG)
                commitAllowingStateLoss()
            }
        }
        /* TODO: Allowing for state loss probably isn't best solution, should figure out how to avoid exception */
    }

    fun showWebExtensionPopupPanel(webExtId : String) {
        val session = requireContext().components.core.store.state.extensions[webExtId]?.popupSession
        val tab = requireContext().components.core.store.state.selectedTab!!

        webExtensionActionPopupPanel = WebExtensionActionPopupPanel(
                context = requireContext(),
                lifecycleOwner = this,
                tabUrl = tab.content.url,
                isConnectionSecure = tab.content.securityInfo.secure,
        ).also { currentEtp -> currentEtp.show() }

        if (session != null) {
            webExtensionActionPopupPanel?.renderSettingsView(session)
            consumePopupSession(webExtId)
        } else {
            consumeFrom(requireContext().components.core.store) { state ->
                state.extensions[webExtId]?.let { extState ->
                    extState.popupSession?.let {
                        if (engineSession == null) {
                            webExtensionActionPopupPanel?.renderSettingsView(it)
                            consumePopupSession(webExtId)
                            engineSession = it
                        }
                    }
                }
            }
        }
    }


    private fun consumePopupSession(webExtId: String) {
        requireContext().components.core.store.dispatch(
                WebExtensionAction.UpdatePopupSessionAction(webExtId, popupSession = null)
        )
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        /* CENO: Add this transaction to back stack to go back to correct fragment on back pressed */
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment(), TabsTrayFragment.TAG)
            commit()
        }
    }

    private fun onTabUrlChanged(url : String) {
        activity?.supportFragmentManager?.findFragmentByTag(TabsTrayFragment.TAG)?.let {
            if (it.isVisible) {
                /* CENO: TabsTrayFragment is open, don't switch to home or browser,
                *  TabsTray will handle fragment transactions on it's own */
                return
            }
        }
        activity?.supportFragmentManager?.findFragmentByTag(ShutdownFragment.TAG)?.let {
            if (it.isVisible) {
                /* CENO: TabsTrayFragment is open, don't switch to home or browser,
                *  TabsTray will handle fragment transactions on it's own */
                return
            }
        }
        if(url == CenoHomeFragment.ABOUT_HOME) {
            showHome()
        }
        else {
            showBrowser()
        }
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled) {
            activity?.enterToImmersiveMode()
            binding.toolbar.visibility = View.GONE
            binding.engineView.setDynamicToolbarMaxHeight(0)
        } else {
            activity?.exitImmersiveMode()
            binding.toolbar.visibility = View.VISIBLE
            binding.engineView.setDynamicToolbarMaxHeight(resources.getDimensionPixelSize(R.dimen.browser_toolbar_height))
        }
    }

    private fun viewportFitChanged(viewportFit: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requireActivity().window.attributes.layoutInDisplayCutoutMode = viewportFit
        }
    }

    @CallSuper
    override fun onBackPressed(): Boolean {
        return backButtonHandler.any { it.onBackPressed() }
    }

    final override fun onHomePressed(): Boolean {
        return pictureInPictureIntegration.get()?.onHomePressed() ?: false
    }

    final override fun onPictureInPictureModeChanged(enabled: Boolean) {
        val session = requireComponents.core.store.state.selectedTab
        val fullScreenMode = session?.content?.fullScreen ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    final override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        val feature: PermissionsFeature? = when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.get()
            REQUEST_CODE_APP_PERMISSIONS -> sitePermissionFeature.get()
            else -> null
        }
        feature?.onPermissionsResult(permissions, grantResults)
    }

    companion object {
        private const val SESSION_ID = "session_id"

        @JvmStatic
        protected fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Logger.info(
            "Fragment onActivityResult received with " +
                "requestCode: $requestCode, resultCode: $resultCode, data: $data",
        )

        return activityResultHandler.any { it.onActivityResult(requestCode, data, resultCode) }
    }
}
