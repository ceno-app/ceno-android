/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceCategory
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.SettingsFragment.Companion.DELAY_ONE_SECOND
import ie.equalit.ceno.settings.dialogs.ExtraBTBootstrapsDialog
import ie.equalit.ceno.settings.dialogs.WaitForOuinetRestartDialog
import ie.equalit.ceno.settings.utils.RadioButtonPreference
import ie.equalit.ceno.settings.utils.addToRadioGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import java.util.Locale

class NetworkSettingsFragment : PreferenceFragmentCompat() {

    // This variable stores a map of all the sources from local.properties
    private val btSourcesMap = mutableMapOf<String, String>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.network_detail_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            findNavController().popBackStack()
        }
        callback.isEnabled = true
    }

    private fun setupDnsPreferences() {
        val preferenceDohEnabled = getPreferenceCategory(R.string.pref_key_doh_enabled)
        val radioDnsPlain = getPreference(R.string.pref_key_dns_plain) as RadioButtonPreference
        val radioDnsHttps = getPreference(R.string.pref_key_dns_https) as RadioButtonPreference
        val radioDnsBoth = getPreference(R.string.pref_key_dns_both) as RadioButtonPreference
        addToRadioGroup(
            radioDnsPlain,
            radioDnsHttps,
            radioDnsBoth
        )
        if (requireComponents.ouinet.isDohDisabledForLocale()) {
            preferenceDohEnabled?.isEnabled = false
            radioDnsPlain.updateRadioValue(true)
            radioDnsHttps.updateRadioValue(false)
            radioDnsBoth.updateRadioValue(false)
        }
        radioDnsPlain.onClickListener {
            onClickListenerForDnsProtocols()
        }
        radioDnsHttps.onClickListener {
            onClickListenerForDnsProtocols()
        }
        radioDnsBoth.onClickListener {
            onClickListenerForDnsProtocols()
        }
    }

    override fun onResume() {
        super.onResume()

        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_network_config)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar)
                    .toDrawable()
            )
        }

        for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) btSourcesMap[
            Locale.Builder()
                .setLanguage("")
                .setRegion(entry[0])
                .build()
                .displayCountry
        ] = entry[1]
        setupPreferences()
    }

    private fun setupPreferences() {

        val preferenceAboutOuinetProtocol = getPreference(R.string.pref_key_about_ouinet_protocol)
        val preferenceReachabilityStatus =
            getPreference(R.string.pref_key_ouinet_reachability_status)
        val preferenceOuinetProxyEndpoint = getPreference(R.string.pref_key_ouinet_proxy_endpoint)
        val preferenceOuinetFrontendEndpoint =
            getPreference(R.string.pref_key_ouinet_frontend_endpoint)
        val preferenceLocalUdpEndpoint = getPreference(R.string.pref_key_ouinet_local_udp_endpoints)
        val preferenceExternalUdpEndpoint =
            getPreference(R.string.pref_key_ouinet_external_udp_endpoints)
        val preferencePublicUdpEndpoint =
            getPreference(R.string.pref_key_ouinet_public_udp_endpoints)
        val preferenceUpnpStatus = getPreference(R.string.pref_key_ouinet_upnp_status)
        val extraBootstrapBittorrentKey =
            requireContext().getPreferenceKey(R.string.pref_key_ouinet_extra_bittorrent_bootstraps)

        val preferenceExtraBitTorrentBootstrap =
            findPreference<Preference>(extraBootstrapBittorrentKey)
        preferenceExtraBitTorrentBootstrap?.onPreferenceClickListener =
            getClickListenerForExtraBitTorrentBootstraps()

        preferenceAboutOuinetProtocol?.summary =
            "${CenoSettings.getOuinetProtocol(requireContext())}"
        preferenceReachabilityStatus?.summary = CenoSettings.getReachabilityStatus(requireContext())
        preferenceOuinetProxyEndpoint?.summary =
            "${CenoSettings.getProxyEndpoint(requireContext())}"
        preferenceOuinetFrontendEndpoint?.summary =
            "${CenoSettings.getFrontendEndpoint(requireContext())}"
        preferenceLocalUdpEndpoint?.summary = CenoSettings.getLocalUdpEndpoint(requireContext())
            .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferenceExternalUdpEndpoint?.summary =
            CenoSettings.getExternalUdpEndpoint(requireContext())
                .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferencePublicUdpEndpoint?.summary = CenoSettings.getPublicUdpEndpoint(requireContext())
            .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferenceUpnpStatus?.summary = CenoSettings.getUpnpStatus(requireContext())
        preferenceExtraBitTorrentBootstrap?.summary = getBTPreferenceSummary()
        setupDnsPreferences()
    }

    private fun onClickListenerForDnsProtocols() {
        //restart ouinet for change to take effect
        val waitForOuinetRestartDialog = WaitForOuinetRestartDialog(
            requireContext(),
            getString(R.string.updating_doh_dialog_title)
        ).getDialog()
        waitForOuinetRestartDialog.show()
        var hasOuinetStopped = false
        requireComponents.ouinet.background.shutdown(false) {
            hasOuinetStopped = true
        }
        lifecycleScope.launch {
            while (!hasOuinetStopped) {
                delay(DELAY_ONE_SECOND)
            }
            requireComponents.ouinet.setConfig()
            requireComponents.ouinet.setBackground(requireContext())
            requireComponents.ouinet.background.startup {
                waitForOuinetRestartDialog.dismiss()
            }
        }
    }

    private fun getClickListenerForExtraBitTorrentBootstraps(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            val extraBTBootstrapsDialog = ExtraBTBootstrapsDialog(
                requireContext(),
                viewLifecycleOwner,
                btSourcesMap
            ) {
                getPreference(R.string.pref_key_ouinet_extra_bittorrent_bootstraps)?.summary =
                    getBTPreferenceSummary()
            }
            extraBTBootstrapsDialog.getDialog()
                .show()

            true
        }
    }

    private fun getPreference(key: Int): Preference? {
        val prefKey = requireContext().getPreferenceKey(key)
        return findPreference(prefKey)
    }

    private fun getBTPreferenceSummary(): String {
        var summary = ""

        CenoSettings.getLocalBTSources(requireContext())
            ?.forEach {
                summary = if (btSourcesMap.values.contains(it)) {
                    "$summary ${
                        btSourcesMap.entries.find { e -> e.value.trim() == it }?.key?.replace(
                            " ",
                            ""
                        )
                    }"
                } else {
                    "$summary $it"
                }
            }

        return when {
            summary.trim()
                .isEmpty() -> getString(R.string.bt_sources_none)

            else -> summary.trim()
                .replace(" ", ", ")
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "NetworkSettingsFragment"
    }
}
