/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.profilebackup

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentProfileBackupBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mozilla.components.support.base.log.logger.Logger
import org.equalitie.ouisync.session.close

@SuppressWarnings("TooManyFunctions", "LargeClass")
class ProfileBackupFragment : Fragment(R.layout.fragment_profile_backup) {

    private lateinit var controller: ProfileBackupController
    private var scope: CoroutineScope? = null

    private var _binding: FragmentProfileBackupBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBackupBinding.bind(view)
        controller = DefaultProfileBackupController()

        binding.enableSwitch.isChecked = Settings.isOuisyncEnabled(requireContext())

        getCheckboxes().iterator().forEach {
            it.onCheckListener = { _ ->
                updateButtons()
                updatePreference(it)
            }
        }

        getCheckboxes().iterator().forEach {
            it.isChecked = when (it.id) {
                R.id.customization_item -> Settings.backupCustomizations(requireContext())
                R.id.top_site_item -> Settings.backupTopSites(requireContext())
                else -> true
            }
        }

        binding.enableSwitch.setOnClickListener {
            if (Settings.isOuisyncEnabled(requireContext())){
                disableOuisync()
            }
            else {
                enableOuisyncDialog()
            }
        }

        binding.createProfile.setOnClickListener(getOnClickListenerForCreateProfile())
        binding.updateProfile.setOnClickListener(getOnClickListenerForUpdateProfile())
        binding.deleteProfile.setOnClickListener(getOnClickListenerForDeleteProfile())
        updateCheckboxes()
        viewLifecycleOwner.lifecycleScope.launch {
            requireComponents.ouisync.apply {
                updateButtons()
                loadProfile(openAndReadFromRepo())
            }
        }
    }

    private fun updatePreference(it: ProfileBackupItem) {
        when (it.id) {
            R.id.customization_item -> Settings.setBackupCustomizations(requireContext(), it.isChecked)
            R.id.top_site_item -> Settings.setBackupTopSites(requireContext(), it.isChecked)
            else -> return
        }
    }

    override fun onStart() {
        super.onStart()
        /*
        updateTopSitesCount()
        scope = requireComponents.core.store.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { state -> state.tabs.size }
                .distinctUntilChanged()
                .collect { _ -> updateTopSitesCount() }
        }
         */
    }

    override fun onResume() {
        super.onResume()
        //showToolbar(getString(R.string.preferences_delete_browsing_data))

        getCheckboxes().iterator().forEach {
            it.visibility = View.VISIBLE
        }
        //updateItemCounts()
    }

    private fun enableOuisyncDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.preference_enable_ouisync)
            setMessage(R.string.enable_ouisync_message)
            setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.onboarding_battery_button) { _, _ ->
                Settings.setOuisyncEnabled(requireContext(), true)
                updateCheckboxes()
                viewLifecycleOwner.lifecycleScope.launch {
                    requireComponents.ouisync.apply {
                        createSession()
                        updateButtons()
                    }
                }
            }
            setOnCancelListener {
                binding.enableSwitch.isChecked = false
            }
            create()
        }.show()
    }

    private fun disableOuisync() {
        Settings.setOuisyncEnabled(requireContext(), false)
        updateCheckboxes()
        viewLifecycleOwner.lifecycleScope.launch {
            requireComponents.ouisync.apply {
                updateButtons()
                session?.close()
            }
        }
    }

    private fun getPrefsForSync(context: Context) : String {
        val backupPrefs: MutableMap<String, *>? =
            PreferenceManager.getDefaultSharedPreferences(context)?.all
        val prefsIncluded: Array<String> =
            resources.getStringArray(R.array.prefs_included_in_backup)
        backupPrefs?.iterator()?.let {
            while (it.hasNext()) {
                if (!prefsIncluded.contains(it.next().key)) {
                    it.remove()
                }
            }
        }
        var prefsString = backupPrefs.toString().replace(", ", "\n")
        prefsString = prefsString.replace("{", "")
        prefsString = prefsString.replace("}", "")
        return prefsString
    }

    private fun importPrefsDialog(context : Context) {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.settings_restore_header)
            // TODO: add padding to text input
            val tokenInput = EditText(context)
            setView(tokenInput)
            setMessage(R.string.settings_restore_message)
            setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
            setPositiveButton(R.string.onboarding_battery_button) { _, _ ->
                var content: String?
                val token = tokenInput.getText().toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    if (token == "") {
                        val prefs = getPrefsForSync(context)
                        requireComponents.ouisync.apply {
                            createOrImportRepo(contentW = prefs)
                        }
                    }
                    else {
                        requireComponents.ouisync.apply {
                            createOrImportRepo(token = token)
                        }
                    }
                }
                /* TODO: need to check if repo is synced before trying to open files from it
                 */
                AlertDialog.Builder(requireContext()).apply {
                    val title = if (token == "")
                        R.string.preferences_created_profile_title
                    else
                        R.string.preferences_imported_profile_title
                    setTitle(title)
                    val shareToken = requireComponents.ouisync.writeToken?.value
                    binding.syncProfile.text = shareToken
                    setPositiveButton(R.string.onboarding_battery_button) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            requireComponents.ouisync.apply {
                                content = openAndReadFromRepo()
                                updateButtons()
                                loadProfile(content)
                            }
                        }
                        create()
                    }
                }.show()
            }
            create()
        }.show()
    }

    private fun updateCheckboxes() {
        val enabled =  Settings.isOuisyncEnabled(requireContext())

        getCheckboxes().iterator().forEach {
            it.isEnabled = enabled
            it.alpha =  if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
        }
    }

    private fun loadProfile(profileView : String?) {
        binding.profileSettingsView.text = profileView
        if (profileView == null) {
            binding.syncProfile.text = ""
            binding.createProfile.visibility = View.VISIBLE
            binding.deleteProfile.visibility = View.GONE
            binding.syncProfile.visibility = View.GONE
            binding.updateProfile.visibility = View.GONE
            binding.profileSettingsView.visibility = View.GONE
            binding.profileSettingsTitle.visibility = View.GONE
        }
        else {
            binding.syncProfile.text = requireComponents.ouisync.writeToken?.value
            binding.createProfile.visibility = View.GONE
            binding.deleteProfile.visibility = View.VISIBLE
            binding.syncProfile.visibility = View.VISIBLE
            binding.updateProfile.visibility = View.VISIBLE
            binding.profileSettingsView.visibility = View.VISIBLE
            binding.profileSettingsTitle.visibility = View.GONE
        }
    }
    private fun updateButtons() {
        val enabled =  Settings.isOuisyncEnabled(requireContext()) &&
                getCheckboxes().any { it.isChecked }
        binding.createProfile.isEnabled = enabled
        binding.createProfile.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
        binding.deleteProfile.isEnabled = enabled
        binding.deleteProfile.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
        binding.updateProfile.isEnabled = enabled
        binding.updateProfile.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    private fun askToImport() {
        context?.let {
            importPrefsDialog(it)
        }
    }

    private fun askToDelete() {
        context?.let {
            AlertDialog.Builder(it).apply {
                setTitle(R.string.preferences_delete_profile_title)
                setMessage(R.string.preferences_delete_profile_message)
                setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _ -> dialog.cancel() }
                setPositiveButton(R.string.onboarding_battery_button) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        requireComponents.ouisync.apply {
                            deleteRepository()
                            loadProfile(openAndReadFromRepo())
                        }
                    }
                }.create()
                show()
            }
        }
    }

    private fun updateSettings(context: Context) {
        var prefsString: String?
        var theme = -1
        viewLifecycleOwner.lifecycleScope.launch {
            requireComponents.ouisync.apply {
                prefsString =
                    openAndReadFromRepo()
            }
            prefsString?.split("\n")?.forEach {
                Logger.debug("string: $it")
                if (it != "") {
                    val kv: List<String> = it.split("=")
                    Logger.debug("key: ${kv[0]}, val: ${kv[1]}")
                    when (kv[0]) {
                        "pref_key_theme" -> {
                            theme = kv[1].toInt()
                            PreferenceManager.getDefaultSharedPreferences(context)
                                .edit() {
                                    putString(kv[0], kv[1])
                                }
                        }
                        "pref_key_clear_behavior",
                        "pref_key_default_search_engine",
                        "pref_key_backup_top_sites",
                        "pref_key_selected_app_icon" -> {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                .edit() {
                                    putString(kv[0], kv[1])
                                }
                        }

                        else -> {
                            if (kv[0] != "") {
                                PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit() {
                                        putBoolean(kv[0], kv[1].toBoolean())
                                    }
                            }
                        }
                    }
                }
            }
            if (AppCompatDelegate.getDefaultNightMode() != theme) {
                AppCompatDelegate.setDefaultNightMode(theme)
                activity?.recreate()
            }
        }
    }

    /*
    private fun backupSelected() {
        lifecycleScope.launch(IO) {
            getCheckboxes().mapIndexed { i, v ->
                if (v.isChecked) {
                    when (i) {
                        CUSTOMIZATIONS_INDEX -> controller.getPrefs()
                        TOP_SITES_INDEX -> controller.getTopSites()
                    }
                }
            }

            withContext(Main) {
                finishDeletion()
            }
        }
    }
    */

    override fun onPause() {
        super.onPause()
        //binding.progressBar.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        scope?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateItemCounts() {
        updateTopSitesCount()
    }

    private fun updateTopSitesCount(openTabs: Int = requireComponents.core.cenoTopSitesStorage.cachedTopSites.size) {
        binding.topSitesItem.apply {
            subtitleView.text = resources.getQuantityString(
                R.plurals.preferences_delete_browsing_data_tabs_subtitle,
                openTabs,
                openTabs
            )
            subtitleView.visibility = View.VISIBLE
        }
    }

    private fun getCheckboxes(): List<ProfileBackupItem> {
        return listOf(
            binding.customizationItem,
            binding.topSitesItem,
        )
    }

    private fun getOnClickListenerForCreateProfile(): View.OnClickListener {
        return View.OnClickListener {
            askToImport()
        }
    }

    private fun getOnClickListenerForDeleteProfile(): View.OnClickListener {
        return View.OnClickListener {
            askToDelete()
        }
    }

    private fun getOnClickListenerForUpdateProfile(): View.OnClickListener {
        return View.OnClickListener {
            updateSettings(requireContext())
        }
    }

    companion object {
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.6f

        private const val CUSTOMIZATIONS_INDEX = 0
        private const val TOP_SITES_INDEX = 1
    }
}
