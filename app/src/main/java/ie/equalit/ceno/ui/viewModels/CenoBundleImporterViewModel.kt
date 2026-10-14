package ie.equalit.ceno.ui.viewModels

import android.R.attr.label
import android.R.id.input
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

class CenoBundleImporterViewModel: ViewModel() {

    fun processZips(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
    ) {
        val intent = Intent(context, CenoBundleImporterService::class.java).apply {
            putExtra(CenoBundleImporterService.BASE_ZIP_EXTRA, baseZipUri)
            putExtra(CenoBundleImporterService.LATEST_ZIP_EXTRA, latestZipUri)
        }
        try {
            context.startForegroundService(intent)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun unzip(
        context: Context,
        baseZipUri: String,
        latestZipUri: String,
        progressCallback: (label: String, filename: String?, totalSize: Int, progress: Int) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mapOf(Pair("base", baseZipUri.toUri()), Pair("latest", latestZipUri.toUri())).forEach { (label, uri) ->
                val baseFilename = uri.getFileName(context.contentResolver)
                val outputFile = File(context.cacheDir, baseFilename)

                var totalSize = 0
                context.contentResolver.openInputStream(baseZipUri.toUri())?.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                    ZipFile(outputFile).use { zip ->
                        totalSize = zip.size();
                        progressCallback(label, null,totalSize, 0)
                    }
                }

                val targetDirectory = File(context.filesDir.toString() + "/ouinet/")
                var progress = 0
                ZipInputStream(context.contentResolver.openInputStream(baseZipUri.toUri())).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        progress += 1
                        progressCallback(label,entry.name, totalSize, progress)
                        val newFile = File(targetDirectory, entry.name)

                        val safePrefix = if (targetDirectory.endsWith(File.separator)) targetDirectory.canonicalPath else targetDirectory.canonicalPath + File.separator
                        if (!newFile.canonicalPath.startsWith(safePrefix)) {
                            throw IOException("Security Exception: Path traversal detected!")
                        }

                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            // Ensure parent directories exist for the file
                            newFile.parentFile?.mkdirs()

                            // Write file content
                            FileOutputStream(newFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            progressCallback("Bundle imported successfully", null,0, 0)
        }
    }
}