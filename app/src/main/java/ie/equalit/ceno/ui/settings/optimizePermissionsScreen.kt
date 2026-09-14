package ie.equalit.ceno.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ie.equalit.ceno.R

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun OptimizePermissionsScreen() {
    Column( Modifier.padding(), ) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.preferences_allow_notifications)) },
            supportingContent = { Text(stringResource(R.string.status_enabled)) },
            modifier = Modifier.clickable(
                onClick = {
                    TODO()
                }
            )
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.preferences_disable_battery_opt)) },
            supportingContent = { Text(stringResource(R.string.status_enabled)) },
            modifier = Modifier.clickable(
                onClick = {
                    TODO()
                }
            )
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN || LocalInspectionMode.current) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_access_local_network)) },
                supportingContent = { Text(stringResource(R.string.status_enabled)) },
                modifier = Modifier.clickable(
                    onClick = {
                        TODO()
                    }
                )
            )
        }
    }
}


