package ie.equalit.ceno.bookmarks

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.ceno.loadIntoView
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType

class BookmarkNodeViewHolder(
    private val view: WebsiteListItemView,
    private val interactor: BookmarkViewInteractor
) : RecyclerView.ViewHolder(view) {
    var item: BookmarkNode? = null
    val hiddenTelegramChannelsPref = view.context.cenoPreferences().hiddenTelegramChannelsGuid

    fun bind(
        item: BookmarkNode,
        mode: BookmarkFragmentState.Mode,
        payload: BookmarkPayload,
    ) {
        this.item = item

        val isTelegramChannel = payload.modeChanged &&
                item.parentGuid == view.context.components.cenoPreferences.telegramChannelsBookGuid

        val menu = BookmarkItemMenu(
            view.context,
            isTelegramChannel = isTelegramChannel,
            isTelegramChannelHidden = hiddenTelegramChannelsPref.contains(item.guid)
        )

        menu.onItemTapped = { menuItem ->
            when (menuItem) {
                BookmarkItemMenu.Item.Edit -> interactor.onEditPressed(item)
                BookmarkItemMenu.Item.Copy -> interactor.onCopyPressed(item)
                BookmarkItemMenu.Item.Share -> interactor.onSharePressed(item)
                BookmarkItemMenu.Item.OpenInNewTab -> interactor.onOpenInNormalTab(item)
                BookmarkItemMenu.Item.OpenInPersonalTab -> interactor.onOpenInPersonalTab(item)
                BookmarkItemMenu.Item.Delete -> interactor.onDelete(setOf(item))
                BookmarkItemMenu.Item.Hide -> interactor.onHide(setOf(item))
                BookmarkItemMenu.Item.UnHide -> interactor.onUnHide(item)
            }
        }
        view.attachMenu(menu.menuController)
        view.urlView.isVisible = item.type == BookmarkNodeType.ITEM
        view.setSelectionInteractor(item, interactor)

        CoroutineScope(Dispatchers.Default).launch {
            menu.updateMenu(item.type)
        }

        CoroutineScope(Dispatchers.Default).launch {
            menu.updateMenu(item.type)
        }

        // Hide menu button if this item is a root folder or is selected
        if (item.type == BookmarkNodeType.FOLDER && item.inRoots()) {
            view.overflowView.visibility = View.GONE
            view.overflowView.isEnabled = false
        } else if (payload.modeChanged) {
            if (mode is BookmarkFragmentState.Mode.Selecting) {
                view.overflowView.visibility = View.INVISIBLE
                view.overflowView.isEnabled = false
            } else {
                view.overflowView.visibility = View.VISIBLE
                view.overflowView.isEnabled = true
            }
        }

        val useTitleFallback = item.type == BookmarkNodeType.ITEM && item.title.isNullOrBlank()
        if (payload.titleChanged) {
            view.titleView.text = if (useTitleFallback) item.url else item.title
        } else if (payload.urlChanged && useTitleFallback) {
            view.titleView.text = item.url
        }
        //
        if (payload.urlChanged) {
            view.urlView.text = item.url
        }

        if (payload.iconChanged) {
            updateIcon(item)
        }

        if (hiddenTelegramChannelsPref.contains(item.guid)) {
            view.titleView.setTextColor(view.context.getColor(R.color.fx_mobile_text_color_disabled))
            view.urlView.setTextColor(view.context.getColor(R.color.fx_mobile_text_color_disabled))
        } else {
            view.titleView.setTextColor(view.context.getColor(R.color.fx_mobile_text_color_primary))
            view.urlView.setTextColor(view.context.getColor(R.color.fx_mobile_text_color_primary))
        }
    }

    private fun updateIcon(item: BookmarkNode) {
        val context = view.context
        val iconView = view.iconView
        val url = item.url

        when {
            // Item is a folder
            item.type == BookmarkNodeType.FOLDER ->
                iconView.setImageResource(R.drawable.ic_folder_icon)
            // Item has a http/https URL
            url != null && url.startsWith("http") ->
                context.components.core.icons.loadIntoView(iconView, url)

            else ->
                iconView.setImageDrawable(null)
        }
    }
}
