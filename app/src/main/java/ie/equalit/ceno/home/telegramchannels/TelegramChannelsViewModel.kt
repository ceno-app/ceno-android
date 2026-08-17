package ie.equalit.ceno.home.telegramchannels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.utils.XMLParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelsViewModel : ViewModel() {

    private val _channels = MutableStateFlow<List<TopSite>?>(null)
    val channels = _channels.asStateFlow()

    fun getChannels(context: Context) {
        viewModelScope.launch {
            if (context.components.cenoPreferences.telegramChannelsBookGuid.isEmpty()) {
                initializeTelegramChannels(context)
            }

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
                            id = it.guid.hashCode()
                                .toLong(),
                            title = it.title,
                            url = it.url ?: "",
                            createdAt = it.dateAdded
                        )
                    }
                if (!topSite.isNullOrEmpty()) {
                    topSites.add(topSite.first())
                }
            }

            _channels.update { topSites }
        }
    }

    private suspend fun initializeTelegramChannels(context: Context) {/*  Launch a coroutine to initialize top site storage cache and update it in the store */
        val telegramChannels: List<Pair<String, String>> =
            XMLParser.parseTelegramChannelsXml(
                context.resources.getXml(R.xml.default_telegram_channels),
                context
            ) as List<Pair<String, String>>

        val guid = context.components.core.bookmarksStorage.addFolder(
            BookmarkRoot.Mobile.id,
            context.getString(R.string.telegram_channels_bookmark_folder_title),
            null,
        )
            .getOrNull()
            .toString()

        telegramChannels.forEach { channelPair ->
            context.components.core.bookmarksStorage.addItem(
                parentGuid = guid,
                url = channelPair.second,
                title = channelPair.first,
                position = null
            )
        }

        context.components.cenoPreferences.telegramChannelsBookGuid = guid
    }
}