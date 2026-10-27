package ie.equalit.ceno.ui.viewModels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.ETC1.isValid
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.R.string.no_content_shared
import ie.equalit.ceno.R.string.ouinet_client_fetch_fail
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.OuinetKey
import ie.equalit.ceno.settings.OuinetResponseListener
import ie.equalit.ceno.settings.OuinetValue
import ie.equalit.ceno.settings.SiteContentGroupFragment.PinnedCacheGroup
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterService
import io.sentry.util.CollectionUtils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mozilla.components.support.ktx.android.net.getFileName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.collections.forEachIndexed
import kotlin.collections.toMutableList

val Context.dataStore by preferencesDataStore(name = "ceno_bundle_importer_datastore")

class CenoBundleImporterViewModel : ViewModel() {

    @Serializable
    data class CenoBundleMetaData(
        val version: Int,
        val proto: Int,
        val id: String,
        val type: String,
        val site: String,
        val basefilename: String,
        val basedate: String,
        val updatets: String? = null,
        var importationDate: Long = System.currentTimeMillis(),
        var filename: String? = null,
        var uri: String? = null,
        var isValid: Boolean? = true,
        var isLoading: Boolean? = false
    )


    val importedSitesKey = stringPreferencesKey("importedSitesKey")

    private var _storedSites = MutableStateFlow<List<CenoBundleMetaData>>(emptyList())
    val storedSites: StateFlow<List<CenoBundleMetaData>> = _storedSites.asStateFlow()

    private var _processingCounter = MutableStateFlow(0)
    val processingCounter: StateFlow<Int> = _processingCounter.asStateFlow()

    private var _processedFiles = MutableStateFlow<List<Pair<Boolean, CenoBundleMetaData>>>(emptyList())
    val processedFiles: StateFlow<List<Pair<Boolean, CenoBundleMetaData>>> = _processedFiles.asStateFlow()

    private var _uiState = MutableStateFlow(false)
    val uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    fun setUiState(state: Boolean) {
        _uiState.value = state
    }

    fun processSelectedFiles(
        context: Context,
        uris: List<Uri>
    ) {
        val uris = uris.filter{ !_processedFiles.value.any { pf-> pf.second.uri == it.toString() } }
        _processedFiles.update { cl ->
            cl + uris.map { uri ->
                Pair(
                    true,
                    CenoBundleMetaData(
                        version = 0,
                        proto = 0,
                        id = "",
                        type = "",
                        site = "",
                        basefilename = "",
                        basedate = "",
                        uri = uri.toString(),
                        isLoading = true
                    )
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val cmds: MutableList<CenoBundleMetaData> = mutableListOf()
            uris.forEach { uri ->
                getMetadata(
                    context = context,
                    uri = uri
                )?.let { cmds.add(it) }
            }
            _processedFiles.update { cl ->
                cl.map { site ->
                    Pair(false, (cmds.find{ it.uri == site.second.uri } ?: site.second).apply {
                        isLoading = false
                    })
                }
            }
            checkValidity(context)
        }
    }

    private fun checkValidity(context: Context, newList: List<Pair<Boolean, CenoBundleMetaData>>? = null) {
        viewModelScope.launch {
            val tmp = (newList ?: _processedFiles.value).map { site ->
                Pair(
                    site.first,
                    site.second.copy(
                        isValid = isBaseAvailable(context, site.second, newList),
                    )
                )
            }
            _processedFiles.update { _ ->
                tmp.sortedWith(
                    compareBy<Pair<Boolean, CenoBundleMetaData>> { it.second.isValid }
                        .thenBy { it.second.type }
                )
            }
        }
    }

    fun removeProcessedFiles(context: Context, cmd: CenoBundleMetaData) {
        viewModelScope.launch {
            val newList = _processedFiles.value.filter { it.second.uri != cmd.uri }
            checkValidity(context, newList)
        }
    }

    fun processZips(context: Context) {
        _uiState.value = true
        val bundles = Json.encodeToString<List<CenoBundleMetaData>>(
            _processedFiles.value.filter{ it.second.isValid == true }.map{ it.second }
        )
        val intent = Intent(context, CenoBundleImporterService::class.java).apply {
            putExtra(CenoBundleImporterService.ZIP_EXTRA, bundles)
        }
        try {
            context.startForegroundService(intent)
            _processedFiles.update {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = false
    }

    fun unzip(
        context: Context,
        cmd: List<CenoBundleMetaData>,
        progressCallback: (siteName: String, progress: Int, total: Int) -> Unit,
        onFinishedCallback: () -> Unit,
    ) {
        val targetDirectory = File(context.filesDir.toString() + "/ouinet/")
        viewModelScope.launch(Dispatchers.IO) {
            cmd.forEachIndexed { index, bundle ->
                val uri = bundle.uri?.toUri() ?: throw Exception("uri not found")

                ZipInputStream(context.contentResolver.openInputStream(uri)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val newFile = File(targetDirectory, entry.name)

                        val safePrefix = if (targetDirectory.endsWith(File.separator))
                            targetDirectory.canonicalPath
                        else targetDirectory.canonicalPath + File.separator

                        if (!newFile.canonicalPath.startsWith(safePrefix)) {
                            throw IOException("Security Exception: Path traversal detected!")
                        }

                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            Log.d(TAG, "Unzipping $entry")
                            // Ensure parent directories exist for the file
                            newFile.parentFile?.mkdirs()

                            FileOutputStream(newFile).use { fos ->
                                try {
                                    zis.copyTo(fos)
                                } catch(e: ZipException) {
                                    e.printStackTrace()
                                    Log.e(TAG, "Error unzipping $entry")
                                }
                            }
                        }
                        try {
                            zis.closeEntry()
                            entry = zis.nextEntry
                        } catch(e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "Error unzipping $entry", e)
                        }
                    }
                }

                if (bundle.type == "base") {
                    storeImportedBaseMetaData(
                        context,
                        bundle
                    )
                }
                progressCallback(bundle.site, index + 1, cmd.size)
            }
            onFinishedCallback()
        }
    }

    fun getMetadata(
        context: Context,
        uri: Uri
    ): CenoBundleMetaData? {
        val fileName = uri.getFileName(context.contentResolver) ?: "temp.zip"
        val outputFile = File(context.cacheDir, fileName)

        try {
            context.contentResolver.openInputStream(uri)
                ?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: return null

            ZipFile(outputFile).use { zip ->
                val metaDataFile = zip.getEntry("metadata")
                    ?: throw IllegalArgumentException("No metadata found for zip")

                val json = zip.getInputStream(metaDataFile)
                    .use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8)
                            .use { it.readText() }
                    }

                val jsonDecoder = Json {
                    ignoreUnknownKeys = true
                }
                return jsonDecoder.decodeFromString<CenoBundleMetaData>(json).apply {
                    this.filename = uri.getFileName(context.contentResolver)
                    this.uri = uri.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (outputFile.exists()) {
                outputFile.delete()
            }
        }
        return null
    }

    fun loadSites(sites: String) {
        viewModelScope.launch {
            val jsonDecoder = Json {
                ignoreUnknownKeys = true
            }

            val storedSites = jsonDecoder.decodeFromString<List<CenoBundleMetaData>>(sites)
            _storedSites.value = storedSites
        }
    }

    private suspend fun isBaseAvailable(
        context: Context,
        cmd: CenoBundleMetaData,
        newList: List<Pair<Boolean, CenoBundleMetaData>>? = null
    ): Boolean {
        if ((newList ?: _processedFiles.value).any {
            it.second.type == "base" &&
                    it.second.id == cmd.id &&
                    it.second.site == cmd.site
        })
            return true

        val currentData = context.dataStore.data.map {
            it[importedSitesKey]
        }

        currentData.first() ?: return false

        val cd = Json.decodeFromString<List<CenoBundleMetaData>>(currentData.first()!!)
        return cd.any { it.id == cmd.id && it.site == cmd.site }
    }

    private fun storeImportedBaseMetaData(
        context: Context,
        baseMetaData: CenoBundleMetaData,
    ) {
        viewModelScope.launch {
            val jsonDecoder = Json {
                ignoreUnknownKeys = true
            }

            val currentData = context.dataStore.data.map {
                it[importedSitesKey] ?: "[]"
            }

            val currentJson = jsonDecoder.decodeFromString<List<CenoBundleMetaData>>(currentData.first()).toMutableList()

            val currentMd = currentJson.find { it.id == baseMetaData.id }.run {
                if(this != null) {
                    currentJson.remove(this)
                }
                baseMetaData
            }

            currentJson.add(currentMd)
            val storeJson = jsonDecoder.encodeToString<List<CenoBundleMetaData>>(currentJson)

            context.dataStore.edit { preferences ->
                preferences[importedSitesKey] = storeJson
            }
        }
    }

    private val _sharedSites = MutableStateFlow<List<CachedGroupAdapter.GroupItem>>(emptyList())
    val sharedSites: StateFlow<List<CachedGroupAdapter.GroupItem>> = _sharedSites.asStateFlow()


    fun getSharedContents(
        context: Context,
        groups: String
    ) {
        _sharedSites.value = convertToMap(context, groups)
    }

    private fun convertToMap(context: Context, groups: String): List<CachedGroupAdapter.GroupItem> {
        val urls = groups.split("\n")
        var pinnedUrls = mutableListOf<String>()
        CenoSettings.ouinetClientRequest(
            context,
            viewModelScope,
            OuinetKey.PINNED_GROUPS,
            OuinetValue.OTHER,
            ouinetResponseListener = object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    pinnedUrls = context.components.json
                        .decodeFromString<PinnedCacheGroup>(message)
                        .pinnedGroups.toMutableList()
                }

                override fun onError() {
                    //TODO: Not yet implemented
                }
            }
        )

        val map = mutableMapOf<String, MutableList<CachedGroupAdapter.GroupChildItem>>()
        for (url in urls) {
            val parts = url.split("/")
            val baseUrl = parts.first()

            val isPinned = pinnedUrls.contains(url)
            map[baseUrl] = if (map[baseUrl].isNullOrEmpty()) {
                mutableListOf<CachedGroupAdapter.GroupChildItem>().apply {
                    add(CachedGroupAdapter.GroupChildItem(url, isPinned))
                }
            } else {
                map[baseUrl].apply {
                    this!!.add(
                        CachedGroupAdapter.GroupChildItem(
                            url,
                            isPinned
                        )
                    )
                }!!
            }
        }

        val result = mutableListOf<CachedGroupAdapter.GroupItem>()
        map.keys.forEach {
            result.add(CachedGroupAdapter.GroupItem(it, map[it]!!.toList()))
        }

        return result
    }

    fun prepareOuinetForNavigation(
        context: Context,
        onNavigationCallback: (String) -> Unit
    ) {
        viewModelScope.launch {
            CenoSettings.ouinetClientRequest(
                context = context,
                coroutineScope = this,
                key = OuinetKey.GROUPS_TXT,
                ouinetResponseListener = object : OuinetResponseListener {
                    override fun onSuccess(message: String, data: Any?) {
                        if (message.trim()
                                .isEmpty()
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context,
                                    context.getString(no_content_shared),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        } else {
                            onNavigationCallback(message)
                        }
                    }

                    override fun onError() {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                ContextCompat.getString(context, ouinet_client_fetch_fail),
                                LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                },
                shouldRefresh = false
            )
        }
    }


    companion object {
        const val TAG = "CenoBundleImporterViewModel"
    }
}