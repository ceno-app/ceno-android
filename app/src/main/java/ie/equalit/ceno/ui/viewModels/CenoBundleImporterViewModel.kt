package ie.equalit.ceno.ui.viewModels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        intent: Intent,
        baseZipUri: Uri,
        latestZipUri: Uri,
        progressCallback: (Boolean, Int) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }
}