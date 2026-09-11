package ie.equalit.ceno.home.telegramchannels

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.databinding.ComponentTelegramChannelsBinding
import ie.equalit.ceno.home.sessioncontrol.TelegramChannelInteractor
import ie.equalit.ceno.utils.CenoGridLayoutManager
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelViewHolder(
    view: View,
    viewLifecycleOwner: LifecycleOwner,
    interactor: TelegramChannelInteractor
) : RecyclerView.ViewHolder(view) {

    private val telegramChannelAdapter = TelegramChannelsAdapter(viewLifecycleOwner, interactor)
    val binding = ComponentTelegramChannelsBinding.bind(view)

    init {
        val gridLayoutManager =
            CenoGridLayoutManager(view.context, SPAN_COUNT)

        binding.telegramChannelList.apply {
            adapter = telegramChannelAdapter
            layoutManager = gridLayoutManager
        }
    }

    fun bind(topSites: List<TopSite>) {
        telegramChannelAdapter.submitList(topSites)
    }

    companion object {
        const val SPAN_COUNT = 4
    }
}
