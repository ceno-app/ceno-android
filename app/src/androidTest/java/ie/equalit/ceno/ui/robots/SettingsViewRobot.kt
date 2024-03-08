/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package ie.equalit.ceno.ui.robots

import android.view.KeyEvent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.assertIsChecked
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.CoreMatchers.allOf

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {
    fun verifySettingsViewExists() = assertSettingsView()

    fun verifyNavigateUp(): Unit = assertNavigateUpButton()

    fun verifyGeneralHeading(): ViewInteraction = assertGeneralHeading()
    fun verifyTrackingProtectionButton(): ViewInteraction = assertTrackingProtectionButton()
    fun verifyTrackingProtectionSummary(): ViewInteraction = assertTrackingProtectionSummary()
    fun verifySearchButton(): ViewInteraction = assertSearchButton()
    fun verifySearchSummary(): ViewInteraction = assertSearchSummary()
    fun verifyCustomizationButton(): ViewInteraction = assertCustomizationButton()
    fun verifyCustomizationSummary(): ViewInteraction = assertCustomizationButton()
    fun verifyOpenLinksInApps() = assertOpenLinksInApps()
    fun verifyMakeDefaultBrowserButton() = assertMakeDefaultBrowserButton()
    fun verifyAutofillAppsButton() = assertAutofillAppsButton()
    fun verifyAutofillAppsSummary() = assertAutofillAppsSummary()
    fun verifyAddOnsButton() = assertAddOnsButton()
    fun verifyDeleteBrowsingData(): ViewInteraction = assertDeleteBrowsingData()
    fun verifyDisableBatteryOptimization(): Unit = assertDisableBatteryOptimizationButton()
    fun verifyShowOnboarding(): ViewInteraction = assertShowOnboarding()
    fun verifyCrashReportingButton() = assertCrashReportingButton()

    fun verifyDataHeading(): ViewInteraction = assertDataHeading()
    fun verifyLocalCacheDisplay(): ViewInteraction = assertLocalCacheDisplay()
    fun verifyLocalCacheDefaultValue(): ViewInteraction = assertLocalCacheDefaultValue()
    fun verifyContentsSharedButton(): ViewInteraction = assertContentsSharedButton()
    fun verifyContentsSharedDefaultValue(): ViewInteraction = assertContentsSharedDefaultValue()
    fun verifyClearCachedContentButton(): ViewInteraction = assertClearCachedContentButton()
    fun verifyClearCachedContentSummary(): ViewInteraction = assertClearCachedContentSummary()

    fun verifyDeveloperToolsHeading() = assertDeveloperToolsHeading()
    fun verifyRemoteDebugging() = assertRemoteDebugging()
    fun verifyCustomAddonCollectionButton() = assertCustomAddonCollectionButton()
    fun verifyCenoNetworkDetailsButton(): ViewInteraction = assertCenoNetworkDetailsButton()
    fun verifyCenoNetworkDetailsSummary(): ViewInteraction = assertCenoNetworkDetailsSummary()
    fun verifyEnableLogFile(): ViewInteraction = assertEnableLogFile()
    fun verifyWebsiteSourcesButton(): ViewInteraction = assertWebsiteSourcesButton()
    fun verifyWebsiteSourcesSummary(): ViewInteraction = assertWebsiteSourcesSummary()

    fun verifyAboutHeading() = assertAboutHeading()

    fun verifyCenoBrowserServiceDisplay(): ViewInteraction = assertCenoBrowserServiceDisplay()
    fun verifyGeckoviewVersionDisplay(): ViewInteraction = assertGeckoviewVersionDisplay()
    fun verifyOuinetVersionDisplay(): ViewInteraction = assertOuinetVersionDisplay()
    fun verifyAboutEqualitieButton() = assertAboutEqualitieButton()
    fun verifySettingsRecyclerViewToExist() = waitForSettingsRecyclerViewToExist()

    fun clickCustomAddonCollectionButton() = customAddonCollectionButton().click()
    fun verifyCustomAddonCollectionPanelExist() = assertCustomAddonCollectionPanel()

    fun clickOpenLinksInApps() = openLinksInAppsToggle().click()

    fun clickEnableLogFile() = enableLogFile().click()

    fun clickDownRecyclerView(count: Int) {
        for (i in 1..count) {
            recycleView().perform(ViewActions.pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
        }
    }

    // toggleRemoteDebugging does not yet verify that the debug service is started
    // server runs on port 6000
    fun toggleRemoteDebuggingOn() : ViewInteraction {
        //onView(withText("OFF")).check(matches(isDisplayed()))
        remoteDebuggingToggle().assertIsChecked(false)
        remoteDebuggingToggle().click()
        return remoteDebuggingToggle().assertIsChecked(true)
        //return onView(withText("ON")).check(matches(isDisplayed()))
    }

    fun toggleRemoteDebuggingOff() : ViewInteraction {
        remoteDebuggingToggle().assertIsChecked(true)
        remoteDebuggingToggle().click()
        return remoteDebuggingToggle().assertIsChecked(false)
    }

    class Transition {
        /*
        fun openSettingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit):
                SettingsViewPrivacyRobot.Transition {
            privacyButton().click()
            SettingsViewPrivacyRobot().interact()
            return SettingsViewPrivacyRobot.Transition()
        }
        */

        fun openSettingsViewSearch(interact: SettingsViewSearchRobot.() -> Unit):
                SettingsViewSearchRobot.Transition {
            searchButton().click()
            SettingsViewSearchRobot().interact()
            return SettingsViewSearchRobot.Transition()
        }

        fun makeDefaultBrowser(interact: ExternalAppsRobot.() -> Unit):
                ExternalAppsRobot.Transition {
            makeDefaultBrowserButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun clickAutofillAppsButton(interact: ExternalAppsRobot.() -> Unit):
                ExternalAppsRobot.Transition {
            autofillAppsButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun openAboutReferenceBrowser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            aboutEqualitieButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openSettingsViewSources(interact: SettingsViewSourcesRobot.() -> Unit):
                SettingsViewSourcesRobot.Transition {
            websiteSourcesButton().click()
            SettingsViewSourcesRobot().interact()
            return SettingsViewSourcesRobot.Transition()
        }

        fun openSettingsViewNetworkDetails(interact: SettingsViewNetworkDetailsRobot.() -> Unit):
                SettingsViewNetworkDetailsRobot.Transition {
            cenoNetworkDetailsButton().click()
            SettingsViewNetworkDetailsRobot().interact()
            return SettingsViewNetworkDetailsRobot.Transition()
        }

        fun goBack(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            val navigateUpButton = mDevice.findObject(UiSelector().descriptionContains("Navigate up"))
            navigateUpButton.clickAndWaitForNewWindow()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun waitForSettingsRecyclerViewToExist() {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/recycler_view"))
        .waitForExists(
            waitingTime,
        )
}

private fun assertSettingsView() {
    // verify that we are in the correct settings view
    Espresso.onView(withText(R.string.settings))
    Espresso.onView(withText(R.string.preferences_about_page))
}

private fun recycleView() = onView(withId(R.id.recycler_view))
private fun generalHeading() = onView(withText(R.string.general_category))
private fun trackingProtectionButton() = onView(withText(R.string.tracker_category))
private fun trackingProtectionSummary() = onView(withText(R.string.preferences_privacy_summary))
private fun searchButton() = onView(withText(R.string.set_default_search_engine))
private fun searchSummary() = onView(withText("DuckDuckGo selected"))
//TODO: check for different search engines when they are set
private fun customizationButton() = onView(withText(R.string.preferences_customization))
private fun customizationSummary() = onView(withText(R.string.preferences_customization_summary))
private fun openLinksInAppsToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.open_links_in_apps))))
private fun makeDefaultBrowserButton() = Espresso.onView(withText(R.string.preferences_make_default_browser))
private fun autofillAppsButton() = onView(withText("Autofill apps"))
private fun autofillAppsSummary() = onView(withText("Autofill logins and passwords in other apps"))

private fun addOnsButton() = onView(withText(R.string.preferences_add_ons))
private fun deleteBrowsingDataButton() =  onView(withText(R.string.preferences_delete_browsing_data))
private fun showOnboardingToggle() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_show_onboarding))))

private fun crashReportingButton() = onView(withText(R.string.preferences_allow_crash_reporting))

private fun sourcesHeading() = onView(withText(R.string.ceno_sources_category))

private fun dataHeading() = onView(withText(R.string.ceno_data_category))
private fun localCacheDisplay() = onView(withText(R.string.preferences_ceno_cache_size))
private fun localCacheDefaultValue() = onView(withText("O B"))
private fun contentsSharedButton() = onView(withText(R.string.preferences_ceno_groups_count))
private fun contentsSharedDefaultValue() = onView(withText("0 sites"))
private fun clearCachedContentButton() = onView(withText(R.string.preferences_clear_ceno_cache))
private fun clearCachedContentSummary() = onView(withText(R.string.preferences_clear_ceno_cache_summary))

private fun developerToolsHeading() = Espresso.onView(withText(R.string.developer_tools_category))
private fun remoteDebuggingToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_remote_debugging))))
private fun customAddonCollectionButton() = onView(withText("Custom Add-on collection"))
private fun cenoNetworkDetailsButton() = onView(withText(R.string.preferences_ceno_network_config))
private fun cenoNetworkDetailsSummary() = onView(withText(R.string.preferences_ceno_network_config_summary))
private fun enableLogFile() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_ceno_enable_log))))

private fun websiteSourcesButton() = onView(withText(R.string.preferences_ceno_website_sources))
private fun websiteSourcesSummary() = onView(withText(R.string.preferences_website_sources_summary))

private fun aboutHeading() = Espresso.onView(withText(R.string.about_category))
private fun cenoBrowserServiceDisplay() = onView(withText(R.string.ceno_notification_title))
private fun geckoviewVersionDisplay() = onView(withText(R.string.preferences_about_geckoview))
private fun ouinetVersionDisplay() = onView(withText(R.string.preferences_about_ouinet))
private fun aboutEqualitieButton() = Espresso.onView(withText(R.string.preferences_about_page))

private fun assertGeneralHeading() = generalHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionButton() = trackingProtectionButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionSummary() = trackingProtectionSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSearchButton() = searchButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSearchSummary() = searchSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomizationButton() = customizationButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomizationSummary() = customizationSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertNavigateUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertOpenLinksInApps() = openLinksInAppsToggle()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAutofillAppsButton() = autofillAppsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAutofillAppsSummary() = autofillAppsSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddOnsButton() = addOnsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDeleteBrowsingData() = deleteBrowsingDataButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDisableBatteryOptimizationButton() {
    mDevice.wait(Until.findObject(By.text("Disable Battery Optimization")), waitingTime)
}
private fun assertShowOnboarding() = showOnboardingToggle()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCrashReportingButton() = crashReportingButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertDataHeading() = dataHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertLocalCacheDisplay() = localCacheDisplay()
private fun assertLocalCacheDefaultValue() = localCacheDefaultValue()
private fun assertContentsSharedButton() = contentsSharedButton()
private fun assertContentsSharedDefaultValue() = contentsSharedDefaultValue()
private fun assertClearCachedContentButton() = clearCachedContentButton()
private fun assertClearCachedContentSummary() = clearCachedContentSummary()

private fun assertDeveloperToolsHeading() = developerToolsHeading()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRemoteDebugging() = remoteDebuggingToggle()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomAddonCollectionButton() = customAddonCollectionButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCenoNetworkDetailsButton() = cenoNetworkDetailsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCenoNetworkDetailsSummary() = cenoNetworkDetailsSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertEnableLogFile() = enableLogFile()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSourcesButton() = websiteSourcesButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSourcesSummary() = websiteSourcesSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertAboutHeading() { aboutHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}
private fun assertCenoBrowserServiceDisplay() = cenoBrowserServiceDisplay()
private fun assertGeckoviewVersionDisplay() = geckoviewVersionDisplay()
private fun assertOuinetVersionDisplay() = ouinetVersionDisplay()
private fun assertAboutEqualitieButton() = aboutEqualitieButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomAddonCollectionPanel() {
    mDevice.waitForIdle()
    mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
        .waitForExists(waitingTime)
    onView(
        allOf(
            withText(R.string.preferences_customize_amo_collection),
            isDescendantOfA(withId(R.id.title_template)),
        ),
    ).check(matches(isCompletelyDisplayed()))
}
