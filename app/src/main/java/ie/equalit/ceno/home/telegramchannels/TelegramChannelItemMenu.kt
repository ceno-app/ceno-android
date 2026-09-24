/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.telegramchannels

import android.content.Context
import ie.equalit.ceno.R
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.feature.top.sites.TopSite

/**
 * Helper class for building a context menu for a top site item.
 *
 * @property context An Android context.
 * @property topSite The [TelegramChannel] to show the context menu for.
 * @property onItemTapped Callback invoked when the user taps on a menu item.
 */
class TelegramChannelItemMenu(
    private val context: Context,
    private val topSite: TopSite,
    private val onItemTapped: (Item) -> Unit = {}
) {
    sealed class Item {
        object OpenInPrivateTab : Item()
        object RenameTelegramChannel : Item()
        object HideTelegramChannel : Item()
    }

    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    private val menuItems by lazy {
        val isPinnedSite = topSite is TopSite.Pinned || topSite is TopSite.Default
        val isProvidedSite = topSite is TopSite.Provided

        listOfNotNull(
            SimpleBrowserMenuItem(
                context.getString(R.string.bookmark_menu_open_in_private_tab_button)
            ) {
                onItemTapped.invoke(Item.OpenInPrivateTab)
            },
            if (isPinnedSite) {
                SimpleBrowserMenuItem(
                    context.getString(R.string.rename_top_site)
                ) {
                    onItemTapped.invoke(Item.RenameTelegramChannel)
                }
            } else {
                null
            },
            if (!isProvidedSite) {
                SimpleBrowserMenuItem(context.getString(R.string.hide_telegram_channel)) {
                    onItemTapped.invoke(Item.HideTelegramChannel)
                }
            } else {
                null
            },
        )
    }
}
