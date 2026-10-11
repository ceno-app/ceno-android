package ie.equalit.ceno.ui.viewModels

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.unifiedpush.android.connector.UnifiedPush

class SettingsViewModel : ViewModel() {

    private val _notificationsPermissionStatus = MutableStateFlow(false)
    val notificationsPermissionStatus: StateFlow<Boolean> =
        _notificationsPermissionStatus.asStateFlow()

    private val _accessLocalNetworkPermissionStatus = MutableStateFlow(false)
    val accessLocalNetworkPermissionStatus: StateFlow<Boolean> =
        _accessLocalNetworkPermissionStatus.asStateFlow()

    private val _isIgnoringBatteryOptimization = MutableStateFlow(false)
    val isIgnoringBatteryOptimization: StateFlow<Boolean> =
        _isIgnoringBatteryOptimization.asStateFlow()

    fun checkNotifications(activity: ComponentActivity) {
        UnifiedPush.tryUseCurrentOrDefaultDistributor(activity) { success ->
            if (success) {
                // We have a distributor
                // Register your app to the distributor
                //                    UnifiedPush.register(activity, messageForDistributor, vapid)
                UnifiedPush.register(activity)
            }
        }

    }

    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _notificationsPermissionStatus.value = context.components.permissionHandler
                    .isAllowingPostNotifications()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                _accessLocalNetworkPermissionStatus.value = context.components.permissionHandler
                    .isAllowingNetworkAccess()
            }

            _isIgnoringBatteryOptimization.value = context.components.permissionHandler
                .isIgnoringBatteryOptimizations()
        }
    }

    fun requestPermissions(
        activity: ComponentActivity,
        fragment: Fragment,
    ) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissions = mutableListOf(Manifest.permission.POST_NOTIFICATIONS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    permissions.add(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
                activity.components.permissionHandler.multiplePermissionHandler(
                    fragment,
                    permissions.toTypedArray()
                )
            } else {
                /* This is NOT Android 13, just ask to disable battery optimization */
                activity.components.permissionHandler.requestBatteryOptimizationsOff(activity)
            }

        }
    }

    fun onPermissionsGranted(
        activity: ComponentActivity,
        permissions: Map<String, Boolean>,
    ) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permissions.containsKey(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                val isGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
                Settings.setAllowNotifications(activity, isGranted)
                activity.components.permissionHandler.requestBatteryOptimizationsOff(activity)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
                permissions.containsKey(Manifest.permission.ACCESS_LOCAL_NETWORK)
            ) {
                val isGranted = permissions[Manifest.permission.ACCESS_LOCAL_NETWORK] ?: true
                Settings.setAccessNetworkPermissionGranted(activity, isGranted)
            }
        }
    }

    fun disableBatteryOpt(activity: Activity) {
        viewModelScope.launch {
            Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.packageName)
                activity.startActivity(this)
            }
        }
    }

    fun notificationsPermissionsOpt(activity: Activity) {
        viewModelScope.launch {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.packageName)
                activity.startActivity(this)
            }
        }
    }

    fun accessLocalNetworkPermissionsOpt(activity: Activity) {
        viewModelScope.launch {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(this)
            }
        }
    }

    fun sendPushNotification(
        context: Context,
        url: String,
        userInput: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(userInput.toRequestBody())
                .build()

            try {
                client.newCall(request)
                    .execute()
                    .use { response ->
                        if (!response.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context, response.body.toString(), Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, response.body?.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, e.message, Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }
}