package ie.equalit.ceno.home.topsites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.utils.CenoPreferences
import ie.equalit.ceno.utils.XMLParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.feature.top.sites.TopSite

class TopSiteViewModel : ViewModel() {

    private val _topSites = MutableStateFlow<List<TopSite>?>(null)
    val topSites = _topSites.asStateFlow()

    fun getTopSites(context: Context) {
        viewModelScope.launch {
            var defaultTopSites =
                XMLParser.parseTopsitesXml(
                    context.resources.getXml(R.xml.default_topsites),
                    context
                ) as List<Pair<String, String>>

            if (context.components.cenoPreferences.topSitesBookmarkGuid.isEmpty()) {
                /**
                 * The user customized version if available are maintained.
                 * The list if filtered to take only topsites and not the telegram channels.
                 */
                if (context.components.cenoPreferences.defaultTopSitesAdded) {
                    val topSites = context.components.core.cenoTopSitesStorage
                        .getTopSites(CenoPreferences.TOP_SITES_MAX_COUNT)
                    defaultTopSites = topSites.filter {
                        !it.url.startsWith(context.getString(R.string.default_telegram_channel))
                    }
                        .map {
                            Pair(it.title ?: it.url, it.url)
                        }
                }
                initializeTopSites(context, defaultTopSites)
            }

            val topSites: MutableList<TopSite> = mutableListOf()

            defaultTopSites.forEach { ts ->
                val topSite = context.components.core.bookmarksStorage
                    .getBookmarksWithUrl(ts.second)
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

            _topSites.update { topSites }
        }
    }

    private suspend fun initializeTopSites(
        context: Context,
        defaultTopSites: List<Pair<String, String>>?
    ) {
        val defaultTopSites: List<Pair<String, String>> =
            defaultTopSites ?: XMLParser.parseTopsitesXml(
                context.resources.getXml(R.xml.default_topsites),
                context
            ) as List<Pair<String, String>>

        val guid = context.components.core.bookmarksStorage.addFolder(
            BookmarkRoot.Mobile.id,
            context.getString(R.string.shortcut_bookmark_label),
            null,
        )
            .getOrNull()
            .toString()

        defaultTopSites.forEach { channelPair ->
            context.components.core.bookmarksStorage.addItem(
                parentGuid = guid,
                url = channelPair.second,
                title = channelPair.first,
                position = null
            )
        }

        context.components.cenoPreferences.topSitesBookmarkGuid = guid

        getTopSites(context)
    }
}