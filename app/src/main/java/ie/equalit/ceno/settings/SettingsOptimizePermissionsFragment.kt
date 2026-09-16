package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.ui.settings.OptimizePermissionsScreen
import ie.equalit.ceno.ui.theme.DefaultThemeManager
import ie.equalit.ceno.ui.theme.ThemeManager
import ie.equalit.ceno.ui.viewModels.SettingsViewModel
import mozilla.components.support.ktx.android.content.getColorFromAttr
import kotlin.text.Typography.amp

class SettingsOptimizePermissionsFragment: Fragment() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    OptimizePermissionsScreen(settingsViewModel)
                }
            }
        }
    }
}

@Composable
private fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}
