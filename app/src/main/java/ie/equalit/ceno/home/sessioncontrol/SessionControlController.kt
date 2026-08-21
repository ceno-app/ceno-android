/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.sessioncontrol

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.ceno.AppStore
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.announcements.RSSAnnouncementViewHolder
import ie.equalit.ceno.home.ouicrawl.OuicrawlSite
import ie.equalit.ceno.home.telegramchannels.TelegramChannelsViewModel
import ie.equalit.ceno.home.topsites.TopSiteViewModel
import ie.equalit.ceno.utils.CenoPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.support.ktx.android.view.showKeyboard

/**
 * [SessionControlView] controller. An interface that handles the view manipulation of the Tabs triggered
 * by the Interactor.
 */
@Suppress("TooManyFunctions")
interface SessionControlController {
    /**
     * @see [TopSiteInteractor.onRenameTopSiteClicked]
     */
    fun handleRenameTopSiteClicked(topSite: TopSite)

    /**
     * @see [TopSiteInteractor.onRemoveTopSiteClicked]
     */
    fun handleRemoveTopSiteClicked(topSite: TopSite)

    /**
     * @see [TopSiteInteractor.onSelectTopSite]
     */
    fun handleSelectTopSite(topSite: TopSite, position: Int)

    /**
     * @see [TopSiteInteractor.onOpenInPrivateTabClicked]
     */
    fun handleOpenInPrivateTabClicked(url: String)

    /**
     * @see [SessionControlInteractor.onOuicrawlSiteMenuOpened] and [TopSiteInteractor.onTopSiteMenuOpened]
     */
    fun handleMenuOpened()

    fun handleCardClicked(homepageCardType: HomepageCardType, mode: BrowsingMode)

    fun handleMenuItemClicked(homepageCardType: HomepageCardType)

    fun handleRemoveCard(homepageCardType: HomepageCardType)

    fun handleRemoveAnnouncementCard(index: Int)

    fun handleUrlClicked(homepageCardType: HomepageCardType, url: String)

    fun handleAddToShortcuts(ouicrawlSite: OuicrawlSite, isTopSite: Boolean)

    fun handleOnSectionHeaderClicked(listIsHidden: Boolean)
    fun handleRemoveTelegramChannel(topSite: TopSite)
    fun handleRenameTelegramChannel(topSite: TopSite)
}

@Suppress("TooManyFunctions", "LargeClass", "LongParameterList")
class DefaultSessionControlController(
    private val activity: BrowserActivity,
    private val preferences: CenoPreferences,
    private val appStore: AppStore,
    private val viewLifecycleScope: CoroutineScope,
    private val topSiteViewModel: TopSiteViewModel,
    private val telegramChanViewModel: TelegramChannelsViewModel,
    private val rssAnnouncementSwipeListener: RSSAnnouncementViewHolder.RssAnnouncementSwipeListener?,
) : SessionControlController {

    override fun handleMenuOpened() {
        //dismissSearchDialogIfDisplayed()
    }

    @SuppressLint("InflateParams")
    override fun handleRenameTopSiteClicked(topSite: TopSite) {
        activity.let {
            val customLayout =
                LayoutInflater.from(it)
                    .inflate(R.layout.top_sites_rename_dialog, null)
            val topSiteLabelEditText: EditText =
                customLayout.findViewById(R.id.top_site_title)
            topSiteLabelEditText.setText(topSite.title)

            AlertDialog.Builder(it)
                .apply {
                    setTitle(R.string.rename_top_site)
                    setView(customLayout)
                    setPositiveButton(R.string.dialog_ok) { dialog, _ ->
                        topSiteViewModel.renameTopSite(
                            it.applicationContext,
                            topSiteLabelEditText.text.toString(),
                            topSite.url
                        )
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                }
                .show()
                .also {
                    topSiteLabelEditText.setSelection(0, topSiteLabelEditText.text.length)
                    topSiteLabelEditText.showKeyboard()
                }
        }
    }

    override fun handleRemoveTopSiteClicked(topSite: TopSite) {
        topSiteViewModel.removeShortcut(activity.applicationContext, topSite.url)
    }

    override fun handleSelectTopSite(topSite: TopSite, position: Int) {
        activity.openToBrowser(topSite.url, newTab = true)
    }

    override fun handleOpenInPrivateTabClicked(url: String) {
        with(activity) {
            openToBrowser(
                url = url,
                newTab = true,
                private = true
            )
        }
    }

    override fun handleCardClicked(homepageCardType: HomepageCardType, mode: BrowsingMode) {
        if (homepageCardType == HomepageCardType.PERSONAL_MODE_CARD) {
            activity.apply {
                openToBrowser(
                    getString(R.string.ceno_support_link_url),
                    newTab = true,
                    private = true
                )
            }
        }
        if (homepageCardType == HomepageCardType.MODE_MESSAGE_CARD) {
            activity.switchBrowsingModeHome(mode)
        }
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            activity.apply {
                openSettings()
            }
        }
    }

    override fun handleMenuItemClicked(homepageCardType: HomepageCardType) {
        if (homepageCardType == HomepageCardType.MODE_MESSAGE_CARD) {
            activity.apply {
                browsingModeManager.mode = BrowsingMode.Personal
            }
        }
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            activity.apply {
                openToBrowser(getString(R.string.website_button_link), newTab = true)
            }
        }
    }

    override fun handleRemoveCard(homepageCardType: HomepageCardType) {
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            preferences.showBridgeAnnouncementCard = false
            appStore.dispatch(AppAction.BridgeCardChange(false))
        }
    }

    override fun handleRemoveAnnouncementCard(index: Int) {
        rssAnnouncementSwipeListener?.onSwipeCard(index)
    }

    override fun handleUrlClicked(homepageCardType: HomepageCardType, url: String) {
        activity.openToBrowser(url, newTab = true)
    }

    override fun handleAddToShortcuts(ouicrawlSite: OuicrawlSite, isTopSite: Boolean) {
        val url = "https://${ouicrawlSite.SiteURL}/"
        activity.lifecycleScope.launch {
            if(topSiteViewModel.isTopSite(activity.applicationContext, url)) {
                topSiteViewModel.removeShortcut(
                    activity.applicationContext,
                    url,
                )
            } else {
                topSiteViewModel.addToTopSites(
                    activity.applicationContext,
                    ouicrawlSite.SiteName,
                    url,
                ) {
                    AlertDialog.Builder(activity)
                        .apply {
                            setTitle(R.string.shortcut_max_limit_title)
                            setMessage(R.string.shortcut_max_limit_content)
                            setPositiveButton(R.string.top_sites_max_limit_confirmation_button) { dialog, _ ->
                                dialog.dismiss()
                            }
                            create()
                        }
                        .show()
                }
            }
        }
    }

    override fun handleOnSectionHeaderClicked(listIsHidden: Boolean) {
        appStore.dispatch(AppAction.OuicrawlSitesChange(listIsHidden))
    }

    override fun handleRemoveTelegramChannel(topSite: TopSite) {
        telegramChanViewModel.removeChannel(activity.applicationContext, topSite.url)
    }

    override fun handleRenameTelegramChannel(topSite: TopSite) {
        activity.let {
            val customLayout =
                LayoutInflater.from(it)
                    .inflate(R.layout.top_sites_rename_dialog, null)
            val topSiteLabelEditText: EditText =
                customLayout.findViewById(R.id.top_site_title)
            topSiteLabelEditText.setText(topSite.title)

            AlertDialog.Builder(it)
                .apply {
                    setTitle(R.string.rename_top_site)
                    setView(customLayout)
                    setPositiveButton(R.string.dialog_ok) { dialog, _ ->
                        telegramChanViewModel.renameTelegramChannel(
                            it.applicationContext,
                            topSiteLabelEditText.text.toString(),
                            topSite.url
                        )
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                }
                .show()
                .also {
                    topSiteLabelEditText.setSelection(0, topSiteLabelEditText.text.length)
                    topSiteLabelEditText.showKeyboard()
                }
        }
    }
}
