package ie.equalit.ceno.home.telegram_channels

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.databinding.ComponentTopSitesBinding
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import ie.equalit.ceno.home.topsites.TopSitesAdapter
import ie.equalit.ceno.utils.CenoGridLayoutManager
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelViewHolder(
    view: View,
    viewLifecycleOwner: LifecycleOwner,
    interactor: TopSiteInteractor
) : RecyclerView.ViewHolder(view) {

    private val topSitesAdapter = TelegramChannelsAdapter(viewLifecycleOwner, interactor)
    val binding = ComponentTopSitesBinding.bind(view)

    init {
        val gridLayoutManager =
            CenoGridLayoutManager(view.context, SPAN_COUNT)

        binding.topSitesList.apply {
            adapter = topSitesAdapter
            layoutManager = gridLayoutManager
        }
    }

    fun bind(topSites: List<TopSite>) {
        topSitesAdapter.submitList(topSites)
    }

    companion object {
        const val SPAN_COUNT = 4
    }
}