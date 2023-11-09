/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("DEPRECATION")

package ie.equalit.ceno.ui

import android.os.Build
import androidx.core.net.toUri
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.helpers.TestHelper.scrollToElementByText
import ie.equalit.ceno.ui.robots.mDevice
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import org.junit.Ignore

/**
 *   Tests for verifying the settings view options exist as expected:
 * - Appears when the settings submenu is tapped
 * - Expected options are displayed as listed below
 */

class SettingsViewTest {
    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unrgeadable grouping.

    // Grant the app access to the camera so that we can test the Firefox Accounts QR code reader
    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @get:Rule val browserActivityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(1)

    @Before
    fun setUp() {
        onboarding {
        }.skipOnboardingIfNeeded()
    }

    // This test verifies settings view items are all in place
    @Test
    fun settingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifySettingsRecyclerViewToExist()
            verifyNavigateUp()
            verifyGeneralHeading()
            verifySearchButton()
            verifySearchSummary()
            verifyCustomizationButton()
            verifyCustomizationSummary()
            verifyOpenLinksInApps()
            verifyMakeDefaultBrowserButton()
            verifyAutofillAppsButton()
            verifyAutofillAppsSummary()
            verifyAddOnsButton()
            clickDownRecyclerView(6)
            Thread.sleep(5000)
            verifyShowOnboarding()
            verifyCrashReportingButton()
            verifyDeleteBrowsingData()
            clickDownRecyclerView(3)
            Thread.sleep(5000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                clickDownRecyclerView(1)
                Thread.sleep(5000)
                verifyDisableBatteryOptimization()
            }
            //clickDownRecyclerView(5)
            //Thread.sleep(5000)
            //verifySourcesHeading()
            //verifyWebsiteCheckbox()
            //verifyWebsiteSummary()
            //verifyPrivatelyCheckbox()
            //verifyPrivatelySummary()
            //verifyPubliclyCheckbox()
            //verifyPubliclySummary()
            //verifySharedCheckbox()
            //verifySharedSummary()
            clickDownRecyclerView(3)
            Thread.sleep(5000)
            verifyDataHeading()
            verifyLocalCacheDisplay()
            verifyLocalCacheDefaultValue()
            verifyContentsSharedButton()
            verifyContentsSharedDefaultValue()
            verifyClearCachedContentButton()
            verifyClearCachedContentSummary()
            clickDownRecyclerView(6)
            Thread.sleep(5000)
            verifyDeveloperToolsHeading()
            verifyRemoteDebugging()
            verifyCustomAddonCollectionButton()
            verifyCenoNetworkDetailsButton()
            verifyCenoNetworkDetailsSummary()
            verifyEnableLogFile()
            verifyTrackingProtectionButton()
            verifyTrackingProtectionSummary()
            verifyWebsiteSourcesButton()
            verifyWebsiteSourcesSummary()
            clickDownRecyclerView(5)
            Thread.sleep(5000)
            verifyAboutHeading()
            verifyCenoBrowserServiceDisplay()
            verifyGeckoviewVersionDisplay()
            verifyOuinetVersionDisplay()
            verifyOuinetProtocolDisplay()
            verifyAboutEqualitieButton()
            // TODO: check if that the displayed values match some patterns
        }
    }

    /*
    @Test
    fun privacySettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewPrivacy {
            verifyPrivacyUpButton()
            verifyPrivacySettings()
            verifyTrackingProtectionHeading()
            verifyTPEnableInNormalBrowsing()
            verifyTPEnableinPrivateBrowsing()
            verifyDataChoicesHeading()
            verifyUseTelemetryToggle()
            verifyTelemetrySummary()
        }
    }
     */

    @Test
    fun setDefaultBrowserTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.makeDefaultBrowser {
            verifyAndroidDefaultApps()
        }
    }

    @Test
    fun autofillAppsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.clickAutofillAppsButton {
            verifyAndroidAutofillServices()
        }
    }

    @Test
    fun remoteDebuggingViaUSB() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(13)
            Thread.sleep(5000)
            verifyRemoteDebugging()
            toggleRemoteDebuggingOn()
            toggleRemoteDebuggingOff()
            toggleRemoteDebuggingOn()
        }
    }

    @Test
    fun aboutReferenceBrowserTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(23)
            Thread.sleep(5000)
        }.openAboutReferenceBrowser {
            verifyAboutBrowser()
        }
    }

    /* Can't check further because after creating the custom add-on collection
    the currently running process is terminated see:
    /blob/master/app/src/main/java/org/mozilla/reference/browser/settings/SettingsFragment.kt#L217
    Confirming the custom add-on collection creation or trying to continue testing afterwards
    will cause the test instrumentation process to crash */
    @Test
    fun customAddonsCollectionTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(14)
            Thread.sleep(5000)
            verifyCustomAddonCollectionButton()
            clickCustomAddonCollectionButton()
            verifyCustomAddonCollectionPanelExist()
        }
    }

    @Test
    @Ignore("Disabled - too dependent on third-party UI, find some other not terrible app to test against")
    fun openLinksInAppsTest() {
        val url = "m.youtube.com"
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifyOpenLinksInApps()
            clickOpenLinksInApps()
        }.goBack {
        }.enterUrlAndEnterToBrowser(url.toUri()) {
            clickOpenInAppPromptButton()
        }.checkExternalApps {
            verifyYouTubeApp()
        }
        mDevice.pressHome()
    }
}
