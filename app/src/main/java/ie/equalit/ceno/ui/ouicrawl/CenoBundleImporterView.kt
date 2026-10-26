package ie.equalit.ceno.ui.ouicrawl

import android.content.Intent
import android.net.Uri
import android.opengl.ETC1.isValid
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.components.HorizontalPulsePlaceholder
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel
import mozilla.components.support.ktx.android.net.getFileName

@Composable
fun zipFilePickerManager(
    callback: (List<Uri>) -> Unit
): ManagedActivityResultLauncher<Array<String>, List<Uri>> {
    return rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            callback(uris)
        }
    }
}

@Composable
fun CenoBundleImporterView(
    modifier: Modifier = Modifier,
    viewModel: CenoBundleImporterViewModel,
    isDarkTheme: Boolean,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val zipCounter by viewModel.processingCounter.collectAsStateWithLifecycle()
    val processedZips by viewModel.processedFiles.collectAsStateWithLifecycle()

    val filePicker = zipFilePickerManager { uris: List<Uri> ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        uris.forEach { uri ->
            context.contentResolver.takePersistableUriPermission(uri, flag)
            viewModel.processSelectedFiles(context, uris)
        }
    }

    val textColor = if (!isDarkTheme) {
        colorResource(R.color.novaBlackA50)
    } else {
        colorResource(R.color.novaBlackA50)
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

            Column {
                AnimatedVisibility(visible = processedZips.isEmpty()) {
                    Card(
                        onClick = {},
                        colors = CardDefaults.cardColors(
                            colorResource(R.color.ceno_blue_200)
                        )
                    ) {
                        Row(Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mozac_ic_information_24),
                                "",
                                modifier = Modifier
                                    .size(25.dp),
                                tint = colorResource(R.color.ceno_blue_800)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                stringResource(
                                    R.string.ceno_bundles_are_zip_files_containing_copies_of_websites_they_can_be_downloaded_from,
                                    ouicrawlUrl
                                ),
                                color = textColor
                            )
                        }
                    }
                    Spacer(Modifier.padding(8.dp))

                }
            }
            Text(
                stringResource(R.string.you_must_import_a_base_zip_and_then_a_latest_zip_of_a_website_to_create_a_full_copy_of_a_website_in_your_local_cache),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            Column {
                AnimatedVisibility(visible = processedZips.isEmpty()) {
                    Spacer(Modifier.padding(16.dp))

                    Text(
                        stringResource(R.string.you_can_select_multiple_zip_files_you_will_get_a_notification_once_processing_and_importation_begins_and_completes),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            ZipImporterComponentView(
                text = stringResource(R.string.click_to_import_file),
                metadata = null,
                isDarkTheme = isDarkTheme,
                minimize = processedZips.isNotEmpty(),
                onCloseCallback = {
                    TODO()
                }
            ) {
                filePicker.launch(arrayOf("application/zip"))
            }

            Spacer(Modifier.size(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = processedZips,
                    key = { items -> items.second.uri!! }
                ) { md ->
                    ZipImporterComponentView(
                        text = stringResource(R.string.click_to_import_file),
                        metadata = md.second,
                        isDarkTheme = isDarkTheme,
                        onCloseCallback = {
                            viewModel.removeProcessedFiles(context,md.second)
                        }
                    ) {
                        filePicker.launch(arrayOf("application/zip"))
                    }
                }
            }
        }
    }
}

@Composable
fun ZipImporterComponentView(
    text: String,
    metadata: CenoBundleImporterViewModel.CenoBundleMetaData?,
    isDarkTheme: Boolean,
    minimize: Boolean = false,
    onCloseCallback: () -> Unit,
    onClickCallback: () -> Unit,
) {
    val isLoading = metadata?.isLoading == true
    val isValid = metadata?.isValid == true

    val cardColor = if (!isDarkTheme || minimize) {
        colorResource(R.color.ceno_blue_200)
    } else {
        colorResource(R.color.ceno_blue_800)
    }

    val iconColors = if (!isDarkTheme) {
        colorResource(R.color.ceno_blue_800)
    } else {
        colorResource(R.color.ceno_blue_200)
    }

    val invalidColor = if (!isDarkTheme) {
        colorResource(R.color.snackbar_error_color)
    } else {
        colorResource(R.color.device_type_desktop_background)
    }

    val textColor = if (!isDarkTheme || minimize) {
        colorResource(R.color.novaBlackA50)
    } else {
        colorResource(R.color.novaWhite)
    }

    Card(
        onClick = onClickCallback,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
    ) {
        if (!isLoading && metadata == null) {
            if(!minimize) {
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
                        tint = iconColors
                    )

                    Text(text, color = textColor)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_folder_zip_24),
                        "",
                        modifier = Modifier.size(50.dp),
                        tint = textColor
                    )

                    Spacer(Modifier.size(16.dp))

                    Text(text, color = textColor)
                }
            }
        } else {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.outline_folder_zip_24),
                        "",
                        modifier = Modifier.size(50.dp),
                        tint = if(!isValid) invalidColor else iconColors
                    )
                },
                headlineContent = {
                    HorizontalPulsePlaceholder(
                        isLoading = isLoading,
                        widthFraction = 1f
                    ) {
                        Text(
                            metadata.filename ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                supportingContent = {
                    HorizontalPulsePlaceholder(
                        isLoading = isLoading,
                        widthFraction = 0.5f
                    ) {
                        Column {
                            if (metadata.isValid == false) {
                                Text(
                                    stringResource(R.string.base_file_not_found),
                                    color = invalidColor
                                )
                                Text(
                                    stringResource(R.string.base_filename, metadata.basefilename),
                                    color = invalidColor
                                )
                            } else {
                                Text(stringResource(R.string.version, metadata.version))
                                Text(stringResource(R.string.site, metadata.site))
                            }
                        }
                    }
                },
                trailingContent = {
                    if(isLoading) {
                        CircularProgressIndicator(
                            color = iconColors,
                            trackColor = cardColor,
                        )
                    } else {
                        IconButton(
                            onClick = onCloseCallback,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                painterResource(R.drawable.mozac_ic_close),
                                ""
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = cardColor
                )
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
            isDarkTheme = false,
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245",
                filename = "ouicrawl-fa.wikipedia.org-base.zip"
            ),
            onCloseCallback = {}
        ) {}
    }
}


@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_invalid_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            isDarkTheme = false,
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245",
                filename = "ouicrawl-fa.wikipedia.org-base.zip",
                isValid = false
            ),
            onCloseCallback = {}
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_invalid_dark_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            isDarkTheme = true,
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245",
                filename = "ouicrawl-fa.wikipedia.org-base.zip",
                isValid = false
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
            isDarkTheme = true,
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245",
                filename = "ouicrawl-fa.wikipedia.org-base.zip"
            ),
            onCloseCallback = {}
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ZipImporterComponentView_filename_loading_preview() {
    MaterialTheme {
        ZipImporterComponentView(
            text = "Click to import",
            isDarkTheme = true,
            metadata = CenoBundleImporterViewModel.CenoBundleMetaData(
                version = 1,
                proto = 1,
                id = "id",
                type = "type",
                site = "site.com",
                basefilename = "basefilename.zip",
                basedate = "12343434",
                updatets = "123245",
                isLoading = true
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
            viewModel = remember { CenoBundleImporterViewModel() },
            isDarkTheme = false,
        )
    }
}