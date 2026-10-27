package ie.equalit.ceno.ui.ouicrawl

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedSitesModal(
    site: CachedGroupAdapter.GroupItem,
    show: Boolean = false,
    onChildClickCallback: (String) -> Unit,
    onDismissCallback: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()

    if (show || LocalInspectionMode.current) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissCallback()
                    }
                }
                onDismissCallback()
            },
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.secondary,
                )
            },
            containerColor = colorResource(R.color.ceno_home_background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        site.name,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(site.children.filter{ it.url != site.name }) { child ->
                        HorizontalDivider(Modifier.padding())
                        SharedSitesModalComponent(
                            name = child.url
                        ) {
                            onDismissCallback()
                            onChildClickCallback(child.url)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedSitesModalComponent(
    name: String,
    onOpenCallback: () -> Unit
) {
    ListItem(
        headlineContent = {},
        supportingContent = { Text(name) },
        trailingContent = {
            TextButton(onClick = onOpenCallback) {
                Text(stringResource(R.string.shared_sites_open))
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = colorResource(R.color.ceno_home_background)
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun SharedSitesModalComponent_Preview() {
    MaterialTheme {
        SharedSitesModalComponent("sample site") {}
    }
}