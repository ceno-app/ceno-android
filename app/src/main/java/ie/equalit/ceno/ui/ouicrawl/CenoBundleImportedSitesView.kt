package ie.equalit.ceno.ui.ouicrawl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel
import ie.equalit.ceno.ui.viewModels.dataStore


@Composable
fun CenoBundleImportedSitesView(
    modifier: Modifier,
    cenoBundleImporterViewModel: CenoBundleImporterViewModel,
) {
    val storedSites by cenoBundleImporterViewModel.storedSites.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn {
            items(storedSites) { site ->
                ImportedSitesView(
                    filename = site.basefilename,
                    site = site.site,
                    latestUpdate = site.latestMetaData.orEmpty().first().importationDate.toString(),
                    releaseDate = site.basedate
                )
            }
        }
    }
}

@Composable
fun ImportedSitesView(
    filename: String,
    site: String,
    latestUpdate: String,
    releaseDate: String
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        ListItem(
            overlineContent = {
                Text(filename)
            },
            headlineContent = {
                Text(site)
            },
            supportingContent = {
                Text(stringResource(R.string.latest_update, latestUpdate))
            },
            trailingContent = {
                Text(stringResource(R.string.release_date, releaseDate))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportedSitesView_preview() {
    MaterialTheme {
        ImportedSitesView(
            filename = "sample.org.zip",
            site = "sample.org",
            latestUpdate = "Yesterday",
            releaseDate = "Monday"
        )
    }
}
