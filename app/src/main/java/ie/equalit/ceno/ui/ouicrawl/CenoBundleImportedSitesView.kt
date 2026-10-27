package ie.equalit.ceno.ui.ouicrawl

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel
import ie.equalit.ceno.ui.viewModels.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Composable
fun CenoBundleImportedSitesView(
    modifier: Modifier,
    cenoBundleImporterViewModel: CenoBundleImporterViewModel,
) {
    val sharedSites by cenoBundleImporterViewModel.sharedSites.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    var showModal by remember { mutableStateOf(false) }

    var selectedSite: CachedGroupAdapter.GroupItem? by remember{ mutableStateOf(null)}

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn {
            items(sharedSites.filter{ it.name.isNotBlank() }) { site ->
                ImportedSitesView(
                    site = site.name,
                    releaseDate = "",
                    onOpenClicked = {
                        (activity as BrowserActivity)
                            .openToBrowser(url = "https://${site.name}", newTab = true)
                    }
                ) {
                    selectedSite = site
                    showModal = true
                }

                HorizontalDivider(Modifier
                    .fillMaxWidth()
                    .padding()
                )
            }
        }

        selectedSite?.let { ss ->
            SharedSitesModal(
                show = showModal,
                site = ss,
                onChildClickCallback = { url ->
                    (activity as BrowserActivity)
                        .openToBrowser(url = "https://$url", newTab = true)
                }
            ) {
                showModal = false
                selectedSite = ss
            }
        }
    }
}

@Composable
fun ImportedSitesView(
    site: String,
    releaseDate: String,
    onOpenClicked: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    painterResource(R.drawable.ouinet_globe_pm),
                    ""
                )
            },
            headlineContent = {
                Text(site)
            },
            supportingContent = {
//                Text(stringResource(R.string.latest_update, latestUpdate))
            },
            trailingContent = {
                TextButton(onClick = onOpenClicked) {
                    Text(stringResource(R.string.shared_sites_open))
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = colorResource(R.color.ceno_home_background)
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportedSitesView_preview() {
    MaterialTheme {
        ImportedSitesView(
            site = "sample.org",
//            latestUpdate = "Yesterday",
            releaseDate = "Monday",
            onOpenClicked = {}
        ) {}
    }
}
