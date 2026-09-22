package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.settings.PermissionsScreen
import ie.equalit.ceno.ui.viewModels.SettingsViewModel

class SettingsOptimizePermissionsFragment : Fragment() {
    private val settingsViewModel: SettingsViewModel by viewModels()


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                val DarkColorScheme = darkColorScheme(
                    background = colorResource(R.color.ceno_home_background),
                )

                val LightColorScheme = lightColorScheme(
                    background = colorResource(R.color.ceno_home_background),
                )
                MaterialTheme(
                    colorScheme = if (isNightMode()) DarkColorScheme else LightColorScheme,
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        stringResource(R.string.optimize_permissions),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.mozac_ic_back),
                                            stringResource(R.string.back_button_description)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = colorResource(R.color.ceno_action_bar)
                                ),
                                modifier = Modifier.shadow(elevation = 8.dp)
                            )
                        }
                    ) { innerPadding ->
                        PermissionsScreen(
                            modifier = Modifier
                                .background(colorResource(R.color.ceno_home_background))
                                .fillMaxSize()
                                .padding(innerPadding),
                            settingsViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Hide the parent toolbar when this fragment is visible
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        // Show the parent toolbar again when leaving this fragment
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
}

@Composable
private fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}
