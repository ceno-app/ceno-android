package ie.equalit.ceno.tabs

import android.view.LayoutInflater
import ie.equalit.ceno.R
import mozilla.components.browser.state.state.TabPartition
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsAdapter
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.ViewHolderProvider
import mozilla.components.concept.base.images.ImageLoader

class CenoTabsAdapter(
    thumbnailLoader: ImageLoader? = null,
    viewHolderProvider: ViewHolderProvider = { parent ->
        DefaultTabViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.mozac_browser_tabstray_item, parent, false),
          thumbnailLoader,
            )
    },
    styling: TabsTrayStyling = TabsTrayStyling(),
    delegate: TabsTray.Delegate,
    val onUpdateList: () -> Unit
): TabsAdapter(thumbnailLoader, viewHolderProvider, styling, delegate) {

    override fun updateTabs(
        tabs: List<TabSessionState>,
        tabPartition: TabPartition?,
        selectedTabId: String?
    ) {
        super.updateTabs(tabs, tabPartition, selectedTabId)
        onUpdateList.invoke()
    }
}