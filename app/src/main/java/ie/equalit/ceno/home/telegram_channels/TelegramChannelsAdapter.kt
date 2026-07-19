package ie.equalit.ceno.home.telegram_channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ie.equalit.ceno.R
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import ie.equalit.ceno.home.topsites.TopSiteItemViewHolder
import ie.equalit.ceno.home.topsites.TopSitesAdapter
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelsAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: TopSiteInteractor
) : ListAdapter<TopSite, TelegramItemViewHolder>(TopSitesDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelegramItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.telegram_channel_site_item, parent, false)
        return TelegramItemViewHolder(view, viewLifecycleOwner, interactor)
    }

    override fun onBindViewHolder(holder: TelegramItemViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onBindViewHolder(
        holder: TelegramItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when (payloads[0]) {
                is TopSite -> {
                    holder.bind((payloads[0] as TopSite), position)
                }
            }
        }
    }

    internal object TopSitesDiffCallback : DiffUtil.ItemCallback<TopSite>() {
        override fun areItemsTheSame(oldItem: TopSite, newItem: TopSite) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TopSite, newItem: TopSite) =
            oldItem.id == newItem.id && oldItem.title == newItem.title && oldItem.url == newItem.url

        override fun getChangePayload(oldItem: TopSite, newItem: TopSite): Any? {
            return if (oldItem.id == newItem.id && oldItem.url == newItem.url && oldItem.title != newItem.title) {
                newItem
            } else null
        }
    }
}