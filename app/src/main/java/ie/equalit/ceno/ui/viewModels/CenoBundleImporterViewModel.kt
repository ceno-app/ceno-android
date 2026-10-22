package ie.equalit.ceno.ui.viewModels

import android.R.attr.name
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterService
import io.sentry.util.CollectionUtils.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mozilla.components.support.ktx.android.net.getFileName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

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
        var latestMetaData: List<CenoBundleMetaData>? = null,
        var importationDate: Long = System.currentTimeMillis(),
        var filename: String? = null,
        var uri: String? = null
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
        _processedFiles.value = uris.map {
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
                    uri = it.toString()
                )
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            uris.forEach { uri ->
                val fMd = getMetadata(
                    context = context,
                    uri = uri
                )
                fMd?.let { md ->
                    _processedFiles.update { currentList ->
                        currentList.map { site ->
                            Pair(false, if (site.second.uri == md.uri) md else site.second)
                        }
                    }
                }
            }
        }
    }

    fun processZips(context: Context) {
        _uiState.value = true
        val bundles = Json.encodeToString<List<CenoBundleMetaData>>(_processedFiles.value.map{ it.second} )
        val intent = Intent(context, CenoBundleImporterService::class.java).apply {
            putExtra(CenoBundleImporterService.ZIP_EXTRA, bundles)
        }
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = false
    }

    fun unzip(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
        progressCallback: (label: String) -> Unit,
        onFinishedCallback: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mapOf(
                Pair("base", baseZipUri.toUri()),
                Pair("latest", latestZipUri.toUri())
            ).forEach { (label, uri) ->

                val targetDirectory = File(context.filesDir.toString() + "/ouinet/")

                ZipInputStream(context.contentResolver.openInputStream(uri)).use { zis ->
                    var entry = zis.nextEntry

                    progressCallback(label)

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
                            Log.d("CenoBundleImporterViewModel", "Unzipping $entry")
                            // Ensure parent directories exist for the file
                            newFile.parentFile?.mkdirs()

                            FileOutputStream(newFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
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

    fun storeImportedSite(
        context: Context,
        baseMetaData: CenoBundleMetaData,
        latestMetaData: CenoBundleMetaData
    ) {
        viewModelScope.launch {
            val jsonDecoder = Json {
                ignoreUnknownKeys = true
            }

            val currentData = context.dataStore.data.map {
                it[importedSitesKey] ?: ""
            }
            val storeJson = if(currentData.first().isBlank()) {
                baseMetaData.latestMetaData = latestMetaData.latestMetaData
                    ?.plus(latestMetaData) ?: listOf(latestMetaData)
                jsonDecoder.encodeToString<List<CenoBundleMetaData>>(listOf(baseMetaData))
            } else {
                val currentJson = jsonDecoder.decodeFromString<List<CenoBundleMetaData>>(currentData.first()).toMutableList()

                val currentMd = currentJson.find { it.id == latestMetaData.id }.run {
                    if(this != null) {
                        currentJson.remove(this)
                        this.latestMetaData = listOf(latestMetaData)
                        this
                    } else {
                        baseMetaData.latestMetaData = latestMetaData.latestMetaData
                            ?.plus(latestMetaData) ?: listOf(latestMetaData)
                        baseMetaData
                    }
                }

                currentJson.add(currentMd)
                jsonDecoder.encodeToString<List<CenoBundleMetaData>>(currentJson)
            }

            context.dataStore.edit { preferences ->
                preferences[importedSitesKey] = storeJson
            }
        }
    }
}