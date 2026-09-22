package ie.equalit.ceno.home.telegramchannels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.internal.ViewUtils.getChildren
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.utils.XMLParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelsViewModel : ViewModel() {

    private val _channels = MutableStateFlow<List<TopSite>?>(null)
    val channels = _channels.asStateFlow()

    private val _refresh = MutableSharedFlow<Boolean>()
    val refresh = _refresh.asSharedFlow()

    fun getChannels(context: Context) {
        viewModelScope.launch {
            var guid = context.components.cenoPreferences.telegramChannelsBookGuid
            if (guid.isEmpty()) {
                val presentGuid = getTelegramChannelsGuid(context)
                guid = if(!presentGuid.isNullOrEmpty()) {
                    context.components.cenoPreferences.telegramChannelsBookGuid = guid
                    presentGuid
                }
                else {
                    initializeTelegramChannels(context)
                }
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
                            createdAt = it.dateAdded
                        )
                    )
                }
            }
            _channels.update { topSites }
        }
    }

    private suspend fun getTelegramChannelsGuid(context: Context): String? {
        val children = context.components.core.bookmarksStorage.getTree(
            BookmarkRoot.Mobile.id,
        )
            .getOrNull()
            ?.children
        val channel = children
            ?.find{ it.title == context.getString(R.string.telegram_channels_bookmark_folder_title) }
        return channel?.guid
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

    private suspend fun initializeTelegramChannels(context: Context): String {/*  Launch a coroutine to initialize top site storage cache and update it in the store */
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
        return guid
    }

    fun renameTelegramChannel(context: Context, newName: String, url: String) {
        viewModelScope.launch {
            val guid = context.components.cenoPreferences.telegramChannelsBookGuid
            guid.let {
                val tree = context.components.core.bookmarksStorage
                    .getTree(guid)
                    .getOrNull()

                var child: BookmarkNode? = null
                var position = 0
                tree?.children?.forEachIndexed { index, node ->
                    if (node.url == url) {
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
            }
            _refresh.emit(true)
        }
    }

    fun removeChannel(context: Context, url: String) {
        viewModelScope.launch {
            val guid = context.components.cenoPreferences.telegramChannelsBookGuid
            val tree = context.components.core.bookmarksStorage
                .getTree(guid)
                .getOrNull()

            val child = tree?.children?.find { it.url == url }
            child?.let {
                context.components.core.bookmarksStorage
                    .deleteNode(it.guid)
            }
            _refresh.emit(true)
        }
    }
}