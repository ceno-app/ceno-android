package ie.equalit.ceno.ui.ouicrawl

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel
import mozilla.components.support.ktx.android.net.getFileName

@Composable
fun zipFilePickerManager(
    callback: (Uri) -> Unit
): ManagedActivityResultLauncher<Array<String>, Uri?> {
    return rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            callback(uri)
        }
    }
}

@Composable
fun CenoBundleImporterView(
    modifier: Modifier = Modifier,
    viewModel: CenoBundleImporterViewModel,
) {
    val context = LocalContext.current

    var baseFilename by remember{ mutableStateOf("") }
    var latestFilename by remember{ mutableStateOf("") }

    val baseFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        baseFilename = uri.getFileName(context.contentResolver)
    }

    val latestFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        latestFilename = uri.getFileName(context.contentResolver)
    }


    Column(
        modifier = modifier
    ) {
        Button(onClick = {
            baseFilePicker.launch(arrayOf("application/zip"))
        }) {
            Column {
                Text("Click to select file base zip")
                Text(baseFilename)
            }
        }

        Button(onClick = {
            latestFilePicker.launch(arrayOf("application/zip"))
        }) {
            Column {
                Text("Click to select file latest zip")
                Text(latestFilename)
            }
        }

        if (baseFilename.isNotEmpty() && latestFilename.isNotEmpty()) {
            Button(onClick = {
                viewModel.processZips(
                    context = context,
                    baseZipUri = baseFilename,
                    latestZipUri = latestFilename
                )
            }) {
                Column {
                    Text("Import")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CenoBundleImporterView_preview() {
    MaterialTheme {
        CenoBundleImporterView(
            viewModel = remember { CenoBundleImporterViewModel() }
        )
    }
}