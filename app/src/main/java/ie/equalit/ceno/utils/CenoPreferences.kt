/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.ext.getPreferenceKey
import mozilla.components.support.ktx.android.content.PreferencesHolder
import mozilla.components.support.ktx.android.content.booleanPreference
import mozilla.components.support.ktx.android.content.intPreference

/**
 * A simple wrapper for SharedPreferences that makes reading preference a little bit easier.
 * @param appContext Reference to application context.
 */
@Suppress("LargeClass", "TooManyFunctions")
class CenoPreferences(private val appContext: Context) : PreferencesHolder {

    companion object {
        const val CENO_PREFERENCES = "ceno_preferences"

        // The maximum number of top sites to display.
        const val TOP_SITES_MAX_COUNT = 16

        /**
         * Only fetch top sites from the [ContileTopSitesProvider] when the number of default and
         * pinned sites are below this maximum threshold.
         */
        const val TOP_SITES_PROVIDER_MAX_THRESHOLD = 8
    }

    override val preferences: SharedPreferences =
        appContext.getSharedPreferences(CENO_PREFERENCES, MODE_PRIVATE)


    var defaultTopSitesAdded by booleanPreference(
        appContext.getPreferenceKey(R.string.default_top_sites_added),
        default = false
    )

    val topSitesMaxLimit by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_top_sites_max_limit),
        default = TOP_SITES_MAX_COUNT
    )

    var sharedPrefsReload by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_shared_prefs_reload),
        default = false
    )

    var sharedPrefsUpdate by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_shared_prefs_update),
        default = false
    )

    var showBridgeAnnouncementCard by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_bridge_announcement),
        default = true
    )

    var isBridgeCardExpanded by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_bridge_card_expanded),
        default = true
    )

    var isModeCardExpanded by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_mode_card_expanded),
        default = false
    )

    var nextTooltip by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_ceno_tour),
        default = 1
    )

    var showMetricsConsentDialog by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_show_metrics_consent_dialog),
        default = true
    )

    var isSecureScreenPersonalOnly by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_secure_screen_personal),
        true
    )

    /**
     * Save browsing mode in preferences
     * From Fenix
     */
    var lastKnownBrowsingMode: BrowsingMode = BrowsingMode.Normal
        get() {
            val lastKnownModeWasPersonal = preferences.getBoolean(
                appContext.getPreferenceKey(R.string.pref_last_known_browsing_mode_personal),
                false,
            )

            return if (lastKnownModeWasPersonal) {
                BrowsingMode.Personal
            } else {
                BrowsingMode.Normal
            }
        }
        set(value) {
            val lastKnownModeWasPersonal = (value == BrowsingMode.Personal)

            preferences.edit {
                putBoolean(
                    appContext.getPreferenceKey(R.string.pref_last_known_browsing_mode_personal),
                    lastKnownModeWasPersonal,
                )
            }

            field = value
        }
}
