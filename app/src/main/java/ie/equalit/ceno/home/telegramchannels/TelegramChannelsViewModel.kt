package ie.equalit.ceno.home.telegramchannels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.utils.XMLParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelsViewModel: ViewModel() {

    private val _channels = MutableStateFlow<List<TopSite>?>(null)
    val channels = _channels.asStateFlow()

    fun getChannels(context: Context) {
        viewModelScope.launch {
            val telegramChannels: List<Pair<String, String>> =
                XMLParser.parseTelegramChannelsXml(
                    context.resources.getXml(R.xml.default_telegram_channels),
                    context
                ) as List<Pair<String, String>>

            val topSites: MutableList<TopSite> = mutableListOf()

            telegramChannels.forEach { tgChan ->
                val topSite = context.components.core.bookmarksStorage
                    .getBookmarksWithUrl(tgChan.second)
                    .getOrNull()
                    ?.map {
                        TopSite.Frecent(
                            id = it.guid.hashCode().toLong(),
                            title = it.title,
                            url = it.url ?: "",
                            createdAt = it.dateAdded
                        )
                    }
                if(!topSite.isNullOrEmpty()) {
                    topSites.add(topSite.first())
                }
            }

            _channels.value = topSites
        }
    }
}