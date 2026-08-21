package ie.equalit.ceno.home.topsites

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.utils.CenoPreferences
import ie.equalit.ceno.utils.XMLParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.feature.top.sites.TopSite

class TopSiteViewModel : ViewModel() {

    private val _topSites = MutableStateFlow<List<TopSite>?>(null)
    val topSites = _topSites.asStateFlow()

    private val _refresh = MutableSharedFlow<Boolean>()
    val refresh = _refresh.asSharedFlow()

    fun getTopSites(context: Context) {
        viewModelScope.launch {
            var guid = context.components.cenoPreferences.topSitesBookmarkGuid
            if (guid.isEmpty()) {
                var defaultTopSites =
                    XMLParser.parseTopsitesXml(
                        context.resources.getXml(R.xml.default_topsites),
                        context
                    ) as List<Pair<String, String>>

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
                guid = initializeTopSites(context, defaultTopSites)
            }

            val tree = context.components.core.bookmarksStorage
                .getTree(guid, true)
                .getOrNull()
                ?.children

            val topSites = mutableListOf<TopSite>()
            tree?.forEach {
                if (it.type == BookmarkNodeType.FOLDER) {
                    topSites.addAll(getChildren(it))
                } else {
                    topSites.add(
                        TopSite.Pinned(
                            id = it.guid.hashCode()
                                .toLong(),
                            title = it.title,
                            url = it.url ?: "",
                            createdAt = it.dateAdded,
                        )
                    )
                }
            }
            _topSites.update { topSites }
        }
    }

    private fun getChildren(bookmarkNode: BookmarkNode): List<TopSite> {
        val children = mutableListOf<TopSite>()
        bookmarkNode.children!!.forEach { folder ->
            if (folder.type == BookmarkNodeType.FOLDER) {
                children.addAll(getChildren(folder))
            } else {
                children.add(
                    TopSite.Pinned(
                        id = folder.guid.hashCode()
                            .toLong(),
                        title = folder.title,
                        url = folder.url ?: "",
                        createdAt = folder.dateAdded
                    )
                )
            }
        }
        return children
    }

    private suspend fun initializeTopSites(
        context: Context,
        defaultTopSites: List<Pair<String, String>>?
    ): String {
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
        return guid
    }

    suspend fun isMaxReached(context: Context) : Boolean {
        val guid = context.components.cenoPreferences.topSitesBookmarkGuid

        val tree = context.components.core.bookmarksStorage
            .getTree(guid)
            .getOrNull()

        val numPinnedSites = tree?.children?.size ?: 0
        return numPinnedSites >= context.components.cenoPreferences.topSitesMaxLimit
    }

    fun addToTopSites(
        context: Context,
        title: String,
        url: String,
        onMaxReachedCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            if(isMaxReached(context)) {
                onMaxReachedCallback()
            } else {
                val guid = context.components.cenoPreferences.topSitesBookmarkGuid

                context.components.core.bookmarksStorage.addItem(
                    parentGuid = guid,
                    url = url,
                    title = title,
                    position = null
                )
                _refresh.emit(true)
            }
        }
    }

    fun renameTopSite(context: Context, newName: String, url: String) {
        viewModelScope.launch {
            val guid = context.components.cenoPreferences.topSitesBookmarkGuid
            val tree = context.components.core.bookmarksStorage
                .getTree(guid)
                .getOrNull()

            var child: BookmarkNode? = null
            var position = 0
            tree?.children?.forEachIndexed { index, node ->
                if(node.url == url) {
                    child = node
                    position = index
                    return@forEachIndexed
                }
            }

            child?.let {
                context.components.core.bookmarksStorage
                    .deleteNode(it.guid)
                context.components.core.bookmarksStorage
                    .addItem(
                        guid,
                        url = url,
                        title = newName,
                        position = position.toUInt(),
                    )
            }
            _refresh.emit(true)
        }
    }

    suspend fun isTopSite(context: Context, url: String): Boolean {
        val guid = context.components.cenoPreferences.topSitesBookmarkGuid
        return context.components.core.bookmarksStorage
            .getTree(guid)
            .getOrNull()
            ?.children?.find { it.url == url }.run {
                this != null
            }
    }

    fun removeShortcut(context: Context, url: String) {
        viewModelScope.launch {
            val guid = context.components.cenoPreferences.topSitesBookmarkGuid
            guid.let {
                val tree = context.components.core.bookmarksStorage
                    .getTree(guid)
                    .getOrNull()

                val child = tree?.children?.find { it.url == url }
                child?.let {
                    context.components.core.bookmarksStorage
                        .deleteNode(it.guid)
                }
            }
            _refresh.emit(true)
        }
    }
}