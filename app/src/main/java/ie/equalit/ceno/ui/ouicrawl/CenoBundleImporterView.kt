package ie.equalit.ceno.ui.ouicrawl

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ie.equalit.ceno.R
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
    var baseFileSize by remember{ mutableStateOf("") }

    var latestFilename by remember{ mutableStateOf("") }
    var latestFileSize by remember{ mutableStateOf("") }

    val baseFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        baseFilename = uri.getFileName(context.contentResolver)
        baseFileSize = uri.getQueryParameter(OpenableColumns.SIZE) ?: ""
    }

    val latestFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        latestFilename = uri.getFileName(context.contentResolver)
        latestFileSize = uri.getQueryParameter(OpenableColumns.SIZE) ?: ""
    }


    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(Modifier
            .padding(16.dp)
        ) {
            val ouicrawlUrl = "https://build.ouinet.work/ouicrawl"

            Card(
                onClick = {},
            ) {
                Box {
                    Text(
                        "Ceno bundles are zip files containing copies of websites. They can be downloaded from $ouicrawlUrl.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(Modifier.padding(8.dp))

            Text("You must import a base zip and then a latest zip of a website to create a full copy of a website in your local cache")

            Spacer(Modifier.padding(16.dp))

            Text(
                "Import base zip file",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            ZipImporterComponentView(
                text = "Click to import base file",
                filename = baseFilename,
                fileSize = baseFileSize,
                onCloseCallback = {
                    baseFilename = ""
                }
            ) {
                baseFilePicker.launch(arrayOf("application/zip"))
            }

            Spacer(Modifier.padding(16.dp))

            Text(
                "Import latest zip file",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            ZipImporterComponentView(
                text = "Click to import latest file",
                filename = latestFilename,
                fileSize = latestFileSize,
                onCloseCallback = {
                    latestFilename = ""
                }
            ) {
                latestFilePicker.launch(arrayOf("application/zip"))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.processZips(
                        context = context,
                        baseZipUri = baseFilename,
                        latestZipUri = latestFilename
                    )
                },
                enabled = baseFilename.isNotBlank() && latestFilename.isNotBlank()
            ) {
                Column {
                    Text("Import")
                }
            }
        }
    }
}

@Composable
fun ZipImporterComponentView(
    text: String,
    filename: String?,
    fileSize: String?,
    onCloseCallback: () -> Unit,
    onClickCallback: () -> Unit,
) {
    Card(
        onClick = onClickCallback,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (filename.isNullOrBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_folder_zip_24),
                    "",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(text)
            }
        } else {
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.outline_folder_zip_24),
                        "",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                headlineContent = {
                    Text(
                        filename,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text( fileSize ?: "" )
                },
                trailingContent = {
                    IconButton(
                        onClick = onCloseCallback,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            painterResource(R.drawable.mozac_ic_close),
                            ""
                        )
                    }
                },
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            filename = null,
            fileSize = "10.8 MB",
            onCloseCallback = {}
        ){}
    }
}

@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_filename_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            filename = "filename.zip",
            fileSize = "10.8 MB",
            onCloseCallback = {}
        ){}
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