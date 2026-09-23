package ie.equalit.ceno.ui.settings

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.preference.PreferenceManager
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ui.theme.ThemeUtils
import ie.equalit.ceno.ui.viewModels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val isNotificationsEnabled by settingsViewModel.notificationsPermissionStatus
        .collectAsStateWithLifecycle()

    val isAccessLocalNetworkEnabled by settingsViewModel.accessLocalNetworkPermissionStatus
        .collectAsStateWithLifecycle()

    val isIgnoringBatteryOptimization by settingsViewModel.isIgnoringBatteryOptimization
        .collectAsStateWithLifecycle()

    var openLinksInAppsCheck by remember {
        mutableStateOf(
            prefs.getBoolean(
                context.getPreferenceKey(R.string.pref_key_launch_external_app),
                true
            )
        )
    }

    var warnBeforeOpeningLinksFromOtherAppsCheck by remember {
        mutableStateOf(
            prefs.getBoolean(
                context.getPreferenceKey(R.string.pref_key_verify_external_url),
                false
            )
        )
    }

    val activity = LocalActivity.current

    val isDarkTheme = ThemeUtils.isNightMode()

    val switchCheckColor = colorResource(R.color.accent)

    val switchTrackColor = if (!isDarkTheme) {
        colorResource(R.color.ceno_blue_200)
    } else {
        colorResource(R.color.ceno_blue_800)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.checkPermissions(context)
    }

    Column(
        modifier
            .padding(),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_allow_notifications)) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (isNotificationsEnabled) R.string.status_enabled
                            else R.string.status_disabled
                        )
                    )
                },
                modifier = Modifier.clickable(
                    onClick = {
                        settingsViewModel.notificationsPermissionsOpt(
                            activity ?: return@clickable,
                        )
                    }
                ),
                colors = ListItemDefaults.colors(
                    containerColor = colorResource(R.color.ceno_home_background)
                )
            )
        }

        ListItem(
            headlineContent = { Text(stringResource(R.string.preferences_disable_battery_opt)) },
            supportingContent = {
                Text(
                    stringResource(
                        if (isIgnoringBatteryOptimization) R.string.status_disabled
                        else R.string.status_enabled
                    )
                )
            },
            modifier = Modifier.clickable(
                onClick = {
                    settingsViewModel.disableBatteryOpt(
                        activity ?: return@clickable,
                    )
                }
            ),
            colors = ListItemDefaults.colors(
                containerColor = colorResource(R.color.ceno_home_background)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN || LocalInspectionMode.current) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_access_local_network)) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (isAccessLocalNetworkEnabled) R.string.status_enabled
                            else R.string.status_disabled
                        )
                    )
                },
                modifier = Modifier.clickable(
                    onClick = {
                        settingsViewModel.accessLocalNetworkPermissionsOpt(
                            activity ?: return@clickable,
                        )
                    }
                ),
                colors = ListItemDefaults.colors(
                    containerColor = colorResource(R.color.ceno_home_background)
                )
            )
        }

        ListItem(
            headlineContent = { Text(stringResource(R.string.open_links_in_apps)) },
            modifier = Modifier.clickable(
                onClick = {
                    openLinksInAppsCheck = !openLinksInAppsCheck
                    prefs.edit {
                        putBoolean(
                            context.getPreferenceKey(R.string.pref_key_launch_external_app),
                            openLinksInAppsCheck
                        )
                    }
                }
            ),
            trailingContent = {
                Switch(
                    checked = openLinksInAppsCheck,
                    onCheckedChange = {
                        openLinksInAppsCheck = it
                        prefs.edit {
                            putBoolean(
                                context.getPreferenceKey(R.string.pref_key_launch_external_app),
                                it
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = switchCheckColor,
                        checkedTrackColor = switchTrackColor
                    )
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = colorResource(R.color.ceno_home_background)
            )
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.preferences_verify_external_url)) },
            modifier = Modifier.clickable(
                onClick = {
                    warnBeforeOpeningLinksFromOtherAppsCheck =
                        !warnBeforeOpeningLinksFromOtherAppsCheck
                    prefs.edit {
                        putBoolean(
                            context.getPreferenceKey(R.string.pref_key_verify_external_url),
                            warnBeforeOpeningLinksFromOtherAppsCheck
                        )
                    }
                }
            ),
            trailingContent = {
                Switch(
                    checked = warnBeforeOpeningLinksFromOtherAppsCheck,
                    onCheckedChange = {
                        warnBeforeOpeningLinksFromOtherAppsCheck = it
                        prefs.edit {
                            putBoolean(
                                context.getPreferenceKey(R.string.pref_key_verify_external_url),
                                it
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = switchCheckColor,
                        checkedTrackColor = switchTrackColor
                    )
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = colorResource(R.color.ceno_home_background)
            )
        )
    }
}


@Preview
@Composable
private fun PermissionsScreen_Preview() {
    MaterialTheme {
        PermissionsScreen(
            settingsViewModel = remember { SettingsViewModel() }
        )
    }
}