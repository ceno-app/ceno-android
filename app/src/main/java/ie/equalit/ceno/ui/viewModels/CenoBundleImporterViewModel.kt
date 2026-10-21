package ie.equalit.ceno.ui.viewModels

import android.R.attr.name
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mozilla.components.support.ktx.android.net.getFileName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
        var importationDate: Long = System.currentTimeMillis()
    )


    private val importedSitesKey = stringPreferencesKey("importedSitesKey")

    private var _storedSites = MutableStateFlow<List<CenoBundleMetaData>>(emptyList())
    val storedSites: StateFlow<List<CenoBundleMetaData>> = _storedSites.asStateFlow()

    private var _uiState = MutableStateFlow(false)
    val uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    fun setUiState(state: Boolean) {
        _uiState.value = state
    }

    fun processZips(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
        siteName: String,
    ) {
        _uiState.value = true
        val intent = Intent(context, CenoBundleImporterService::class.java).apply {
            putExtra(CenoBundleImporterService.BASE_ZIP_EXTRA, baseZipUri)
            putExtra(CenoBundleImporterService.LATEST_ZIP_EXTRA, latestZipUri)
            putExtra(CenoBundleImporterService.SITE_NAME, siteName)
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
                return jsonDecoder.decodeFromString<CenoBundleMetaData>(json)
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

    fun getImportedSites(
        context: Context
    ) {
        viewModelScope.launch {
            val importedSitesJson = context.dataStore.data
                .map { preferences ->
                    preferences[importedSitesKey]
                }

            if(!importedSitesJson.first().isNullOrBlank()) {
                val jsonDecoder = Json {
                    ignoreUnknownKeys = true
                }
                _storedSites.value = jsonDecoder.decodeFromString<List<CenoBundleMetaData>>(importedSitesJson.first()!!)
            }
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