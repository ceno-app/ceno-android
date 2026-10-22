package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImportedSitesView
import ie.equalit.ceno.ui.ouicrawl.CenoBundleImporterView
import ie.equalit.ceno.ui.theme.ThemeUtils.isNightMode
import ie.equalit.ceno.ui.viewModels.CenoBundleImporterViewModel

class ImportedCenoBundleFragment : Fragment() {

    private val cenoBundleImporterViewModel = CenoBundleImporterViewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
//            setViewCompositionStrategy(
//                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
//            )

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
                                        stringResource(R.string.imported_ceno_bundles),
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
                        CenoBundleImportedSitesView(
                            modifier = Modifier
                                .padding(innerPadding),
                            cenoBundleImporterViewModel = cenoBundleImporterViewModel
                        )
                    }
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        // Hide the parent toolbar when this fragment is visible
//        (activity as? AppCompatActivity)?.supportActionBar?.hide()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Show the parent toolbar again when leaving this fragment
//        (activity as? AppCompatActivity)?.supportActionBar?.show()
//    }
}