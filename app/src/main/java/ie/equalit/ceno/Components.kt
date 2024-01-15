/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import mozilla.components.feature.autofill.AutofillConfiguration
import ie.equalit.ceno.autofill.AutofillConfirmActivity
import ie.equalit.ceno.autofill.AutofillSearchActivity
import ie.equalit.ceno.autofill.AutofillUnlockActivity
import ie.equalit.ceno.components.Analytics
import ie.equalit.ceno.components.Core
import ie.equalit.ceno.components.Ouinet
import ie.equalit.ceno.components.PermissionHandler
import ie.equalit.ceno.components.Services
import ie.equalit.ceno.components.UseCases
import ie.equalit.ceno.components.Utilities
import ie.equalit.ceno.components.WebExtensionPort
import ie.equalit.ceno.components.ceno.AppStore
import ie.equalit.ceno.components.ceno.appstate.AppState
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.utils.CenoPreferences
import mozilla.components.support.base.android.NotificationsDelegate

/**
 * Provides access to all components.
 */
class Components(private val context: Context) {
    val core by lazy { Core(context) }
    val useCases by lazy {
        UseCases(
            context,
            core.engine,
            core.store,
            core.shortcutManager,
            core.cenoTopSitesStorage
        )
    }

    val analytics by lazy { Analytics(context) }
    val utils by lazy {
        Utilities(
            context,
            core.store,
            useCases.sessionUseCases,
            useCases.searchUseCases,
            useCases.tabsUseCases,
            useCases.customTabsUseCases
        )
    }
    val services by lazy { Services(context) }
    /* CENO F-Droid: Do not use firebase push */
    //val push by lazy { Push(context, analytics.crashReporter) }

    val autofillConfiguration by lazy {
        /* CENO: Support older versions of Android, which don't have Autofill activities */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AutofillConfiguration(
                storage = core.loginsStorage,
                publicSuffixList = utils.publicSuffixList,
                unlockActivity = AutofillUnlockActivity::class.java,
                confirmActivity = AutofillConfirmActivity::class.java,
                searchActivity = AutofillSearchActivity::class.java,
                applicationName = context.getString(R.string.app_name),
                httpClient = core.client
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }


    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    val notificationsDelegate: NotificationsDelegate by lazy {
        NotificationsDelegate(
            notificationManagerCompat,
        )
    }

    /* CENO: Allow access to CENO SharedPreference wrapper through components*/
    val cenoPreferences by lazy { CenoPreferences(context) }

    /* CENO: Initialize AppStore with cached top sites */
    val appStore by lazy {
        AppStore(
            initialState = AppState(
                topSites = core.cenoTopSitesStorage.cachedTopSites.sort(),
                showCenoModeItem = cenoPreferences.showCenoModeItem,
                showThanksCard = cenoPreferences.showThanksCard
            )
        )
    }
    val ouinet by lazy { Ouinet(context) }
    val permissionHandler by lazy { PermissionHandler(context) }

    val webExtensionPort by lazy { WebExtensionPort(context) }
}
