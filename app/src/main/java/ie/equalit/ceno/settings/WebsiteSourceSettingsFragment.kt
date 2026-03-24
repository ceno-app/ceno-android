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
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreference

class WebsiteSourceSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sources_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_website_sources)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
        }

        setupSettings()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            findNavController().popBackStack()
        }
        callback.isEnabled = true
        showWarningMessageIfNeeded(key = null)
    }

    private fun setupSettings() {

        if (CenoSettings.isStatusUpdateRequired(requireContext())) {
            /* Ouinet status not yet updated */
            /* Grey out all Ceno related options */
            setPreference(getPreference(R.string.pref_key_ceno_sources_origin), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_private), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_public), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_shared), false)
            /* Fetch ouinet status */
            CenoSettings.ouinetClientRequest(
                requireContext(),
                viewLifecycleOwner.lifecycleScope,
                OuinetKey.API_STATUS)
        } else {
            /* Enable Ceno related options */
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_origin),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.ORIGIN_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_private),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.PROXY_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_public),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.INJECTOR_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_shared),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.DISTRIBUTED_CACHE)
            )
        }
    }

    private fun getChangeListenerForCenoSetting(key: OuinetKey): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { _, newValue ->
            val value = if (newValue == true) {
                OuinetValue.ENABLE
            } else {
                OuinetValue.DISABLE
            }
            CenoSettings.ouinetClientRequest(
                requireContext(),
                viewLifecycleOwner.lifecycleScope,
                key,
                value)
            showWarningMessageIfNeeded(newValue as Boolean, key)
            true
        }
    }

    private fun showWarningMessageIfNeeded(newValue: Boolean = false, key: OuinetKey?) {
        var private = (getPreference(R.string.pref_key_ceno_sources_private) as CheckBoxPreference).isChecked
        var origin = (getPreference(R.string.pref_key_ceno_sources_origin) as CheckBoxPreference).isChecked
        var public = (getPreference(R.string.pref_key_ceno_sources_public) as CheckBoxPreference).isChecked
        var shared = (getPreference(R.string.pref_key_ceno_sources_shared) as CheckBoxPreference).isChecked

        when(key) {
            OuinetKey.ORIGIN_ACCESS -> origin = newValue
            OuinetKey.PROXY_ACCESS -> private = newValue
            OuinetKey.INJECTOR_ACCESS -> public = newValue
            OuinetKey.DISTRIBUTED_CACHE -> shared = newValue
            else -> {}
        }
        var isWarningVisible = false
        var warningMessage: String = ""
        //if all are disabled
        if (!private && !public && !origin && !shared) {
            isWarningVisible= true
            warningMessage = getString(R.string.warning_all_website_sources_disabled)
        }
        if (!public && !origin && !shared) {
            //if all public sources are disabled
            isWarningVisible = true
            warningMessage = getString(R.string.warning_public_website_sources_disabled)
        }
        //if private source is disabled
        if(!private) {
            isWarningVisible = true
            warningMessage = getString(R.string.warning_personal_website_sources_disabled)
        }
        val pref = getPreference(R.string.pref_key_website_sources_warning)
        pref?.isVisible = isWarningVisible
        pref?.summary = warningMessage
    }

    private fun setPreference(
        pref: Preference?,
        enabled: Boolean,
        changeListener: Preference.OnPreferenceChangeListener? = null,
        clickListener: Preference.OnPreferenceClickListener? = null
    ) {
        pref?.let {
            it.isEnabled = enabled
            it.shouldDisableView = !enabled
            it.onPreferenceChangeListener = changeListener
            it.onPreferenceClickListener = clickListener
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "PrivacySettingsFragment"
    }
}