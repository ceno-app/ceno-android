package ie.equalit.ceno.ui.ouicrawl

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val resources = LocalResources.current

    var baseFileUri by remember { mutableStateOf("") }
    var baseFilename by remember { mutableStateOf("") }

    var latestFileUri by remember { mutableStateOf("") }
    var latestFilename by remember { mutableStateOf("") }

    var baseFileMetaData: CenoBundleImporterViewModel.CenoBundleMetaData? by remember {
        mutableStateOf(
            null
        )
    }
    var latestFileMetaData: CenoBundleImporterViewModel.CenoBundleMetaData? by remember {
        mutableStateOf(
            null
        )
    }

    val baseFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        val tmpFileUri = uri.toString()
        val tmpFilename = uri.getFileName(context.contentResolver)

        val md = viewModel.getMetadata(
            context = context,
            uri = uri
        )
        if (md?.type == "base") {
            baseFileMetaData = md
            baseFileUri = tmpFileUri
            baseFilename = tmpFilename
        } else {
            Toast.makeText(
                context,
                resources.getString(R.string.selected_file_is_not_a_base_zip),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    val latestFilePicker = zipFilePickerManager { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val tmpFileUri = uri.toString()
        val tmpFilename = uri.getFileName(context.contentResolver)

        val md = viewModel.getMetadata(
            context = context,
            uri = uri
        )

        if (md?.type == "update") {
            context.contentResolver.takePersistableUriPermission(uri, flag)
            latestFileMetaData = md
            latestFileUri = tmpFileUri
            latestFilename = tmpFilename
        } else {
            Toast.makeText(
                context,
                resources.getString(R.string.file_is_not_a_latest_zip),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(latestFilename, baseFilename) {

        viewModel.setUiState(baseFilename.isNotBlank() && latestFilename.isNotBlank())

        if (baseFilename.isBlank()) {
            baseFileUri = ""
            baseFileMetaData = null
        }

        if (latestFilename.isBlank()) {
            latestFileUri = ""
            latestFileMetaData = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val ouicrawlUrl = "https://build.ouinet.work/ouicrawl"

            Card(
                onClick = {},
            ) {
                Box {
                    Text(
                        stringResource(
                            R.string.ceno_bundles_are_zip_files_containing_copies_of_websites_they_can_be_downloaded_from,
                            ouicrawlUrl
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(Modifier.padding(8.dp))

            Text(stringResource(R.string.you_must_import_a_base_zip_and_then_a_latest_zip_of_a_website_to_create_a_full_copy_of_a_website_in_your_local_cache))

            Spacer(Modifier.padding(16.dp))

            Text(
                stringResource(R.string.import_base_zip_file),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            ZipImporterComponentView(
                text = stringResource(R.string.click_to_import_base_file),
                filename = baseFilename,
                metadata = baseFileMetaData,
                onCloseCallback = {
                    baseFilename = ""
                }
            ) {
                baseFilePicker.launch(arrayOf("application/zip"))
            }

            Spacer(Modifier.padding(16.dp))

            Text(
                stringResource(R.string.import_latest_zip_file),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            ZipImporterComponentView(
                text = stringResource(R.string.click_to_import_latest_file),
                filename = latestFilename,
                metadata = latestFileMetaData,
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
                        baseZipUri = baseFileUri,
                        latestZipUri = latestFileUri,
                        siteName = baseFileMetaData?.site ?: ""
                    )
                    baseFilename = ""
                    latestFilename = ""
                },
                enabled = baseFilename.isNotBlank() && latestFilename.isNotBlank() && uiState
            ) {
                Column {
                    Text(stringResource(R.string.ceno_bundle_import))
                }
            }
        }
    }
}

@Composable
fun ZipImporterComponentView(
    text: String,
    filename: String?,
    metadata: CenoBundleImporterViewModel.CenoBundleMetaData?,
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
                    /**
                     * {
                     *     "version":1,
                     *     "proto":6,
                     *     "id":"0b188d92555807fb505c2fc8354a91dd0e10c8d9",
                     *     "type": "base",
                     *     "site":"arkhipov.syg.ma",
                     *     "basefilename":"ouicrawl-arkhipov.syg.ma-base.zip",
                     *     "basedate":"2026-10-01"
                     * }
                     */
                    Column {
                        Text(stringResource(R.string.version, metadata?.version ?: 0))
                        Text(stringResource(R.string.site, metadata?.site ?: ""))
                    }
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
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245"
            ),
            onCloseCallback = {}
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_filename_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            filename = "filename.zip",
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245"
            ),
            onCloseCallback = {}
        ) {}
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