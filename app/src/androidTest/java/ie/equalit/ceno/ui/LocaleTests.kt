package ie.equalit.ceno.ui

import androidx.test.filters.SdkSuppress
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.ui.robots.locale
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.selectLanguageButton
import ie.equalit.ceno.ui.robots.standby
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 28)
class LocaleTests {

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(1)

    @Before
    fun setUp() {
        standby {}.waitForStandbyIfNeeded()
    }

    @Test
    fun changeLocaleFromStartTooltip() {
        onboarding {
            verifyStartTooltipExists()
            verifyStartTooltip()
            //verify locale
            locale {
                verifyDefaultLocale()
            }
            //change locale
            selectLanguageButton().click()
            locale {
                verifyChangeLanguageDialog()
            }.changeLanguage("French")
            standby {}.waitForStandbyIfNeeded()
            //verify new locale
            locale {
                verifyLocale("fr")
            }
        }
    }

    @Test
    fun changeLocaleFromSettings() {
        onboarding {
        }.skipOnboardingIfNeeded()
        locale {
            verifyDefaultLocale()
        }
        navigationToolbar {
        }.openThreeDotMenu {
        }
            .openSettings {
                Thread.sleep(2000)
                verifyChangeLanguageButton()
                clickChangeLanguageButton()
            }
        locale {
            verifyChangeLanguageDialog()
        }.changeLanguage("French")
        standby {}.waitForStandbyIfNeeded()
        //verify new locale
        locale {
            verifyLocale("fr")
        }
    }
}
