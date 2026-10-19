package ie.equalit.ceno.ui.viewModels

import android.R.attr.label
import android.R.id.input
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import mozilla.components.support.ktx.android.net.getFileName
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.Map.entry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.copyTo

class CenoBundleImporterViewModel: ViewModel() {

    @Serializable
    data class CenoBundleMetaData(
        val version: Int,
        val proto: Int,
        val id: String,
        val type: String,
        val site: String,
        val basefilename: String,
        val basedate: String,
        val updatets: String? = null
    )

    private var _uiState = MutableStateFlow(false)
    var uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    fun setUiState(state: Boolean) {
        _uiState.value = state
    }

    fun processZips(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
    ) {
        _uiState.value = true
        val intent = Intent(context, CenoBundleImporterService::class.java).apply {
            putExtra(CenoBundleImporterService.BASE_ZIP_EXTRA, baseZipUri)
            putExtra(CenoBundleImporterService.LATEST_ZIP_EXTRA, latestZipUri)
        }
        try {
            context.startForegroundService(intent)
        } catch(e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = false
    }

    fun unzip(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
        progressCallback: (label: String, filename: String?, totalSize: Long, progress: Int) -> Unit,
        onFinishedCallback: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mapOf(Pair("base", baseZipUri.toUri()), Pair("latest", latestZipUri.toUri())).forEach { (label, uri) ->
                val targetDirectory = File(context.filesDir.toString() + "/ouinet/")
                var progress = 0
                var totalSize = 0L

                val outputFile = File(context.cacheDir, uri.getFileName(context.contentResolver))
                ZipFile(outputFile).use { zip ->
                    zip.entries()
                        .iterator()
                        .forEach {
                            if (!it.isDirectory)
                                totalSize += it.size
                        }
                }
                ZipInputStream(context.contentResolver.openInputStream(uri)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        progress += 1
                        progressCallback(label,entry.name, totalSize,progress)
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
        filename: String
    ) : CenoBundleMetaData? {
        val outputFile = File(context.cacheDir, filename)
        try {
            ZipFile(outputFile).use { zip ->
                val metaDataFile = zip.getEntry("metadata") ?: throw Exception("No metadata found for zip")
                zip.getInputStream(metaDataFile).use { inputStream ->
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()

                    val json = String(buffer, Charsets.UTF_8)
                    val jsonDecoder = Json {
                        ignoreUnknownKeys = true
                    }
                    return jsonDecoder.decodeFromString<CenoBundleMetaData>(json)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}