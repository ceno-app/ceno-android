package ie.equalit.ceno.ui

import android.os.Build
import androidx.core.net.toUri
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.ActivityTestRule
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.helpers.AndroidAssetDispatcher
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.ui.robots.backgroundAllowButton
import ie.equalit.ceno.ui.robots.clickNext
import ie.equalit.ceno.ui.robots.clickPermissions
import ie.equalit.ceno.ui.robots.hasPermissions
import ie.equalit.ceno.ui.robots.homepage
import ie.equalit.ceno.ui.robots.mDevice
import ie.equalit.ceno.ui.robots.navigateToSourcesAndSet
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.permissionAllowButton
import ie.equalit.ceno.ui.robots.standby
import ie.equalit.ceno.ui.robots.waitForNextTooltipButton
import ie.equalit.ceno.ui.robots.waitForPermissionsTooltip
import ie.equalit.ceno.ui.robots.waitForStandbyLogo
import ie.equalit.ceno.ui.robots.waitForStandbyLogoGone
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(JUnit4::class)
class ScreenshotGenerator {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    var activityRule = ActivityScenarioRule(BrowserActivity::class.java)

    @get:Rule
    var activityTestRule = ActivityTestRule(BrowserActivity::class.java)

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setUp() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        CleanStatusBar.enableWithDefaults()

        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun after() {
        CleanStatusBar.disable()
        mockWebServer.shutdown()
    }

    fun takeScreenshotWithWait(name : String, wait: Long = 1000 ) {
        Thread.sleep(wait)
        Screengrab.screenshot(name)
    }

    fun onboardingScreenshots() {
        standby {
            waitForStandbyLogo()
            Screengrab.screenshot("onboarding_standby")
            waitForStandbyLogoGone()
        }
        onboarding {
            takeScreenshotWithWait("onboarding_welcome")
            //click get started
            beginTooltipsTour()

            waitForNextTooltipButton()
            takeScreenshotWithWait("onboarding_tour_1")
            clickNext()

            waitForNextTooltipButton()
            takeScreenshotWithWait("onboarding_tour_2")
            clickNext()

            waitForNextTooltipButton()
            takeScreenshotWithWait("onboarding_tour_3")
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser("https://ouinet.work".toUri()){
        }
        onboarding {
            waitForNextTooltipButton()
            takeScreenshotWithWait("onboarding_tour_4")
            clickNext()

            waitForNextTooltipButton()
            takeScreenshotWithWait("onboarding_tour_5")
            clickNext()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasPermissions()) {
                //wait for permissions tooltip
                waitForPermissionsTooltip()
                takeScreenshotWithWait("onboarding_permissions")
                clickPermissions()
                permissionAllowButton().waitForExists(waitingTime)
                takeScreenshotWithWait("onboarding_permissions_notifications")
                permissionAllowButton().click()
                if(backgroundAllowButton().waitForExists(waitingTime)) {
                    takeScreenshotWithWait("onboarding_permissions_optimisation")
                    backgroundAllowButton().click()
                }
            }
        }
        navigationToolbar {
            takeScreenshotWithWait("origin_source_browser")
        }.openContentSourcesSheet {
            takeScreenshotWithWait("origin_source_popup")
        }.closeContentSourcesSheet {
        }
    }

    fun settingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
            takeScreenshotWithWait("settings_three_dot")
        }.openSettings {
            takeScreenshotWithWait("settings_general")
            clickDownRecyclerView(15)
            takeScreenshotWithWait("settings_data")
            clickDownRecyclerView(4)
            takeScreenshotWithWait("settings_developer_tools")
            clickDownRecyclerView(4)
            takeScreenshotWithWait("settings_about")
        }.goBack {
        }
    }

    fun searchSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewSearch {
            takeScreenshotWithWait("search_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun customizationSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewCustomization {
            toggleShowHomeButton()
            takeScreenshotWithWait("customization")
            toggleShowHomeButton()
        }.openSettingsViewChangeAppIcon {
            takeScreenshotWithWait("customization_change_icon")
        }.goBack {
            clickSetAppTheme()
            takeScreenshotWithWait("customization_set_app_theme")
            clickCancelDialog()
            clickDefaultBehavior()
            takeScreenshotWithWait("customization_default_behavior")
            clickCancelDialog()
        }.goBack {
        }.goBack {
        }
    }

    fun deleteBrowsingDataSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(13)
            Thread.sleep(1000)
        }.openSettingsViewDeleteBrowsingData {
            verifyCookiesCheckbox()
            toggleCookiesCheckbox()
            takeScreenshotWithWait("delete_browsing_data_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun websiteSourcesSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(16)
            Thread.sleep(1000)
        }.openSettingsViewSources {
            takeScreenshotWithWait("website_sources_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun metricsSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(11)
            Thread.sleep(1000)
        }.openSettingsViewMetrics {
            verifyCrashReportingButton()
            toggleCrashReporting()
            takeScreenshotWithWait("metrics_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun aboutSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(25)
            Thread.sleep(1000)
        }.openSettingsViewAboutPage {
            takeScreenshotWithWait("about_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun additionalDeveloperToolsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(24)
            for (i in 0..7) {
                clickCenoVersionDisplay()
            }
            Screengrab.screenshot("additional_developer_tools")
            // Wait for developer tool toasts to disappear
            Thread.sleep(10000)
        }.openSettingsViewDeveloperTools {
            Screengrab.screenshot("additional_developer_tools_settings")
            clickExportOuinetLog()
            Screengrab.screenshot("additional_developer_tools_export_log")
            mDevice.pressBack()
            clickAnnouncementSource()
            Screengrab.screenshot("additional_developer_tools_announcement_source")
            clickCancelDialog()
        }.goBack {
        }.goBack {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(24)
            Thread.sleep(1000)
            // Disable developer tools before finishing test
            for (i in 0..7) {
                clickCenoVersionDisplay()
            }
            Thread.sleep(10000)
        }.goBack {
        }
    }

    fun enableBridgeModeScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifySettingsRecyclerViewToExist()
            verifyBridgeModeToggle()
            clickBridgeModeToggle()
            waitForBridgeModeDialogToExist()
            takeScreenshotWithWait("enable_bridge_mode_dialog", 0)
            waitForBridgeModeDialog()
            // TODO: localize checking for success dialog instead of sleeping
            takeScreenshotWithWait("enable_bridge_mode_success", 5000)
            clickOk()
            takeScreenshotWithWait("enable_bridge_mode_enabled")
        }.goBack {
        }
    }

    fun injectorSourceScreenshots() {
        navigateToSourcesAndSet(
            website = false,
            private = true,
            public = true,
            shared = true
        )
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser("https://wikipedia.org".toUri()){
            // TODO: implement check that page has finished loading
            takeScreenshotWithWait("injector_source_browser", 15000)
        }
        navigationToolbar {
        }.openContentSourcesSheet {
            mDevice.waitForIdle(waitingTime)
            takeScreenshotWithWait("injector_source_popup")
        }.closeContentSourcesSheet {
        }
    }


    fun cachedContentScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(15)
            Thread.sleep(1000)
            verifyClearCachedContentButton()
            takeScreenshotWithWait("cached_content_with_data")
        }.openSettingsViewCachedContent {
            takeScreenshotWithWait("cached_content_list")
        }.goBack {
            clickClearCacheButton()
            takeScreenshotWithWait("cached_content_clear_dialog")
            clickCancel()
        }.goBack {
        }
    }


    fun dcacheSourceScreenshots() {
        navigateToSourcesAndSet(
            website = false,
            private = false,
            public = false,
            shared = true
        )
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser("https://meduza.io".toUri()){
            // TODO: implement check that page has finished loading
            takeScreenshotWithWait("dcache_source_browser", 30000)
        }
        navigationToolbar {
        }.openContentSourcesSheet {
            mDevice.waitForIdle(waitingTime)
            takeScreenshotWithWait("dcache_source_popup")
        }.closeContentSourcesSheet {
        }
    }

    fun changeLanguageScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(10)
            Thread.sleep(1000)
            verifyChangeLanguageButton()
        }.openSettingsViewChangeLanguage {
            takeScreenshotWithWait("change_language")
        }.goBack {
        }.goBack {
        }
    }

    fun homepageScreenshots() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            takeScreenshotWithWait("homepage_public")
        }.openThreeDotMenu {
            takeScreenshotWithWait("homepage_three_dot")
        }.closeMenu {
        }
        homepage {
        }.openPersonalHomepage {
            takeScreenshotWithWait("homepage_personal")
        }
        homepage {
        }.openPublicHomepage {
        }
    }

    fun tabsTrayScreenshots() {
        navigationToolbar {
        }.openTabTrayMenu {
            takeScreenshotWithWait("tabs_tray_public")
        }.openNewTab {
        }
        navigationToolbar {
        }.openTabTrayMenu {
        }.openMoreOptionsMenu(activityTestRule.activity) {
            takeScreenshotWithWait("tabs_tray_three_dot")
        }.closeMenu {
        }.goBackFromTabTray {
        }
        homepage {
        }.openPersonalHomepage {
        }
        navigationToolbar {
        }.openTabTrayMenu {
            takeScreenshotWithWait("tabs_tray_personal")
        }.openNewTab {
        }
        homepage {
        }.openPublicHomepage {
        }
    }

    @Test
    fun testScreenshots() {
        // For testing, uncomment to skip onboarding screenshots
        /*
        standby {
        }.waitForStandbyIfNeeded()
        onboarding {
        }.skipOnboardingIfNeeded()
        */
        onboardingScreenshots()
        settingsScreenshots()
        homepageScreenshots()
        searchSettingsScreenshots()
        customizationSettingsScreenshots()
        deleteBrowsingDataSettingsScreenshots()
        websiteSourcesSettingsScreenshots()
        metricsSettingsScreenshots()
        aboutSettingsScreenshots()
        additionalDeveloperToolsScreenshots()
        changeLanguageScreenshots()
        enableBridgeModeScreenshots()
        injectorSourceScreenshots()
        cachedContentScreenshots()
        dcacheSourceScreenshots()
        tabsTrayScreenshots()
    }
}

