package ie.equalit.ceno.ui

import android.os.Build
import androidx.core.net.toUri
import androidx.test.ext.junit.rules.ActivityScenarioRule
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

    fun onboardingScreenshots() {
        standby {
            waitForStandbyLogo()
            Screengrab.screenshot("030_standby_screen")
            waitForStandbyLogoGone()
        }
        onboarding {
            Thread.sleep(1000)
            Screengrab.screenshot("000_tooltip_begin_tour")
            //click get started
            beginTooltipsTour()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("001_tooltip_browsing_modes")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("002_tooltip_shortcuts")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("003_tooltip_address_bar")
        }
        navigationToolbar {
        }.enterUrlAndEnterToBrowser("https://ouinet.work".toUri()){
        }
        onboarding {
            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("004_tooltip_ceno_sources")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("005_tooltip_clear_ceno")
            clickNext()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasPermissions()) {
                //wait for permissions tooltip
                waitForPermissionsTooltip()
                Thread.sleep(1000)
                Screengrab.screenshot("006_tooltip_permissions")
                clickPermissions()
                permissionAllowButton().waitForExists(waitingTime)
                Screengrab.screenshot("028_tooltip_permissions_notifications")
                permissionAllowButton().click()
                if(backgroundAllowButton().waitForExists(waitingTime)) {
                    Screengrab.screenshot("029_tooltip_permissions_battery_optimization")
                    backgroundAllowButton().click()
                }
            }
        }
        navigationToolbar {
            Thread.sleep(1000)
            Screengrab.screenshot("007_fragment_browser")
        }.openContentSourcesSheet {
            Thread.sleep(1000)
            Screengrab.screenshot("035_content_sources_origin")
        }.closeContentSourcesSheet {
        }
    }

    fun settingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
            Thread.sleep(1000)
            Screengrab.screenshot("008_fragment_browser_threedot")
        }.openSettings {
            Thread.sleep(1000)
            Screengrab.screenshot("009_preferences_general")
            clickDownRecyclerView(15)
            Thread.sleep(1000)
            Screengrab.screenshot("010_preferences_data")
            clickDownRecyclerView(4)
            Thread.sleep(1000)
            Screengrab.screenshot("011_preferences_developertools")
            clickDownRecyclerView(4)
            Thread.sleep(1000)
            Screengrab.screenshot("031_preferences_about")
        }.goBack {
        }
    }

    fun searchSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewSearch {
            Thread.sleep(1000)
            Screengrab.screenshot("012_search_engine_settings")
        }.goBack {
        }.goBack {
        }
    }

    fun customizationSettingsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewCustomization {
            Thread.sleep(1000)
            Screengrab.screenshot("013_customization_preferences")
        }.openSettingsViewChangeAppIcon {
            Thread.sleep(1000)
            Screengrab.screenshot("014_fragment_change_icon")
        }.goBack {
            clickSetAppTheme()
            Thread.sleep(1000)
            Screengrab.screenshot("015_customization_preferences_setapptheme")
            clickCancelDialog()
            clickDefaultBehavior()
            Thread.sleep(1000)
            Screengrab.screenshot("016_customization_preferences_defaultbehavior")
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
            Thread.sleep(1000)
            Screengrab.screenshot("017_fragment_delete_browsing_data")
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
            Thread.sleep(1000)
            Screengrab.screenshot("019_sources_preferences")
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
            Thread.sleep(1000)
            Screengrab.screenshot("027_metrics_preferences")
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
            Thread.sleep(1000)
            Screengrab.screenshot("020_fragment_about")
        }.goBack {
        }.goBack {
        }
    }

    fun developerToolsScreenshots() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            clickDownRecyclerView(24)
            for (i in 0..7) {
                clickCenoVersionDisplay()
            }
            Screengrab.screenshot("023_preferences_additionaldevelopertools")
            // Wait for developer tool toasts to disappear
            Thread.sleep(10000)
        }.openSettingsViewDeveloperTools {
            Screengrab.screenshot("024_fragment_developer_tools")
            clickExportOuinetLog()
            Screengrab.screenshot("025_developer_tools_export_ouinet_log")
            mDevice.pressBack()
            clickAnnouncementSource()
            Screengrab.screenshot("026_developer_tools_announcement_source")
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
            Screengrab.screenshot("032_bridgemode_dialog")
            waitForBridgeModeDialog()
            // TODO: localize checking for success dialog instead of sleeping
            Thread.sleep(5000)
            Screengrab.screenshot("033_bridgemode_success")
            clickOk()
            Thread.sleep(1000)
            Screengrab.screenshot("034_bridgemode_enabled")
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
            Thread.sleep(15000)
            Screengrab.screenshot("036_browser_injector_source")
        }
        navigationToolbar {
        }.openContentSourcesSheet {
            mDevice.waitForIdle(waitingTime)
            Screengrab.screenshot("037_content_sources_injector")
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
            Screengrab.screenshot("041_cache_with_data")
        }.openSettingsViewCachedContent {
            Thread.sleep(1000)
            Screengrab.screenshot("042_cached_content")
        }.goBack {
            clickClearCacheButton()
            Thread.sleep(1000)
            Screengrab.screenshot("043_clear_cache_dialog")
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
            Thread.sleep(30000)
            Screengrab.screenshot("039_browser_dcache_source")
        }
        navigationToolbar {
        }.openContentSourcesSheet {
            mDevice.waitForIdle(waitingTime)
            Screengrab.screenshot("040_content_dcache_injector")
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
            Thread.sleep(1000)
            Screengrab.screenshot("044_change_language")
        }.goBack {
        }.goBack {
        }
    }

    fun homepageScreenshots() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            Screengrab.screenshot("021_fragment_public_home")
        }
        homepage {
        }.openPersonalHomepage {
            Thread.sleep(1000)
            Screengrab.screenshot("022_fragment_personal_home")
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
        searchSettingsScreenshots()
        customizationSettingsScreenshots()
        deleteBrowsingDataSettingsScreenshots()
        websiteSourcesSettingsScreenshots()
        metricsSettingsScreenshots()
        aboutSettingsScreenshots()
        developerToolsScreenshots()
        changeLanguageScreenshots()
        enableBridgeModeScreenshots()
        injectorSourceScreenshots()
        cachedContentScreenshots()
        dcacheSourceScreenshots()
        homepageScreenshots()
    }
}

