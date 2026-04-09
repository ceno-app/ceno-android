/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ie.equalit.ceno.R
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.ext.getPreference
import ie.equalit.ceno.ext.setSecureScreen
import ie.equalit.ceno.settings.utils.RadioButtonPreference
import ie.equalit.ceno.settings.utils.addToRadioGroup

class CustomizationSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.customization_preferences, rootKey)
        setupThemePreferences()
        setupClearCenoPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            findNavController().popBackStack()
        }
        callback.isEnabled = true
    }

    private fun setupThemePreferences() {
        val radioLightTheme = getPreference(R.string.pref_key_light_theme) as RadioButtonPreference
        val radioDarkTheme = getPreference(R.string.pref_key_dark_theme) as RadioButtonPreference
        val radioFollowDeviceTheme = getPreference(R.string.pref_key_follow_system) as RadioButtonPreference
        addToRadioGroup(
            radioLightTheme,
            radioDarkTheme,
            radioFollowDeviceTheme
        )
        radioDarkTheme.onClickListener {
            applySelectedTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }
        radioLightTheme.onClickListener {
            applySelectedTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }
        radioFollowDeviceTheme.onClickListener {
            applySelectedTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        val key = when (Settings.getAppTheme(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.string.pref_key_dark_theme
            AppCompatDelegate.MODE_NIGHT_NO -> R.string.pref_key_light_theme
            else -> R.string.pref_key_follow_system
        }
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
            putBoolean(getString(key), true)
        }
    }

    private fun setupClearCenoPreferences() {
        val radioPromptAlways = getPreference(R.string.pref_key_prompt) as RadioButtonPreference
        val radioClearCache = getPreference(R.string.pref_key_clear_cache_only) as RadioButtonPreference
        val radioClearCacheAndAppData = getPreference(R.string.pref_key_clear_cache_and_app_data) as RadioButtonPreference
        addToRadioGroup(
            radioPromptAlways,
            radioClearCache,
            radioClearCacheAndAppData
        )
        radioPromptAlways.onClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
                putString(getString(R.string.pref_key_clear_behavior), "0")
            }
        }
        radioClearCache.onClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
                putString(getString(R.string.pref_key_clear_behavior), "1")
            }
        }
        radioClearCacheAndAppData.onClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
                putString(getString(R.string.pref_key_clear_behavior), "2")
            }
        }
        val key = when (PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(getString(R.string.pref_key_clear_behavior), "0")) {
            "1" -> R.string.pref_key_clear_cache_only
            "2" -> R.string.pref_key_clear_cache_and_app_data
            else -> R.string.pref_key_prompt
        }
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
            putBoolean(getString(key), true)
        }
    }

    private fun applySelectedTheme(theme: Int) {
        Settings.setAppTheme(requireContext(), theme.toString())
        if (AppCompatDelegate.getDefaultNightMode() != theme) {
            AppCompatDelegate.setDefaultNightMode(theme)
            activity?.recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
        getActionBar().apply{
            show()
            setTitle(R.string.customization_settings)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
        }
    }

    private fun setupPreferences() {

        getPreference(R.string.pref_key_change_app_icon)?.let {
            it.onPreferenceClickListener = getClickListenerForChangeAppIcon()
        }
        getPreference(R.string.pref_key_secure_screen)?.let {
            it.onPreferenceChangeListener = getChangeListenerForSecureScreen()
        }
        getPreference(R.string.pref_key_secure_screen_personal)?.let {
            it.onPreferenceChangeListener = getChangeListenerForSecureScreenPersonal()
            it.isEnabled = Settings.secureScreen(requireContext())
        }
    }

    private fun getClickListenerForChangeAppIcon(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_customizationSettingsFragment_to_changeIconFragment
            )
            getActionBar().setTitle(R.string.preferences_change_app_icon)
            true
        }
    }

    private fun getChangeListenerForSecureScreen(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { _, newValue ->
            val isEnabled = if (newValue == true)
                    (activity as BrowserActivity).browsingModeManager.mode.isPersonal
                else
                    false
            activity?.window?.setSecureScreen(isEnabled)
            getPreference(R.string.pref_key_secure_screen_personal)?.let {
                it.isEnabled = newValue as Boolean
            }
            true
        }
    }

    private fun getChangeListenerForSecureScreenPersonal(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { _, newValue ->
            context?.let {
                if(Settings.secureScreen(it)) {
                    if (newValue as Boolean)
                        activity?.window?.setSecureScreen((activity as BrowserActivity).browsingModeManager.mode.isPersonal)
                    else
                        activity?.window?.setSecureScreen(true)
                }
            }
            true
        }
    }
    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "CustomizationSettingsFragment"
    }
}
