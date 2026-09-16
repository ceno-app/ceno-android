package ie.equalit.ceno.ui.settings

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.viewModels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizePermissionsScreen(
    settingsViewModel: SettingsViewModel
) {
    val isNotificationsEnabled by settingsViewModel.notificationsPermissionStatus
        .collectAsStateWithLifecycle()

    val isAccessLocalNetworkEnabled by settingsViewModel.accessLocalNetworkPermissionStatus
        .collectAsStateWithLifecycle()

    val isIgnoringBatteryOptimization by settingsViewModel.isIgnoringBatteryOptimization
        .collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = LocalActivity.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.checkPermissions(context)
    }

    Column( Modifier.padding(), ) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_allow_notifications)) },
                supportingContent = { Text(stringResource(
                    if(isNotificationsEnabled) R.string.status_enabled
                    else R.string.status_disabled
                )) },
                modifier = Modifier.clickable(
                    onClick = {
                        settingsViewModel.notificationsPermissionsOpt(
                            activity ?: return@clickable,
                        )
                    }
                )
            )
        }

        ListItem(
            headlineContent = { Text(stringResource(R.string.preferences_disable_battery_opt)) },
            supportingContent = { Text(stringResource(
                if(isIgnoringBatteryOptimization) R.string.status_disabled
                else R.string.status_enabled
            )) },
            modifier = Modifier.clickable(
                onClick = {
                    settingsViewModel.disableBatteryOpt(
                        activity ?: return@clickable,
                    )
                }
            )
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN || LocalInspectionMode.current) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_access_local_network)) },
                supportingContent = { Text(stringResource(
                    if(isAccessLocalNetworkEnabled) R.string.status_enabled
                    else R.string.status_disabled
                )) },
                modifier = Modifier.clickable(
                    onClick = {
                        settingsViewModel.accessLocalNetworkPermissionsOpt(
                            activity ?: return@clickable,
                        )
                    }
                )
            )
        }
    }
}


@Preview
@Composable
private fun OptimizePermissionsScreen_Preview() {
    MaterialTheme {
        OptimizePermissionsScreen(
            remember{ SettingsViewModel() }
        )
    }
}