package ie.equalit.ceno.ui.viewModels

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.Settings
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    fun requestPermissions(
        activity: ComponentActivity,
        fragment: Fragment,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = mutableListOf(Manifest.permission.POST_NOTIFICATIONS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                permissions.add(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
            activity.applicationContext.components.permissionHandler.multiplePermissionHandler(
                fragment,
                permissions.toTypedArray()
            )
        } else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            activity.applicationContext.components.permissionHandler.requestBatteryOptimizationsOff(activity)
        }
    }

    fun onPermissionsGranted(
        activity: ComponentActivity,
        permissions: Map<String, Boolean>,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            permissions.containsKey(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            val isGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
            Settings.setAllowNotifications(activity.applicationContext, isGranted)
            activity.applicationContext.components.permissionHandler.requestBatteryOptimizationsOff(activity)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            permissions.containsKey(Manifest.permission.ACCESS_LOCAL_NETWORK)
        ) {
            val isGranted = permissions[Manifest.permission.ACCESS_LOCAL_NETWORK] ?: true
            Settings.setAccessNetworkPermissionGranted(activity.applicationContext, isGranted)
        }
    }
}