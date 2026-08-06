package ie.equalit.ceno.home.telegramchannels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ie.equalit.ceno.R
import ie.equalit.ceno.home.sessioncontrol.AdapterItem.TelegramChannelPagerPayload
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import ie.equalit.ceno.home.topsites.TopSitePagerViewHolder.Companion.TOP_SITES_PER_PAGE
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelsPagerAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: TopSiteInteractor
) : ListAdapter<List<TopSite>, TelegramChannelViewHolder>(TelegramChannelDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelegramChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_telegram_channels, parent, false)
        return TelegramChannelViewHolder(view, viewLifecycleOwner, interactor)
    }

    override fun onBindViewHolder(
        holder: TelegramChannelViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (payloads[0] is TelegramChannelPagerPayload) {
                val adapter = holder.binding.telegramChannelList.adapter as TelegramChannelsAdapter
                val payload = payloads[0] as TelegramChannelPagerPayload

                update(payload, position, adapter)
            }
        }
    }

    @VisibleForTesting
    internal fun update(
        payload: TelegramChannelPagerPayload,
        position: Int,
        adapter: TelegramChannelsAdapter
    ) {
        // Only currently selected page items need to be updated
        val currentPageChangedItems = getCurrentPageChanges(payload, position)

        // If no changes have been made to the current page no need to continue
        if (currentPageChangedItems.isEmpty()) return

        // Build the new list from the old one
        val refreshedItems: MutableList<TopSite> = mutableListOf()
        refreshedItems.addAll(adapter.currentList)

        // Update new list with the changed items
        currentPageChangedItems.forEach { item ->
            val index = item.first - (position * TOP_SITES_PER_PAGE)
            if (index in refreshedItems.indices) {
                refreshedItems[index] = item.second
            }
        }

        // Display the updated list without any of the removed items
        adapter.submitList(refreshedItems.filter { it.id != -1L })
    }

    /**
     * @returns the changed only items for the currently specified page in [position]
     */
    @VisibleForTesting
    internal fun getCurrentPageChanges(payload: TelegramChannelPagerPayload, position: Int) =
        payload.changed.filter { changedPair ->
            if (position == 0) {
                changedPair.first < TOP_SITES_PER_PAGE
            } else {
                changedPair.first >= TOP_SITES_PER_PAGE
            }
        }

    override fun onBindViewHolder(holder: TelegramChannelViewHolder, position: Int) {
        val adapter = holder.binding.telegramChannelList.adapter as TelegramChannelsAdapter
        adapter.submitList(getItem(position))
    }

    internal object TelegramChannelDiffCallback : DiffUtil.ItemCallback<List<TopSite>>() {
        override fun areItemsTheSame(oldItem: List<TopSite>, newItem: List<TopSite>): Boolean {
            return oldItem.size == newItem.size
        }

        override fun areContentsTheSame(oldItem: List<TopSite>, newItem: List<TopSite>): Boolean {
            return newItem.zip(oldItem)
                .all { (new, old) ->
                    (new.id == old.id) && (new.url == old.url) && (new.title == old.title) && (new.type == old.type)
                }
        }

        override fun getChangePayload(oldItem: List<TopSite>, newItem: List<TopSite>): Any? {
            val changed = mutableSetOf<Pair<Int, TopSite>>()
            for ((index, item) in newItem.withIndex()) {
                if (oldItem.getOrNull(index) != item) {
                    changed.add(Pair(index, item))
                }
            }
            return if (changed.isNotEmpty()) TelegramChannelPagerPayload(changed) else null
        }
    }
}
